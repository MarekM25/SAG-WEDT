import java.io.{File, PrintWriter}
import java.net.URL

import com.gargoylesoftware.htmlunit.html.HtmlPage
import weka.classifiers.misc.InputMappedClassifier
//import org.apache.commons.lang3.StringEscapeUtils

import akka.actor.{Actor, ActorSystem, Props}

/**
  * Created by Kamil on 28.03.2017.
  */

class TrainingActor extends Actor {
  def buildNewTreesOnDict = {
    val treeCreator = context.actorOf(Props[TreeCreator], name = "treeCreator")
    val filename = "Files\\SearchDict.txt"
    for (line <- scala.io.Source.fromFile(filename).getLines) {
      //println("Pages\\"+line+".html")
      treeCreator ! ("Pages\\"+line+".html")
    }
//    treeCreator ! "Pages\\computer.html"
//    treeCreator ! "Pages\\shoes.html"
  }

  def receive = {
    case x: InputMappedClassifier => {
      //print(x)
      Model.addAndSave(x)
      //Model.saveModel
    }
      //val predictor = context.actorOf(Props[MyClassifier], name= "Classifier")
      //predictor ! ("Discount Laptops now here low prices free shipping",x)}
    case "start" => buildNewTreesOnDict
    case y:Boolean => println(y)


  }
}


object Main extends App {

  def createModel(): Unit ={
    //Tworzenie modelu na podstawie słownika
    val treeActor = system.actorOf(Props[TrainingActor], name = "treeActor")
    treeActor ! "start"
  }
  // turn off html unit warnings
  java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF)
  System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
  Model.loadModel//loads model from files stored at /Files/Models/...
  val system = ActorSystem("HelloSystem")
  createModel();//Funkcja do tworzenia modelu

  val classifier = system.actorOf(Props[MyClassifier], name = "trainingActor")
  classifier ! "https://search.yahoo.com/search;?p=music"
  ///val filename = "Files\\computer.html"
  //for (line <- scala.io.Source.fromFile(filename).getLines) {
  //val helloActor = system.actorOf(Props[TreeCreator])

  //}
}
