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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;

/**
 * Top level Strut's action which retrieves and makes available subjects with 
 * user-selected privilege. 
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
    <td><font face="Arial, Helvetica, sans-serif">Identifies group we want to 
      see members for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">asMemberOf</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">if groupId and findForNode are 
      empty, asMemberOf identifies group</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">privilege</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Naming privilege for which we 
      are listing privilegees - if empty use default and SET</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">start</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used by CollectionPager</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;thisPageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;Allows callerPageId to 
      be added to links/forms so this page can be returned to</font></td>
  </tr>
   <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseParent</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map for stem of current stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">pager</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">CollectionPager instance</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">pagerParams</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of params set on pager</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">stemPrivs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of privileges the Subject 
      identified by request parameters has for this stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">AllStemPrivs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of all Naming privileges</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">stemMembership</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map used by Strut's &lt;html:link&gt; 
      tags when generating parameters for &lt;a&gt; tags</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitleArgs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used to give context to UI</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">privileges</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array which may be set by DoAssignNewMembersAction. 
      Used if privilege not set</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">title=stems.manage</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitle=stems.action.show-priviligees</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">stemMap</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Current stem wrapped as a Map</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Use if stemId not set</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">stemPrivilege</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">READ if privilege not set, otherwise 
      SET to current value</font></td>
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
 * @version $Id: PopulateStemPriviligeesAction.java,v 1.11 2009-08-12 04:52:14 mchyzer Exp $
 */

public class PopulateStemPriviligeesAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local

	static final private String FORWARD_StemPriviligees = "StemPriviligees";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		DynaActionForm groupOrStemForm = (DynaActionForm) form;
		saveAsCallerPage(request,groupOrStemForm,"findForNode");
		String privilege = (String)groupOrStemForm.get("privilege");
		
		session.setAttribute("title", "stems.manage");
		session.setAttribute("subtitle",
				"stems.action.show-members");
		String displayPrivilege=privilege;
		try {
			displayPrivilege=GrouperUiFilter.retrieveSessionNavResourceBundle().getString("priv." + privilege);
		}catch(MissingResourceException mre){}
		request.setAttribute("subtitleArgs", new Object[] { displayPrivilege });
		
		
		//Determine stem for which we are retrieving privilegees
		String stemId = (String)groupOrStemForm.get("stemId");
		
		//Determine the actual privilege
		
		if (isEmpty(privilege)) {
			privilege = (String) session.getAttribute("stemPrivilege");
		}
		if (isEmpty(privilege)) {
			String[] privileges = (String[]) request.getAttribute("privileges");
			if (privileges.length > 0) {
				privilege = privileges[0];
			}
		}
		session.setAttribute("stemPrivilege", privilege);

		boolean forStems = false;

		if (isEmpty(stemId))
			stemId = (String) session.getAttribute("findForNode");
		if (isEmpty(stemId))
			stemId = request.getParameter("asMemberOf");
		if (isEmpty(stemId))
			stemId = getBrowseNode(session);
		Stem stem = null;
		
		groupOrStemForm.set("privilege", privilege);
		
		stem = StemFinder.findByUuid(grouperSession, stemId, true);
		//Retrieve privilegees
		Set subjects = GrouperHelper.getSubjectsWithPriv(stem,privilege);
		List subjectPrivilegeMaps = GrouperHelper.subjects2SubjectPrivilegeMaps(
					grouperSession,sort(subjects,request,"privilegees", -1, null),stem,privilege);
		

		//Set up CollectionPager for view
		String startStr = request.getParameter("start");
		if (startStr == null || "".equals(startStr))
			startStr = "0";

		int start = Integer.parseInt(startStr);
		int pageSize = getPageSize(session);
		CollectionPager pager = new CollectionPager(null, subjectPrivilegeMaps, subjectPrivilegeMaps
				.size(), null, start, null, pageSize);
		pager.setParam("stemId", stemId);
		pager.setParam("privilege", privilege);

		pager.setTarget(mapping.getPath());
		request.setAttribute("pager", pager);
		request.setAttribute("linkParams", pager.getParams().clone());

		Map membership = new HashMap();
		
		membership.put("stemId", stemId);
		membership.put("privilege", privilege);
		
		Map privs = GrouperHelper.hasAsMap(grouperSession, GroupOrStem.findByStem(grouperSession,stem));
		request.setAttribute("stemPrivs", privs);

		request.setAttribute("stemMembership", membership);
		Map stemMap = GrouperHelper.stem2Map(
				grouperSession, stem);
		request.setAttribute("browseParent",stemMap );
		request.setAttribute("stem",stemMap );
		request.setAttribute("allStemPrivs", GrouperHelper
				.getStemPrivsWithLabels(GrouperUiFilter.retrieveSessionNavResourceBundle()));
		

		session.setAttribute("subtitle",
				"stems.action.show-priviligees");

		return mapping.findForward(FORWARD_StemPriviligees);
	}
}
