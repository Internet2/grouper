/*
 * @author mchyzer
 * $Id: AuditControl.java,v 1.1 2009-02-09 05:33:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;


/**
 *
 */
public enum AuditControl {

  /** will audit this call (or will defer to outside context if auditing */
  WILL_AUDIT, 
  
  /** will not audit */
  WILL_NOT_AUDIT;
  
}
