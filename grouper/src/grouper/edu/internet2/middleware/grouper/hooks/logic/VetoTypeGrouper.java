/*
 * @author mchyzer
 * $Id: VetoTypeGrouper.java,v 1.1 2008-06-26 11:16:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;



/**
 * built in veto types
 */
public enum VetoTypeGrouper implements VetoType {

  /** veto of group */
  GROUP_PRE_INSERT,
  
  /** veto of group */
  GROUP_POST_INSERT,
  
  /** veto of group */
  GROUP_PRE_UPDATE,
  
  /** veto of group */
  GROUP_POST_UPDATE,
  
  /** veto of group */
  GROUP_PRE_DELETE,
  
  /** veto of group */
  GROUP_POST_DELETE,
  
  
  
  /** veto of membership */
  MEMBERSHIP_PRE_ADD_MEMBER,
  
  /** veto of membership */
  MEMBERSHIP_POST_ADD_MEMBER,
  
  /** veto of membership */
  MEMBERSHIP_PRE_INSERT,
  
  /** veto of membership */
  MEMBERSHIP_POST_INSERT,
  
  /** veto of membership */
  MEMBERSHIP_PRE_UPDATE,
  
  /** veto of membership */
  MEMBERSHIP_POST_UPDATE,
  
  /** veto of membership */
  MEMBERSHIP_PRE_DELETE,
  
  /** veto of membership */
  MEMBERSHIP_POST_DELETE;
  
}
