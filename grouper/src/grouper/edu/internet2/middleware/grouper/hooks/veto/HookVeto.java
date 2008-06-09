/*
 * @author mchyzer
 * $Id: HookVeto.java,v 1.1.2.1 2008-06-09 05:52:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.veto;


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
