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
import org.apache.strutsel.taglib.utils.EvalHelper;

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
   * key of the nav.properties for text in subtitle
   */
  private String key;

  /**
   * first param value of the string
   */
  private String param1;

  /**
   * second param value of the string
   */
  private String param2;

  /**
   * reset field on construct or recycle
   */
  private void init() {
    this.key = null;
    this.param1 = null;
    this.param2 = null;
  }

  /**
   * Processes all attribute values which use the JSTL expression evaluation
   * engine to determine their values.
   * 
   * @exception JspException
   *                if a JSP exception has occurred
   */
  public void evaluateExpressions() throws JspException {
    String string = null;
  
    if ((string = EvalHelper.evalString("key", 
        this.key, this, this.pageContext)) != null) {
      this.key = string;
    }
    
    if ((string = EvalHelper.evalString("param1", 
        this.param1, this, this.pageContext)) != null) {
      this.param1 = string;
    }
    
    if ((string = EvalHelper.evalString("param2", 
        this.param2, this, this.pageContext)) != null) {
      this.param2 = string;
    }
    
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

    this.evaluateExpressions();

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
      
      String leftSize = "100%";
      String middleSize = "10%";
      //if there is a body, put to the right
      if (!StringUtils.isBlank(body)) {
        
        //shrink down right
        leftSize = "90%";
        
      }
      

      out.print("<table border=\"0\" class=\"actionheaderTable\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>" +
      		"<td width=\"" + leftSize + "\" class=\"actionheader\">");
      
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
      out.print("</td>\n");
      
      //if there is a body, put to the right
      if (!StringUtils.isBlank(body)) {
        out.print("<td align=\"right\" width=\"" + middleSize + "\">");
        out.print(body);
        out.print("</td>\n");
        
      }

      //set width to 0 since image will push it out anyway
      out.print("<td align=\"right\" width=\"22px\">");
      out.flush();
      
      //see if there is an infodot for this subtitle
      String infodotKey = "infodot.subtitle." + this.key;
      MapBundleWrapper mapBundleWrapper = (MapBundleWrapper)((HttpServletRequest)this
          .pageContext.getRequest()).getSession().getAttribute("navNullMap");
      
      boolean hasInfodot = !StringUtils.isEmpty((String)mapBundleWrapper.get(infodotKey));
      String htmlHideShowId = GrouperUiUtils.uniqueId();
      if (hasInfodot) {
        
        //write an infodot tag
        GrouperInfodotTag grouperInfodotTag = new GrouperInfodotTag();
        grouperInfodotTag.setPageContext(this.pageContext);
        grouperInfodotTag.setHideShowHtmlId(htmlHideShowId);
        grouperInfodotTag.doStartTag();
        grouperInfodotTag.doEndTag();
        out.flush();
      } else {
        //give it something so not empty, might eventually need a transparent gif
        out.print("&nbsp;");
      }
      out.print("</td></tr></table>\n");
      out.print("<!-- subtitle infodot from nav.properties key: ");
      out.print(infodotKey);
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
        grouperMessageTag = new GrouperMessageTag();
        grouperMessageTag.setPageContext(this.pageContext);
        grouperMessageTag.setBundle("${nav}");
        grouperMessageTag.setKey(infodotKey);
        grouperMessageTag.setUseNewTermContext("true");
        grouperMessageTag.doStartTag();
        grouperMessageTag.doEndTag();
        out.flush();
        
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

}
