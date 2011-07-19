package edu.internet2.middleware.grouper.permissions.limits;

import java.io.Serializable;
import java.util.List;

/**
 * key to the nav.properties, and values for args
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class PermissionLimitDocumentation implements Serializable {

  /**
   * 
   */
  public PermissionLimitDocumentation() {    
  }

  /**
   * construct with key
   * @param theDocumentationKey
   */
  public PermissionLimitDocumentation(String theDocumentationKey) {
    this.documentationKey = theDocumentationKey;
  }
  
  /** documentation key in nav.properties */
  private String documentationKey;
  
  /** args for {0}, {1}, etc in the documentation value */
  private List<String> args;

  /**
   * documentation key in nav.properties
   * @return documentation key
   */
  public String getDocumentationKey() {
    return this.documentationKey;
  }

  /**
   * documentation key in nav.properties
   * @param documentationKey1
   */
  public void setDocumentationKey(String documentationKey1) {
    this.documentationKey = documentationKey1;
  }

  /**
   * args for {0}, {1}, etc in the documentation value
   * @return args for {0}, {1}, etc in the documentation value
   */
  public List<String> getArgs() {
    return this.args;
  }

  /**
   * args for {0}, {1}, etc in the documentation value
   * @param args1
   */
  public void setArgs(List<String> args1) {
    this.args = args1;
  }
  
  
  
}
