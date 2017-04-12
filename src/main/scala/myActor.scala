import java.net.URL

import akka.actor.Actor.Receive
import weka.classifiers.trees.RandomTree
//import org.apache.commons.lang3.StringEscapeUtils

import akka.actor.{Actor, ActorSystem, Props}
import org.htmlcleaner.HtmlCleaner

import scala.collection.mutable.ListBuffer

/**
  * Created by Kamil on 28.03.2017.
  */

class myActor extends Actor{
  def getHeadlinesFromUrl(url: String): List[String] = {
    var stories = new ListBuffer[String]
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties
    val rootNode = cleaner.clean(new URL(url))
    val elements = rootNode.getElementsByName("div", true)
    for (elem <- elements) {
      val classType = elem.getAttributeByName("class")
      if (classType != null && classType.contains("ads"/*"scta reg searchCenterTopAds"*/)) {
        // stories might be "dirty" with text like "'", clean it up
        //val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
        stories += elem.getText.toString//text
      }
    }
    return stories.toList//.filter(storyContainsDesiredPhrase(_)).toList
  }
  //var stories = new ListBuffer[String]
  def receive = {
    case "hello" => println("hello back at you")
    case _       => println("huh?")
      var stories = getHeadlinesFromUrl("file:D:\\Documents\\magisterka\\AdBlock\\data\\shoes - Yahoo Search Results.html")
      stories.foreach(println);
  }
}

class TrainingActor extends Actor{
  def buildNewTree = {
    val treeActor = context.actorOf(Props[TreeCreator], name = "treeActor")
    treeActor ! "D:\\Documents\\magisterka\\SAGWEDT\\data\\computers.csv"
  //context.actorSelection("../treeActor") ! "D:\\Documents\\magisterka\\SAGWEDT\\data\\computers.csv" Tak się można dostać do aktora brata

    //treeActor ! "create"
  }
  def receive= {
    case x:RandomTree => println(x)
    case _ => buildNewTree


  }
}
object Main extends App{
  val system = ActorSystem("HelloSystem")

  val trainingActor = system.actorOf(Props[TrainingActor], name = "trainingActor")
  val helloActor = system.actorOf(Props[myActor], name = "helloactor")
  // default Actor constructor
  trainingActor ! "start"
  //helloActor ! "hello"
  //helloActor ! "buenos dias"
}

