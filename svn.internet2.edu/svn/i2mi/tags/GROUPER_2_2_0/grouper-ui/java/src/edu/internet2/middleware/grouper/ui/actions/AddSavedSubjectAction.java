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


import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;


import edu.internet2.middleware.grouper.GrouperSession;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;

import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Top level Strut's action which adds a Subject to the 'saved' list. 
 * <p/>
<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the Subject to save</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectType</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The type of the subject to be 
      saved</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p>&nbsp;</p></td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">sessionMessage</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Message indicating subject/group 
      added to list</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Strut's Action Parameter</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
</table>

 * @author Gary Brown.
 * @version $Id: AddSavedSubjectAction.java,v 1.8 2009-08-12 04:52:14 mchyzer Exp $
 */
public class AddSavedSubjectAction extends GrouperCapableAction {
	protected static final Log LOG = LogFactory.getLog(AddSavedSubjectAction.class);
	
	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		NavExceptionHelper neh=getExceptionHelper(session);
		DynaActionForm subjectForm = (DynaActionForm) form;
		String subjectId = subjectForm.getString("subjectId");
		String subjectType = subjectForm.getString("subjectType");
		String sourceId = subjectForm.getString("sourceId");
		if(!isEmpty(subjectId) && !isEmpty(subjectType) && !isEmpty(sourceId)) {
			LOG.info("Saved entity to workspace ("+subjectId + "," +subjectType+"," + sourceId+")");
			try {
				Subject subj = SubjectFinder.findById(subjectId,subjectType,sourceId, true);
				addSavedSubject(session,subj);
			}catch (Exception e) {
				String contextError=null;
				if(!"group".equals(subjectType)) {
					 contextError = GrouperUiFilter.retrieveSessionNavResourceBundle().getString("error.saved-subjects.exception");
				}else{
					contextError = GrouperUiFilter.retrieveSessionNavResourceBundle().getString("error.saved-groups.exception");	
				}
				contextError=MessageFormat.format(contextError, subjectId);
				session.setAttribute("sessionMessage",new Message(neh.key(e),contextError,true));
				return redirectToCaller(subjectForm);
			
			}
			if(!"group".equals(subjectType)) {
				session.setAttribute("sessionMessage",new Message("saved-subjects.added"));
			}else{
				session.setAttribute("sessionMessage",new Message("saved-subjects.groups.added"));	
			}
		}else{
			String msg = neh.missingParameters(subjectId,"subjectId",subjectType,"subjectType",sourceId,"sourceId");
			LOG.error(msg);
			if(!"group".equals(subjectType)) {
				session.setAttribute("sessionMessage",new Message("error.saved-subjects.missing-parameter",true));
			}else{
				session.setAttribute("sessionMessage",new Message("error.saved-subjects.groups.missing-parameter",true));	
			}
		}
		
		return redirectToCaller(subjectForm);
	}
}
