/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class RuleVeto extends HookVeto {

  /**
   * @param theReasonKey
   * @param theReason
   */
  public RuleVeto(String theReasonKey, String theReason) {
    super(theReasonKey, theReason);
    
  }

}
