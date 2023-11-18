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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * This will generate a toolip (e.g. on an image or other HTML element).
 * Note, if the element is text based, you can just use a grouper:message
 * and potentially a targetted tooltip to do ths job
 * 
 * This outputs an onmouseover tag, so nest this in an html tag...
 * 
 * </pre>
 * @author mchyzer
 *
 */
public class GrouperTooltipTag extends BodyTagSupport {

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
   * init fields on construct
   */
  public GrouperTooltipTag() {
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
      
      //onmouseover="grouperTooltip('<grouper:message key="group.icon.tooltip" 
      //tooltipDisable="true" escapeSingleQuotes="true" />');"
      
      out.print("onmouseover=\"grouperTooltip('");
      out.flush();
      GrouperMessageTag grouperMessageTag = new GrouperMessageTag();
      grouperMessageTag.setPageContext(this.pageContext);
      grouperMessageTag.setKey(this.key);
      grouperMessageTag.setTooltipDisable("true");
      grouperMessageTag.setEscapeHtml(true);
      grouperMessageTag.setEscapeSingleQuotes("true");
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
      out.print("');\" onmouseout=\"UnTip()\"");
      
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
