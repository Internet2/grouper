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
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Top level Strut's action which forwards to a URL which is a login screen, 
 * or which causes a login screen to be displayed due to invocation of the 
 * challenge mechanism of an external authentication scheme. Sites can configure 
 * a different URL through the Struts config file, by changing the definition of the 
 * forward identified by callLogin.
 * 
 * There are no parameters 
 */
public class CallLoginAction extends org.apache.struts.action.Action {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_callLogin = "callLogin";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String redirect = (String)request.getAttribute("forceRedirect");
		if(redirect !=null && !"".equals(redirect)) {
			return new ActionForward(redirect);
		}
		return mapping.findForward(FORWARD_callLogin);
	}
}
