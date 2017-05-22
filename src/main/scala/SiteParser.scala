import java.io.{File, PrintWriter}
import java.net.URL

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.apache.commons.lang3.StringEscapeUtils
import org.htmlcleaner.{HtmlCleaner, TagNode}

import scala.collection.mutable.ListBuffer
/**
  * Created by Marek on 21.04.2017.
  */
class SiteParser {

  def htmlToTreeFromUrl(url: String) : TagNode = {
    val cleaner = new HtmlCleaner

    val webClient = new WebClient()
    val page : HtmlPage = webClient.getPage(new URL(url))
    val rootNode = cleaner.clean(page.getWebResponse().getContentAsString()) // get tree representation of html
    rootNode
  }

  def htmlToTreeFromFile(fileName: String) : TagNode = {
    val cleaner = new HtmlCleaner

    val content = scala.io.Source.fromFile(fileName).mkString
    val rootNode = cleaner.clean(content) // get tree representation of html
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

  def getYahooSites() = {
    val webClient = new WebClient()
    val filename = "Files\\SearchDict.txt"
    for (line <- scala.io.Source.fromFile(filename).getLines) {
      val page : HtmlPage = webClient.getPage(new URL("https://search.yahoo.com/search;?p="+line))
      new PrintWriter("Pages\\"+line+".html") { write(page.getWebResponse().getContentAsString()); close }
    }
  }

  def removeSitesWithNoAds() = {
    val filename = "Files\\SearchDict.txt"
    for (line <- scala.io.Source.fromFile(filename).getLines) {
      if (!this.checkIfHasAds("Pages\\" +line +".html"))
        new File("Pages\\"+line+".html").delete()
      else
        print(line+"\n")
    }
  }

  def getDataToFileFromUrl(url: String) = {
    val webClient = new WebClient()
    val page : HtmlPage = webClient.getPage(new URL(url))
    new PrintWriter("filename") { write(page.asXml()); close }
  }
  def c1(x: (String, Boolean)) = x._2

  def checkIfHasAds(filename : String) : Boolean = {
    val parser = new SiteParser()
    val listOfTexts = parser.getDivTextsFromFile(filename)
    listOfTexts.exists(x=>c1(x))
  }


//  // input: url, output: list of (div text, is ad) pairs
//  def getDivTextsFromFile(file: String) : List[(String, Boolean)] = {
//    var texts = new ListBuffer[(String, Boolean)]
//    //val rootNode = removeStyleNodes(htmlToTree(url))
//    val rootNode = htmlToTreeFromFile(file)
//    var elements = rootNode.getElementsByName("div", true)
//    //var lastParent = rootNode
//    for (elem <- elements) {
//      val classType = elem.getAttributeByName("class")
//      // quick, well performing, but limited to yahoo
//      //val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
//      if (classType != null && classType.contains("ads")) {
//        val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
//        texts += ((text, true))
//      }
//      else if (classType != null && classType.contains("dd algo algo-sr")) {
//        val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
//        texts += ((text, false))
//      }
//    }
//
//    /*if (classType != null && classType.contains("ads" /*"scta reg searchCenterTopAds"*/)) {
//      // stories might be "dirty" with text like "'", clean it up
//      val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
//      texts += ((text, true)) //text
//      var level = 0
//      var parent = elem.getParent()
//      while (parent != null && parent.getName != "div") {
//        parent = parent.getParent()
//        level += 1
//      }
//      elem.removeFromTree()
//      //val siblings : Array[TagNode] = parent.getAllChildren().asScala.toArray//.getElementsByName("div", true)//getAllChildren().asScala
//      //        val siblings = parent.getChildTags()
//      if (parent != lastParent) {
//
//        var siblings = parent.getChildTags() //parent.getElementsByName("div", true)
//        val nlsiblings: ArrayBuffer[TagNode] = new ArrayBuffer[TagNode]
//        while (level > 0) {
//          for (sib: TagNode <- siblings) {
//            nlsiblings ++= sib.getChildTags()
//          }
//          level -= 1
//          siblings = nlsiblings.toArray
//        }
//        //siblings = siblings.filter(_.getName == "div")
//        for (sib: TagNode <- siblings) {
//          val classType = sib.getAttributeByName("class")
//          if (classType != null && classType.contains("ads")) {
//            texts += ((sib.getText.toString, true))
//          }
//          else {
//            texts += ((sib.getText.toString, false))
//          }
//        }
//        lastParent = parent
//        parent.removeFromTree()
//      }
//    }
//    //elem = rootNode.findElementByName("div", true)
//    }*/
//
//    //    elem = rootNode
//    //    while(elem != null){
//    //      elem = rootNode.findElementByName("div", true)
//    //      if(elem != null) {
//    //        texts += ((elem.getText.toString, false))
//    //        elem.removeFromTree()
//    //      }
//    //    }
//
//    //    var parent = rootNode
//    //    while(parent.getName != "div")
//    //      parent = parent.findElementByName("div", true);
//    //    texts += ((parent.getText.toString, false))
//
//    texts.toList
//  }


  // input: url, output: list of (div text, is ad) pairs
  def getDivTextsFromFile(file: String) : List[(String, Boolean)] = {
    var texts = new ListBuffer[(String, Boolean)]
    //val rootNode = removeStyleNodes(htmlToTree(url))
    val rootNode = htmlToTreeFromFile(file)
    val list = getElementsToClassify(rootNode)
    for (elem <- list) {
      var isAdd = false
      val subelems = elem._2.getElementsByName("div", true)
      for (selem <- subelems) {
        val classType = selem.getAttributeByName("class")
        // quick, well performing, but limited to yahoo
        if (classType != null && classType.contains("ads"))
          isAdd = true
      }
      if (isAdd)
        texts += ((elem._1, true))
      else
        texts += ((elem._1, false))
    }
    texts.toList
  }

  def getElementsToClassify (rootNode : TagNode) = {
    val list = new ListBuffer[(String, TagNode)]
    var elements = rootNode.getElementsByName("li", true)
    for (elem <- elements) {
      val subelems = elem.getElementsByName("div", true)
      var hasTitle = false
      var hasText = false
      for (selem <- subelems) {
        val classType = selem.getAttributeByName("class")
        if (classType != null) {
          if (classType.contains("Title")) {
            hasTitle = true
          }
          else if (classType.contains("Text")) {
            hasText = true
          }
        }
      }
      if (hasText && hasTitle) {
        val text = StringEscapeUtils.unescapeHtml4(elem.getText.toString)
        val words = text.split("\\s+")
        val wordsnum = text.split("\\s+").length
        if (wordsnum >= 10) {
          list += ((text, elem))

          //if (mc.predict(text, x))
          //elem.removeFromTree();
        }
      }
    }
    list
  }




//    def receive = {
//      case x:String=> {
//        //var divTexts = getDivTextsFromUrl(x)
//        //divTexts.foreach(println);
//        var cleanHtml = removeAds(x)
//        //cleanHtml.foreach(println);
//      }
//    }
}