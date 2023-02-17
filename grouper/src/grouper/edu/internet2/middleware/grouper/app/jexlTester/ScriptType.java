package edu.internet2.middleware.grouper.app.jexlTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.abac.GrouperAbac;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.provider.SubjectImpl;

public enum ScriptType {
  
  ABAC {
    
    @Override
    public Class<? extends ScriptExample> retrieveScriptExampleForType() {
      return ScriptExampleForAbacTranslation.class;
    }

    @Override
    public Object runJexl(Map<String, Object> elVariableMap, String jexlScript) {
      return GrouperAbac.runScriptStatic(jexlScript, elVariableMap);
    }

    @Override
    public boolean hasNullCheckingOption() {
      return false;
    }
    
  },
  
  GSH_TEMPLATE_SHOW_EL {

    @Override
    public Class<? extends ScriptExample> retrieveScriptExampleForType() {
      return ScriptExampleForGshTemplateShowEl.class;
    }

    @Override
    public Object runJexl(Map<String, Object> elVariableMap, String jexlScript) {
      return GrouperUtil.substituteExpressionLanguageScript(jexlScript, elVariableMap, true, false, false);
    }

    @Override
    public boolean hasNullCheckingOption() {
      return false;
    }
    
  },
  
  LDAP_LOADER_NAME_EXPRESSION {
    
    @Override
    public Class<? extends ScriptExample> retrieveScriptExampleForType() {
      return ScriptExampleForLdapLoaderNameExpression.class;
    }

    @Override
    public Object runJexl(Map<String, Object> elVariableMap, String jexlScript) {
      return LoaderLdapUtils.runScriptStatic(jexlScript, elVariableMap);
    }

    @Override
    public boolean hasNullCheckingOption() {
      return false;
    }
    
  },
  
  PROVISIONING_ENTITY_TRANSLATION {

    @Override
    public Class<? extends ScriptExample> retrieveScriptExampleForType() {
      return ScriptExampleForProvisioningEntityTranslation.class;
    }

    @Override
    public Object runJexl(Map<String, Object> elVariableMap, String jexlScript) {
      return GrouperProvisioningTranslator.runScriptStatic(jexlScript, elVariableMap);
    }

    @Override
    public boolean hasNullCheckingOption() {
      return true;
    }
  },
  
  PROVISIONING_GROUP_TRANSLATION {

    @Override
    public Class<? extends ScriptExample> retrieveScriptExampleForType() {
      return ScriptExampleForProvisioningGroupTranslation.class;
    }

    @Override
    public Object runJexl(Map<String, Object> elVariableMap, String jexlScript) {
      return GrouperProvisioningTranslator.runScriptStatic(jexlScript, elVariableMap);
    }

    @Override
    public boolean hasNullCheckingOption() {
      return true;
    }
    

  },
  
  PROVISIONING_MEMBERSHIP_TRANSLATION {

    @Override
    public Class<? extends ScriptExample> retrieveScriptExampleForType() {
      return ScriptExampleForProvisioningMembershipTranslation.class;
    }

    @Override
    public Object runJexl(Map<String, Object> elVariableMap, String jexlScript) {
      return GrouperProvisioningTranslator.runScriptStatic(jexlScript, elVariableMap);
    }

    @Override
    public boolean hasNullCheckingOption() {
      return true;
    }
  },
  
  SUBJECT_SOURCE {

    @Override
    public Class<? extends ScriptExample> retrieveScriptExampleForType() {
      return ScriptExampleForSubjectSourceTranslation.class;
    }

    @Override
    public Object runJexl(Map<String, Object> elVariableMap, String jexlScript) {
      return SubjectImpl.runScriptStatic(jexlScript, elVariableMap);
    }

    @Override
    public boolean hasNullCheckingOption() {
      return false;
    }
  };
  
  /**
   * @return
   */
  abstract Class<? extends ScriptExample> retrieveScriptExampleForType();
  
  /**
   * @param elVariableMap
   * @param shouldContinueJexl
   * @return
   */
  public boolean nullCheckingShouldContinue(Map<String, Object> elVariableMap,
      String shouldContinueJexl) {
    Object object = GrouperUtil.substituteExpressionLanguageScript(shouldContinueJexl, elVariableMap, true, true, true);
    if (GrouperUtil.isBlank(object)) {
      return false;
    }
    return GrouperUtil.booleanValue(object);
  }
  
  /**
   * @param elVariableMap
   * @param jexlScript
   * @return
   */
  public abstract Object runJexl(Map<String, Object> elVariableMap, String jexlScript);
  
  /**
   * @return true if null checking option is available otherwise false
   */
  public abstract boolean hasNullCheckingOption();
  
  public List<ScriptExample> retrieveScriptExamplesForType() {
    
    List<ScriptExample> scriptExamples = new ArrayList<>();
    
    Class<? extends ScriptExample> scriptExampleForTypeClass = this.retrieveScriptExampleForType();
    
    ScriptExample[] enumConstants = scriptExampleForTypeClass.getEnumConstants();
    
    for (ScriptExample scriptExample: enumConstants) {
      scriptExamples.add(scriptExample);
    }
    // jexlScriptTest.institutionExample.<SCRIPT_TYPE>.<configId> = <EXAMPLE_NAME>
    Pattern pattern = Pattern.compile("^jexlScriptTest\\.institutionExample\\."+this.name()+"\\.(.*)$");
    Map<String, String> propertiesMap = GrouperConfig.retrieveConfig().propertiesMap(pattern);
    for (String exampleName: propertiesMap.values()) {
      ScriptExample scriptExample = new ScriptExample() {
        
        @Override
        public ScriptType retrieveScriptType() {
          return ScriptType.this;
        }
        
        @Override
        public String name() {
          return exampleName;
        }
        
        @Override
        public Object expectedOutput() {
          return null;
        }
      };
      scriptExamples.add(scriptExample);
    }
    
    return scriptExamples;
  }
  
  /**
   * @return result to be shown on the screen
   */
  public String resultForScreen() {
    
    Object result = JexlScriptTester.retrieveOutputFromGshScript();
    if (result == null) {
      return "<null>";
    }
    
    return result.getClass().getName() + ": " + GrouperUtil.stringValue(result);
  }
  
}
