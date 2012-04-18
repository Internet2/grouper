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

import edu.internet2.middleware.grouper.ui.tags.GrouperHideShowTarget;
import edu.internet2.middleware.grouper.ui.tags.GrouperInfodotTag;
import edu.internet2.middleware.grouper.ui.tags.GrouperMessageTag;
import edu.internet2.middleware.grouper.ui.tags.GrouperParamTag;
import edu.internet2.middleware.grouper.ui.util.MapBundleWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * This will generate a title
 * 
 * Generates a title, with optional infodot
 * 
 * </pre>
 * @author mchyzer
 *
 */
public class GrouperTitleTag extends BodyTagSupport {

  /**
   * if using value instead of key, this is the infodot value
   */
  private String infodotValue;
  
  /**
   * 
   * @param theValue
   */
  public void setInfodotValue(String theValue) {
    this.infodotValue = theValue;
  }
  
  /**
   * key of the nav.properties for text in title, mutually exclusive with label
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
  public GrouperTitleTag() {
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
      throw new RuntimeException("Cant set key and label in title tag: " 
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
      
      //see if there is an infodot for this title
      String infodotKey = null;
      MapBundleWrapper mapBundleWrapper = null;
      
      boolean hasInfodot = false;

      out.println("<div id=\"TitleBox\">");
      
      if (!StringUtils.isBlank(this.key)) {
        //see if there is an infodot for this title
        infodotKey = "infodot.title." + this.key;
        mapBundleWrapper = (MapBundleWrapper)((HttpServletRequest)this
            .pageContext.getRequest()).getSession().getAttribute("navNullMap");
        
        hasInfodot = !StringUtils.isEmpty((String)mapBundleWrapper.get(infodotKey));
        
        out.println("<!-- trying title infodot with nav.properties key: infodot.title." + this.key + ": " + hasInfodot + " -->");
      }

      if (!StringUtils.isBlank(this.infodotValue)) {
        hasInfodot = true;
      }
      
      //<!-- trying title infodot with nav.properties key: infodot.title.groups.my -->
      //
      //<h1 id="title">My memberships
      //
      // 
      //  &nbsp;<a href="#" class="infodotLink" onclick="return grouperHideShow(event, 'titleHideShow_infodot.title.groups.my');"><img 
      //src="grouperExternal/public/assets/images/infodot.gif" border="0" alt="More" 
      //class="infodotImage" /></a>
      //</h1>
      //
      //  <div class="helpText"
      // id="titleHideShow_infodot.title.groups.my0" style="display:none;" 
      //>This page lets you examine the groups where you are enrolled a member. <br />You could be enrolled as a direct member, or as an indirect member (as a direct member of a group that is itself a member of another group). <br /><br />You have a choice of two exploring modes (Browse or List) and two search modes (basic or advanced).  <br /><br />Note: <br />During your session, if you click "My memberships" in the left menu, this page will always display<br /> &nbsp; - your most recent choice of mode <br /> &nbsp; - your most recently selected location (folder) in the hierarchy<br />even if you click away and return later.</div>

      out.print("<h1 id=\"title\">");
      
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

      String htmlHideShowId = GrouperUtil.uniqueId();

      if (hasInfodot) {
        out.print(" &nbsp;");
        out.flush();
        //write an infodot tag
        GrouperInfodotTag grouperInfodotTag = new GrouperInfodotTag();
        grouperInfodotTag.setPageContext(this.pageContext);
        grouperInfodotTag.setHideShowHtmlId(htmlHideShowId);
        grouperInfodotTag.doStartTag();
        grouperInfodotTag.doEndTag();
        out.flush();
      }

      out.println("</h1>");
      
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
        
        //  ><grouper:message bundle="${nav}" key="infodot.title.whatever"
        //    useNewTermContext="true"
        //  /></div>
        out.print(" >");
        out.flush();
        if (!StringUtils.isBlank(infodotKey)) {
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
      
      //if there is a body, put to the right
      if (!StringUtils.isBlank(body)) {
        out.print("&nbsp;&nbsp;");
        out.print(body);
      }
      

      out.print("</div>\n");

    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }

    return Tag.EVAL_PAGE;
  }

  
  /**
   * key of the nav.properties for text in title
   * @return the key
   */
  public String getKey() {
    return this.key;
  }

  
  /**
   * key of the nav.properties for text in title
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

}
