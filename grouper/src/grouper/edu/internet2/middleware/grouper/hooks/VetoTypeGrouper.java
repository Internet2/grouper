/*
 * @author mchyzer
 * $Id: VetoTypeGrouper.java,v 1.1.2.1 2008-06-12 05:44:59 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;


/**
 * built in veto types
 */
public enum VetoTypeGrouper implements VetoType {

  /** veto of group insert */
  GROUP_PRE_INSERT,
  
  /** membership pre insert */
  MEMBERSHIP_PRE_INSERT;
}
