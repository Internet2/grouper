/*******************************************************************************
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
 ******************************************************************************/
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.misc;

/**
 * Grouper Error Messages.
 * @author  blair christensen.
 * @version $Id: E.java,v 1.6 2009-09-21 06:14:27 mchyzer Exp $
 * @since   1.0
 */
public class E {

  /** */
  public static final String CANNOT_ATTR_ADMIN              = "subject cannot ATTR_ADMIN";

  /** */
  public static final String CANNOT_ATTR_UPDATE              = "subject cannot ATTR_UPDATE";

  /** */
  public static final String CANNOT_ATTR_VIEW              = "subject cannot ATTR_VIEW";

  /** */
  public static final String CANNOT_ATTR_READ              = "subject cannot ATTR_READ";

  /** */
  public static final String CANNOT_ATTR_OPTIN              = "subject cannot ATTR_OPTIN";

  /** */
  public static final String CANNOT_ATTR_OPTOUT              = "subject cannot ATTR_OPTOUT";


  
  public static final String CANNOT_ADMIN              = "subject cannot ADMIN";
  public static final String CANNOT_CREATE             = "subject cannot CREATE";
  public static final String CANNOT_CREATE_GROUP       = "cannot create child group: ";
  public static final String CANNOT_CREATE_STEM        = "cannot create child stem: ";
  public static final String CANNOT_OPTIN              = "subject cannot OPTIN";
  public static final String CANNOT_OPTOUT             = "subject cannot OPTOUT";
  public static final String CANNOT_REALIZE_INTERFACE  = "cannot realize interface ";
  public static final String CANNOT_SET_SUBJECTID      = "subject cannot setSubjectId()";
  public static final String CANNOT_STEM               = "subject cannot STEM";
  public static final String CANNOT_UPDATE             = "subject cannot UPDATE";
  public static final String CANNOT_VIEW               = "subject cannot VIEW";
  public static final String CANNOT_READ               = "subject cannot READ";
  public static final String ELEMENT_NOT_UNIQUE        = "element is not unique: ";
  public static final String GROUP_DOES_NOT_HAVE_TYPE  = "group does not have group type: ";
  public static final String INVALID_ATTR_NAME         = "invalid attribute name: ";
  public static final String INVALID_ATTR_VALUE        = "invalid attribute value: ";
  public static final String INVALID_DOC               = "null document";
  public static final String INVALID_GROUP_TYPE        = "invalid group type: ";
  public static final String INVALID_GROUP_UUID        = "invalid group uuid: ";
  public static final String MEMBER_NEITHER_FOUND_NOR_CREATED  = "member neither found nor created: ";
  public static final String NO_STEM                   = "could not find stem";
  public static final String NO_STEM_NAME              = "could not find stem by name: ";
  public static final String NO_STEM_UUID              = "could not find stem by uuid: ";
  public static final String ROOTLIKE_TO_ADD_HSUBJ     = "must be root-like subject to add HibernateSubject";
  public static final String SUBJ_ALREADY_EXISTS       = "subject already exists: ";
  public static final String SYSTEM_MAINTAINED         = "system maintained: ";
  public static final String UNKNOWN_PRIVILEGE         = "unknown privilege: ";

