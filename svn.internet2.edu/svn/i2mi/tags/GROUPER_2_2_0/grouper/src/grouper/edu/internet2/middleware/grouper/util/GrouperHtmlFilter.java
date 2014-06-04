/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;


/**
 * default implementation of html filter
 */
public class GrouperHtmlFilter implements HtmlFilter {

  /**
   * 
   * @param string
   * @return the string
   */
  public static String escapeHtml(String string) {
    if (StringUtils.isBlank(string)) {
      return string;
    }
    string = StringUtils.replace(string, "<", "&lt;");
    string = StringUtils.replace(string, ">", "&gt;");
    return string;
  }
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperHtmlFilter.class);
  
  /** allowed html tags */
  public static Set<String> allowedHtml = new HashSet<String>();

  /**
   * allo tag
   * @param tag
   */
  public static void allowTag(String tag) {
    tag = tag.toLowerCase();
    allowedHtml.add(tag);
    allowedHtml.add("/" + tag);
    allowedHtml.add("/ " + tag);
    allowedHtml.add(tag + "/");
    allowedHtml.add(tag + " /");
  }
  
  static {
    try {
      
      allowTag("b");
      allowTag("br");
      allowTag("p");
      allowTag("i");
      allowTag("em");
      allowTag("strong");
      allowTag("blockquote");
      allowTag("ul");
      allowTag("li");
      allowTag("ol");
      allowTag("div");
      allowTag("tt");
      allowTag("hr");
      allowTag("s");
      allowTag("span");

    } catch (RuntimeException e) {
      LOG.error("error", e);
      throw e;
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.util.HtmlFilter#filterHtml(java.lang.String)
   */
  public String filterHtml(String html) {
    
    if (StringUtils.isBlank(html)) {
      return html;
    }

    //dont go to the trouble if nothing there
    if (!html.contains("<") && !html.contains(">")) {
      return html;
    }
    
    int index = 0;

    // matching < exp >   (non-greedy)
    Pattern pattern = Pattern.compile("<([^<]*?)\\>");
    Matcher matcher = pattern.matcher(html);
    
    StringBuilder result = new StringBuilder();

    //loop through and find each script
    while(matcher.find()) {
      String startString = html.substring(index, matcher.start());
      startString = escapeHtml(startString);
      result.append(startString);
      
      //here is the script inside the curlies
      String tag = matcher.group(1);
      
      //if not allowed, strip
      if (allowedHtml.contains(tag.toLowerCase())) {
        result.append("<");
        result.append(tag);
        result.append(">");
      } else {
        result.append("&lt;");
        result.append(escapeHtml(tag));
        result.append("&gt;");
        
      }
      
      index = matcher.end();
    }
    String endString = html.substring(index, html.length());
    endString = escapeHtml(endString);
    result.append(endString);
    
    return result.toString();
  }

}
