import weka.classifiers.misc.InputMappedClassifier
import weka.classifiers.trees.RandomTree

/**
  * Created by Kamil on 16.04.2017.
  */
class MyClassifier extends TreeCreator {
  def predict(location: String, tree: InputMappedClassifier): Boolean = {
    val input = createVector(location)
    //println(input.get(0))
    //val mappedClassifier: InputMappedClassifier = new InputMappedClassifier()
    //mappedClassifier.setClassifier(tree)
    //mappedClassifier.constructMappedInstance(input.get(0))
    //weka.core.SerializationHelper.write("Files\\tree.model",tree)
    //mappedClassifier.setModelPath("Files\\tree.model")
    //mappedClassifier.buildClassifier(input)
    //println(mappedClassifier.getOptions.foreach(print))
    //println(tree.classifyInstance(input.get(0)))
    //println(mappedClassifier)
    //println(tree.classifyInstance(input.get(0)))
    val addRate = 0.5
    val vote = tree.classifyInstance(input.get(0))
    println(vote)
    if (vote < addRate)
      false
    else
      true
  }

  override def receive = {
    case (location: String, tree: InputMappedClassifier) => sender() ! predict(location, tree) //Odsyłamy senderowi
  }
}
