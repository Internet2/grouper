package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.jexlTester.JexlScriptTester;
import edu.internet2.middleware.grouper.app.jexlTester.JexlScriptTesterResult;
import edu.internet2.middleware.grouper.app.jexlTester.ScriptExample;
import edu.internet2.middleware.grouper.app.jexlTester.ScriptType;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ScriptTesterContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;

public class UiV2ScriptTester {
  

  /** logger */
  private static Log LOG = GrouperUtil.getLog(UiV2ScriptTester.class);;

  /**
   * show form to test script
   * @param request
   * @param response
   */
  public void testScript(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ScriptTesterContainer scriptTesterContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getScriptTesterContainer();
      
      if (!scriptTesterContainer.isCanScriptTester()) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("userNotInJexlScriptTestingGroup")));
        return;
        
      }
      
      String scriptType = request.getParameter("scriptType");
      String previousScriptType = request.getParameter("previousScriptType");
      
      if (StringUtils.isNotBlank(scriptType)) {
        
        scriptTesterContainer.setSelectedScriptType(scriptType);
        
        // user has changed script type, let's reset the form 
        if (StringUtils.isNotBlank(previousScriptType) && !StringUtils.equals(scriptType, previousScriptType)) {
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/scriptTester/scriptTesterScreen.jsp"));
          return;
        }
        
        String exampleType = request.getParameter("exampleType");
        if (StringUtils.isNotBlank(exampleType)) {
          
          ScriptType scriptTypeEnum = ScriptType.valueOf(scriptType);
          ScriptExample scriptExample = ScriptExample.retrieveInstance(scriptTypeEnum, exampleType);
          scriptTesterContainer.setSelectedExample(exampleType);
        }
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/scriptTester/scriptTesterScreen.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void testScriptSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ScriptTesterContainer scriptTesterContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getScriptTesterContainer();
      
      if (!scriptTesterContainer.isCanScriptTester()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String scriptType = request.getParameter("scriptType");
      if (StringUtils.isBlank(scriptType)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#scriptTypeId",
            TextContainer.retrieveFromRequest().getText().get("scriptTypeRequired")));
        return;
      }
      String exampleType = request.getParameter("exampleType");
      
      if (StringUtils.isBlank(exampleType)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#exampleTypeId",
            TextContainer.retrieveFromRequest().getText().get("exampleRequired")));
        return;
      }

      String availableBeansGshScript = request.getParameter("availableBeansGshScript");
      
      if (StringUtils.isBlank(availableBeansGshScript)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#availableBeansId",
            TextContainer.retrieveFromRequest().getText().get("availableBeansRequired")));
        return;
      }
      
      String nullCheckingJexlScript = request.getParameter("nullCheckingJexlScript");
      String jexlScript = request.getParameter("jexlScript");
      
      if (StringUtils.isBlank(jexlScript)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#scriptSourceId",
            TextContainer.retrieveFromRequest().getText().get("jexlScriptRequired")));
        return;
      }

      scriptTesterContainer.setSelectedScriptType(scriptType);
      scriptTesterContainer.setSelectedExample(exampleType);
      scriptTesterContainer.setAvailableBeansGshScript(availableBeansGshScript);
      scriptTesterContainer.setNullCheckingJexlScript(nullCheckingJexlScript);
      scriptTesterContainer.setJexlScript(jexlScript);
      
      ScriptType scriptTypeEnum = ScriptType.valueOf(scriptType);
      
      ScriptExample scriptExample = ScriptExample.retrieveInstance(scriptTypeEnum, exampleType);
      
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.JEXL_TEST_EXEC, "jexlTestExample",
          scriptExample.name(),
          "script", jexlScript);
      auditEntry.setDescription(jexlScript);
      
      auditEntry.saveOrUpdate(true);

      LOG.warn(SubjectUtils.subjectToString(loggedInSubject) + ", tested jexl script: "+scriptExample.name() +
          ", script: "+jexlScript + " ,"
              + "nullCheckingScript: "+nullCheckingJexlScript + " ,availableBeans: "+availableBeansGshScript);
      
      JexlScriptTesterResult jexlScriptTesterResult = JexlScriptTester.runJexlScript(scriptExample, availableBeansGshScript, nullCheckingJexlScript, jexlScript);
      
      scriptTesterContainer.setJexlScriptTesterResult(jexlScriptTesterResult);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/scriptTester/scriptTesterScreen.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  

}
