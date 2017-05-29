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

  // input: file location, output: list of (div text, is ad) pairs
  def getDivTexts(rootNode: TagNode) : List[(String, Boolean)] = {
    var texts = new ListBuffer[(String, Boolean)]
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

  // input: url, output: list of (div text, is ad) pairs
  def getDivTextsFromUrl(url: String) : List[(String, Boolean)] = {
    val rootNode = htmlToTreeFromUrl(url)
   getDivTexts(rootNode)
  }

  // input: file location, output: list of (div text, is ad) pairs
  def getDivTextsFromFile(file: String) : List[(String, Boolean)] = {
    val rootNode = htmlToTreeFromFile(file)
    getDivTexts(rootNode)
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
}