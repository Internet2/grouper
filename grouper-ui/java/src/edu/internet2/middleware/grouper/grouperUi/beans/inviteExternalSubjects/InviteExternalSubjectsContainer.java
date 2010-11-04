package edu.internet2.middleware.grouper.grouperUi.beans.inviteExternalSubjects;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister.ExternalRegisterContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;

/**
 * request container for inviting external subjects
 * @author mchyzer
 */
public class InviteExternalSubjectsContainer {

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static ExternalRegisterContainer retrieveFromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
  
    ExternalRegisterContainer externalRegisterContainer = (ExternalRegisterContainer)httpServletRequest
      .getAttribute("inviteExternalSubjectsContainer");
    if (externalRegisterContainer == null) {
      throw new NoSessionException(GrouperUiUtils.message("inviteExternalSubjects.noContainer"));
    }
    return externalRegisterContainer;
  }

  /**
   * store to session scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("inviteExternalSubjectsContainer", this);
  }

}
