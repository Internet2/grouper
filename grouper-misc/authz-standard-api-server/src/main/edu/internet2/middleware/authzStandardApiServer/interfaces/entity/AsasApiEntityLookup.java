package edu.internet2.middleware.authzStandardApiServer.interfaces.entity;

/**
 * lookup entity lookup
 * @author mchyzer
 *
 */
public class AsasApiEntityLookup {

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
