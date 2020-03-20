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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * abbreviate with a more button and textarea.  escape everything
 * </pre>
 * @author mchyzer
 *
 */
public class GrouperAbbreviateTextareaTag extends SimpleTagSupport {

  /** number of chars to show */
  private int showCharCount = 50;
  
  
  /**
   * number of chars to show
   * @return the showCharCount
   */
  public int getShowCharCount() {
    return this.showCharCount;
  }

  
  /**
   * number of chars to show
   * @param showCharCount1 the showCharCount to set
   */
  public void setShowCharCount(int showCharCount1) {
    this.showCharCount = showCharCount1;
  }

  /**
   * the text
   */
  private String text;
  
  
  
  
  /**
   * the text
   * @return the text
   */
  public String getText() {
    return this.text;
  }


  
  /**
   * the text
   * @param text1 the text to set
   */
  public void setText(String text1) {
    this.text = text1;
  }

  /**
   * textarea cols
   */
  private int cols = 20;
  
  /**
   * textarea rows
   */
  private int rows = 3;
  
  

  
  /**
   * @return the cols
   */
  public int getCols() {
    return this.cols;
  }


  
  /**
   * @param cols the cols to set
   */
  public void setCols(int cols) {
    this.cols = cols;
  }


  
  /**
   * @return the rows
   */
  public int getRows() {
    return this.rows;
  }


  
  /**
   * @param rows the rows to set
   */
  public void setRows(int rows) {
    this.rows = rows;
  }


  /**
   * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
   */
  @Override
  public void doTag() throws JspException, IOException {

    //    <c:choose>
    //    <c:when test="${fn:length(guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.jobMessage) > 0}">
    //      <span id="jobMessageSpan__${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.id}"
    //        >${grouper:abbreviate(guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.jobMessage, 30, false, true)}
    //        <a href="#" onclick="$('#jobMessageTextarea__${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.id}').show('slow'); 
    //        $('#jobMessageSpan__${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.id}').hide('slow');
    //        return false">${textContainer.text['grouperLoaderLogsLoadedJobMessageShow']}</a>
    //      </span>
    //    
    //      <textarea cols="20" rows="3" style="display: none" 
    //       id="jobMessageTextarea__${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.id}"
    //       >${grouper:escapeHtml(guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.jobMessage)}</textarea>
    //    </c:when>
    //  </c:choose>

    //if nothing, then nothing
    if (StringUtils.isBlank(this.text)) {
      return;
    }
    
    //if we are under the limit, then we good
    if (this.text.length() <= this.showCharCount) {
      this.getJspContext().getOut().print(GrouperUtil.xmlEscape(this.text));
      return;
    }

    StringBuilder result = new StringBuilder();

    String uniqueId = GrouperUtil.uniqueId();
    result.append("<span id=\"grouperAbbreviateSpan__" + uniqueId + "\">");
    result.append(GrouperUtil.xmlEscape(StringUtils.abbreviate(this.text, this.showCharCount)));
    result.append(" <a href=\"$\" onclick=\"$('#grouperAbbreviateTextarea__" + uniqueId + "').show('slow'); $('#grouperAbbreviateSpan__" + uniqueId + "').hide('slow'); return false;\">");
    result.append(TextContainer.textOrNull("guiAbbreviateShow"));
    result.append("</a></span>");
    result.append("<textarea cols=\"" + this.cols + "\" rows=\"" + this.rows 
        + "\" style=\"display: none\" id=\"grouperAbbreviateTextarea__" + uniqueId + "\">");
    result.append(GrouperUtil.xmlEscape(this.text));
    result.append("</textarea>");
    this.getJspContext().getOut().print(result.toString());
  }
  

}
