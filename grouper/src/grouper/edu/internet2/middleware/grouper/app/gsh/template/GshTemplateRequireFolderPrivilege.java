package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GshTemplateRequireFolderPrivilege {

  admin {

    @Override
    public Privilege getPrivilege() {
      return NamingPrivilege.STEM_ADMIN;
    }
  }, 
  
  create {

    @Override
    public Privilege getPrivilege() {
      return NamingPrivilege.CREATE;
    }
  }, 
  
  stemAttrRead {

    @Override
    public Privilege getPrivilege() {
      return NamingPrivilege.STEM_ATTR_READ;
    }
  }, 
  
  stemAttrUpdate {

    @Override
    public Privilege getPrivilege() {
      return NamingPrivilege.STEM_ATTR_UPDATE;
    }
    
  };
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   */
  public static GshTemplateRequireFolderPrivilege valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GshTemplateRequireFolderPrivilege.class, string, exceptionOnNotFound);
  }
  
  
  public abstract Privilege getPrivilege();
  
}
