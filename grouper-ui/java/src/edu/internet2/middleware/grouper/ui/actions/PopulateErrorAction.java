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

import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.internet2.middleware.grouper.ui.ErrorFilter;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.SessionInitialiser;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;

/**
 * Top level Strut's action called on any Exception - can arise normally - or 
 * from within template.jsp. 
 * 
 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: PopulateErrorAction.java,v 1.3 2008-04-15 14:12:34 isgwb Exp $
 */

public class PopulateErrorAction extends org.apache.struts.action.Action {
	protected static Log LOG = LogFactory.getLog(PopulateErrorAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_Error = "error";
	static final private String FORWARD_AuthError = "authError";



	//------------------------------------------------------------ Action
	// Methods

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String msg=null;
		request.setAttribute("title","error.title");
		String code=request.getParameter("code");
		if(org.apache.log4j.NDC.getDepth()==0) ErrorFilter.initNDC(request);
		if(code!=null && !"".equals(code)) {
			LOG.error("Caught '" + code + "' for " + request.getAttribute("javax.servlet.error.request_uri"));
			try {
				msg=LowLevelGrouperCapableAction.getNavResources(request).getString("error." + code);
			}catch(MissingResourceException mre) {
				msg=code;
			}
		}else{
			HttpSession session = request.getSession();
			Exception e = (Exception) session.getAttribute("templateException");
			if(e!=null) {
				request.setAttribute("exception",e);
				session.removeAttribute("templateException");
			}else{
				e = (Exception) request.getAttribute("javax.servlet.error.exception");
				request.setAttribute("exception",e);
			}
			LOG.error("Caught unhandled Exception: ",e);
			NavExceptionHelper neh=LowLevelGrouperCapableAction.getExceptionHelper(session);
			msg = neh.getMessage(new UnrecoverableErrorException(e));
		}
		request.setAttribute("seriousError",msg);
		String user = SessionInitialiser.getAuthUser(request.getSession());
		if(user==null){
			return mapping.findForward(FORWARD_Error);
		}
		return mapping.findForward(FORWARD_AuthError);

		
	}

}