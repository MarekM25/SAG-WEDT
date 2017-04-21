import java.net.URL
import javax.xml.transform.Source

import akka.actor.Actor.Receive
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.apache.commons.lang3.StringEscapeUtils
import org.htmlcleaner.TagNode
import weka.classifiers.misc.InputMappedClassifier
import weka.classifiers.trees.RandomTree
import weka.core.pmml.jaxbbindings.True

import scala.collection.mutable.ArrayBuffer
//import org.apache.commons.lang3.StringEscapeUtils

import akka.actor.{Actor, ActorSystem, Props}
import org.htmlcleaner.HtmlCleaner
import com.gargoylesoftware.htmlunit._
import scala.collection.JavaConverters._

import scala.collection.mutable.ListBuffer

/**
  * Created by Kamil on 28.03.2017.
  */

class myActor extends Actor {
  def getDivTextsFromUrl(url: String): List[(String, Boolean)] = {
    var texts = new ListBuffer[(String, Boolean)]
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties


    val url1 = new URL(url)
    java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF)
    val webClient = new WebClient()
    val page : HtmlPage = webClient.getPage(url1)
    //val connection = url1.openConnection()
    //connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)")


    val rootNode = cleaner.clean(page.getWebResponse().getContentAsString())//(new URL(url))
    var elements = rootNode.getElementsByName("div", true)
    var elem = rootNode.findElementByName("div", true)
    var lastParent = rootNode
    for (elem <- elements) {
      //while(elem != null) {
      val classType = elem.getAttributeByName("class")

      // quick, well performing, but limited to yahoo
      val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
      if (classType != null && classType.contains("ads")) {
        val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
        texts += ((text, true))
      }
      else if (classType != null && classType.contains("dd algo algo-sr")) {
        val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
        texts += ((text, false))
      }
    }


    /*if (classType != null && classType.contains("ads" /*"scta reg searchCenterTopAds"*/)) {
      // stories might be "dirty" with text like "'", clean it up
      val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
      texts += ((text, true)) //text
      var level = 0
      var parent = elem.getParent()
      while (parent != null && parent.getName != "div") {
        parent = parent.getParent()
        level += 1
      }
      elem.removeFromTree()
      //val siblings : Array[TagNode] = parent.getAllChildren().asScala.toArray//.getElementsByName("div", true)//getAllChildren().asScala
      //        val siblings = parent.getChildTags()
      if (parent != lastParent) {

        var siblings = parent.getChildTags() //parent.getElementsByName("div", true)
        val nlsiblings: ArrayBuffer[TagNode] = new ArrayBuffer[TagNode]
        while (level > 0) {
          for (sib: TagNode <- siblings) {
            nlsiblings ++= sib.getChildTags()
          }
          level -= 1
          siblings = nlsiblings.toArray
        }
        //siblings = siblings.filter(_.getName == "div")
        for (sib: TagNode <- siblings) {
          val classType = sib.getAttributeByName("class")
          if (classType != null && classType.contains("ads")) {
            texts += ((sib.getText.toString, true))
          }
          else {
            texts += ((sib.getText.toString, false))
          }
        }
        lastParent = parent
        parent.removeFromTree()
      }
    }
    //elem = rootNode.findElementByName("div", true)
    }*/

    //    elem = rootNode
    //    while(elem != null){
    //      elem = rootNode.findElementByName("div", true)
    //      if(elem != null) {
    //        texts += ((elem.getText.toString, false))
    //        elem.removeFromTree()
    //      }
    //    }

    //    var parent = rootNode
    //    while(parent.getName != "div")
    //      parent = parent.findElementByName("div", true);
    //    texts += ((parent.getText.toString, false))
    return texts.toList //.filter(storyContainsDesiredPhrase(_)).toList
  }

  //var stories = new ListBuffer[String]
  def receive = {
    case x:String=> {
      var stories = getDivTextsFromUrl(x)
      stories.foreach(println);
    }
  }
}

class TrainingActor extends Actor {
  def buildNewTree = {
    val treeCreator = context.actorOf(Props[TreeCreator], name = "treeCreator")
    treeCreator ! "Files\\Text.csv"
  }

  def receive = {
    case x: InputMappedClassifier =>{
      val predictor = context.actorOf(Props[MyClassifier], name= "Classifier")
      predictor ! ("Discount Laptops now here low prices free shipping",x)}
    case "start" => buildNewTree
    case y:Boolean => println(y)


  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")

  val trainingActor = system.actorOf(Props[TrainingActor], name = "trainingActor")

  val filename = "Files\\SearchDict.txt"
  for (line <- scala.io.Source.fromFile(filename).getLines) {
    val helloActor = system.actorOf(Props[myActor])
    helloActor ! "https://search.yahoo.com/search;?p="+line
  }
}

