/*
 * @author mchyzer
 * $Id: VetoTypeGrouper.java,v 1.4 2008-10-17 12:06:37 mchyzer Exp $
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
  


  /** veto of stem */
  STEM_PRE_INSERT,
  
  /** veto of stem */
  STEM_POST_INSERT,
  
  /** veto of stem */
  STEM_PRE_UPDATE,
  
  /** veto of stem */
  STEM_POST_UPDATE,
  
  /** veto of stem */
  STEM_PRE_DELETE,
  
  /** veto of stem */
  STEM_POST_DELETE,
  

  /** veto of composite */
  COMPOSITE_PRE_INSERT,
  
  /** veto of composite */
  COMPOSITE_POST_INSERT,
  
  /** veto of composite */
  COMPOSITE_PRE_UPDATE,
  
  /** veto of composite */
  COMPOSITE_POST_UPDATE,
  
  /** veto of composite */
  COMPOSITE_PRE_DELETE,
  
  /** veto of composite */
  COMPOSITE_POST_DELETE,

  
  
  /** veto of field */
  FIELD_PRE_INSERT,
  
  /** veto of field */
  FIELD_POST_INSERT,
  
  /** veto of field */
  FIELD_PRE_UPDATE,
  
  /** veto of field */
  FIELD_POST_UPDATE,
  
  /** veto of field */
  FIELD_PRE_DELETE,
  
  /** veto of field */
  FIELD_POST_DELETE,

  

  /** veto of groupType */
  GROUP_TYPE_PRE_INSERT,
  
  /** veto of groupType */
  GROUP_TYPE_POST_INSERT,
  
  /** veto of groupType */
  GROUP_TYPE_PRE_UPDATE,
  
  /** veto of groupType */
  GROUP_TYPE_POST_UPDATE,
  
  /** veto of groupType */
  GROUP_TYPE_PRE_DELETE,
  
  /** veto of groupType */
  GROUP_TYPE_POST_DELETE,
  
  
  /** veto of groupTypeTuple */
  GROUP_TYPE_TUPLE_PRE_INSERT,
  
  /** veto of groupTypeTuple */
  GROUP_TYPE_TUPLE_POST_INSERT,
  
  /** veto of groupTypeTuple */
  GROUP_TYPE_TUPLE_PRE_UPDATE,
  
  /** veto of groupTypeTuple */
  GROUP_TYPE_TUPLE_POST_UPDATE,
  
  /** veto of groupTypeTuple */
  GROUP_TYPE_TUPLE_PRE_DELETE,
  
  /** veto of groupTypeTuple */
  GROUP_TYPE_TUPLE_POST_DELETE,

  
  
  /** veto of grouperSession */
  GROUPER_SESSION_PRE_INSERT,
  
  /** veto of grouperSession */
  GROUPER_SESSION_POST_INSERT,
  
  /** veto of grouperSession */
  GROUPER_SESSION_PRE_UPDATE,
  
  /** veto of grouperSession */
  GROUPER_SESSION_POST_UPDATE,
  
  /** veto of grouperSession */
  GROUPER_SESSION_PRE_DELETE,
  
  /** veto of grouperSession */
  GROUPER_SESSION_POST_DELETE,

  
  /** veto of member */
  MEMBER_PRE_CHANGE_SUBJECT,
  
  /** veto of member */
  MEMBER_POST_CHANGE_SUBJECT,
  
  /** veto of member */
  MEMBER_PRE_INSERT,
  
  /** veto of member */
  MEMBER_POST_INSERT,
  
  /** veto of member */
  MEMBER_PRE_UPDATE,
  
  /** veto of member */
  MEMBER_POST_UPDATE,
  
  /** veto of member */
  MEMBER_PRE_DELETE,
  
  /** veto of member */
  MEMBER_POST_DELETE,
  
  
  
  /** veto of membership */
  MEMBERSHIP_PRE_ADD_MEMBER,
  
  /** veto of membership */
  MEMBERSHIP_POST_ADD_MEMBER,
  
  /** veto of membership */
  MEMBERSHIP_PRE_REMOVE_MEMBER,
  
  /** veto of membership */
  MEMBERSHIP_POST_REMOVE_MEMBER,
  
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
