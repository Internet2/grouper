/*
 * @author mchyzer
 * $Id: HookVetoMembershipUpdate.java,v 1.1.2.1 2008-06-09 19:26:05 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.veto;


/**
 * veto for membership update (high level or low level)
 */
@SuppressWarnings("serial")
public class HookVetoMembershipUpdate extends HookVeto {

  /**
   * construct a veto
   * @param theReasonKey key of the reason, e.g. something that could be used in an
   * externalized string file e.g. for the UI.  e.g. hook.veto.group.invalidExtension
   * @param theReason default text that would be shown to user if there is no externalized
   * string available.  e.g. 'Invalid ID for group.  ID must be less than 
   * 20 characters.
   */
  public HookVetoMembershipUpdate(String theReasonKey, String theReason) {
    super(theReasonKey, theReason);
  }

}
