package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.jexlTester.JexlScriptTesterResult;
import edu.internet2.middleware.grouper.app.jexlTester.ScriptExample;
import edu.internet2.middleware.grouper.app.jexlTester.ScriptType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;

public class ScriptTesterContainer {
  
  private String selectedScriptType;
  
  private String selectedExample;
  
  private String availableBeansGshScript;
  
  private String nullCheckingJexlScript;
  
  private String jexlScript;
  
  private JexlScriptTesterResult jexlScriptTesterResult;
  
  
  public JexlScriptTesterResult getJexlScriptTesterResult() {
    return jexlScriptTesterResult;
  }

  
  public void setJexlScriptTesterResult(JexlScriptTesterResult jexlScriptTesterResult) {
    this.jexlScriptTesterResult = jexlScriptTesterResult;
  }

  public void setAvailableBeansGshScript(String availableBeansGshScript) {
    this.availableBeansGshScript = availableBeansGshScript;
  }
  
  public void setNullCheckingJexlScript(String nullCheckingJexlScript) {
    this.nullCheckingJexlScript = nullCheckingJexlScript;
  }
  
  public void setJexlScript(String jexlScript) {
    this.jexlScript = jexlScript;
  }

  public String getSelectedScriptType() {
    return selectedScriptType;
  }
  
  public void setSelectedScriptType(String selectedScriptType) {
    this.selectedScriptType = selectedScriptType;
  }
  
  public String getSelectedExample() {
    return selectedExample;
  }

  
  public void setSelectedExample(String selectedExample) {
    this.selectedExample = selectedExample;
  }

  /**
   * @return all examples available for the selected script type
   */
  public List<String> getExamplesAvailableForSelectedScriptType() {
    
    List<String> examples = new ArrayList<>();
    
    ScriptType scriptType = ScriptType.valueOf(this.selectedScriptType);
    
    List<ScriptExample> scriptExamplesForType = scriptType.retrieveScriptExamplesForType();
    
    for (ScriptExample scriptExample: scriptExamplesForType) {
      examples.add(scriptExample.name());
    }
    
    return examples;
    
  }
  
  public String getAvailableBeansForSelectedScriptType() {
    
    if (StringUtils.isNotBlank(this.availableBeansGshScript)) {
      return this.availableBeansGshScript;
    }
    
    ScriptType scriptType = ScriptType.valueOf(this.selectedScriptType);
    
    ScriptExample scriptExample = ScriptExample.retrieveInstance(scriptType, selectedExample);
    
    String availableBeansScript = scriptExample.retrieveAvailableBeansGshScript();
    
    return availableBeansScript;
  }
  
  public String getNullCheckingJexlScript() {
    
    if (StringUtils.isNotBlank(this.nullCheckingJexlScript)) {
      return this.nullCheckingJexlScript;
    }
    
    ScriptType scriptType = ScriptType.valueOf(this.selectedScriptType);
    
    ScriptExample scriptExample = ScriptExample.retrieveInstance(scriptType, selectedExample);
    
    String nullCheckingJexlScript = scriptExample.retrieveNullCheckingJexlScript();
    
    return nullCheckingJexlScript;
  }
  
  public boolean isShowNullCheckingJexlScript() {
    
    ScriptType scriptType = ScriptType.valueOf(this.selectedScriptType);
    return scriptType.hasNullCheckingOption();
  }
  
  public String getJexlScript() {
    
    if (StringUtils.isNotBlank(this.jexlScript)) {
      return this.jexlScript;
    }
    
    ScriptType scriptType = ScriptType.valueOf(this.selectedScriptType);
    
    ScriptExample scriptExample = ScriptExample.retrieveInstance(scriptType, selectedExample);
    String exampleJexlScript = scriptExample.retrieveExampleJexlScript();
    
    return exampleJexlScript;
  }
  
  public String getScriptDescription() {
    //jexlScriptType_PROVISIONING_GROUP_TRANSLATION_description
    return GrouperTextContainer.textOrNull("jexlScriptType_"+this.selectedScriptType+"_description");
  }
  
  public String getExampleDescription() {
    // jexlScriptExample_PROVISIONING_GROUP_TRANSLATION__GENERIC_description
    return GrouperTextContainer.textOrNull("jexlScriptExample_"+this.selectedScriptType+"__"+this.selectedExample+"_description");
  }

  /**
   * Server-side gate for the JEXL script tester page and submit handlers (UiV2ScriptTester calls
   * this before doing anything).  The tester runs request-supplied GSH/Groovy in the UI JVM, so
   * the user must be BOTH a Grouper sysadmin (wheel or root) AND a member of the configured
   * jexlScriptTestingGroup, as the config doc and the error text say.  Group membership alone is
   * not enough.
   * @return true if the logged-in user can use the script tester
   */
  public boolean isCanScriptTester() {
    
    // must be wheel or root first: before this check a non-admin member of the tester group could
    // call the handlers directly (the link check is only presentation) and run arbitrary Groovy
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (loggedInSubject == null || !PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return false;
    }
    
    String groupName = GrouperConfig.retrieveConfig().propertyValueString("jexlScriptTestingGroup");
    if (StringUtils.isBlank(groupName)) {
      return false;
    }

    return (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Group group = GroupFinder.findByName(grouperSession, groupName, false);
        if (group == null) {
          return false;
        }
        // and must be a member (direct or indirect) of the configured tester group
        return group.hasMember(loggedInSubject);
        
      }
    });
    
  }
  
  /**
   * whether to show the script tester link on the miscellaneous page.  Follows the same rule as
   * isCanScriptTester() so the link and the server-side gate cannot drift apart
   * @return true if the link should be shown
   */
  public boolean isCanScriptTesterLink() {
    return this.isCanScriptTester();
  }
  
  /**
   * @return all the script types available
   */
  public List<String> getAllScriptTypes() {
    
    List<String> scriptTypes = new ArrayList<>();
    
    for (ScriptType scriptType: ScriptType.values()) {
      scriptTypes.add(scriptType.name());
    }
    
    return scriptTypes;
  }

}
