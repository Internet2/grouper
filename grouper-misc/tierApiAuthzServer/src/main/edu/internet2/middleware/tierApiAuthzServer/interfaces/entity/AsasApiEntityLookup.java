package edu.internet2.middleware.tierApiAuthzServer.interfaces.entity;

import edu.internet2.middleware.tierApiAuthzServer.j2ee.TaasFilterJ2ee;

/**
 * lookup entity lookup
 * @author mchyzer
 *
 */
public class AsasApiEntityLookup {

  /**
   * logged in entity
   * @return the lookup
   */
  public static AsasApiEntityLookup retrieveLoggedInUser() {

    AsasApiEntityLookup asasApiEntityLookup = new AsasApiEntityLookup();
    String lookupString = TaasFilterJ2ee.retrieveUserPrincipalNameFromRequest();
    asasApiEntityLookup.setLookupString(lookupString);
    return asasApiEntityLookup;

  }

  /** string to lookup entity */
  private String lookupString;

  /** handle name of a way to refer to an entity */
  private String handleName;
  
  /** handle value of a way to refer to an entity */
  private String handleValue;

  /**
   * string to lookup entity
   * @return the lookupString
   */
  public String getLookupString() {
    return this.lookupString;
  }

  /**
   * string to lookup entity
   * @param lookupString1 the lookupString to set
   */
  public void setLookupString(String lookupString1) {
    this.lookupString = lookupString1;
  }

  /**
   * handle name of a way to refer to an entity
   * @return the handleName
   */
  public String getHandleName() {
    return handleName;
  }

  /**
   * handle value of a way to refer to an entity
   * @return the handleValue
   */
  public String getHandleValue() {
    return handleValue;
  }

  /**
   * handle name of a way to refer to an entity
   * @param handleName the handleName to set
   */
  public void setHandleName(String handleName) {
    this.handleName = handleName;
  }

  /**
   * handle value of a way to refer to an entity
   * @param handleValue the handleValue to set
   */
  public void setHandleValue(String handleValue) {
    this.handleValue = handleValue;
  }
  
}
