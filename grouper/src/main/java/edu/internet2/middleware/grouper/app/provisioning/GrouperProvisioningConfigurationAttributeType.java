package edu.internet2.middleware.grouper.app.provisioning;

/**
 * is this attribute for groups, entities, or memberships
 * @author mchyzer
 *
 */
public enum GrouperProvisioningConfigurationAttributeType {

  group {

    @Override
    public String getConfigPrefix() {
      return "targetGroupAttribute";
    }
  },
  
  entity {

    @Override
    public String getConfigPrefix() {
      return "targetEntityAttribute";
    }
  },
  
  membership {

    @Override
    public String getConfigPrefix() {
      return "targetMembershipAttribute";
    }
  };
  
  public abstract String getConfigPrefix();
  
}

// 