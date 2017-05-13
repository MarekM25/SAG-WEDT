import java.util

import akka.actor.{ActorSystem, Props}
import weka.core.{Attribute, DenseInstance, Instances}

import scala.concurrent.duration.DurationConversions.Classifier

/**
  * Created by Kamil on 28.03.2017.
  */
object Test extends App {
  val system = ActorSystem("HelloSystem")
  val trainingActor = system.actorOf(Props[TrainingActor], name = "trainingActor")
  Model.loadModel
  var x = Model.getOneRandomClassifier
  println(x)
  val predictor = system.actorOf(Props[MyClassifier], name= "Classifier")
  predictor ! ("Discount Laptops now here low prices free shipping",x)
  //Model.printModels()
  //trainingActor ! "start"
  //val trainingActor = system.actorOf(Props[TrainingActor], name = "trainingActor")
  //val helloActor = system.actorOf(Props[myActor], name = "helloactor")
  // default Actor constructor
  //  trainingActor ! "start"
}
