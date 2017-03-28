import akka.actor.{Actor, ActorSystem, Props}
import akka.actor.Actor.Receive

/**
  * Created by Kamil on 28.03.2017.
  */
class myActor extends Actor{
  def receive = {
    case "hello" => println("hello back at you")
    case _       => println("huh?")
  }
}

object Main extends App{
  val system = ActorSystem("HelloSystem")
  // default Actor constructor
  val helloActor = system.actorOf(Props[myActor], name = "helloactor")
  helloActor ! "hello"
  helloActor ! "buenos dias"
}

