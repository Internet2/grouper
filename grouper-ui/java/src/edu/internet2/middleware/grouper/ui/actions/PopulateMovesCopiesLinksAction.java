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
package edu.internet2.middleware.grouper.ui.actions;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;

/**
 * @author shilen
 * @version $Id: PopulateMovesCopiesLinksAction.java,v 1.1 2009-05-08 12:03:37 shilen Exp $
 */
public class PopulateMovesCopiesLinksAction extends GrouperCapableAction {

  static final private String FORWARD_MovesCopiesLinks = "MovesCopiesLinks";

  public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response,
      HttpSession session, GrouperSession grouperSession)
      throws Exception {

    DynaActionForm groupForm = (DynaActionForm) form;

    String curNode = (String)groupForm.get("stemId");
    Stem stem = StemFinder.findByUuid(grouperSession, curNode, true);
    
    // this is needed to display the current path in the JSP page.
    request.setAttribute("browseParent", GrouperHelper.stem2Map(
        grouperSession, stem));

    // this is needed to show the subtitle on the page.
    session.setAttribute("subtitle", "stems.action.movesandcopies");

    // now lets see which links we should populate
    Set<Privilege> privs = new LinkedHashSet<Privilege>();
    if (stem.hasStem(grouperSession.getSubject())) {
      privs.add(NamingPrivilege.STEM);
    }
    if (stem.hasCreate(grouperSession.getSubject())) {
      privs.add(NamingPrivilege.CREATE);
    }
    boolean canCopy = PrivilegeHelper.canCopyStems(grouperSession.getSubject());
    boolean canMove = PrivilegeHelper.canMoveStems(grouperSession.getSubject());
    
    request.setAttribute("canCopyStem", GrouperHelper.canCopyStem(stem, canCopy));
    request.setAttribute("canMoveStem", GrouperHelper.canMoveStem(stem, canMove, privs));
    request.setAttribute("canCopyOtherStemToStem", GrouperHelper.canCopyOtherStemToStem(stem, canCopy, privs));
    request.setAttribute("canMoveOtherStemToStem", GrouperHelper.canMoveOtherStemToStem(stem, canMove, privs));
    request.setAttribute("canCopyGroupToStem", GrouperHelper.canCopyGroupToStem(stem, privs));
    request.setAttribute("canMoveGroupToStem", GrouperHelper.canMoveGroupToStem(stem, privs));
    
    Map<String, String> stemMovesCopiesParams = new HashMap<String, String>();
    stemMovesCopiesParams.put("stemId", stem.getUuid());
    request.setAttribute("stemMovesCopiesParams",stemMovesCopiesParams);
    
    return mapping.findForward(FORWARD_MovesCopiesLinks);

  }

}
