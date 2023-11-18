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
/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Pennsylvania
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.MapBundleWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * This will generate a subtitle
 * 
 * Generates a subtitle, with optional infodot (right aligned)
 * 
 * </pre>
 * @author mchyzer
 *
 */
public class GrouperSubtitleTag extends BodyTagSupport {

  /**
   * key of the nav.properties for text in subtitle, mutually exclusive with label
   */
  private String key;

  /**
   * mutually exclusive with key, if not from nav.properties
   */
  private String label;

  /**
   * first param value of the string
   */
  private String param1;

  /**
   * second param value of the string
   */
  private String param2;

  /**
   * if using value instead of key, this is the infodot value
   */
  private String infodotValue;

  /**
   * reset field on construct or recycle
   */
  private void init() {
    this.infodotValue = null;
    this.label = null;
    this.key = null;
    this.param1 = null;
    this.param2 = null;
  }

  /**
   * init fields on construct
   */
  public GrouperSubtitleTag() {
    this.init();
  }

  /** 
     * Releases any resources we may have (or inherit)
     */
  @Override
  public void release() {
    super.release();
    init();
  }

  /**
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
   */
  @Override
  public int doEndTag() throws JspException {

    if (!StringUtils.isBlank(this.key) && !StringUtils.isBlank(this.label)) {
      throw new RuntimeException("Cant set key and label in subtitle tag: " 
          + this.key + ", " + this.label);
    }

    
    // ... retrieving and trimming our body
    String body = this.bodyContent == null ? null : this.bodyContent.getString();
    body = StringUtils.trim(body);
    
    List<String> paramsList = new ArrayList<String>();
    if (StringUtils.isNotBlank(this.param1)) {
      paramsList.add(this.param1);
    }
    if (StringUtils.isNotBlank(this.param2)) {
      paramsList.add(this.param2);
    }
    
    String[] params = GrouperUtil.toArray(paramsList, String.class);
    
    //lets print the tag
    try {
      JspWriter out = this.pageContext.getOut();
      
      //see if there is an infodot for this subtitle
      String infodotKey = null;
      MapBundleWrapper mapBundleWrapper = null;
      
      boolean hasInfodot = false;

      if (!StringUtils.isBlank(this.key)) {
        //see if there is an infodot for this subtitle
        infodotKey = "infodot.subtitle." + this.key;
        mapBundleWrapper = (MapBundleWrapper)((HttpServletRequest)this
            .pageContext.getRequest()).getSession().getAttribute("navNullMap");
        
        hasInfodot = !StringUtils.isEmpty((String)mapBundleWrapper.get(infodotKey));
        
      }

      if (!StringUtils.isBlank(this.infodotValue)) {
        hasInfodot = true;
      }

      //note, div didnt work since it didnt fully enclose the inner span
      out.print("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"actionheaderContainer" 
          + (hasInfodot ? " actionheaderContainerInfodot" : "") //add a different style for if there is an infodot 
          + "\" width=\"100%\"><tr><td>" +
      		"<span class=\"actionheader\">");
      
      if (!StringUtils.isBlank(this.key)) {
        out.flush();
        GrouperMessageTag grouperMessageTag = new GrouperMessageTag();
        grouperMessageTag.setPageContext(this.pageContext);
        grouperMessageTag.setBundle("${nav}");
        grouperMessageTag.setKey(this.key);
        grouperMessageTag.doStartTag();
        //maybe we have to do some substitutions
        if (GrouperUtil.length(params) > 0) {
          for (String param : params) {
            GrouperParamTag grouperParamTag = new GrouperParamTag();
            grouperParamTag.setPageContext(this.pageContext);
            grouperParamTag.setParent(grouperMessageTag);
            grouperParamTag.setValue(param);
            grouperParamTag.doStartTag();
            grouperParamTag.doEndTag();
          }
        }
        grouperMessageTag.doEndTag();
        out.flush();
      } else {
        out.print(this.label);
      }
      out.print("</span>");
      
      String htmlHideShowId = GrouperUiUtils.uniqueId();
      if (hasInfodot) {
        out.print("&nbsp;&nbsp;");
        out.flush();
        //write an infodot tag
        GrouperInfodotTag grouperInfodotTag = new GrouperInfodotTag();
        grouperInfodotTag.setPageContext(this.pageContext);
        grouperInfodotTag.setHideShowHtmlId(htmlHideShowId);
        grouperInfodotTag.doStartTag();
        grouperInfodotTag.doEndTag();
        out.flush();
      }

      //if there is a body, put to the right
      if (!StringUtils.isBlank(body)) {
        out.print("&nbsp;&nbsp;");
        out.print(body);
      }

      out.print("</td></tr></table>\n");
      
      out.print("<!-- subtitle infodot from nav.properties key: ");
      if (!StringUtils.isBlank(this.key)) {
        out.print(infodotKey);
      } else {
        out.print("none, not a nav.properties subtitle");
      }
      out.print(" \n-->");
      if (hasInfodot) {
        out.print("<div class=\"helpText\" \n");

        //  <grouper:hideShowTarget hideShowHtmlId="abc"  />
        out.flush();
        GrouperHideShowTarget grouperHideShowTarget = new GrouperHideShowTarget();
        grouperHideShowTarget.setPageContext(this.pageContext);
        grouperHideShowTarget.setHideShowHtmlId(htmlHideShowId);
        grouperHideShowTarget.doStartTag();
        grouperHideShowTarget.doEndTag();
        out.flush();
        
        //  ><grouper:message bundle="${nav}" key="infodot.subtitle.whatever"
        //    useNewTermContext="true"
        //  /></div>
        out.print(" >");
        out.flush();
        if (!StringUtils.isBlank(infodotKey) && StringUtils.isBlank(this.infodotValue)) {
          GrouperMessageTag grouperMessageTag = new GrouperMessageTag();
          grouperMessageTag.setPageContext(this.pageContext);
          grouperMessageTag.setBundle("${nav}");
          grouperMessageTag.setKey(infodotKey);
          grouperMessageTag.setUseNewTermContext("true");
          grouperMessageTag.doStartTag();
          grouperMessageTag.doEndTag();
          out.flush();
        } else {
          out.print(this.infodotValue);
        }
        out.print("</div>\n");
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }

    return Tag.EVAL_PAGE;
  }

  
  /**
   * key of the nav.properties for text in subtitle
   * @return the key
   */
  public String getKey() {
    return this.key;
  }

  
  /**
   * key of the nav.properties for text in subtitle
   * @param _key the key to set
   */
  public void setKey(String _key) {
    this.key = _key;
  }

  
  /**
   * first param value of the string
   * @return the param1
   */
  public String getParam1() {
    return this.param1;
  }

  
  /**
   * first param value of the string
   * @param _param1 the param1 to set
   */
  public void setParam1(String _param1) {
    this.param1 = _param1;
  }

  
  /**
   * second param value of the string
   * @return the param2
   */
  public String getParam2() {
    return this.param2;
  }

  
  /**
   * second param value of the string
   * @param _param2 the param2 to set
   */
  public void setParam2(String _param2) {
    this.param2 = _param2;
  }

  
  /**
   * mutually exclusive with key, if not from nav.properties
   * @param label1 the label to set
   */
  public void setLabel(String label1) {
    this.label = label1;
  }

  /**
   * 
   * @param theValue
   */
  public void setInfodotValue(String theValue) {
    this.infodotValue = theValue;
  }

}
