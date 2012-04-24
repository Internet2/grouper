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
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

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

package edu.internet2.middleware.grouper.ui.actions;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ui.Message;

/**
 * Top level Strut's action which launches JSP editor configured through 
 * populateDebugPrefs. Should only be used on local development system. 
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: EditJSPAction.java,v 1.3 2009-03-02 13:44:42 isgwb Exp $
 */
public class EditJSPAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_Editor = "Editor";
	static final private String FORWARD_DebugNotAllowed = "NotAllowed";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		session.removeAttribute("subtitle");
		session.setAttribute("title", "prefs.debug.title");
		if(!Boolean.TRUE.equals(session.getAttribute("enableHtmlEditor"))){
			addMessage(new Message("debug.error.editor-not-allowed",true), request);
			return mapping.findForward(FORWARD_DebugNotAllowed);
		}
		Map prefs = (Map)session.getAttribute("debugPrefs");
		
		String jsp=request.getParameter("jsp");
		//Launches editor
		Runtime.getRuntime().exec(new String[]{(String)prefs.get("JSPEditor"),jsp});
		//Web page should be launched in new window which closes itself
		return mapping.findForward(FORWARD_Editor);
	}

}
