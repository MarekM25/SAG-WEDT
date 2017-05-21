import akka.actor.ActorRef
import weka.classifiers.misc.InputMappedClassifier

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
//import org.apache.commons.lang3.StringEscapeUtils

import akka.actor.{Actor, ActorSystem, Props}

/**
  * Created by Kamil on 28.03.2017.
  */

object treeCreators {
  @volatile var tcs = new ArrayBuffer[(ActorRef, Boolean)]
}

object listOfPages {
  @volatile var lst = new ListBuffer[String]
}

class TrainingDispacher(nTreeCreators: Int) extends Actor {

  val nActors = nTreeCreators
  for (i <- 1 to nActors)
    treeCreators.tcs += ((context.actorOf(Props[TreeCreator], name = "treeCreator"+i), true))
  //val treeGatherer = context.actorOf(Props[TrainingGatherer], name = "treeGatherer")

  def buildNewTreesOnDict = {
    val filename = "Files\\SearchDict.txt"
    for (line <- scala.io.Source.fromFile(filename).getLines) {
      listOfPages.lst += ("Pages\\" + line + ".html");
    }
    while (listOfPages.lst.nonEmpty && buildNewTree) {}
  }

  def buildNewTree : Boolean = {
    var ret = false
    if(listOfPages.lst.nonEmpty) {
      var idx = treeCreators.tcs.indexWhere(_._2 == true)
      if (idx != -1) {
        val s = listOfPages.lst.remove(0)
        val tc = treeCreators.tcs(idx)
        treeCreators.tcs(idx) = (tc._1, false)
        tc._1 ! (s)
        ret = true
      }
    }
    ret
  }

  def buildNewTreeOnURL(url : String) = {
    var idx = treeCreators.tcs.indexWhere(_._2 == true)
    while (idx == -1) {
      Thread.sleep(100)
      idx = treeCreators.tcs.indexWhere(_._2 == true)
    }
    //println("Pages\\"+line+".html")
    val tc = treeCreators.tcs(idx)
    treeCreators.tcs(idx) = (tc._1, false)
    tc._1 ! (url)
  }

  //  def buildNewTreesOnDict = {
  //    val treeCreator = context.actorOf(Props[TreeCreator], name = "treeCreator")
  //    val filename = "Files\\SearchDict.txt"
  //    for (line <- scala.io.Source.fromFile(filename).getLines) {
  //      //println("Pages\\"+line+".html")
  //      treeCreator ! ("Pages\\" + line + ".html")
  //    }
  //    //    treeCreator ! "Pages\\computer.html"
  //    //    treeCreator ! "Pages\\shoes.html"
  //  }

  def reciveTree(act: ActorRef, tree: InputMappedClassifier) = {
    val idx = treeCreators.tcs.indexWhere(_._1 == act)
    if(idx != -1) {
      treeCreators.tcs(idx) = (treeCreators.tcs(idx)._1, true)
      if(listOfPages.lst.nonEmpty)
        buildNewTree
    }
    Model.addAndSave(tree)
  }

  def receive = {
    //case x: InputMappedClassifier => reciveTree(sender(), x)
    //    {
    //      //print(x)
    //      val s = sender
    //      val tc = treeCreators
    //      val idx = treeCreators.tcs.indexWhere(_._1 == sender)
    //      if(idx != -1)
    //        treeCreators.tcs(idx) = (treeCreators.tcs(idx)._1, true)
    //      Model.addAndSave(x)
    //      //Model.saveModel
    //    }
    //val predictor = context.actorOf(Props[MyClassifier], name= "Classifier")
    //predictor ! ("Discount Laptops now here low prices free shipping",x)}
    case "start" => buildNewTreesOnDict
    case url : String => buildNewTreeOnURL(url)
    case y: Boolean => println(y)
    case x: InputMappedClassifier => reciveTree(sender(), x)

  }
}

object Main extends App {

  def createModel(): Unit = {
    //Tworzenie modelu na podstawie słownika
    val treeActor = system.actorOf(Props(new TrainingDispacher(20)), name = "treeActor")
    treeActor ! "start"
  }

  // turn off html unit warnings
  java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF)
  System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
  val system = ActorSystem("HelloSystem")
  createModel();//Funkcja do tworzenia modelu
  //
//  Model.loadModel
//  //loads model from files stored at /Files/Models/...
//  val classifier = system.actorOf(Props[MyClassifier], name = "trainingActor")
//
//
//  while(true){
//    println("Podaj slowo do wyszukania")
//    val word = scala.io.StdIn.readLine()
//    classifier ! "https://search.yahoo.com/search;?p="+word
//  }
//  ///val filename = "Files\\computer.html"
//  //for (line <- scala.io.Source.fromFile(filename).getLines) {
//  //val helloActor = system.actorOf(Props[TreeCreator])
//
//  //}
}
