/*
 * @author mchyzer
 * $Id: ChangeLogTypeIdentifier.java,v 1.1 2009-05-08 05:28:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;


/**
 * interface which could be on the enum, or class, or whatever.
 * generally you will use the enum AuditTypeBuiltin
 */
public interface ChangeLogTypeIdentifier {

  /**
   * get the audit category
   * @return the id
   */
  public String getChangeLogCategory();

  /**
   * get the action name of the audit type
   * @return the name
   */
  public String getActionName();
  
}
