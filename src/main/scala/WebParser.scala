import java.net.URL

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.apache.commons.lang3.StringEscapeUtils
import org.htmlcleaner.{CleanerProperties, HtmlCleaner, SimpleHtmlSerializer, TagNode}

import scala.collection.mutable.ListBuffer

/**
  * Created by Marek on 21.04.2017.
  */
 class WebParser {

  def htmlToTree(url: String) : TagNode = {
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties

    val webClient = new WebClient()
    val page : HtmlPage = webClient.getPage(new URL(url))
    val rootNode = cleaner.clean(page.getWebResponse().getContentAsString()) // get tree representation of html
    rootNode
  }

  def removeStyleNodes(rootNode : TagNode) : TagNode = {
    // get rid of style nodes to get cleaner text
    val styleNodes = rootNode.getElementsByName("style", true)
    for (node <- styleNodes) {
      node.removeFromTree()
    }
    rootNode
  }

  // input: url, output: list of (div text, is ad) pairs
  def getDivTextsFromUrl(url: String) : List[(String, Boolean)] = {
    var texts = new ListBuffer[(String, Boolean)]
    //val rootNode = removeStyleNodes(htmlToTree(url))
    val rootNode = htmlToTree(url)
    var elements = rootNode.getElementsByName("div", true)
    //var lastParent = rootNode
    for (elem <- elements) {
      val classType = elem.getAttributeByName("class")
      // quick, well performing, but limited to yahoo
      //val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
      if (classType != null && classType.contains("ads")) {
        val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
        texts += ((text, true))
      }
      else if (classType != null && classType.contains("dd algo algo-sr")) {
        val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
        texts += ((text, false))
      }
    }


    /*if (classType != null && classType.contains("ads" /*"scta reg searchCenterTopAds"*/)) {
      // stories might be "dirty" with text like "'", clean it up
      val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
      texts += ((text, true)) //text
      var level = 0
      var parent = elem.getParent()
      while (parent != null && parent.getName != "div") {
        parent = parent.getParent()
        level += 1
      }
      elem.removeFromTree()
      //val siblings : Array[TagNode] = parent.getAllChildren().asScala.toArray//.getElementsByName("div", true)//getAllChildren().asScala
      //        val siblings = parent.getChildTags()
      if (parent != lastParent) {

        var siblings = parent.getChildTags() //parent.getElementsByName("div", true)
        val nlsiblings: ArrayBuffer[TagNode] = new ArrayBuffer[TagNode]
        while (level > 0) {
          for (sib: TagNode <- siblings) {
            nlsiblings ++= sib.getChildTags()
          }
          level -= 1
          siblings = nlsiblings.toArray
        }
        //siblings = siblings.filter(_.getName == "div")
        for (sib: TagNode <- siblings) {
          val classType = sib.getAttributeByName("class")
          if (classType != null && classType.contains("ads")) {
            texts += ((sib.getText.toString, true))
          }
          else {
            texts += ((sib.getText.toString, false))
          }
        }
        lastParent = parent
        parent.removeFromTree()
      }
    }
    //elem = rootNode.findElementByName("div", true)
    }*/

    //    elem = rootNode
    //    while(elem != null){
    //      elem = rootNode.findElementByName("div", true)
    //      if(elem != null) {
    //        texts += ((elem.getText.toString, false))
    //        elem.removeFromTree()
    //      }
    //    }

    //    var parent = rootNode
    //    while(parent.getName != "div")
    //      parent = parent.findElementByName("div", true);
    //    texts += ((parent.getText.toString, false))

    texts.toList
  }


  def removeAds(url: String): String =
  {
    //val orgRootNode = htmlToTree(url)
    //val rootNode = removeStyleNodes(orgRootNode)
    val rootNode = htmlToTree(url)
    var elements = rootNode.getElementsByName("div", true)
    for (elem <- elements) {
      val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
      val words1 = text.split("\\s+")
      val words = text.split("\\s+").length
      var dec : Boolean = false
      val r = scala.util.Random
      if(words >= 10) {
        dec = false //r.nextBoolean();
        val classType = elem.getAttributeByName("class")
        if (classType != null && classType.contains("ads"))
          dec = true
        if(dec == true)
          elem.removeFromTree();
      }
    }

    val props = new CleanerProperties();
    val htmlSerializer = new SimpleHtmlSerializer(props);
    var str = htmlSerializer.getAsString(rootNode);
    str
  }

//  def receive = {
//    case x:String=> {
//      var divTexts = getDivTextsFromUrl(x)
//      divTexts.foreach(println);
//      var cleanHtml = removeAds(x)
//      cleanHtml.foreach(println);
//    }
}