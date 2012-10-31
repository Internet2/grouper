/*
 * @author mchyzer
 * $Id: XmlIndenter.java,v 1.2 2008-12-01 07:40:23 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiServer.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * indent xml, assumes the input is not yet indented.  Also, this is only for
 * testing or logging or documentation purposes, not production
 */
public class XmlIndenter {
  
  /** chars to process */
  private String xml;
  
  /** current start tag */
  private int startTagIndex;
  
  /** current end tag */
  private int endTagIndex;
  
  /** current number of indents (times to is the indent */
  private int currentNumberOfIndents;
  
  /** current tag we are on */
  private String currentTagName;
  
  /** result */
  private StringBuilder result;
  
  /**
   * get the result
   * @return the result
   */
  public String result() {
    try {
      this.indent();
    } catch (RuntimeException re) {
      throw new RuntimeException("Problem here: " + this, re);
    }
    if (this.xml == null) {
      return null;
    }
    return StandardApiServerUtils.trim(this.result.toString());
  }

  /**
   * indent the string
   */
  private void indent() {
    if (this.xml == null) {
      return;
    }
    this.result = new StringBuilder();
    this.startTagIndex = -1;
    this.endTagIndex = -1;
    this.currentTagName = null;
    this.currentNumberOfIndents = 0;
    //<a><b whatever=\"whatever\"><c>hey</c><d><e>there</e><f /><g / ><h></h></d></b></a>
    //<a>
    //  <b whatever="whatever">
    //    <c>hey</c>
    //    <d>
    //      <e>there</e>
    //      <f />
    //      <g / >
    //      <h></h>
    //    </d>
    //  </b>
    //</a>
    while(true) {
      this.startTagIndex = findStartTagIndex();
      if (this.startTagIndex == -1) {
        //cant find anything else...  make sure everything there
        if (this.endTagIndex != this.xml.length()-1) {
          this.result.append(this.xml, this.endTagIndex+1, this.xml.length());
        }
        break;
      }
      this.endTagIndex = findEndTagIndex();
      
      //if XML or doctype, then just print with newline and continue
      if (ignoreTag(this.xml, this.startTagIndex, this.endTagIndex)) {
        
        //just return and indent
        //lets put this tag on the queue
        this.printNewlineIndent(this.startTagIndex, this.endTagIndex+1);
        continue;
      }
      
      this.currentTagName = findTagName();
      
      //if self closed, then carry on
      if (selfClosedTag(this.xml, this.endTagIndex)) {
        //just return and indent
        //lets put this tag on the queue
        this.printNewlineIndent(this.startTagIndex, this.endTagIndex+1);
      } else if (closeTag(this.xml, this.startTagIndex)) {
        //if end tag, then return and unindent
        this.unindent();
        this.currentNumberOfIndents--;
        //lets put this tag on the queue
        this.printNewlineIndent(this.startTagIndex, this.endTagIndex+1);
        
      } else {
        int nextTagStartIndex = findNextStartTagIndex(this.xml, this.endTagIndex+1);
        int nextTagEndIndex = findNextEndTagIndex(this.xml, nextTagStartIndex+1);
        
        String nextTagName = tagName(this.xml, nextTagStartIndex, nextTagEndIndex);
        boolean isNextTagCloseTag = closeTag(this.xml, nextTagStartIndex);
        if (!textTag(this.xml, this.endTagIndex, this.currentTagName, nextTagName, isNextTagCloseTag)) {
          this.currentNumberOfIndents++;
          this.printNewlineIndent(this.startTagIndex, this.endTagIndex+1);
        } else {
          //else this is a text tag, print from here to end of next tag, newline and indent
          this.printNewlineIndent(this.startTagIndex, nextTagEndIndex+1);
          //increment past the next one
          this.startTagIndex = nextTagEndIndex;
          this.endTagIndex = nextTagEndIndex;
        }
      }
    }
  }
  
  /**
   * see if we can ignore the tag, e.g. xml header or doctype
   * @param theXml
   * @param theStartTagIndex
   * @param theEndTagIndex
   * @return true if ignore
   */
  static boolean ignoreTag(String theXml, int theStartTagIndex, int theEndTagIndex) {
    char firstChar = theXml.charAt(theStartTagIndex+1);
    if (firstChar == '?' || firstChar == '!') {
      return true;
    }
    return false;
  }
  
  /**
   * put a newline and indent
   * @param start
   * @param end
   */
  private void printNewlineIndent(int start, int end) {
    //lets put this tag on the queue
    this.result.append(this.xml, start, end);
    this.newlineIndent();
    
  }

  /**
   * put a newline and indent
   */
  private void newlineIndent() {
    this.result.append("\n").append(StandardApiServerUtils.repeat("  ", this.currentNumberOfIndents));
  }
  
