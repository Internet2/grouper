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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.ui.ErrorFilter;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.SessionInitialiser;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;

/**
 * Top level Strut's action called on any Exception - can arise normally - or 
 * from within template.jsp. 
 * 
 * 
 * 
 * @author Gary Brown.
 * @version $Id: PopulateErrorAction.java,v 1.4 2009-08-12 04:52:14 mchyzer Exp $
 */

public class PopulateErrorAction extends GrouperCapableAction {
	protected static Log LOG = LogFactory.getLog(PopulateErrorAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_Error = "error";
	static final private String FORWARD_AuthError = "authError";


	/**
	 * 
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
	  boolean needsThreadLocalInit = GrouperUiFilter.retrieveHttpServletRequest() == null;
	  if (needsThreadLocalInit) {
      //try catch since we dont want looping errors
      try {
        request = new GrouperRequestWrapper(request);
        GrouperUiFilter.initRequest((GrouperRequestWrapper)request, response);
      } catch (Exception e) {
        LOG.error("Problem initting", e);
      }
	  }
	  try {
  		String msg=null;
  		request.setAttribute("title","error.title");
  		String code=request.getParameter("code");
  		if(org.apache.log4j.NDC.getDepth()==0) ErrorFilter.initNDC(request);
  		if(code!=null && !"".equals(code)) {
  			LOG.error("Caught '" + code + "' for " + request.getAttribute("javax.servlet.error.request_uri"));
  			try {
  				msg=GrouperUiFilter.retrieveSessionNavResourceBundle().getString("error." + code);
  			} catch(Exception mre) {
  				msg=code;
  			}
  		}else{
  			//HttpSession session = request.getSession();
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
	  } finally {
	    if (needsThreadLocalInit) {
	      GrouperUiFilter.finallyRequest();
	    }
	  }
		
	}

}
