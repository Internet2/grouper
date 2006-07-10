/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

package edu.internet2.middleware.grouper;

/**
 * Grouper Error Messages.
 * @author  blair christensen.
 * @version $Id: E.java,v 1.15 2006-07-10 15:18:34 blair Exp $
 * @since   1.0
 */
class E {

  // PROTECTED CLASS CONSTANTS //
  protected static final String ATTR_NULL                 = "null attribute value";
  protected static final String ATTR_COLON                = "value contains colon";
  protected static final String CACHE                     = "cache error: ";
  protected static final String CACHE_INIT                = "unable to get cache manager: ";
  protected static final String CACHE_NOTFOUND            = "cache not found: ";
  protected static final String COMP_CL                   = "left factor is owner";
  protected static final String COMP_CR                   = "right factor is owner";
  protected static final String COMP_L                    = "no composite left factor";
  protected static final String COMP_LC                   = "composite left factor is not a group";
  protected static final String COMP_LR                   = "same left and right composite factors";
  protected static final String COMP_NOTOWNER             = "not a composite owner";
  protected static final String COMP_NULL_LEFT_GROUP      = "composite with null left group: uuid=";
  protected static final String COMP_NULL_OWNER_GROUP     = "composite with null owner group: uuid=";
  protected static final String COMP_NULL_RIGHT_GROUP     = "composite with null right group: uuid=";
  protected static final String COMP_O                    = "no composite owner";
  protected static final String COMP_OC                   = "invalid owner class";
  protected static final String COMP_R                    = "no composite right factor";
  protected static final String COMP_RC                   = "composite right factor is not a group";
  protected static final String COMP_T                    = "no composite type";
  protected static final String COMP_UPDATE               = "unable to update composite membership: ";
  protected static final String COMPF_FINDASFACTOR        = "error retrieving owner group: ";
  protected static final String COMPF_ISFACTOR            = "error determing where object is a factor: ";
  protected static final String CONFIG_READ               = "unable to read grouper configuration file: ";
  protected static final String EVENT_EFFADD              = "unable to log effective membership addition: ";
  protected static final String EVENT_EFFDEL              = "unable to log effective membership deletion";
  protected static final String EVENT_EFFOWNER            = "effective membership owner not found: ";
  protected static final String EVENT_EFFSUBJ             = "effective membership subject not found: ";
  protected static final String FIELD_FINDALL             = "unable to find all fields: ";
  protected static final String FIELD_FINDTYPE            = "unable to find all fields by type: ";
  protected static final String FIELD_NULL                = "null field";
  protected static final String FIELD_REQNOTFOUND         = "required field not found: ";
  protected static final String FIELD_TYPE                = "invalid field type: ";
  protected static final String FILTER_SCOPE              = "class cannot be filtered by scope: ";
  protected static final String GAA_GNF                   = "membership group not found: ";
  protected static final String GF_FBNAME                 = "error finding group by name: ";
  protected static final String GF_FBUUID                 = "error finding group by uuid: ";
  protected static final String GNA_SNF                   = "membership stem not found: ";
  protected static final String GPA_MNF                   = "membership member not found: ";
  protected static final String GROUP_G2M                 = "could not convert group to member: ";
  protected static final String GROUP_G2S                 = "could not convert group to subject: ";
  protected static final String GROUP_ACTC                = "cannot add composite membership to group with composite membership";
  protected static final String GROUP_ACTM                = "cannot add composite membership to group with members";
  protected static final String GROUP_AMTC                = "cannot add member to composite membership";
  protected static final String GROUP_AV                  = "invalid attribute value";
  protected static final String GROUP_COI                 = "cannot OPTIN";
  protected static final String GROUP_COO                 = "cannot OPTOUT";
  protected static final String GROUP_DCFC                = "cannot delete non-existent composite membership";
  protected static final String GROUP_DCFM                = "cannot delete composite membership from group with members";
  protected static final String GROUP_DMFC                = "cannot delete member from composite membership";
  protected static final String GROUP_DRA                 = "cannot delete required attribute: ";
  protected static final String GROUP_GETATTRS            = "error retrieving group attributes: ";
  protected static final String GROUP_GT                  = "invalid group type: ";
  protected static final String GROUP_HEM                 = "error checking for effective membership: ";
  protected static final String GROUP_HIM                 = "error checking for immediate membership: ";
  protected static final String GROUP_HM                  = "error checking for membership: ";
  protected static final String GROUP_NODE                = "group without displayExtension";
  protected static final String GROUP_NODEFAULTLIST       = "'members' list does not exist: ";
  protected static final String GROUP_NODN                = "group without displayName";
  protected static final String GROUP_NOE                 = "group without extension";
  protected static final String GROUP_NON                 = "group without name";
  protected static final String GROUP_NOTFOUND            = "unable to find group";
  protected static final String GROUP_NULL                = "null group";
  protected static final String GROUP_SCHEMA              = "group schema error: ";
  protected static final String GROUP_TYPEADD             = "unable to add type: ";
  protected static final String GROUP_TYPEDEL             = "unable to delete type: ";
  protected static final String GROUPTYPE_ADD             = "unable to add type: ";
  protected static final String GROUPTYPE_DEL             = "unable to delete type: ";
  protected static final String GROUPTYPE_DELINUSE        = "cannot delete group type that is being used";
  protected static final String GROUPTYPE_EXISTS          = "type already exists: ";
  protected static final String GROUPTYPE_FIELDADD        = "unable to add field: ";
  protected static final String GROUPTYPE_FIELDDEL        = "cannot delete field: ";
  protected static final String GROUPTYPE_FIELDNODELINUSE = "cannot delete field that is in use: ";
  protected static final String GROUPTYPE_FIELDNODELMISS  = "type unexpectedly does not have field";
  protected static final String GROUPTYPE_FIELDNODELTYPE  = "cannot delete field of type: ";
  protected static final String GROUPTYPE_FINDALL         = "unable to find group types: ";
  protected static final String GROUPTYPE_INVALID         = "invalid group type: ";
  protected static final String GROUPTYPE_NOADD           = "subject not privileged to add group types";
  protected static final String GROUPTYPE_NODEL           = "subject not privileged to delete group types";
  protected static final String GROUPTYPE_NODELSYS        = "cannot delete system group type: ";
  protected static final String GSA_SEARCH                = "error searching: ";
  protected static final String GSUBJ_NOCREATOR           = "group creator not found: ";
  protected static final String HH_GETPERSISTENT          = "error getting persistent object: ";
  protected static final String HIBERNATE                 = "hibernate error: ";
  protected static final String HIBERNATE_GETPERSISTENT   = "error getting persistent object: ";
  protected static final String HIBERNATE_INIT            = "unable to initialize hibernate: ";
  protected static final String MEMBER_NOGROUP            = "member of group that cannot be found: member=";
  protected static final String MEMBER_NULL               = "null member";
  protected static final String MEMBER_SUBJNOTFOUND       = "unable to find member as subject: ";
  protected static final String MEMBERF_FINDALLMEMBER     = "unable to find ALL subject as member: ";
  protected static final String MOF_CTYPE                 = "invalid composite type: ";
  protected static final String MSF_FINDSUBJECTS          = "error finding subjects: ";
  protected static final String MSF_FINDALLCHILDREN       = "error finding all child memberships: ";
  protected static final String MSF_FINDALLVIA            = "error finding all via memberships: ";
  protected static final String MSV_CIRCULAR              = "cannot create a circular membership";
  protected static final String MSV_NO_PARENT             = "no parent membership";
  protected static final String MSV_TYPE                  = "invalid membership type: ";
  protected static final String NI                        = "NOT IMPLEMENTED: ";
  protected static final String Q_G                       = "getting groups from ";
  protected static final String Q_M                       = "getting members from ";
  protected static final String Q_MS                      = "getting memberships from ";
  protected static final String Q_S                       = "getting stems from ";
  protected static final String RI_IS                     = "unable to install schema: ";
  protected static final String RI_ISG                    = "unable to install base stems and groups: ";
  protected static final String S_NOSTARTROOT             = "unable to start root session: ";
  protected static final String S_GETSUBJECT              = "unable to get subject associated with session: ";
  protected static final String S_START                   = "unable to start session: ";
  protected static final String S_STOP                    = "unable to stop session: ";
  protected static final String SC_NOTFOUND               = "subject cache not found: ";
  protected static final String SF_GETSA                  = "unable to find GrouperSourceAdapter!";
  protected static final String SF_IAS                    = "unable to initialize ALL subject: ";
  protected static final String SF_INIT                   = "failed to initialize source manager: ";
  protected static final String SF_SNF                    = "subject not found: ";
  protected static final String SF_SNU                    = "subject not unique: ";
  protected static final String SETTINGS                  = "unable to retrieve settings: ";
  protected static final String STEM_EXISTS               = "stem already exists: stem=";
  protected static final String STEM_GETCHILDGROUPS       = "error getting child groups: ";
  protected static final String STEM_GETCHILDSTEMS        = "error getting child stems: ";
  protected static final String STEM_NULL                 = "null stem";
  protected static final String STEM_ROOTNOTFOUND         = "unable to find root stem";
  protected static final String STEM_ROOTINSTALL          = "unable to install root stem: ";
  protected static final String STEMF_ISCHILDGROUP        = "could not find parent stem: stem=";
  protected static final String STEMF_ISCHILDSTEM         = "could not find parent stem: stem=";
  protected static final String STEMF_NOTFOUND            = "stem not found: ";
  protected static final String SUBJ_NULL                 = "null subject";
  protected static final String SV_I                      = "null session id";
  protected static final String SV_O                      = "null session object";
  protected static final String SV_M                      = "null session member";
  protected static final String SV_T                      = "null session start time";

} // class E