  /**
   * unindent a previous indent if it is there
   */
  private void unindent() {
    for (int i=0;i<2;i++) {
      if (this.result.charAt(this.result.length()-1) == ' ') {
        this.result.deleteCharAt(this.result.length()-1);
      }
    }
  }
  
  /**
   * find the current tag name
   * should support: &lt; a /&gt;
   * or &lt; / b&gt;
   * @param xml
   * @param startTagIndex
   * @param endTagIndex (or -1 if none found)
   * @return the current tag name
   */
  static String tagName(String xml, int startTagIndex, int endTagIndex) {
    endTagIndex = endTagIndex > startTagIndex ? endTagIndex : (xml.length()-1);
    String tag = xml.substring(startTagIndex, endTagIndex+1);
    Pattern tagPattern = Pattern.compile("^<[\\s/]*([a-zA-Z_\\-0-9:\\.]+).*$", Pattern.DOTALL);
    Matcher matcher = tagPattern.matcher(tag);
    if (!matcher.matches()) {
      Pattern commentPattern = Pattern.compile("^<!--.*-->$");
      matcher = commentPattern.matcher(tag);
      if (matcher.matches()) {
        return "XML_COMMENT";
      }
      throw new RuntimeException("Cant match tag: '" + tag + "'");
    }
    //assume this matches...
    String tagName = matcher.group(1);
    return tagName;
  }
  
  /**
   * after the last end tag, find the next start tag
   * @return the next start tag
   */
  private int findStartTagIndex() {
    return findNextStartTagIndex(this.xml, this.endTagIndex+1);
  }

  /**
   * after the last end tag, find the next start tag
   * @return the next start tag
   */
  private String findTagName() {
    return tagName(this.xml, this.startTagIndex, this.endTagIndex);
  }

  /**
   * after the last start tag, find the next end start tag
   * @return the next start tag
   */
  private int findEndTagIndex() {
    return findNextEndTagIndex(this.xml, this.startTagIndex+1);
  }

  /**
   * find the start tag from xml and a start from index
   * @param xml
   * @param startFrom
   * @return the start tag index of -1 if not found another
   */
  static int findNextStartTagIndex(String xml, int startFrom) {
    int length = xml.length();
    for (int i= startFrom; i<length;i++) {
      if (xml.charAt(i) == '<') {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * find the end tag from xml and a start from index
   * @param xml
   * @param startFrom
   * @return the start tag index of -1 if not found another
   */
  static int findNextEndTagIndex(String xml, int startFrom) {
    int length = xml.length();
    for (int i= startFrom; i<length;i++) {
      if (xml.charAt(i) == '>') {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * find if the tag is closed on 
   * @param xml
   * @param endTagIndex
   * @return true if self closed
   */
  static boolean selfClosedTag(String xml, int endTagIndex) {
    for (int i=endTagIndex-1;i>=0;i--) {
      char curChar = xml.charAt(i);
      //ignore whitespace
      if (Character.isWhitespace(curChar)) {
        continue;
      }
      if (curChar == '/') {
        return true;
      }
      return false;
    }
    //shouldnt really get here...
    return false;
  }

  /**
   * find if the tag is a close tag (e.g. &lt;/a&gt;)
   * @param xml
   * @param startTagIndex
   * @return true if self closed
   */
  static boolean closeTag(String xml, int startTagIndex) {
    for (int i=startTagIndex+1;i<xml.length();i++) {
      char curChar = xml.charAt(i);
      //ignore whitespace
      if (Character.isWhitespace(curChar)) {
        continue;
      }
      //could be a comment
      if (curChar == '/' || curChar == '!') {
        return true;
      }
      return false;
    }
    //shouldnt really get here...
    return false;
  }

  /**
   * find if the tag contains text (note, dont call this if know it is self closed,
   * though in that case it shouldnt be text anyways)
   * @param xml
   * @param endTagIndex
   * @param tagName 
   * @param nextTagName 
   * @param isNextCloseTag 
   * @return true if contains text (as opposed to other tags)
   */
  static boolean textTag(String xml, int endTagIndex, String tagName, 
      String nextTagName, boolean isNextCloseTag) {
    if (StandardApiServerUtils.equals("XML_COMMENT", tagName)) {
      return false;
    }
    if (StandardApiServerUtils.equals(tagName, nextTagName) && isNextCloseTag) {
      return true;
    }
    return false;
  }

  /**
   * @param theXml is the xml to format
   * indenter
   */
  public XmlIndenter(String theXml) {
    if (theXml != null) {
      this.xml = StandardApiServerUtils.trimToEmpty(theXml);
    }
  }
  
}
