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


import edu.internet2.middleware.grouper.ui.Message;

/**
 * Not used yet. 
 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: SaveGroupAttributesAction.java,v 1.3 2005-12-14 15:06:01 isgwb Exp $
 */
public class SaveGroupAttributesAction extends org.apache.struts.action.Action {


  //------------------------------------------------------------ Local Forwards
  static final private String FORWARD_GroupMembers = "GroupMembers";
  static final private String FORWARD_FindNewMembers = "FindNewMembers";
  static final private String FORWARD_GroupSummary = "GroupSummary";

  //------------------------------------------------------------ Action Methods

  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {
      	
		request.setAttribute("message",new Message("groups.action.saved-attr",true));

		String submit = request.getParameter("submit.save");
		
			if(submit!=null) {
				return mapping.findForward(FORWARD_GroupSummary);
			}
			request.getSession().setAttribute("findForNode",request.getParameter("groupId"));
   return mapping.findForward(FORWARD_FindNewMembers);

    
  }

}