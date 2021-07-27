package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;

public enum GrouperProvisioningMembershipFieldType {

  members {

    @Override
    public String getFieldId() {
      return Group.getDefaultList().getId();
    }

    @Override
    public String getFieldName() {
      return Group.getDefaultList().getName();
    }
  },
  
  readAdmin {

    @Override
    public String getFieldId() {
      return AccessPrivilege.READ.getField().getId();
    }

    @Override
    public String getFieldName() {
      return AccessPrivilege.READ.getField().getName();
    }
  },
  
  updateAdmin {

    @Override
    public String getFieldId() {
      return AccessPrivilege.UPDATE.getField().getId();
    }

    @Override
    public String getFieldName() {
      return AccessPrivilege.UPDATE.getField().getName();
    }
  },
  
  admin {

    @Override
    public String getFieldId() {
      return AccessPrivilege.ADMIN.getField().getId();
    }

    @Override
    public String getFieldName() {
      return AccessPrivilege.ADMIN.getField().getName();
    }
  };
  
  public abstract String getFieldId(); 
  public abstract String getFieldName(); 

}
