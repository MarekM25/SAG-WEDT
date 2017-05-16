import java.io.{File, PrintWriter}

import weka.classifiers.misc.InputMappedClassifier
import weka.core.Debug.Log

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Created by Kamil on 13.05.2017.
  */
object Model {
  var models = new scala.collection.mutable.ArrayBuffer[InputMappedClassifier]
  val r = scala.util.Random

  def addToMap(m: InputMappedClassifier) = {
    models.append(m)
    println(models.size)
  }

  def addAndSave(m: InputMappedClassifier) = {
    models.append(m)
    val writer = new PrintWriter(new File("Files\\Models\\meta.txt" ))
    writer.write(models.size)
    writer.close()
    weka.core.SerializationHelper.write("Files\\Models\\classifier" + (models.size - 1) + ".model", models(models.size - 1))
    println(models.size)
  }

  def printModels(): Unit = {
    //println(models(0))
    models.foreach(m => println(m))
  }

  def saveModel = {
    val writer = new PrintWriter(new File("Files\\Models\\meta.txt" ))
    writer.write(models.size)
    writer.close()
    //println("Start saving")
    for (i <- 0 to models.size - 1) {
      weka.core.SerializationHelper.write("Files\\Models\\classifier" + i + ".model", models(i))
    }
    //println("Save completed")
  }

  def loadModel = {
    val modelsize = Source.fromFile("Files\\Models\\meta.txt").getLines().next().toInt
    println("Loading model")
    for (i <- 0 to modelsize - 1) {
      println(i)
      models.append((weka.core.SerializationHelper.read("Files\\Models\\classifier" + i + ".model")).asInstanceOf[InputMappedClassifier])
    }
  }

  def getOneRandomClassifier: InputMappedClassifier = {
    models(r.nextInt(models.size))
  }

  def getNRandomClassifiers(n: Int): ArrayBuffer[InputMappedClassifier] = {
    var array = new ArrayBuffer[InputMappedClassifier]
    for (i <- 1 to n)
      array += models(r.nextInt(models.size))
    array
  }

  def getNRandomClassifierNoRep(n: Int): ArrayBuffer[InputMappedClassifier] = {
    scala.util.Random.shuffle(models).take(n)
  }
}
