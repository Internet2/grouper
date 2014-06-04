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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Strut's action called on 401 error - configured through web.xml and struts-config.xml.
 * Need to relook at this again, but intended to cope with access to protected pages
 * during basic Http Authentication. 
 * 
 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: Populate401Action.java,v 1.2 2005-12-08 15:30:52 isgwb Exp $
 */

public class Populate401Action extends org.apache.struts.action.Action {

	//------------------------------------------------------------ Local
	// Forwards
	

	static final private String FORWARD_Stop = "stop";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.getSession().setAttribute("hasTriedAuth",Boolean.TRUE);
		response.setHeader("WWW-Authenticate",
				"Basic realm=\"Grouper Application\"");
		response.setStatus(401);

		return mapping.findForward(FORWARD_Stop);
		

	}

}
