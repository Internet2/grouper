/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Bristol
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Bristol nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Bristol, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Bristol, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper.ui.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Grouper;
import edu.internet2.middleware.grouper.GrouperAttribute;
import edu.internet2.middleware.grouper.GrouperMember;
import edu.internet2.middleware.grouper.GrouperNaming;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperStem;
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
 * @version $Id: SaveStemAction.java,v 1.1.1.1 2005-08-23 13:04:16 isgwb Exp $
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
			if(Grouper.NS_ROOT.equals(defaultStem)) {
				curNode = defaultStem;
			}else {
				GrouperStem root = GrouperStem.loadByName(grouperSession, defaultStem);
				curNode = root.id();
			}
		}

		
		GrouperStem stem = null;
		String id = null;
		
		//TODO: should be transactional
		if (stemExists) {
			stem = (GrouperStem)GrouperStem.loadByID(grouperSession, curNode);
		
		} else {
			String curStemStr = null;
			if(curNode.equals(Grouper.NS_ROOT)) {
				curStemStr = curNode;
			}else{
				GrouperStem curStem = (GrouperStem)GrouperStem.loadByID(grouperSession,
					curNode);
				curStemStr = curStem.name();
			}
			stem = GrouperStem.create(grouperSession, curStemStr,
					(String) stemForm.get("stemName"));
			GrouperNaming naming = grouperSession.naming();
			naming.grant(grouperSession, stem, GrouperMember
					.load(grouperSession,grouperSession.subject()), Grouper.PRIV_CREATE);
			id = stem.id();

			stemForm.set("stemId", id);
		}

		String stemName = stem.name().substring(
				stem.name().lastIndexOf(HIER_DELIM) + 1);
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

		stem.attribute("displayExtension", (String) stemForm.get("stemDisplayName"));
		
			String val = (String) stemForm.get("stemDescription");
			if("".equals(val)) val=null;
			GrouperAttribute ga = stem.attribute("description");
			if(!(ga==null && val==null))	stem.attribute("description",val);
		

		request.setAttribute("message", new Message("stems.message.stem-saved",
				(String) stemForm.get("stemDisplayName")));

		String submit = request.getParameter("submit.save");
		if(submit==null) {
			submit = request.getParameter("submit.save_work_in_new");
			if(submit!=null) setBrowseNode(stem.id(),session);
		}
		
		if (submit != null) {
			return mapping.findForward(FORWARD_CreateGroups);
		}
		submit = request.getParameter("submit.save_show_members");
		request.setAttribute("stemId", stem.id());
		request.setAttribute("forStems", Boolean.TRUE);
		session.setAttribute("findForNode", stem.id());
		request.setAttribute("message", new Message(
				"groups.message.group-saved", (String) stemForm
						.get("stemDisplayName")));
		if (submit != null) {
			return mapping.findForward(FORWARD_StemMembers);
		}
		return mapping.findForward(FORWARD_FindNewMembers);
	}

}