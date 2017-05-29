import akka.actor.ActorRef
import weka.classifiers.misc.InputMappedClassifier

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
//import org.apache.commons.lang3.StringEscapeUtils

import akka.actor.{Actor, ActorSystem, Props}

/**
  * Created by Kamil on 28.03.2017.
  */

object UCCreators {
  var uccs = new ArrayBuffer[(ActorRef, Boolean, Long)]

  def check = {
    for (i <- uccs.indices)
      if(uccs(i)._2 == false && uccs(i)._3 + (5 * 60 * 1000) < System.currentTimeMillis)
        uccs(i) = (uccs(i)._1, true, 0)
  }
}

object listOfPages {
  var lst = new ListBuffer[String]
  var urllst = new ListBuffer[String]
}

class TrainingDispacher(nUCCreators: Int) extends Actor {

  for (i <- 1 to nUCCreators)
    UCCreators.uccs += ((context.actorOf(Props[UCCreator], name = "UCCreator"+i), true, 0))

  def buildNewUCsOnDict = {
    val filename = "Files\\SearchDict.txt"
    for (line <- scala.io.Source.fromFile(filename).getLines) {
      listOfPages.lst += ("Pages\\" + line + ".html");
    }
    while (listOfPages.lst.nonEmpty && buildNewUC) {}
  }

  def buildNewUC : Boolean = {
    var ret = false
    UCCreators.check
    val idx = UCCreators.uccs.indexWhere(_._2 == true)
    if (idx != -1) {
      val tc = UCCreators.uccs(idx)
      UCCreators.uccs(idx) = (tc._1, false, System.currentTimeMillis)
      var s = new String
      if(listOfPages.lst.nonEmpty) {
        s = listOfPages.lst.remove(0)
        tc._1 ! (s)
      }
      else if(listOfPages.urllst.nonEmpty) {
        s = listOfPages.urllst.remove(0)
        tc._1 ! ("url", s)
      }
      ret = true
    }
    ret
  }

  def buildNewUCOnURL(url : String) = {
    listOfPages.urllst += url
    buildNewUC
  }

  def reciveTree(act: ActorRef, tree: InputMappedClassifier) = {
    val idx = UCCreators.uccs.indexWhere(_._1 == act)
    if(idx != -1) {
      UCCreators.uccs(idx) = (UCCreators.uccs(idx)._1, true, 0)
      if(listOfPages.lst.nonEmpty || listOfPages.urllst.nonEmpty)
        buildNewUC
      else
        println(System.currentTimeMillis)
    }
    Model.addAndSave(tree)
  }

  def receive = {
    case "start" => buildNewUCsOnDict
    case url : String => buildNewUCOnURL(url)
    case x: InputMappedClassifier => reciveTree(sender(), x)

  }
}

object Main extends App {

  def createModel(): Unit = {
    //Tworzenie modelu na podstawie słownika
    trainingActor ! "start"
  }

  // turn off html unit warnings
  java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF)
  System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
  val system = ActorSystem("AdBlockSystem")
  val trainingActor = system.actorOf(Props(new TrainingDispacher(32)), name = "trainingActor")
  println(System.currentTimeMillis)
  //createModel();//Funkcja do tworzenia modelu

  Model.loadModel
  //loads model from files stored at /Files/Models/...
  val classifier = system.actorOf(Props(new MyClassifier(trainingActor)), name = "classifier")

  while(true){
    println("Podaj slowo do wyszukania")
    val word = scala.io.StdIn.readLine()
    classifier ! "https://search.yahoo.com/search;?p="+word
  }
}

object TestModel extends App {
  val system = ActorSystem("HelloSystem")
  val trainingActor = system.actorOf(Props(new TrainingDispacher(20)), name = "trainingActor")
  trainingActor ! "start"
  val classifier = system.actorOf(Props (new MyClassifier(trainingActor)), name = "trainingActor")
  classifier ! "test"
}