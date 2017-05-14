import java.io.File

import weka.classifiers.misc.InputMappedClassifier
//import org.apache.commons.lang3.StringEscapeUtils

import akka.actor.{Actor, ActorSystem, Props}

/**
  * Created by Kamil on 28.03.2017.
  */

class TrainingActor extends Actor {
  def buildNewTree = {
    val treeCreator = context.actorOf(Props[TreeCreator], name = "treeCreator")
    treeCreator ! "Pages\\computer.html"
    treeCreator ! "Pages\\shoes.html"
  }

  def receive = {
    case x: InputMappedClassifier => {
      //print(x)
      Model.addToMap(x)
      //Model.saveModel
      //println(Model.getOneRandomClassifier)
      //Model.loadModel
      //println(Model.models.size)
    }
      //val predictor = context.actorOf(Props[MyClassifier], name= "Classifier")
      //predictor ! ("Discount Laptops now here low prices free shipping",x)}
    case "start" => buildNewTree
    case y:Boolean => println(y)


  }
}


object Main extends App {
  // turn off html unit warnings
  java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF)
  System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
  Model.loadModel//loads model from files stored at /Files/Models/...
  val system = ActorSystem("HelloSystem")
  val parser = new SiteParser()
  parser.removeSitesWithNoAds()
  //val treeCreator = system.actorOf(Props[TreeCreator], name = "trainingActor")
  // treeCreator  ! "https://search.yahoo.com/search;?p=shoes"
  //treeCreator  ! "Pages\\computer.html"
  //  val treeActor = system.actorOf(Props[TreeCreator], name = "treeActor")
  //  treeActor ! "start"
  //val parser = new SiteParser()
  //parser.getYahooSites();

  //val classifier = system.actorOf(Props[MyClassifier], name = "trainingActor")
  //classifier ! "https://search.yahoo.com/search;?p=shoes"
  ///val filename = "Files\\computer.html"
  //for (line <- scala.io.Source.fromFile(filename).getLines) {
  //val helloActor = system.actorOf(Props[TreeCreator])

  //}
}
