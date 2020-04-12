package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.externalSystem.LdapGrouperExternalSystem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ExternalSystemContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGrouperExternalSystem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class UiV2ExternalSystem {
  
  /**
   * view all external systems
   */
  public void viewExternalSystems(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ExternalSystemContainer externalSystemContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getExternalSystemContainer();
      
      if (!externalSystemContainer.isCanViewExternalSystems()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<GrouperExternalSystem> grouperExternalSystems = new ArrayList<GrouperExternalSystem>();
      grouperExternalSystems.addAll(generateFakeGrouperExternalSystemsForTesting());
      
      List<GuiGrouperExternalSystem> guiGrouperExternalSystems = GuiGrouperExternalSystem.convertFromGrouperExternalSystem(grouperExternalSystems);
      
      externalSystemContainer.setGuiGrouperExternalSystems(guiGrouperExternalSystems);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/externalSystems/externalSystems.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  private static List<GrouperExternalSystem> generateFakeGrouperExternalSystemsForTesting() {
    
    List<GrouperExternalSystem> grouperExternalSystems = new ArrayList<GrouperExternalSystem>();
    
    
    LdapGrouperExternalSystem ldapGrouperExternalSystem = new LdapGrouperExternalSystem();
    ldapGrouperExternalSystem.setConfigId("test-ldap");
    ldapGrouperExternalSystem.setEnabled(true);
    
    grouperExternalSystems.add(ldapGrouperExternalSystem);
    
    return grouperExternalSystems;
    
    
  }

}
