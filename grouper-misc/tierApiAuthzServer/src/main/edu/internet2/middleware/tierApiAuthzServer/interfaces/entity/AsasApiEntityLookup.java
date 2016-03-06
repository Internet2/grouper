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
  
}
