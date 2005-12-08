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
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Privilege;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.ui.Message;


/**
 * Top level Strut's action which saves new / updated stem - automatically gives 
 * creator STEM / CREATE privilege.  
 * <p/>
<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">stemId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies stem to save</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">stem</font><font face="Arial, Helvetica, sans-serif">Name,stemDisplayName,<br>
        stemDescription</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Values retrieved from DynaActionForm</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.save</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates user wants to save 
      stem but not assign privileges</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.save_work_in_new</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates user wants to save 
      stem and change browseNode to new stem</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.save_show_members</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates user wants to save 
      stem and list privilegees</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">message</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">message instance: text derived 
      <br>
      from stems.message.error.invalid-char or stems.message.stem-saved key in 
      nav ResourceBundle</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">stemId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Set because may be new id for 
      new stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">forStems</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates to populateFindNewMembers 
      that we are finding on behalf of a stem</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Set to stemId if user indicates 
      they want to find new privilegees</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseNodeId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If new stem need to set its 
      stem to the current node</font></td>
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
 * @version $Id: SaveStemAction.java,v 1.2 2005-12-08 15:30:52 isgwb Exp $
 */

public class SaveStemAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards

	static final private String FORWARD_FindNewMembers = "FindNewMembers";


	static final private String FORWARD_CreateGroups = "CreateGroups";

	static final private String FORWARD_StemMembers = "StemMembers";

	static final private String FORWARD_CreateAgain = "CreateAgain";

	static final private String FORWARD_EditAgain = "EditAgain";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		DynaActionForm stemForm = (DynaActionForm) form;
		boolean stemExists = false;
		String curNode = (String) stemForm.get("stemId");
		if (curNode == null || "".equals(curNode)) {
			//If new stem need to get parent stem
			curNode = getBrowseNode(session);
		} else {
			stemExists = true;
		}
		if (curNode == null || "".equals(curNode)) {
			String defaultStem = getDefaultRootStemName(session);
			if(GrouperHelper.NS_ROOT.equals(defaultStem)) {
				curNode = defaultStem;
			}else {
				Stem root = StemFinder.findByName(grouperSession, defaultStem);
				curNode = root.getUuid();
			}
		}

		
		Stem stem = null;
		String id = null;
		
		//TODO: should be transactional
		if (stemExists) {
			stem = StemFinder.findByUuid(grouperSession, curNode);
		
		} else {
			Stem parentStem=null;
			if(curNode.equals(GrouperHelper.NS_ROOT)) {
				parentStem=StemFinder.findRootStem(grouperSession);
			}else{
				parentStem = StemFinder.findByUuid(grouperSession,
					curNode);
			}
			stem = parentStem.addChildStem((String) stemForm.get("stemName"),(String) stemForm.get("stemDisplayName"));
			stem.grantPriv(grouperSession.getSubject(),Privilege.getInstance("create"));
			id = stem.getUuid();
			stemForm.set("stemId", id);
		}

		String stemName = stem.getName().substring(
				stem.getName().lastIndexOf(HIER_DELIM) + 1);
		if (!stemExists && !stemName.matches("[^\"<>:\\*]+")) {
			request.setAttribute("message", new Message(
					"stems.message.error.invalid-char", true));
			if (stemExists) {
				return mapping.findForward(FORWARD_EditAgain);
			} else {
				return mapping.findForward(FORWARD_CreateAgain);
			}
		}

		if ("".equals(stemForm.get("stemDisplayName")))
			stemForm.set("stemDisplayName", stemName);

		stem.setDisplayExtension((String) stemForm.get("stemDisplayName"));
		
			String val = (String) stemForm.get("stemDescription");
			if("".equals(val)) val=null;
			if(val!=null)	stem.setDescription(val);
		

		request.setAttribute("message", new Message("stems.message.stem-saved",
				(String) stemForm.get("stemDisplayName")));

		String submit = request.getParameter("submit.save");
		if(submit==null) {
			submit = request.getParameter("submit.save_work_in_new");
			if(submit!=null) setBrowseNode(stem.getUuid(),session);
		}
		
		if (submit != null) {
			return mapping.findForward(FORWARD_CreateGroups);
		}
		submit = request.getParameter("submit.save_show_members");
		request.setAttribute("stemId", stem.getUuid());
		request.setAttribute("forStems", Boolean.TRUE);
		session.setAttribute("findForNode", stem.getUuid());
		request.setAttribute("message", new Message(
				"groups.message.group-saved", (String) stemForm
						.get("stemDisplayName")));
		if (submit != null) {
			return mapping.findForward(FORWARD_StemMembers);
		}
		return mapping.findForward(FORWARD_FindNewMembers);
	}

}