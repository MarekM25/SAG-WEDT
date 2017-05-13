import weka.classifiers.misc.InputMappedClassifier

/**
  * Created by Kamil on 13.05.2017.
  */
object Model {
  var models = new scala.collection.mutable.ArrayBuffer[InputMappedClassifier]
  val r = scala.util.Random

  def addToMap(m:InputMappedClassifier) ={
    models.append(m)
  }

  def printModels(): Unit ={
    //println(models(0))
    models.foreach(m => println(m))
  }
 def saveModel ={
   for(i <- 0 to models.size-1)
      weka.core.SerializationHelper.write("Files\\Models\\classifier"+i+".model",models(i))
 }
  def loadModel = {
    val modelsize = 2
    for(i <- 0 to modelsize -1)
      models.append((weka.core.SerializationHelper.read("Files\\Models\\classifier"+i+".model")).asInstanceOf[InputMappedClassifier])
  }

  def getOneRandomClassifier : InputMappedClassifier ={
    models(r.nextInt(models.size))
  }
}