  public static final String ATTR_NULL                 = "null attribute value";
  public static final String ATTR_COLON                = "value contains colon";
  public static final String CACHE                     = "cache error: ";
  public static final String CACHE_INIT                = "unable to get cache manager: ";
  public static final String CACHE_NOTFOUND            = "cache not found: ";
  public static final String COMP_L                    = "no composite left factor";
  public static final String COMP_LC                   = "composite left factor is not a group";
  public static final String COMP_NOTOWNER             = "not a composite owner";
  public static final String COMP_NULL_LEFT_GROUP      = "composite with null left group: uuid=";
  public static final String COMP_NULL_OWNER_GROUP     = "composite with null owner group: uuid=";
  public static final String COMP_NULL_RIGHT_GROUP     = "composite with null right group: uuid=";
  public static final String COMP_O                    = "no composite owner";
  public static final String COMP_OC                   = "invalid owner class";
  public static final String COMP_R                    = "no composite right factor";
  public static final String COMP_RC                   = "composite right factor is not a group";
  public static final String COMP_UPDATE               = "unable to update composite membership: ";
  public static final String COMPF_FINDASFACTOR        = "error retrieving owner group: ";
  public static final String COMPF_ISFACTOR            = "error determing where object is a factor: ";
  public static final String CONFIG_READ               = "unable to read grouper configuration file: ";
  public static final String CONFIG_READ_HIBERNATE     = "unable to read hibernate configuration file: ";
  public static final String EVENT_EFFADD              = "unable to log effective membership addition: ";
  public static final String EVENT_EFFDEL              = "unable to log effective membership deletion";
  public static final String EVENT_EFFOWNER            = "effective membership owner not found: ";
  public static final String EVENT_EFFSUBJ             = "effective membership subject not found: ";
  public static final String FIELD_ALREADY_EXISTS      = "field already exists: ";
  public static final String FIELD_FINDALL             = "unable to find all fields: ";
  public static final String FIELD_FINDTYPE            = "unable to find all fields by type: ";
  public static final String FIELD_NULL                = "null field";
  public static final String FIELD_REQNOTFOUND         = "required field not found: ";
  public static final String FIELD_INVALID_TYPE        = "invalid field type: ";
  public static final String FIELD_DOES_NOT_BELONG_TO_TYPE = "field does not belong to this group type: ";
  public static final String FIELD_READ_PRIV_NOT_ACCESS    = "read privilege is not an access privilege: ";
  public static final String FIELD_WRITE_PRIV_NOT_ACCESS   = "write privilege is not an access privilege: ";
  public static final String FILTER_SCOPE              = "class cannot be filtered by scope: ";
  public static final String GAA_GNF                   = "membership group not found: ";
  public static final String GF_FBNAME                 = "error finding group by name: ";
  public static final String GF_FBUUID                 = "error finding group by uuid: ";
  public static final String GNA_SNF                   = "membership stem not found: ";
  public static final String GPA_MNF                   = "membership member not found: ";
  public static final String GROUP_G2M                 = "could not convert group to member: ";
  public static final String GROUP_G2S                 = "should never happen: could not convert group to subject: ";
  public static final String GROUP_ACTC                = "cannot add composite membership to group with composite membership";
  public static final String GROUP_ACTM                = "cannot add composite membership to group with members";
  public static final String GROUP_AMTC                = "cannot add member to composite membership";
  public static final String GROUP_AV                  = "invalid attribute value";
  public static final String GROUP_COI                 = "cannot OPTIN";
  public static final String GROUP_COO                 = "cannot OPTOUT";
  public static final String GROUP_DCFC                = "cannot delete non-existent composite membership";
  public static final String GROUP_DCFM                = "cannot delete composite membership from group with members";
  public static final String GROUP_DMFC                = "cannot delete member from composite membership";
  public static final String GROUP_DRA                 = "cannot delete required attribute: ";
  public static final String GROUP_GETATTRS            = "error retrieving group attributes: ";
  public static final String GROUP_HAS_TYPE            = "group already has type";
  public static final String GROUP_HEM                 = "error checking for effective membership: ";
  public static final String GROUP_HIM                 = "error checking for immediate membership: ";
  public static final String GROUP_HM                  = "error checking for membership: ";
  public static final String GROUP_NODE                = "group without displayExtension";
  public static final String GROUP_NODEFAULTLIST       = "'members' list does not exist: ";
  public static final String GROUP_NODN                = "group without displayName";
  public static final String GROUP_NOE                 = "group without extension";
  public static final String GROUP_NON                 = "group without name";
  public static final String GROUP_NOTFOUND            = "unable to find group";
  public static final String GROUP_NULL                = "null group";
  public static final String GROUP_SCHEMA              = "group schema error: ";
  public static final String GROUP_TYPEADD             = "unable to add type: ";
  public static final String GROUP_TYPEDEL             = "unable to delete type: ";
  public static final String GROUPTYPE_ADD             = "unable to add type: ";
  public static final String GROUPTYPE_CANNOT_MODIFY_TYPE  = "not privileged to modify fields on this group type";
  public static final String GROUPTYPE_CANNOT_MODIFY_SYSTEM_TYPES  = "cannot modify fields on a system-maintained group type";
  public static final String GROUPTYPE_DEL             = "unable to delete type: ";
  public static final String GROUPTYPE_DELINUSE        = "cannot delete group type that is being used";
  public static final String GROUPTYPE_EXISTS          = "type already exists: ";
  public static final String GROUPTYPE_FIELDADD        = "unable to add field: ";
  public static final String GROUPTYPE_FIELDDEL        = "cannot delete field: ";
  public static final String GROUPTYPE_FIELDNODELINUSE = "cannot delete field that is in use: ";
  public static final String GROUPTYPE_FIELDNODELMISS  = "type unexpectedly does not have field";
  public static final String GROUPTYPE_FIELDNODELTYPE  = "cannot delete field of type: ";
  public static final String GROUPTYPE_FINDALL         = "unable to find group types: ";
  public static final String GROUPTYPE_NODEL           = "subject not privileged to delete group types";
  public static final String GROUPTYPE_NODELSYS        = "cannot delete system group type: ";
  public static final String GSA_SEARCH                = "error searching: ";
  public static final String GSUBJ_NOCREATOR           = "group creator not found: ";
  public static final String HH_GETPERSISTENT          = "error getting persistent object: ";
  public static final String HIBERNATE                 = "hibernate error: ";
  public static final String HIBERNATE_COMMIT          = "hibernate commit error: ";
  public static final String HIBERNATE_GETPERSISTENT   = "error getting persistent object: ";
  public static final String MEMBER_NOGROUP            = "member of group that cannot be found: member=";
  public static final String MEMBER_NULL               = "null member";
  public static final String MEMBER_SUBJNOTFOUND       = "unable to find member as subject: ";
  public static final String MEMBERF_FINDALLMEMBER     = "unable to find ALL subject as member: ";
  public static final String MOF_CTYPE                 = "invalid composite type: ";
  public static final String MSF_FINDSUBJECTS          = "error finding subjects: ";
  public static final String MSF_FINDALLCHILDREN       = "error finding all child memberships: ";
  public static final String MSF_FINDALLVIA            = "error finding all via memberships: ";
  public static final String MSV_CIRCULAR              = "cannot create a circular membership";
  public static final String MSV_NO_PARENT             = "no parent membership";
  public static final String MSV_TYPE                  = "invalid membership type: ";
  public static final String NO_CHANGE_SUBJID          = "not privileged to change subjectId";
  public static final String NO_WHEEL_GROUP            = "disabling wheel group.  enabled but found found: ";
  public static final String NI                        = "NOT IMPLEMENTED: ";
  public static final String Q_G                       = "getting groups from ";
  public static final String Q_M                       = "getting members from ";
  public static final String Q_MS                      = "getting memberships from ";
  public static final String Q_S                       = "getting stems from ";
  public static final String RI_IS                     = "unable to install schema: ";
  public static final String RI_ISG                    = "unable to install base stems and groups: ";
  public static final String S_NOSTARTROOT             = "unable to start root session: ";
  public static final String S_START                   = "unable to start session: ";
  public static final String S_STOP                    = "unable to stop session: ";
  public static final String SC_NOTFOUND               = "subject cache not found: ";
  public static final String SF_GETSA                  = "unable to find GrouperSourceAdapter!";
  public static final String SF_IAS                    = "unable to initialize ALL subject: ";
  public static final String SF_INIT                   = "failed to initialize source manager: ";
  public static final String SF_ROOT_SUBJECT_NOT_FOUND = "failed to initialize ROOT subject: ";
  public static final String SF_SNF                    = "subject not found: ";
  public static final String SF_SNU                    = "subject not unique: ";
  public static final String SETTINGS                  = "unable to retrieve settings: ";
  public static final String STEM_EXISTS               = "stem already exists: stem=";
  public static final String STEM_GETCHILDGROUPS       = "error getting child groups: ";
  public static final String STEM_GETCHILDSTEMS        = "error getting child stems: ";
  public static final String STEM_NULL                 = "null stem";
  public static final String STEM_ROOTNOTFOUND         = "unable to find root stem";
  public static final String STEM_ROOTINSTALL          = "unable to install root stem: ";
  public static final String STEMF_ISCHILDGROUP        = "could not find parent stem: stem=";
  public static final String STEMF_ISCHILDSTEM         = "could not find parent stem: stem=";
  public static final String SUBJ_NULL                 = "null subject";
  public static final String SV_I                      = "null session id";
  public static final String SV_O                      = "null session object";
  public static final String SV_M                      = "null session member";
  public static final String SV_T                      = "null session start time";

  // From `MembershipValidator`
  public static final String ERR_D   = "membership has invalid depth: ";
  public static final String ERR_EV  = "effective membership has no via";
  public static final String ERR_FT  = "membership has invalid field type: ";
  public static final String ERR_IV  = "immediate membership has via";
  public static final String ERR_M   = "membership has null member";
  public static final String ERR_MAE = "membership already exists";
  public static final String ERR_O   = "membership has null owner";
  public static final String ERR_OC  = "membership has invalid owner class: ";
  public static final String ERR_PMS = "immediate membership has parent membership";
  public static final String ERR_V   = "membership has null via";
  public static final String ERR_VC  = "membership has invalid via class: ";
  
} // class E

