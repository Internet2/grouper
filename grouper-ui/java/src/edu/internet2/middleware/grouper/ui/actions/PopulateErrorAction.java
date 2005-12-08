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

import edu.internet2.middleware.grouper.ui.Message;

/**
 * Top level Strut's action called on any Exception - can arise normally - or 
 * from within template.jsp. 
 * 
 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: PopulateErrorAction.java,v 1.2 2005-12-08 15:30:52 isgwb Exp $
 */

public class PopulateErrorAction extends org.apache.struts.action.Action {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_Error = "error";



	//------------------------------------------------------------ Action
	// Methods

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		request.setAttribute("title","error.title");
		
		Message message = new Message("error.general",true);
		request.setAttribute("message",message);
		HttpSession session = request.getSession();
		Exception e = (Exception) session.getAttribute("templateException");
		if(e!=null) {
			request.setAttribute("exception",e);
			session.removeAttribute("templateException");
		}else{
			e = (Exception) request.getAttribute("javax.servlet.error.exception");
			request.setAttribute("exception",e);
		}
		if(e!=null) {
			request.setAttribute("subtitle","error.subtitle");
			request.setAttribute("subtitleArgs",new Object[]{e.getClass().getName() + ":" + e.getMessage()});
		}else{
			request.setAttribute("subtitle","empty.space");
		}
		return mapping.findForward(FORWARD_Error);
		
	}

}