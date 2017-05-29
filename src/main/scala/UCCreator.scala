import java.util

import akka.actor.Actor
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.misc.InputMappedClassifier
import weka.core.converters.ConverterUtils.DataSource
import weka.core.stemmers.LovinsStemmer
import weka.core.stopwords.WordsFromFile
import weka.core.tokenizers.NGramTokenizer
import weka.core.{Attribute, DenseInstance, Instances}
import weka.filters.unsupervised.attribute.{NominalToString, StringToWordVector}


/**
  * Created by Kamil on 12.04.2017.
  */

class UCCreator extends Actor {

  def createVector(data: Instances) = {
    /*
    Input - lokalizacja pliku csv w postaci text,class
    Output - wektor występowania słów w kolejnych linijkach pliku (UWAGA! atrybut class, znajduje się na początku nowego datasetu)
     */
    var stwv: StringToWordVector = new StringToWordVector()
    stwv.setOptions(Array("-R", "first"))
    stwv.setLowerCaseTokens(true)
    stwv.setMinTermFreq(1);
    val stemmer: LovinsStemmer = new LovinsStemmer()
    val stopWords: WordsFromFile = new WordsFromFile()
    stopWords.setOptions(Array("-stopwords", "Files\\stopwords.txt"))
    stwv.setStopwordsHandler(stopWords)
    stwv.setStemmer(stemmer)
    stwv.setTFTransform(false)
    stwv.setIDFTransform(false)

    var tokenizer: NGramTokenizer = new NGramTokenizer()
    tokenizer.setNGramMinSize(1)
    tokenizer.setNGramMaxSize(2) //Można ustalać długość n-gramów
    stwv.setTokenizer(tokenizer)
    val nominalToString: NominalToString = new NominalToString()
    nominalToString.setInputFormat(data)
    nominalToString.setOptions(Array("-C", "first"))
    val dataStr: Instances = weka.filters.Filter.useFilter(data, nominalToString)
    stwv.setInputFormat(dataStr)
    val newData: Instances = weka.filters.Filter.useFilter(dataStr, stwv)
    newData.setClassIndex(0)
    newData
  }


  def loadDataFromString(input: String) = {

    var textAttribute: Attribute = new Attribute("text", null.asInstanceOf[util.ArrayList[String]])
    val attributes = new util.ArrayList[Attribute]
    attributes.add(textAttribute)
    var data: Instances = new Instances("data", attributes, 0)
    var instanceValue1: Array[Double] = new Array[Double](1)
    instanceValue1(0) = data.attribute(0).addStringValue(input)
    data.add(new DenseInstance(1.0, instanceValue1));
    data
  }

  def loadDataFromString(inputArray: Array[(String, Boolean)]) = {

    var classVal = new util.ArrayList[String]()
    classVal.add("1")
    classVal.add("0")
    var textAttribute: Attribute = new Attribute("text", null.asInstanceOf[util.ArrayList[String]])
    var classAttribute: Attribute = new Attribute("class", classVal)
    val attributes = new util.ArrayList[Attribute]
    attributes.add(textAttribute)
    attributes.add(classAttribute)
    var data: Instances = new Instances("data", attributes, 0)

    def addTodata(input: (String, Boolean)) = {
      var instanceValue1: Array[Double] = new Array[Double](2);
      instanceValue1(0) = data.attribute(0).addStringValue(input._1);
      if (input._2)
        instanceValue1(1) = 0;
      else
        instanceValue1(1) = 1;
      data.add(new DenseInstance(1.0, instanceValue1));
    }

    inputArray.foreach(in => addTodata(in));
    data.setClassIndex(1)
    data
  }

  def loadData(location: String) = {
    var source2: DataSource = new DataSource(location)
    var data: Instances = source2.getDataSet()
    data
  }


  def loadDataFromFile(file: String) = {
    val parser = new SiteParser()
    val data = parser.getDivTextsFromFile(file)
    //val vec = createVector(data)
    data
  }

  def loadDataFromUrl(url: String) = {
    val parser = new SiteParser()
    val data = parser.getDivTextsFromUrl(url)
    //val vec = createVector(data)
    data
  }

  def createUC(fileData : Array[(String, Boolean)]) = {
    //fileData.foreach(x => println(x._1,x._2))
    val loaddata = loadDataFromString(fileData)
    val data = createVector(loaddata)
    var uc: InputMappedClassifier = new InputMappedClassifier()
    uc.setClassifier(new NaiveBayes)
    uc.setSuppressMappingReport(true)
    //println(data)
    uc.buildClassifier(data)
    //var eval: Evaluation = new Evaluation(data)
    //println(uc)
    uc
  }

  def createUCFromFile(location: String) = {
    val fileData = loadDataFromFile(location).toArray[(String, Boolean)]
    createUC(fileData)
  }

  def createUCFromUrl(url: String) = {
    val fileData = loadDataFromUrl(url).toArray[(String, Boolean)]
    createUC(fileData)
  }

  def receive = {
    case location: String => sender() ! createUCFromFile(location) //Odsyłamy nadawcy
    case ("url", url:String) => sender() ! createUCFromUrl(url) //Odsyłamy nadawcy
    //case input: Array[(String,Boolean)] => sender() ! createTree(input)
  }
}