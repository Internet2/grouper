/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Pennsylvania

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * <pre>
 * This tag: 
 * &lt;grouper:hideShowTarget hideShowHtmlId="firstHideShow" showInitially="false"
 * omitStyle="false" /&gt;
 * generates the html:
 * 
 * id="firstHideShow0" style="display:none;"
 *
 * Use this tag like this:
 * 
 * &lt;div &lt;grouper:hideShowTarget hideShowHtmlId="firstHideShow" showInitially="false"
 * omitClass="true" omitStyle="true" /&gt; &gt;
 * Here is help &lt;b&gt;text&lt;/b&gt; that explains whatever this infodot is explaining
 * &lt;/div&gt;
 * 
 * @author mchyzer
 *
 */
public class GrouperHideShowTarget extends BodyTagSupport {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 0L;

	/** 
	 * id of the html tag (actually it will appear an int so multiple can be used).
	 * This must match the infodot tag (or any call to grouperHideShow javascript function)
	 */
	private String hideShowHtmlId = null;

	/**
	 * id of the html tag (actually it will appear an int so multiple can be used).
	 * This must match the infodot tag (or any call to grouperHideShow javascript function)
	 * @param hideShowHtmlId the hideShowHtmlId to set
	 */
	public void setHideShowHtmlId(String hideShowHtmlId) {
		this.hideShowHtmlId = hideShowHtmlId;
	}
	
	/**
	 * if the element should be shown on page draw (default is no)
	 */
	private boolean showInitially = false;

	/**
	 * if the element should be shown on page draw (default is no)
	 * @param showInitially the showInitially to set
	 */
	public void setShowInitially(boolean showInitially) {
		this.showInitially = showInitially;
	}
	
	/**
	 * if true do not generate the style attribute
	 */
	private boolean omitStyle = false;

	/**
	 * if true do not generate the style attribute
	 * @param omitStyle1 the omitStyle to set
	 */
	public void setOmitStyle(boolean omitStyle1) {
		this.omitStyle = omitStyle1;
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int doEndTag() throws JspException {
		
		StringBuilder result = new StringBuilder();
		
		//id="firstHideShow0" style="display:none;"
		
		//we need the suffix of this id
		Map<String, Integer> hideShowHtmlIdMap = (Map<String, Integer>)this.pageContext.getRequest().getAttribute("hideShowHtmlIdMap");
		if (hideShowHtmlIdMap == null) {
			hideShowHtmlIdMap = new HashMap<String, Integer>();
			this.pageContext.getRequest().setAttribute("hideShowHtmlIdMap", hideShowHtmlIdMap);
		}
		
		//get this id
		Integer currentIdIndex = hideShowHtmlIdMap.get(this.hideShowHtmlId);
		
		//if not there, then 0
		if (currentIdIndex == null) {
			currentIdIndex = 0;
		} else {
			//if there then increment
			currentIdIndex++;
		}
		
		//set back
		hideShowHtmlIdMap.put(this.hideShowHtmlId, currentIdIndex);
		
		//probably a good idea to start and end with a space
		result.append(" id=\"").append(this.hideShowHtmlId + currentIdIndex).append("\" ");
		
		//put in style if we need it
		if (!this.omitStyle && !this.showInitially) {
			result.append("style=\"display:none;\" ");
		}
				
		//just print out the image tag
		try {
			this.pageContext.getOut().print(result);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		
		return EVAL_PAGE;
	}

	
}
