import java.io.{BufferedReader, FileReader}

import akka.actor.Actor
import akka.actor.Actor.Receive
import weka.classifiers.Evaluation
import weka.classifiers.trees.RandomTree
import weka.core.Debug.Random
import weka.core.Instances
import weka.core.converters.ConverterUtils.DataSource;
/**
  * Created by Kamil on 12.04.2017.
  */
class TreeCreator extends Actor{

  def loadData = {
    var source: DataSource  = new DataSource("D:\\Documents\\magisterka\\SAGWEDT\\data\\computers.arff")
    var data : Instances = source.getDataSet()
    //data.toArray().foreach(x => println(x))
    data
    //loadData=data
    // var reader:BufferedReader = new BufferedReader(new FileReader("/some/where/data.arff"));
  }
  def createTree ={
    var data =loadData
    var tree: RandomTree = new RandomTree()
//    val options:Array[String] = new Array[String](4)
//    options(0)="-K"
//    options(1)="0"
//    options(2)="-M"
//    options(3)="1.0"
    data.setClassIndex(data.numAttributes()-1)
    //tree.setOptions(options)
    tree.buildClassifier(data)
    var eval: Evaluation= new Evaluation(data)
    //val optionsEvaluation:Array[String] = new Array[String](1)
    //optionsEvaluation(0)="-t"
    //options(1)="0"
    eval.crossValidateModel(tree, data, 10, new Random(1))
    println(tree)
    println(eval.toSummaryString())
  }

  override def receive ={
    case "create" => createTree
    case _       => println("huh?")
  }
}
