package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystemAttribute.GrouperExternalSystemAttributeFormElement;
import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystemAttribute.GrouperExternalSystemAttributeType;

public class LdapGrouperExternalSystem extends GrouperExternalSystem {
  
  
  @Override
  public void insertConfig() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean editConfig() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void deleteConfig() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<GrouperExternalSystem> listAllExternalSystemsOfThisType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<GrouperExternalSystemConsumer> retrieveAllUsedBy() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<GrouperExternalSystemAttribute> retrieveAttributes() {
    
    List<GrouperExternalSystemAttribute> grouperExternalSystemAttributes = new ArrayList<GrouperExternalSystemAttribute>();
    
    GrouperExternalSystemAttribute testAttributeOne = new GrouperExternalSystemAttribute();
    testAttributeOne.setConfigSuffix("url");
    testAttributeOne.setExpressionLanguage(false);
    testAttributeOne.setFormElement(GrouperExternalSystemAttributeFormElement.TEXT);
    testAttributeOne.setRequired(true);
    testAttributeOne.setType(GrouperExternalSystemAttributeType.STRING);
    testAttributeOne.setValue("http://www.ldap.edu");
    
    grouperExternalSystemAttributes.add(testAttributeOne);
    
    
    return grouperExternalSystemAttributes;
  }

  @Override
  public String getType() {
    return "Ldap";
  }

}
