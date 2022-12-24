package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForProvisioningGroupTranslation implements ScriptExample {
  
<<<<<<< GROUPER_5_BRANCH
  GENERIC {

    @Override
    public Object expectedOutput() {
      return "test:testGroup_1234567";
    }
  },
  
  REVERSE_STRING {

    @Override
    public Object expectedOutput() {
      return "historyDepartmentProfessors.groupsForActiveDirectory.policyGroup";
    }
  },
  
  LDAP_DN_TRANSLATION {

    @Override
    public Object expectedOutput() {
      return "cn=admins,ou=users,ou=wiki,DC=activeDirectory,DC=school,DC=edu";
    }
  },
  
  METADATA_OVERRIDE {

    @Override
    public Object expectedOutput() {
      return "true";
    }
  };


  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.PROVISIONING_GROUP_TRANSLATION;
  }
  
  @Override
  public abstract Object expectedOutput();
=======
  GENERIC,
  
  REVERSE_STRING,
  
  LDAP_DN_TRANSLATION,
  
  METADATA_OVERRIDE;


  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.PROVISIONING_GROUP_TRANSLATION;
  }
>>>>>>> 9a44390 Add jexl script tester

}
