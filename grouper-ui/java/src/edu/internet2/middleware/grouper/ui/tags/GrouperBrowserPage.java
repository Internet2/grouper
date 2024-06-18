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
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 *
 */
public class GrouperBrowserPage extends BodyTagSupport {
  
  /**
   */
	private static final long serialVersionUID = 0L;

	/** 
	 * Name of the jsp without the .jsp suffix
	 */
	private String jspName = null;

	
	
	/** 
   * Name of the jsp without the .jsp suffix
   */
  public String getJspName() {
    return jspName;
  }



  /** 
   * Name of the jsp without the .jsp suffix
   */
  public void setJspName(String jspName) {
    this.jspName = jspName;
  }



  /**
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int doEndTag() throws JspException {
		
		
				
		//just print out the image tag
		try {
			String currentTimeStamp = GrouperUtil.timestampIsoUtcMicrosConvertToString(new Timestamp(System.currentTimeMillis()));
      this.pageContext.getOut().print("<span class=\"grouperJspClass\" style=\"display:none\" data-gr-page-loadtime=\"" + currentTimeStamp + "\">" + this.jspName + "</span>");
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		
		return EVAL_PAGE;
	}

	
}
