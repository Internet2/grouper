package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GshTemplateRequireGroupPrivilege {

  admin {

    @Override
    public Privilege getPrivilege() {
      return AccessPrivilege.ADMIN;
    }
  }, 
  
  read {

    @Override
    public Privilege getPrivilege() {
      return AccessPrivilege.READ;
    }
  }, 
  
  update {

    @Override
    public Privilege getPrivilege() {
      return AccessPrivilege.UPDATE;
    }
  }, 
  
  optin {

    @Override
    public Privilege getPrivilege() {
      return AccessPrivilege.OPTIN;
    }
  }, 
  
  optout {

    @Override
    public Privilege getPrivilege() {
      return AccessPrivilege.OPTOUT;
    }
  }, 
  
  view {

    @Override
    public Privilege getPrivilege() {
      return AccessPrivilege.VIEW;
    }
  }, 
  
  groupAttrRead {

    @Override
    public Privilege getPrivilege() {
      return AccessPrivilege.GROUP_ATTR_READ;
    }
  }, 
  
  groupAttrUpdate {

    @Override
    public Privilege getPrivilege() {
      return AccessPrivilege.GROUP_ATTR_UPDATE;
    }
  };
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   */
  public static GshTemplateRequireGroupPrivilege valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GshTemplateRequireGroupPrivilege.class, string, exceptionOnNotFound);
  }
  
  public abstract Privilege getPrivilege();
  
}
