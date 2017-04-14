import java.net.URL

import akka.actor.Actor.Receive
import com.gargoylesoftware.htmlunit.html.HtmlPage
import weka.classifiers.trees.RandomTree
//import org.apache.commons.lang3.StringEscapeUtils

import akka.actor.{Actor, ActorSystem, Props}
import org.htmlcleaner.HtmlCleaner
import com.gargoylesoftware.htmlunit._

import scala.collection.mutable.ListBuffer

/**
  * Created by Kamil on 28.03.2017.
  */

class myActor extends Actor {
  def getHeadlinesFromUrl(url: String): List[String] = {
    var stories = new ListBuffer[String]
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties


    val url1 = new URL(url)
    java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF)
    val webClient = new WebClient()
    val page : HtmlPage = webClient.getPage(url1)
    val connection = url1.openConnection()
    connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)")


    val rootNode = cleaner.clean(page.getWebResponse().getContentAsString())//(new URL(url))
    val elements = rootNode.getElementsByName("div", true)
    for (elem <- elements) {
      val classType = elem.getAttributeByName("class")
      if (classType != null && classType.contains("ads" /*"scta reg searchCenterTopAds"*/)) {
        // stories might be "dirty" with text like "'", clean it up
        //val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
        stories += elem.getText.toString //text
      }
    }
    return stories.toList //.filter(storyContainsDesiredPhrase(_)).toList
  }

  //var stories = new ListBuffer[String]
  def receive = {
    case "hello" => println("hello back at you")
    case _ => println("huh?")
      var stories = getHeadlinesFromUrl("https://search.yahoo.com/search;?p=shoes")//("file:F:\\uni 17.04\\scala\\SAG-WEDT\\example_sites\\text ads sites\\shoes - Yahoo Search Results.htm")
      stories.foreach(println);
  }
}

class TrainingActor extends Actor {
  def buildNewTree = {
    val treeActor = context.actorOf(Props[TreeCreator], name = "treeActor")
    treeActor ! "Files\\computers.csv"
  }

  def receive = {
    case x: RandomTree => println(x)
    case _ => buildNewTree


  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")

  val trainingActor = system.actorOf(Props[TrainingActor], name = "trainingActor")
  val helloActor = system.actorOf(Props[myActor], name = "helloactor")
  // default Actor constructor
//  trainingActor ! "start"
  helloActor ! "hello"
  helloActor ! "buenos dias"
}

