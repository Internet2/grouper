/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: VetoTypeGrouper.java,v 1.6 2009-04-28 20:08:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;



/**
 * built in veto types
 */
public enum VetoTypeGrouper implements VetoType {

  /** veto of external subject insert/update */
  EXTERNAL_SUBJECT_POST_EDIT,
  
  /** veto of attribute */
  ATTRIBUTE_PRE_INSERT,
  
  /** veto of attribute */
  ATTRIBUTE_POST_INSERT,
  
  /** veto of attribute */
  ATTRIBUTE_PRE_UPDATE,
  
  /** veto of attribute */
  ATTRIBUTE_POST_UPDATE,
  
  /** veto of attribute */
  ATTRIBUTE_PRE_DELETE,
  
  /** veto of attribute */
  ATTRIBUTE_POST_DELETE,

  
  /** veto of attribute assign */
  ATTRIBUTE_ASSIGN_PRE_INSERT,
  
  /** veto of attribute assign */
  ATTRIBUTE_ASSIGN_POST_INSERT,
  
  /** veto of attribute assign */
  ATTRIBUTE_ASSIGN_PRE_UPDATE,
  
  /** veto of attribute assign */
  ATTRIBUTE_ASSIGN_POST_UPDATE,
  
  /** veto of attribute assign */
  ATTRIBUTE_ASSIGN_PRE_DELETE,
  
  /** veto of attribute assign */
  ATTRIBUTE_ASSIGN_POST_DELETE,
  
  
  /** veto of attribute assign value */
  ATTRIBUTE_ASSIGN_VALUE_PRE_INSERT,
  
  /** veto of attribute assign value */
  ATTRIBUTE_ASSIGN_VALUE_POST_INSERT,
  
  /** veto of attribute assign value */
  ATTRIBUTE_ASSIGN_VALUE_PRE_UPDATE,
  
  /** veto of attribute assign value */
  ATTRIBUTE_ASSIGN_VALUE_POST_UPDATE,
  
  /** veto of attribute assign value */
  ATTRIBUTE_ASSIGN_VALUE_PRE_DELETE,
  
  /** veto of attribute assign value */
  ATTRIBUTE_ASSIGN_VALUE_POST_DELETE,
  
  
  /** veto of attribute */
  ATTRIBUTE_DEF_PRE_INSERT,
  
  /** veto of attribute */
  ATTRIBUTE_DEF_POST_INSERT,
  
  /** veto of attribute */
  ATTRIBUTE_DEF_PRE_UPDATE,
  
  /** veto of attribute */
  ATTRIBUTE_DEF_POST_UPDATE,
  
  /** veto of attribute */
  ATTRIBUTE_DEF_PRE_DELETE,
  
  /** veto of attribute */
  ATTRIBUTE_DEF_POST_DELETE,
  

  
  /** veto of attribute */
  ATTRIBUTE_DEF_NAME_PRE_INSERT,
  
  /** veto of attribute */
  ATTRIBUTE_DEF_NAME_POST_INSERT,
  
  /** veto of attribute */
  ATTRIBUTE_DEF_NAME_PRE_UPDATE,
  
  /** veto of attribute */
  ATTRIBUTE_DEF_NAME_POST_UPDATE,
  
  /** veto of attribute */
  ATTRIBUTE_DEF_NAME_PRE_DELETE,
  
  /** veto of attribute */
  ATTRIBUTE_DEF_NAME_POST_DELETE,
  

  
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
  
  /** veto hook */
  LOADER_PRE_RUN,
  
  /** veto hook */
  LOADER_POST_RUN,
  
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
