package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.jexlTester.JexlScriptTesterResult;
import edu.internet2.middleware.grouper.app.jexlTester.ScriptExample;
import edu.internet2.middleware.grouper.app.jexlTester.ScriptType;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
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
    Class<? extends ScriptExample> scriptExampleForTypeClass = scriptType.retrieveScriptExampleForType();
    
    ScriptExample[] enumConstants = scriptExampleForTypeClass.getEnumConstants();
    
    for (ScriptExample scriptExample: enumConstants) {
      examples.add(scriptExample.name());
    }
    
    return examples;
    
  }
  
  public String getAvailableBeansForSelectedScriptType() {
    
    ScriptType scriptType = ScriptType.valueOf(this.selectedScriptType);
    
    Class scriptExampleForTypeClass = scriptType.retrieveScriptExampleForType();
    
    ScriptExample scriptExample = (ScriptExample) Enum.valueOf(scriptExampleForTypeClass, this.selectedExample);
    
    String availableBeansScript = scriptExample.retrieveAvailableBeansGshScript();
    
    return availableBeansScript;
  }
  
  public String getNullCheckingJexlScript() {
    ScriptType scriptType = ScriptType.valueOf(this.selectedScriptType);
    
    Class scriptExampleForTypeClass = scriptType.retrieveScriptExampleForType();
    
    ScriptExample scriptExample = (ScriptExample) Enum.valueOf(scriptExampleForTypeClass, this.selectedExample);
    
    String nullCheckingJexlScript = scriptExample.retrieveNullCheckingJexlScript();
    
    return nullCheckingJexlScript;
  }
  
  public String getJexlScript() {
    
    ScriptType scriptType = ScriptType.valueOf(this.selectedScriptType);
    
    Class scriptExampleForTypeClass = scriptType.retrieveScriptExampleForType();
    
    ScriptExample scriptExample = (ScriptExample) Enum.valueOf(scriptExampleForTypeClass, this.selectedExample);
    
    String exampleJexlScript = scriptExample.retrieveExampleJexlScript();
    
    return exampleJexlScript;
  }

  public boolean isCanScriptTester() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
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
