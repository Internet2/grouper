/*
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

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

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
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
 * @version $Id: SaveStemAction.java,v 1.13 2009-08-12 04:52:14 mchyzer Exp $
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
				Stem root = StemFinder.findByName(grouperSession, defaultStem, true);
				curNode = root.getUuid();
			}
		}

		
		Stem stem = null;
		String id = null;
		if ("".equals(stemForm.get("stemDisplayName")))
			stemForm.set("stemDisplayName", (String) stemForm.get("stemName"));
		
		//TODO: should be transactional
		if (stemExists) {
			stem = StemFinder.findByUuid(grouperSession, curNode, true);
		
		} else {
			Stem parentStem=null;
			if(curNode.equals(GrouperHelper.NS_ROOT)) {
				parentStem=StemFinder.findRootStem(grouperSession);
			}else{
				GroupOrStem curGos = GroupOrStem.findByID(grouperSession,curNode);
				
				if(curGos.isStem()) {
					parentStem = curGos.getStem();
				}else{
					parentStem = curGos.getGroup().getParentStem();
				}
				
			}
			
			try{
				stem = parentStem.addChildStem((String) stemForm.get("stemName"),(String) stemForm.get("stemDisplayName"));
				stem.grantPriv(grouperSession.getSubject(),Privilege.getInstance("create"));
			}catch(StemAddException e) {
					request.setAttribute("message", new Message(
							"stems.message.error.add-problem",new String[] {e.getMessage()}, true));
						return mapping.findForward(FORWARD_CreateAgain);
				
			}
			
			
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

		

		if(!stem.getDisplayExtension().equals(stemForm.get("stemDisplayName"))) {
			stem.setDisplayExtension((String) stemForm.get("stemDisplayName"));
		}

		String alternateName = (String) stemForm.get("stemAlternateName");

		String oldAlternateName = null;
		Iterator<String> alternateNames = stem.getAlternateNames().iterator();
		if (alternateNames.hasNext()) {
			oldAlternateName = alternateNames.next();
		}

		if (isEmpty(alternateName) && oldAlternateName != null) {
			stem.deleteAlternateName(oldAlternateName);
		} else if (!isEmpty(alternateName) && (oldAlternateName == null || !oldAlternateName.equals(alternateName))) {
			stem.addAlternateName(alternateName);
		}
		
		if(!stem.getExtension().equals(stemForm.get("stemName"))) 
			stem.setExtension((String) stemForm.get("stemName"));
		
			String val = (String) stemForm.get("stemDescription");
			if("".equals(val)) val=null;
			if(val!=null)	{
			  stem.setDescription(val);
			}
			stem.store();

		request.setAttribute("message", new Message("stems.message.stem-saved",
				(String) stemForm.get("stemDisplayName")));
		
		try {
			if("true".equals(GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("put.in.session.updated.stems"))) {
				addSavedStem(session,stem);
			}
		}catch(Exception e){}

		String submit = request.getParameter("submit.save");
		if(submit==null) {
			submit = request.getParameter("submit.save_work_in_new");
			if(submit!=null) setBrowseNode(stem.getUuid(),session);
		}
		
		if (submit != null) {
			//return mapping.findForward(FORWARD_CreateGroups);
			return new ActionForward("/populate" + getBrowseMode(session)+"Groups.do");
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
