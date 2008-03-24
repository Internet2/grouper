/*
 * @author mchyzer
 * $Id: JsonIndenter.java,v 1.1 2008-03-24 20:15:35 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * indent json, assumes the input is not yet indented.  Also, this is only for
 * testing or logging or documentation purposes, not production
 */
public class JsonIndenter {
  
  /** chars to process */
  private String json;
  
  /** current start tag */
  private int startTagIndex;
  
  /** current end tag */
  private int endTagIndex;
  
  /** current number of indents (times to is the indent */
  private int currentNumberOfIndents;
  
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
    if (this.json == null) {
      return null;
    }
    return StringUtils.trim(this.result.toString());
  }

  /**
   * indent the string
   */
  private void indent() {
    if (this.json == null) {
      return;
    }
    this.result = new StringBuilder();
    this.startTagIndex = -1;
    this.endTagIndex = -1;
    this.currentNumberOfIndents = 0;
    //{
    //  "a":{
    //    "b\"b":{
    //      "c\\":"d"
    //    },
    //    "e":"f",
    //    "g":[
    //      "h":"i"
    //    ]
    //  }
    //}
    while(true) {
      this.startTagIndex = findStartTagIndex();
      
      if (this.startTagIndex == -1) {
        //cant find anything else...  make sure everything there
        if (this.endTagIndex != this.json.length()-1) {
          this.result.append(this.json, this.endTagIndex+1, this.json.length());
        }
        break;
      }
      
      //handles first tag
      if (instantIndent(this.json, this.startTagIndex)) {
        this.currentNumberOfIndents++;
        this.printNewlineIndent(this.startTagIndex, this.startTagIndex+1);
        this.endTagIndex = this.startTagIndex;
        continue;
      }
      
      //handles end of associative array with comma
      if (instantUnindentTwoChars(this.json, this.startTagIndex)) {
        this.currentNumberOfIndents--;
        //this is on a line by itself
        this.newlineIndent();
        this.printNewlineIndent(this.startTagIndex, this.startTagIndex+2);
        this.endTagIndex = this.startTagIndex+1;
        continue;
      }
      
      //handles end of array with comma
      if (instantUnindent(this.json, this.startTagIndex)) {
        this.currentNumberOfIndents--;
        if (onNewline()) {
          this.unindent();
        } else {
          this.newlineIndent();
        }
        this.printNewlineIndent(this.startTagIndex, this.startTagIndex+1);
        this.endTagIndex = this.startTagIndex;
        continue;
      }
      
      //handles end of array with comma
      if (instantNewline(this.json, this.startTagIndex)) {
        this.printNewlineIndent(this.startTagIndex, this.startTagIndex+1);
        this.endTagIndex = this.startTagIndex;
        continue;
      }
      
      this.endTagIndex = findEndTagIndex();
      
      //one thing's for sure, we are printing out this tag
      this.result.append(this.json, this.startTagIndex, this.endTagIndex+1);
      
      //go back to top to end
      if (this.endTagIndex >= this.json.length()-1) {
        continue;
      }
      char nextChar = this.json.charAt(this.endTagIndex+1);
      //if next is colon, print that out too
      if (nextChar == ':') {
        this.result.append(':');
        this.endTagIndex++;
      }
      
      //ready to loop around...
    }
  }
  
  /**
   * see if current pos is on newline
   * @return true if on new line
   */
  private boolean onNewline() {
    for (int i=this.result.length()-1;i>=0;i--) {
      char curChar = this.result.charAt(i);
      if (curChar == '\n') {
        return true;
      }
      if (Character.isWhitespace(curChar)) {
        continue;
      }
      //if not whitespace, then not on own line
      return false;
    }
    //i guess first line is new line
    return true;
  }
  
  /**
   * see if instant indent
   * @param json
   * @param index
   * @return if it is an instant indent
   */
  static boolean instantIndent(String json, int index) {
    char curChar = json.charAt(index);
    if (curChar == '{' || curChar == '[') {
      return true;
    }
    return false;
  }
  
  /**
   * see if instant indent
   * @param json
   * @param index
   * @return if it is an instant indent
   */
  static boolean instantNewline(String json, int index) {
    char curChar = json.charAt(index);
    if (curChar == ',') {
      return true;
    }
    return false;
  }
  
  /**
   * see if instant unindent
   * @param json
   * @param index
   * @return if it is an instant unindent
   */
  static boolean instantUnindent(String json, int index) {
    char curChar = json.charAt(index);
    if (curChar == '}' || curChar == ']') {
      return true;
    }
    return false;
  }
  
  /**
   * see if instant indent
   * @param json
   * @param index
   * @return if it is an instant indent
   */
  static boolean instantUnindentTwoChars(String json, int index) {
    char curChar = json.charAt(index);
    if (index == json.length()-1) {
      return false;
    }
    char nextchar = json.charAt(index+1);
    if (curChar == '}' && nextchar == ',') {
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
    this.result.append(this.json, start, end);
    this.newlineIndent();
    
  }

  /**
   * put a newline and indent
   */
  private void newlineIndent() {
    this.result.append("\n").append(StringUtils.repeat("  ", this.currentNumberOfIndents));
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
   * after the last end tag, find the next start tag
   * @return the next start tag
   */
  private int findStartTagIndex() {
    return findNextStartTagIndex(this.json, this.endTagIndex+1);
  }

  /**
   * after the last start tag, find the next end start tag
   * @return the next start tag
   */
  private int findEndTagIndex() {
    return findNextEndTagIndex(this.json, this.startTagIndex+1);
  }

  /**
   * find the start tag from xml and a start from index
   * either look for a quote, {, [ or scalar.  generally not whitespace
   * @param xml
   * @param startFrom
   * @return the start tag index of -1 if not found another
   */
  static int findNextStartTagIndex(String xml, int startFrom) {
    int length = xml.length();
    for (int i= startFrom; i<length;i++) {
      char curChar = xml.charAt(i);
      if (Character.isWhitespace(curChar)) {
        continue;
      }
      return i;
    }
    return -1;
  }
  
  /**
   * find the end tag from xml and a start from index
   * @param xml
   * @param startFrom is the char after the start of tag
   * @return the start tag index of -1 if not found another
   */
  static int findNextEndTagIndex(String xml, int startFrom) {
    int length = xml.length();
    
    //see if quoted string
    boolean quotedString = xml.charAt(startFrom-1) == '\"';
    
    int ignoreSlashInIndex = -1;
    boolean afterSlash = false;
    for (int i= startFrom; i<length;i++) {
      //we are after a slash, if not ignored, and if last was slash
      afterSlash = i != ignoreSlashInIndex && i!= startFrom && xml.charAt(i-1) == '\\';
      char curChar = xml.charAt(i);
      
      //if first slash, ignore slash in next index
      if (!afterSlash && curChar == '\\') {
        ignoreSlashInIndex = i+2;
      }
      
      if (!quotedString) {
        if (curChar == ':' || Character.isWhitespace(curChar)
            || curChar == ']' || curChar == '}'
              || curChar == ',') {
          return i-1;
          
        }
      } else {
        if (!afterSlash && curChar == '\"') {
          return i;
        }
      }
    }
    //end at end of string
    return xml.length()-1;
  }
  
  /**
   * @param theXml is the xml to format
   * indenter
   */
  public JsonIndenter(String theXml) {
    if (theXml != null) {
      this.json = StringUtils.trimToEmpty(theXml);
    }
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
