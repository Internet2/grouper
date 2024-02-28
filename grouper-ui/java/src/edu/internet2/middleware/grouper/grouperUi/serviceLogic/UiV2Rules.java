package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiRuleDefinition;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.RulesContainer;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleFinder;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class UiV2Rules {
  

  public void viewAllRules(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
            
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs(); 
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      RulesContainer rulesContainer = grouperRequestContainer.getRulesContainer();
      
      Set<RuleDefinition> ruleDefinitions = RuleFinder.retrieveRuleDefinitionsForSubject(loggedInSubject);
      
      Set<GuiRuleDefinition> guiRules = new HashSet<>();
      
      for (RuleDefinition ruleDefinition: ruleDefinitions) {
        GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition(ruleDefinition);
        guiRules.add(guiRuleDefinition);
      }
      
      rulesContainer.setGuiRuleDefinitions(guiRules);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/rules/rules.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
    
    
  }

}
