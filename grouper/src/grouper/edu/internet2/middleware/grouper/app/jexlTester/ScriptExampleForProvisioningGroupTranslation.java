package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForProvisioningGroupTranslation implements ScriptExample {
  
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
  },
  
  CHECK_METADATA_BOOLEAN {

    @Override
    public Object expectedOutput() {
      return "MailingList";
    }
  },
  
  CHECK_IF_METADATA_EXISTS {

    @Override
    public Object expectedOutput() {
      return "M365";
    }
  },
  
  PART_OF_GROUP_NAME {

    @Override
    public Object expectedOutput() {
      return "MyApplication";
    }
  };


  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.PROVISIONING_GROUP_TRANSLATION;
  }
  
  @Override
  public abstract Object expectedOutput();

}
