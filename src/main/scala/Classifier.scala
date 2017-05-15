import java.io.PrintWriter

import org.htmlcleaner.{CleanerProperties, SimpleHtmlSerializer}
import weka.classifiers.misc.InputMappedClassifier
import weka.core.Instances
import weka.core.stemmers.LovinsStemmer
import weka.core.stopwords.WordsFromFile
import weka.core.tokenizers.NGramTokenizer
import weka.filters.unsupervised.attribute.{NominalToString, StringToWordVector}

/**
  * Created by Kamil on 16.04.2017.
  */
class MyClassifier extends TreeCreator {
  //  def predict(location: String, tree: InputMappedClassifier): Boolean = {
  //    val inputData = loadData(location)
  //    val input = createVector(inputData)
  //    //println(input.get(0))
  //    //val mappedClassifier: InputMappedClassifier = new InputMappedClassifier()
  //    //mappedClassifier.setClassifier(tree)
  //    //mappedClassifier.constructMappedInstance(input.get(0))
  //    //weka.core.SerializationHelper.write("Files\\tree.model",tree)
  //    //mappedClassifier.setModelPath("Files\\tree.model")
  //    //mappedClassifier.buildClassifier(input)
  //    //println(mappedClassifier.getOptions.foreach(print))
  //    //println(tree.classifyInstance(input.get(0)))
  //    //println(mappedClassifier)
  //    //println(tree.classifyInstance(input.get(0)))
  //    val addRate = 0.5
  //    val vote = tree.classifyInstance(input.get(0))
  //    println(vote)
  //    if (vote < addRate)
  //      false
  //    else
  //      true
  //  }

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
    //tokenizer.tokenize(input)
    //val stream:InputStream  = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    //val ds: DataSource = new DataSource(input)
    //var data: Instances = ds.getDataSet()
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

  def predict(data: String, tree: InputMappedClassifier): Boolean = {
    val inputData = loadDataFromString(data)
    val input = createVector(inputData)
    //println("PREDICT DATA" +input)
    val addRate = 0.5
    val vote = tree.classifyInstance(input.get(0))
    tree.setSuppressMappingReport(true)
    //println(vote)
    if (vote > addRate)
      false
    else
      true
  }


  def removeAds(url: String) = //: String =
  {
    //Model.loadModel
    val trees = Model.getNRandomClassifiers(500)
    //println(x)
    val parser = new SiteParser()

    val rootNode = parser.htmlToTreeFromUrl(url)
    //parser.htmlToTreeFromFile(url)
    val elements = parser.getElementsToClassify(rootNode)
    for (elem <- elements) {
      var votesFor = 0
      var votesAgainst = 0
      for (t <- trees) {
        if (predict(elem._1, t) == true)
          votesFor += 1
        else
          votesAgainst += 1
      }
      println(votesFor, votesAgainst)
      if (votesFor > votesAgainst)
        elem._2.removeFromTree();
    }
    val props = new CleanerProperties();
    val htmlSerializer = new SimpleHtmlSerializer(props);
    var str = htmlSerializer.getAsString(rootNode);
    new PrintWriter("ClearPages\\somename_clean.html") {
      write(str); close
    }
    //str
  }


  override def receive = {
    case (data: String) => removeAds(data)
    //case (data: String, tree: InputMappedClassifier) => sender() ! predict(data, tree) //Odsyłamy senderowi
    //case (data: (String,Boolean), tree: InputMappedClassifier) => sender() ! predict(data, tree)
  }
}
