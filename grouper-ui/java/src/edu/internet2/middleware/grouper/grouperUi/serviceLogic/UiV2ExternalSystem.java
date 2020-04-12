package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ExternalSystemContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class UiV2ExternalSystem {
  
  /**
   * view all external systems
   */
  public void viewExternalSystems(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ExternalSystemContainer externalSystemContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getExternalSystemContainer();
      
      if (!externalSystemContainer.isCanViewExternalSystems()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      //TODO pull the list of all the configured external systems to show on the UI
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
    
  }

}
