
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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;

/**
 * @author shilen
 * @version $Id: PopulateCopyStemAction.java,v 1.1 2009-05-08 12:03:37 shilen Exp $
 */
public class PopulateCopyStemAction extends GrouperCapableAction {

  static final private String FORWARD_CopyStem = "CopyStem";

  public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response,
      HttpSession session, GrouperSession grouperSession)
      throws Exception {

    DynaActionForm stemForm = (DynaActionForm) form;

    String curNode = (String)stemForm.get("stemId");
    Stem stem = StemFinder.findByUuid(grouperSession, curNode, true);
    
    // this is needed to display the current path in the JSP page.
    request.setAttribute("browseParent", GrouperHelper.stem2Map(
        grouperSession, stem));
    
    // this is needed so that the JSP page can show the saved stems.
    makeSavedStemsAvailable(request);

    // this is needed to show the subtitle on the page.
    session.setAttribute("subtitle", "stems.action.copy");

    
    return mapping.findForward(FORWARD_CopyStem);

  }

}
