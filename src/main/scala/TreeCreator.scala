import java.io.{BufferedReader, ByteArrayInputStream, FileReader, InputStream}
import java.nio.charset.StandardCharsets

import akka.actor.Actor
import akka.actor.Actor.Receive
import weka.classifiers.Evaluation
import weka.classifiers.evaluation.output.prediction.CSV
import weka.classifiers.trees.RandomTree
import weka.core.Debug.Random
import weka.core.Instances
import weka.core.converters.CSVLoader
import weka.core.converters.ConverterUtils.DataSource
import weka.core.tokenizers.NGramTokenizer
import weka.filters.unsupervised.attribute.{NominalToString, StringToWordVector}

import scala.io.Source


/**
  * Created by Kamil on 12.04.2017.
  */

//class VectorCreator extends Actor {
//
//  }
//  def receive = {
//    case text: String => createVector(text)
//  }
//}

class TreeCreator extends Actor {

  def createVector(input: String) = {
    /*
    Input - lokalizacja pliku csv w postaci text,class
    Output - wektor występowania słów w kolejnych linijkach pliku (UWAGA! atrybut class, znajduje się na początku nowego datasetu)
     */
    var stwv: StringToWordVector = new StringToWordVector()
    stwv.setLowerCaseTokens(true)
    stwv.setMinTermFreq(1);
    //stwv.setStopwordsHandler();
    stwv.setOptions(Array("-R", "first"))
    stwv.setTFTransform(false);
    stwv.setIDFTransform(false);
    //println(stwv.getOptions.foreach(print))

    var tokenizer: NGramTokenizer = new NGramTokenizer()
    tokenizer.setNGramMinSize(1)
    tokenizer.setNGramMaxSize(2) //Można ustalać długość n-gramów
    stwv.setTokenizer(tokenizer)
    //tokenizer.tokenize(input)
    //val stream:InputStream  = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    val ds: DataSource = new DataSource(input)
    var data: Instances = ds.getDataSet()
    //data.toArray.foreach(x => println(x))
    val nominalToString: NominalToString = new NominalToString()
    nominalToString.setInputFormat(data)
    nominalToString.setOptions(Array("-C", "first"))
    val dataStr: Instances = weka.filters.Filter.useFilter(data, nominalToString)
    stwv.setInputFormat(dataStr)
    val newData: Instances = weka.filters.Filter.useFilter(dataStr, stwv)
    //println(newData)
    //newData.toArray.foreach(x => println(x))
    //newData.insertAttributeAt(newData.attribute(0).copy("class2"),newData.numAttributes()) //Do rozważenia przy próbie przenoszenia klasy na koniec
    newData.setClassIndex(0)
    newData
  }

  def loadData(location: String) = {
    var source2: DataSource = new DataSource(location)
    var data: Instances = source2.getDataSet()
    data
  }

  def createTree(loccation: String): RandomTree = {
    var data = createVector(loccation)
    var tree: RandomTree = new RandomTree()
    //data.setClassIndex(data.numAttributes() - 1)
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
