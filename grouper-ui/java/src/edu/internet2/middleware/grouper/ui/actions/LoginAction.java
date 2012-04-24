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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.SessionInitialiser;

/**
 * Top level Strut's action which implements default login for 
 * Basic HTTP Authentication.  
 * 
 * It is assumed that some other mechanism will be used e.g. CAS.
 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: LoginAction.java,v 1.4 2009-08-12 04:52:14 mchyzer Exp $
 */
public class LoginAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_default = "default";

	static final private String FORWARD_Index = "Index";

	static final private String FORWARD_Login = "Login";

	static final private String FORWARD_stop = "stop";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session,GrouperSession grouperSession)
			throws Exception {

		String authUser=SessionInitialiser.getAuthUser(request.getSession());
		if (authUser != null) {
			if("GrouperSystem".equals(authUser)) {
				String adminUrl=GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("admin.browse.path");
				return new ActionForward(adminUrl,true);	
			}
		}
		
		return mapping.findForward(FORWARD_default);

	}

}
