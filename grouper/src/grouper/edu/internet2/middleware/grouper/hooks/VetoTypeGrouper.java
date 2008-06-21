/*
 * @author mchyzer
 * $Id: VetoTypeGrouper.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
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
