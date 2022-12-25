package edu.internet2.middleware.grouper.app.jexlTester;

import java.util.Map;

import edu.internet2.middleware.grouper.abac.GrouperAbac;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.provider.SubjectImpl;

public enum ScriptType {
  
  PROVISIONING_GROUP_TRANSLATION {

    @Override
    public Class<? extends ScriptExample> retrieveScriptExampleForType() {
      return ScriptExampleForProvisioningGroupTranslation.class;
    }

    @Override
    public Object runJexl(Map<String, Object> elVariableMap, String jexlScript) {
      return GrouperProvisioningTranslator.runScriptStatic(jexlScript, elVariableMap);
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
  },
  ABAC {
    
    @Override
    public Class<? extends ScriptExample> retrieveScriptExampleForType() {
      return ScriptExampleForAbacTranslation.class;
    }

    @Override
    public Object runJexl(Map<String, Object> elVariableMap, String jexlScript) {
      return GrouperAbac.runScriptStatic(jexlScript, elVariableMap);
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
    
  };
  
  /**
   * @return
   */
  public abstract Class<? extends ScriptExample> retrieveScriptExampleForType();
  
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
