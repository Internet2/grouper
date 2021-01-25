package edu.internet2.middleware.grouper.app.ldapProvisioning;

/**
 * @author shilen
 */
public enum LdapSyncGroupDnType {

  /**
   * 
   */
  bushy {

    @Override
    public String convertNameToDn(String name) {
      // TODO Auto-generated method stub
      return null;
    }
  },
  
  /**
   * 
   */
  flat {

    @Override
    public String convertNameToDn(String name) {
      // TODO Auto-generated method stub
      return null;
    }
  };
  
  public abstract String convertNameToDn(String name);
  
}
