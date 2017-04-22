import java.net.URL

import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.apache.commons.lang3.StringEscapeUtils
import org.htmlcleaner.{CleanerProperties, SimpleHtmlSerializer, TagNode}
import weka.classifiers.misc.InputMappedClassifier
//import org.apache.commons.lang3.StringEscapeUtils

import akka.actor.{Actor, ActorSystem, Props}
import com.gargoylesoftware.htmlunit._
import org.htmlcleaner.HtmlCleaner

import scala.collection.mutable.ListBuffer

/**
  * Created by Kamil on 28.03.2017.
  */

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

  val treeCreator = system.actorOf(Props[TreeCreator], name = "trainingActor")
  //treeCreator  ! "https://search.yahoo.com/search;?p=shoes"
  treeCreator  ! "Files\\Text.csv"
//  val treeActor = system.actorOf(Props[TreeCreator], name = "treeActor")
//  treeActor ! "start"

 // val filename = "Files\\SearchDict.txt"
//  for (line <- scala.io.Source.fromFile(filename).getLines) {
//    val helloActor = system.actorOf(Props[myActor])
//    helloActor ! "https://search.yahoo.com/search;?p="+line

  //}
}

