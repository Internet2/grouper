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

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;
import edu.internet2.middleware.subject.Subject;


/**
 * Not currently used. 
 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: PopulateChainAction.java,v 1.6 2009-03-15 06:37:51 mchyzer Exp $
 */
public class PopulateChainAction extends GrouperCapableAction {
	protected static final Log LOG = LogFactory.getLog(PopulateChainAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_Chain = "Chain";

	

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		NavExceptionHelper neh=getExceptionHelper(session);
		session.setAttribute("subtitle","groups.action.show-members");
		String subjectId = request.getParameter("subjectId");
		String subjectType = request.getParameter("subjectType");
		String sourceId = request.getParameter("sourceId");
		
		if(isEmpty(subjectId) || isEmpty(subjectType) || isEmpty(sourceId)) {
			String msg = neh.missingParameters(subjectId,"subjectId",subjectType,"subjectType",sourceId,"sourceId");
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.chain.missing-parameter");
		}
		Subject subject = null;
		try{ 
			subject=SubjectFinder.findById(subjectId,subjectType,sourceId, true);
		}catch (Exception e) {
			LOG.error("Unable to retrieve subject: " + subjectId + "," + sourceId,e);
			String contextError="error.chain.subject.exception";
			throw new UnrecoverableErrorException(contextError,e,subjectId);
		}
		DynaActionForm groupForm = (DynaActionForm) form;
		
		//Identify the group whose membership we are showing
		String groupId = (String)groupForm.get("groupId");
		//TODO: check following - shouldn't I always pass parameter
		if (groupId == null || groupId.length() == 0)
			groupId = (String) request.getSession().getAttribute("findForNode");
		if (groupId == null)
			groupId = request.getParameter("asMemberOf");
		if(isEmpty(groupId)) {
			String msg = neh.missingAlternativeParameters(groupId,"groupId",groupId,"asMemberOf");
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.chain.missing-group-id");
		}
		
		Group group = null;
		try{
			group=GroupFinder.findByUuid(grouperSession,groupId, true);
		}catch(GroupNotFoundException e) {
			LOG.error(e);
			throw new UnrecoverableErrorException("error.chain.bad-id",groupId);
		}
		
		String[] chainGroupIds=request.getParameterValues("chainGroupIds");
		List chain = new ArrayList();
		Group chainGroup = null;
		
		for(int i=chainGroupIds.length-1;i>-1;i--) {
			try{
				chainGroup = GroupFinder.findByUuid(grouperSession,chainGroupIds[i], true);
				chain.add(GrouperHelper.group2Map(grouperSession,chainGroup));
			}catch(GroupNotFoundException e) {
				LOG.error(e);
				throw new UnrecoverableErrorException("error.chain.bad-chain-id",chainGroupIds[i]);
			}
		}
		
		
		request.setAttribute("chainPath", chain);
		
		request.setAttribute("browseParent", GrouperHelper.group2Map(
				grouperSession, group));
		request.setAttribute("requestParams",request.getParameterMap());
		request.setAttribute("subject",GrouperHelper.subject2Map(subject));
		
		
		
		return mapping.findForward(FORWARD_Chain);

	}

}
