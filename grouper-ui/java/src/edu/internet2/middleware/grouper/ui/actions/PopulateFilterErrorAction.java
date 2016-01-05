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
Copyright 2004-2008 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2008 The University Of Bristol

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

import edu.internet2.middleware.grouper.ui.SessionInitialiser;

/**
 * Top level Strut's action called when ErrorFilter catches an exception
 * 
 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: PopulateFilterErrorAction.java,v 1.1 2008-04-09 14:42:00 isgwb Exp $
 */

public class PopulateFilterErrorAction extends org.apache.struts.action.Action {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_Error = "error";
	static final private String FORWARD_AuthError = "authError";



	//------------------------------------------------------------ Action
	// Methods

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		request.setAttribute("title","error.title");
		String user = SessionInitialiser.getAuthUser(request.getSession());
		if(user==null){
			return mapping.findForward(FORWARD_Error);
		}
		return mapping.findForward(FORWARD_AuthError);
		
	}

}
