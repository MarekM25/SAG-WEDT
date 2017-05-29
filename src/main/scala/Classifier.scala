import java.io.{File, FileOutputStream, PrintWriter}

import akka.actor.ActorRef
import org.htmlcleaner.{CleanerProperties, SimpleHtmlSerializer, TagNode}
import weka.classifiers.misc.InputMappedClassifier
import weka.core.Instances
import weka.core.stemmers.LovinsStemmer
import weka.core.stopwords.WordsFromFile
import weka.core.tokenizers.NGramTokenizer
import weka.filters.unsupervised.attribute.{NominalToString, StringToWordVector}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Kamil on 16.04.2017.
  */
class MyClassifier (TrainingDispacher : ActorRef) extends UCCreator {

  override def createVector(data: Instances) = {
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
    //println(stwv.getOptions.foreach(print))

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
    //println(newData)
    newData.setClassIndex(0)
    newData
  }

  def predict(data: String, tree: InputMappedClassifier): Boolean = {
    val inputData = loadDataFromString(data)
    val input = createVector(inputData)
    //println("PREDICT DATA" +input)
    val addRate = 0.5
    tree.setSuppressMappingReport(true)
    val vote = tree.classifyInstance(input.get(0))
    //println(vote)
    if (vote > addRate)
      false
    else
      true
  }


  def addWordToTrain(word:String): Unit ={
    val writer = new PrintWriter(new FileOutputStream(new File("Files\\DictRetrain.txt"),true))
    writer.write(word+'\n')
    writer.close()
    println("Train: "+word)
  }

  def removeAds(url: String) = //: String =
  {
    //Model.loadModel
    val ucs = Model.getNRandomClassifiers(500)
    //println(x)
    val parser = new SiteParser()

    val rootNode = parser.htmlToTreeFromUrl(url)
    //parser.htmlToTreeFromFile(url)
    val elements = parser.getElementsToClassify(rootNode)
    var results = new ArrayBuffer[(Int, TagNode)]
    for (elem <- elements) {
      var votesFor : Int = 0
      var votesAgainst : Int = 0
      for (t <- ucs) {
        if (predict(elem._1, t) == true)
          votesFor += 1
        else
          votesAgainst += 1
      }
      results += ((votesFor, elem._2))
      //println(votesFor, votesAgainst)
    }
    val unique = results.distinct
    results = results.sortWith(_._1 < _._1)
    val min = results.head._1
    val max = results.last._1
    for (r <- results) {
      if (r._1 >= 2 * min && r._1 >= 0.7 * max && r._1 >= 200)
        r._2.removeFromTree();
    }
    val props = new CleanerProperties();
    val htmlSerializer = new SimpleHtmlSerializer(props);
    var str = htmlSerializer.getAsString(rootNode);
    new PrintWriter("ClearPages\\cleanpage.html") {
      write(str); close
    }
    var dif = results.map{ucs.size - 2*_._1}
    dif = dif.map(math.abs(_))
    if(dif.reduceLeft(_ + _)/dif.length <= ucs.size/5)
      TrainingDispacher ! url
    //str
  }

  def testClassifier(): Unit ={
    val nrOfModels = 500;
    val retrainParam= 5;
    val f= new PrintWriter(new File("Files\\DictRetrain.txt"))
    f.close()
    Model.loadModel
    val TP = 0;
    val FP = 1;
    val FN = 2;
    val TN = 3;
    val filename = "Files\\TestDictClear.txt"
    val parser: SiteParser = new SiteParser
    val results = Array.ofDim[Int](scala.io.Source.fromFile(filename).getLines.size, 4)
    var i:Int = 0;
    results(i)(TP)=0;
    results(i)(FN)=0;
    results(i)(FP)=0;
    results(i)(TN)=0;
    for (line <- scala.io.Source.fromFile(filename).getLines) {
      var diffSum=0;
      println(line)
      //println("getDiv")
      val examples = parser.getDivTextsFromFile("TestPages\\" + line + ".html")
      val ucs = Model.getNRandomClassifiers(nrOfModels)
      for (elem <- examples) {
        var votesFor = 0
        var votesAgainst = 0
        for (t <- ucs) {
          if (predict(elem._1, t) == true)
            votesFor += 1
          else
            votesAgainst += 1
        }
        println(votesFor, votesAgainst,elem._2)
        val diff = votesFor -votesAgainst
        diffSum+=Math.abs(diff)
        val dec =diff>(-1*(nrOfModels/5))
        if(dec) {
          if(elem._2)
          results(i)(TP)+=1
          else
            results(i)(FP)+=1
        }else
        if(elem._2)
          results(i)(FN)+=1
        else
          results(i)(TN)+=1
      }
      i +=1
      //println((diffSum/(examples.size)))
      if((diffSum/(examples.size))<(nrOfModels/retrainParam))
        addWordToTrain(line)
    }
    for(x <-results){
      x.foreach(y=> print(y+" "))
      println()
    }
    results.foreach(x => x.foreach(y => print(y+" ")))
  }

  override def receive = {
    case "test"=> testClassifier()
    case (data: String) => removeAds(data)
    //case (data: String, tree: InputMappedClassifier) => sender() ! predict(data, tree) //Odsyłamy senderowi
    //case (data: (String,Boolean), tree: InputMappedClassifier) => sender() ! predict(data, tree)
  }
}
