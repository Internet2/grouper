/*
 * @author mchyzer
 * $Id: HookVeto.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * base class for grouper hook veto.  Throw this or a subclass to veto an action (needs
 * to be thrown before a commit)
 */
@SuppressWarnings("serial")
public class HookVeto extends RuntimeException {

  /**
   * key of the reason, e.g. something that could be used in an
   * externalized string file e.g. for the UI.  e.g. hook.veto.group.invalidExtension
   */
  private String reasonKey;
  
  /**
   * default text that would be shown to user if there is no externalized
   * string available.  e.g. 'Invalid ID for group.  ID must be less than 
   * 20 characters.
   */
  private String reason;
  
  /**
   * attributes to put in veto
   */
  private Map<String, Object> attribute;
  
  /**
   * veto type.  this will be assigned automatically if not manually
   */
  private VetoType vetoType = null;
  
  /**
   * veto type.  this will be assigned automatically if not manually
   * @return the vetoType
   */
  public VetoType getVetoType() {
    return this.vetoType;
  }

  
  
  /**
   * veto type.  this will be assigned automatically if not manually
   * @param vetoType1 the vetoType to set
   */
  public void setVetoType(VetoType vetoType1) {
    this.vetoType = vetoType1;
  }

  /**
   * veto type.  this will be assigned automatically if not manually
   * @param vetoType1 the vetoType to set
   * @param overwriteIfExisting true to overwrite if existing
   */
  public void assignVetoType(VetoType vetoType1, boolean overwriteIfExisting) {
    if (overwriteIfExisting || this.vetoType == null ) {
      this.vetoType = vetoType1;
    }
  }

  /**
   * get the value of a key or null if not there
   * @param key
   * @return the value or null if not there
   */
  public Object getAttribute(String key) {
    if (this.attribute == null) {
      return null;
    }
    return this.attribute.get(key);
  }
  
  /**
   * put an attribute
   * @param key
   * @param value
   */
  public void putAttribute(String key, Object value) {
    if (this.attribute == null) {
      this.attribute = new HashMap<String, Object>();
    }
    this.attribute.put(key, value);
  }
  
  /**
   * return the keyset of attributes, but never return null.
   * @return the keyset
   */
  public Set<String> attributeKeySet() {
    if (this.attribute == null) {
      return new HashSet<String>();
    }
    return this.attribute.keySet();
  }
  
  /**
   * construct a veto
   * @param theReasonKey key of the reason, e.g. something that could be used in an
   * externalized string file e.g. for the UI.  e.g. hook.veto.group.invalidExtension
   * @param theReason default text that would be shown to user if there is no externalized
   * string available.  e.g. 'Invalid ID for group.  ID must be less than 
   * 20 characters.
   * 
   */
  public HookVeto(String theReasonKey, String theReason) {
    this.reason = theReason;
    this.reasonKey = theReasonKey;
  }

  
  /**
   * key of the reason, e.g. something that could be used in an
   * externalized string file e.g. for the UI.  e.g. hook.veto.group.invalidExtension
   * @return the reasonKey
   */
  public String getReasonKey() {
    return this.reasonKey;
  }

  
  /**
   * default text that would be shown to user if there is no externalized
   * string available.  e.g. 'Invalid ID for group.  ID must be less than 
   * 20 characters.
   * @return the reason
   */
  public String getReason() {
    return this.reason;
  }
}
