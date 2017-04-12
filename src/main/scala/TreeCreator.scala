import java.io.{BufferedReader, FileReader, InputStream}

import akka.actor.Actor
import akka.actor.Actor.Receive
import weka.classifiers.Evaluation
import weka.classifiers.evaluation.output.prediction.CSV
import weka.classifiers.trees.RandomTree
import weka.core.Debug.Random
import weka.core.Instances
import weka.core.converters.CSVLoader
import weka.core.converters.ConverterUtils.DataSource

import scala.io.Source


/**
  * Created by Kamil on 12.04.2017.
  */
class TreeCreator extends Actor {

  def loadData(location: String) = {
    //val src = Source.fromFile("D:\\Documents\\magisterka\\SAGWEDT\\data\\computers.csv")
    //val iter = src.getLines().map(_.split(","))
    //iter.foreach(x => println(x.foreach(print)))

    //var arrayString:Array[String]= new Array[String](src.getLines().size)
    //iter.foreach(x =>arrayString(x))

    //var source: DataSource = new DataSource("D:\\Documents\\magisterka\\SAGWEDT\\data\\computers.arff")
    var source2: DataSource = new DataSource(location)
    var data: Instances = source2.getDataSet()
    //data.toArray().foreach(x => println(x))
    data
    //loadData=data
    // var reader:BufferedReader = new BufferedReader(new FileReader("/some/where/data.arff"));
  }

  def createTree(loccation: String): RandomTree = {
    var data = loadData(loccation)
    var tree: RandomTree = new RandomTree()
    data.setClassIndex(data.numAttributes() - 1)
    //tree.setOptions(options)
    tree.buildClassifier(data)
    var eval: Evaluation = new Evaluation(data)
    //val optionsEvaluation:Array[String] = new Array[String](1)
    //optionsEvaluation(0)="-t"
    //options(1)="0"
    eval.crossValidateModel(tree, data, 10, new Random(1))
    //println(tree)
    println(eval.toSummaryString())
    tree //W taki sposób zwraca się z funkcji
  }

  def receive = {
    case location: String => sender() ! createTree(location) //Odsyłamy senderowi
  }
}
