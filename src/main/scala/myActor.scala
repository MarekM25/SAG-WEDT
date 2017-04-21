import java.net.URL

import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.apache.commons.lang3.StringEscapeUtils
import org.htmlcleaner.TagNode
import weka.classifiers.misc.InputMappedClassifier
//import org.apache.commons.lang3.StringEscapeUtils

import akka.actor.{Actor, ActorSystem, Props}
import com.gargoylesoftware.htmlunit._
import org.htmlcleaner.HtmlCleaner

import scala.collection.mutable.ListBuffer

/**
  * Created by Kamil on 28.03.2017.
  */

class myActor extends Actor {

  // download html, parse it and return tree representation
  // input: url, output: root node of the tree
  def htmlToTree(url: String) : TagNode = {
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties

    val webClient = new WebClient()
    val page : HtmlPage = webClient.getPage(new URL(url))
    val rootNode = cleaner.clean(page.getWebResponse().getContentAsString()) // get tree representation of html
    rootNode
  }

  // input: url, output: list of (div text, is ad) pairs
  def getDivTextsFromUrl(url: String) : List[(String, Boolean)] = {
    var texts = new ListBuffer[(String, Boolean)]
    val rootNode = htmlToTree(url)
    var elements = rootNode.getElementsByName("div", true)
    //var lastParent = rootNode
    for (elem <- elements) {
      val classType = elem.getAttributeByName("class")
      // quick, well performing, but limited to yahoo
      //val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
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

    texts.toList
  }

  //var stories = new ListBuffer[String]
  def receive = {
    case x:String=> {
      var divTexts = getDivTextsFromUrl(x)
      divTexts.foreach(println);
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

  // turn off html unit warnings
  java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF)
  System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

  val system = ActorSystem("HelloSystem")

  val trainingActor = system.actorOf(Props[TrainingActor], name = "trainingActor")

  val filename = "Files\\SearchDict.txt"
  for (line <- scala.io.Source.fromFile(filename).getLines) {
    val helloActor = system.actorOf(Props[myActor])
    helloActor ! "https://search.yahoo.com/search;?p="+line
  }
}

