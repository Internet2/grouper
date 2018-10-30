/**
 * Copyright 2014 Internet2
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
 * $Id: LoaderJobBean.java,v 1.2 2009-04-28 20:08:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for loader job
 */
@GrouperIgnoreDbVersion
public class LoaderJobBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

    /** constant for field name for: andGroups */
    public static final String FIELD_AND_GROUPS = "andGroups";

    /** constant for field name for: attributeDefName */
    public static final String FIELD_ATTRIBUTE_DEF_NAME = "attributeDefName";

    /** constant for field name for: attributeLoaderActionQuery */
    public static final String FIELD_ATTRIBUTE_LOADER_ACTION_QUERY = "attributeLoaderActionQuery";

    /** constant for field name for: attributeLoaderActionSetQuery */
    public static final String FIELD_ATTRIBUTE_LOADER_ACTION_SET_QUERY = "attributeLoaderActionSetQuery";

    /** constant for field name for: attributeLoaderAttrQuery */
    public static final String FIELD_ATTRIBUTE_LOADER_ATTR_QUERY = "attributeLoaderAttrQuery";

    /** constant for field name for: attributeLoaderAttrSetQuery */
    public static final String FIELD_ATTRIBUTE_LOADER_ATTR_SET_QUERY = "attributeLoaderAttrSetQuery";

    /** constant for field name for: attributeLoaderAttrsLike */
    public static final String FIELD_ATTRIBUTE_LOADER_ATTRS_LIKE = "attributeLoaderAttrsLike";

    /** constant for field name for: groupLikeString */
    public static final String FIELD_GROUP_LIKE_STRING = "groupLikeString";

    /** constant for field name for: groupNameOverall */
    public static final String FIELD_GROUP_NAME_OVERALL = "groupNameOverall";

    /** constant for field name for: groupQuery */
    public static final String FIELD_GROUP_QUERY = "groupQuery";

    /** constant for field name for: groupTypes */
    public static final String FIELD_GROUP_TYPES = "groupTypes";

    /** constant for field name for: grouperLoaderDb */
    public static final String FIELD_GROUPER_LOADER_DB = "grouperLoaderDb";

    /** constant for field name for: grouperLoaderType */
    public static final String FIELD_GROUPER_LOADER_TYPE = "grouperLoaderType";

    /** constant for field name for: grouperSession */
    public static final String FIELD_GROUPER_SESSION = "grouperSession";

    /** constant for field name for: hib3GrouploaderLogOverall */
    public static final String FIELD_HIB3_GROUPLOADER_LOG_OVERALL = "hib3GrouploaderLogOverall";

    /** constant for field name for: ldapAttributeFilterExpression */
    public static final String FIELD_LDAP_ATTRIBUTE_FILTER_EXPRESSION = "ldapAttributeFilterExpression";

    /** constant for field name for: ldapExtraAttributes */
    public static final String FIELD_LDAP_EXTRA_ATTRIBUTES = "ldapExtraAttributes";

    /** constant for field name for: ldapFilter */
    public static final String FIELD_LDAP_FILTER = "ldapFilter";

    /** constant for field name for: ldapGroupAttribute */
    public static final String FIELD_LDAP_GROUP_ATTRIBUTE = "ldapGroupAttribute";

    /** constant for field name for: ldapGroupDescriptionExpression */
    public static final String FIELD_LDAP_GROUP_DESCRIPTION_EXPRESSION = "ldapGroupDescriptionExpression";

    /** constant for field name for: ldapGroupDisplayExtensionExpression */
    public static final String FIELD_LDAP_GROUP_DISPLAY_EXTENSION_EXPRESSION = "ldapGroupDisplayExtensionExpression";

    /** constant for field name for: ldapGroupNameExpression */
    public static final String FIELD_LDAP_GROUP_NAME_EXPRESSION = "ldapGroupNameExpression";

    /** constant for field name for: ldapQuartzCron */
    public static final String FIELD_LDAP_QUARTZ_CRON = "ldapQuartzCron";

    /** constant for field name for: ldapSearchDn */
    public static final String FIELD_LDAP_SEARCH_DN = "ldapSearchDn";

    /** constant for field name for: ldapSearchScope */
    public static final String FIELD_LDAP_SEARCH_SCOPE = "ldapSearchScope";

    /** constant for field name for: ldapServerId */
    public static final String FIELD_LDAP_SERVER_ID = "ldapServerId";

    /** constant for field name for: ldapSourceId */
    public static final String FIELD_LDAP_SOURCE_ID = "ldapSourceId";

    /** constant for field name for: ldapSubjectAttribute */
    public static final String FIELD_LDAP_SUBJECT_ATTRIBUTE = "ldapSubjectAttribute";

    /** constant for field name for: ldapSubjectExpression */
    public static final String FIELD_LDAP_SUBJECT_EXPRESSION = "ldapSubjectExpression";

    /** constant for field name for: ldapSubjectIdType */
    public static final String FIELD_LDAP_SUBJECT_ID_TYPE = "ldapSubjectIdType";

    /** constant for field name for: ldapType */
    public static final String FIELD_LDAP_TYPE = "ldapType";

    /** constant for field name for: query */
    public static final String FIELD_QUERY = "query";

    /** constant for field name for: startTime */
    public static final String FIELD_START_TIME = "startTime";

    /**
     * fields which are included in clone method
     */
    private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
        FIELD_AND_GROUPS, FIELD_ATTRIBUTE_DEF_NAME, FIELD_ATTRIBUTE_LOADER_ACTION_QUERY, FIELD_ATTRIBUTE_LOADER_ACTION_SET_QUERY, 
        FIELD_ATTRIBUTE_LOADER_ATTR_QUERY, FIELD_ATTRIBUTE_LOADER_ATTR_SET_QUERY, FIELD_ATTRIBUTE_LOADER_ATTRS_LIKE, FIELD_GROUP_LIKE_STRING, 
        FIELD_GROUP_NAME_OVERALL, FIELD_GROUP_QUERY, FIELD_GROUP_TYPES, FIELD_GROUPER_LOADER_DB, 
        FIELD_GROUPER_LOADER_TYPE, FIELD_GROUPER_SESSION, FIELD_HIB3_GROUPLOADER_LOG_OVERALL, 
        FIELD_LDAP_ATTRIBUTE_FILTER_EXPRESSION, 
        FIELD_LDAP_EXTRA_ATTRIBUTES, FIELD_LDAP_FILTER, FIELD_LDAP_GROUP_ATTRIBUTE, FIELD_LDAP_GROUP_DESCRIPTION_EXPRESSION, 
        FIELD_LDAP_GROUP_DISPLAY_EXTENSION_EXPRESSION, FIELD_LDAP_GROUP_NAME_EXPRESSION, FIELD_LDAP_QUARTZ_CRON, FIELD_LDAP_SEARCH_DN, 
        FIELD_LDAP_SEARCH_SCOPE, FIELD_LDAP_SERVER_ID, FIELD_LDAP_SOURCE_ID, FIELD_LDAP_SUBJECT_ATTRIBUTE, 
        FIELD_LDAP_SUBJECT_EXPRESSION, FIELD_LDAP_SUBJECT_ID_TYPE, FIELD_LDAP_TYPE, FIELD_QUERY, 
        FIELD_START_TIME);

    //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * extension of the attribute def name for type of ldap loader (e.g. LDAP_SIMPLE).
   * Like the SQL loader, this holds the type of job from the GrouperLoaderType enum,
   * currently the only valid values are LDAP_SIMPLE, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES.
   * Simple is a group loaded from LDAP filter which returns subject ids or identifiers.
   * Group list is an LDAP filter which returns group objects, and the group objects
   * have a list of subjects.  Groups from attributes is an LDAP filter that returns
   * subjects which have a multi-valued attribute e.g. affiliations where groups will
   * be created based on subject who have each attribute value
   */
  private String ldapType;
  
  /**
   * extension of the attribute def name for type of ldap loader (e.g. LDAP_SIMPLE).
   * Like the SQL loader, this holds the type of job from the GrouperLoaderType enum,
   * currently the only valid values are LDAP_SIMPLE, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES.
   * Simple is a group loaded from LDAP filter which returns subject ids or identifiers.
   * Group list is an LDAP filter which returns group objects, and the group objects
   * have a list of subjects.  Groups from attributes is an LDAP filter that returns
   * subjects which have a multi-valued attribute e.g. affiliations where groups will
   * be created based on subject who have each attribute value
   * @return type
   */
  public String getLdapType() {
    return this.ldapType;
  }

  /**
   * extension of the attribute def name for type of ldap loader (e.g. LDAP_SIMPLE).
   * Like the SQL loader, this holds the type of job from the GrouperLoaderType enum,
   * currently the only valid values are LDAP_SIMPLE, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES.
   * Simple is a group loaded from LDAP filter which returns subject ids or identifiers.
   * Group list is an LDAP filter which returns group objects, and the group objects
   * have a list of subjects.  Groups from attributes is an LDAP filter that returns
   * subjects which have a multi-valued attribute e.g. affiliations where groups will
   * be created based on subject who have each attribute value
   * @param ldapType1
   */
  public void setLdapType(String ldapType1) {
    this.ldapType = ldapType1;
  }

  /**
   * required for LDAP_GROUPS_FROM_ATTRIBUTES
   * Attribute name of the filter object result that holds the group name 
   */
  private String ldapGroupAttribute;
  
  /**
   * Attribute names (comma separated) to get LDAP data for expressions in group name, 
   * displayExtension, description, optional, for LDAP_GROUP_LIST
   */
  private String ldapExtraAttributes;
  
  /**
   * Attribute names (comma separated) to get LDAP data for expressions in group name, 
   * displayExtension, description, optional, for LDAP_GROUP_LIST
   * @return the ldapExtraAttributes
   */
  public String getLdapExtraAttributes() {
    return this.ldapExtraAttributes;
  }

  /**
   * Attribute names (comma separated) to get LDAP data for expressions in group name, 
   * displayExtension, description, optional, for LDAP_GROUP_LIST
   * @param ldapExtraAttributes1 the ldapExtraAttributes to set
   */
  public void setLdapExtraAttributes(String ldapExtraAttributes1) {
    this.ldapExtraAttributes = ldapExtraAttributes1;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can UPDATE the group memberships
   */
  private String ldapGroupUpdaters;

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can OPTOUT their group membership
   */
  private String ldapGroupOptouts;
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can GROUP_ATTR_READ
   */
  private String ldapGroupAttrReaders;
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can GROUP_ATTR_UPDATE
   */
  private String ldapGroupAttrUpdaters;
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES
   * Class to transform data from ldap for more advanced transformations (e.g. parsing ldap attribute values into multiple groups)
   */
  private String ldapResultsTransformationClass;
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can OPTOUT their group membership
   * @return optouts
   */
  public String getLdapGroupOptouts() {
    return this.ldapGroupOptouts;
  }
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can GROUP_ATTR_READ
   * @return optouts
   */
  public String getLdapGroupAttrReaders() {
    return this.ldapGroupAttrReaders;
  }
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can GROUP_ATTR_UPDATE
   * @return optouts
   */
  public String getLdapGroupAttrUpdaters() {
    return this.ldapGroupAttrUpdaters;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can OPTOUT their group membership
   * @param ldapGroupOptouts1
   */
  public void setLdapGroupOptouts(String ldapGroupOptouts1) {
    this.ldapGroupOptouts = ldapGroupOptouts1;
  }
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can GROUP_ATTR_READ
   * @param ldapGroupAttrReaders1
   */
  public void setLdapGroupAttrReaders(String ldapGroupAttrReaders1) {
    this.ldapGroupAttrReaders = ldapGroupAttrReaders1;
  }
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can GROUP_ATTR_UPDATE
   * @param ldapGroupAttrUpdaters1
   */
  public void setLdapGroupAttrUpdate(String ldapGroupAttrUpdaters1) {
    this.ldapGroupAttrUpdaters = ldapGroupAttrUpdaters1;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can OPTIN their group membership
   */
  private String ldapGroupOptins;
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can OPTIN their group membership
   * @return optins
   */
  public String getLdapGroupOptins() {
    return this.ldapGroupOptins;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can OPTIN their group membership
   * @param ldapGroupOptins1
   */
  public void setLdapGroupOptins(String ldapGroupOptins1) {
    this.ldapGroupOptins = ldapGroupOptins1;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can UPDATE the group memberships
   * @return updaters
   */
  public String getLdapGroupUpdaters() {
    return this.ldapGroupUpdaters;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can UPDATE the group memberships
   * @param ldapGroupUpdaters1
   */
  public void setLdapGroupUpdaters(String ldapGroupUpdaters1) {
    this.ldapGroupUpdaters = ldapGroupUpdaters1;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can ADMIN the group 
   */
  private String ldapGroupAdmins;
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can ADMIN the group 
   * @return admins
   */
  public String getLdapGroupAdmins() {
    return this.ldapGroupAdmins;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can ADMIN the group 
   * @param ldapGroupAdmins1
   */
  public void setLdapGroupAdmins(String ldapGroupAdmins1) {
    this.ldapGroupAdmins = ldapGroupAdmins1;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can VIEW the group 
   */
  private String ldapGroupViewers;
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can VIEW the group 
   * @return viewers
   */
  public String getLdapGroupViewers() {
    return this.ldapGroupViewers;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can VIEW the group 
   * @param ldapGroupViewers1
   */
  public void setLdapGroupViewers(String ldapGroupViewers1) {
    this.ldapGroupViewers = ldapGroupViewers1;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can READ the group memberships 
   */
  private String ldapGroupReaders;
  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can READ the group memberships 
   * @return readers
   */
  public String getLdapGroupReaders() {
    return this.ldapGroupReaders;
  }

  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES, LDAP_GROUP_LIST
   * Comma separated list of subjectIds or subjectIdentifiers who can READ the group memberships 
   * @param ldapGroupReaders1
   */
  public void setLdapGroupReaders(String ldapGroupReaders1) {
    this.ldapGroupReaders = ldapGroupReaders1;
  }


  /**
   * optional, for LDAP_GROUP_LIST, or LDAP_GROUPS_FROM_ATTRIBUTES   
   * JEXL expression language fragment that evaluates to the group name (relative 
   * in the stem as the group which has the loader definition)
   */
  private String ldapGroupNameExpression;
  
  /**
   * optional, for LDAP_GROUP_LIST, or LDAP_GROUPS_FROM_ATTRIBUTES   
   * JEXL expression language fragment that evaluates to the group name (relative 
   * in the stem as the group which has the loader definition)
   * @return the ldapGroupNameExpression
   */
  public String getLdapGroupNameExpression() {
    return this.ldapGroupNameExpression;
  }
  
  /**
   * optional, for LDAP_GROUP_LIST, or LDAP_GROUPS_FROM_ATTRIBUTES   
   * JEXL expression language fragment that evaluates to the group name (relative 
   * in the stem as the group which has the loader definition)
   * @param ldapGroupNameExpression1 the ldapGroupNameExpression to set
   */
  public void setLdapGroupNameExpression(String ldapGroupNameExpression1) {
    this.ldapGroupNameExpression = ldapGroupNameExpression1;
  }

  /**
   * JEXL expression language fragment that evaluates to the group display 
   * extension, optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES
   */
  private String ldapGroupDisplayNameExpression;

  
  /**
   * JEXL expression language fragment that evaluates to the group display 
   * extension, optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES
   * @return the ldapGroupDisplayExtensionExpression
   */
  public String getLdapGroupDisplayNameExpression() {
    return this.ldapGroupDisplayNameExpression;
  }

  
  /**
   * JEXL expression language fragment that evaluates to the group display 
   * extension, optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES
   * @param ldapGroupDisplayExtensionExpression1 the ldapGroupDisplayExtensionExpression to set
   */
  public void setLdapGroupDisplayNameExpression(
      String ldapGroupDisplayExtensionExpression1) {
    this.ldapGroupDisplayNameExpression = ldapGroupDisplayExtensionExpression1;
  }

  /**
   * JEXL expression language fragment that evaluates to the group description, optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES
   */
  private String ldapGroupDescriptionExpression;
  
  /**
   * JEXL expression language fragment that evaluates to the group description, optional 
   * for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES
   * @return the ldapGroupDescriptionExpression
   */
  public String getLdapGroupDescriptionExpression() {
    return this.ldapGroupDescriptionExpression;
  }
  
  /**
   * JEXL expression language fragment that evaluates to the group description, optional 
   * for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES
   * @param ldapGroupDescriptionExpression1 the ldapGroupDescriptionExpression to set
   */
  public void setLdapGroupDescriptionExpression(String ldapGroupDescriptionExpression1) {
    this.ldapGroupDescriptionExpression = ldapGroupDescriptionExpression1;
  }

  /** if filtering which attributes, this is the jexl expression */
  private String ldapAttributeFilterExpression;

  /**
   * if filtering which attributes, this is the jexl expression
   * @return ldap attribute filter expression
   */
  public String getLdapAttributeFilterExpression() {
    return this.ldapAttributeFilterExpression;
  }

  /**
   * if filtering which attributes, this is the jexl expression
   * @param ldapAttributeFilterExpression1
   */
  public void setLdapAttributeFilterExpression(String ldapAttributeFilterExpression1) {
    this.ldapAttributeFilterExpression = ldapAttributeFilterExpression1;
  }

  /**
   * JEXL expression language fragment that processes the subject string before passing it to the subject API
   */
  private String ldapSubjectExpression;
  
  /**
   * JEXL expression language fragment that processes the subject string before passing it to the subject API
   * @return the ldapSubjectExpression
   */
  public String getLdapSubjectExpression() {
    return this.ldapSubjectExpression;
  }
  
  /**
   * JEXL expression language fragment that processes the subject string before passing it to the subject API
   * @param ldapSubjectExpression1 the ldapSubjectExpression to set
   */
  public void setLdapSubjectExpression(String ldapSubjectExpression1) {
    this.ldapSubjectExpression = ldapSubjectExpression1;
  }

  /**
   * required for LDAP_GROUPS_FROM_ATTRIBUTES 
   * Attribute name of the filter object result that holds the group name 
   * @return the ldapGroupAttribute
   */
  public String getLdapGroupAttribute() {
    return this.ldapGroupAttribute;
  }

  
  /**
   * required for LDAP_GROUPS_FROM_ATTRIBUTES 
   * Attribute name of the filter object result that holds the group name 
   * @param ldapGroupAttribute1 the ldapGroupAttribute to set
   */
  public void setLdapGroupAttribute(String ldapGroupAttribute1) {
    this.ldapGroupAttribute = ldapGroupAttribute1;
  }

  /** Server ID that is configured in the grouper-loader.properties that identifies the connection information to the LDAP server */
  private String ldapServerId;
  
  /**
   * Server ID that is configured in the grouper-loader.properties that identifies the connection information to the LDAP server 
   * @return server id
   */
  public String getLdapServerId() {
    return this.ldapServerId;
  }

  /**
   * Server ID that is configured in the grouper-loader.properties that identifies the connection information to the LDAP server 
   * @param ldapServerId1
   */
  public void setLdapServerId(String ldapServerId1) {
    this.ldapServerId = ldapServerId1;
  }

  /** LDAP filter returns objects that have subjectIds or subjectIdentifiers and group name (if LDAP_GROUP_LIST)  */
  private String ldapFilter;
  
  /**
   * LDAP filter returns objects that have subjectIds or subjectIdentifiers and group name (if LDAP_GROUP_LIST) 
   * @return filter
   */
  public String getLdapFilter() {
    return this.ldapFilter;
  }

  /**
   * LDAP filter returns objects that have subjectIds or subjectIdentifiers and group name (if LDAP_GROUP_LIST) 
   * @param ldapFilter1
   */
  public void setLdapFilter(String ldapFilter1) {
    this.ldapFilter = ldapFilter1;
  }

  /** Attribute name of the filter object result that holds the subject id.  */
  private String ldapSubjectAttribute;
  
  /**
   * Attribute name of the filter object result that holds the subject id. 
   * @return attribute name
   */
  public String getLdapSubjectAttribute() {
    return this.ldapSubjectAttribute;
  }

  /**
   * Attribute name of the filter object result that holds the subject id. 
   * @param ldapSubjectAttribute1
   */
  public void setLdapSubjectAttribute(String ldapSubjectAttribute1) {
    this.ldapSubjectAttribute = ldapSubjectAttribute1;
  }

  /** Location that constrains the subtree where the filter is applicable.  Note, this is relative 
   * to the base DN in the ldap server config in the grouper-loader.properties for this server.  
   * This makes the query more efficient  */
  private String ldapSearchDn;
  
  /**
   * Location that constrains the subtree where the filter is applicable.  
   * Note, this is relative to the base DN in the ldap server config in the 
   * grouper-loader.properties for this server.  This makes the query more efficient 
   * @return search dn
   */
  public String getLdapSearchDn() {
    return this.ldapSearchDn;
  }

  /**
   * Location that constrains the subtree where the filter is applicable.  
   * Note, this is relative to the base DN in the ldap server config in the 
   * grouper-loader.properties for this server.  This makes the query more efficient 
   * @param ldapSearchDn1
   */
  public void setLdapSearchDn(String ldapSearchDn1) {
    this.ldapSearchDn = ldapSearchDn1;
  }

  /** Quartz cron config string, e.g. every day at 8am is: 0 0 8 * * ?  */
  private String ldapQuartzCron;
  
  /**
   * Quartz cron config string, e.g. every day at 8am is: 0 0 8 * * ? 
   * @return quartz cron
   */
  public String getLdapQuartzCron() {
    return this.ldapQuartzCron;
  }

  /**
   * Quartz cron config string, e.g. every day at 8am is: 0 0 8 * * ? 
   * @param ldapQuartzCron1
   */
  public void setLdapQuartzCron(String ldapQuartzCron1) {
    this.ldapQuartzCron = ldapQuartzCron1;
  }

  /** Source ID from the subject.properties that narrows the search for subjects.  This is optional though makes the loader job more efficient */
  private String ldapSourceId;
  
  /**
   * Source ID from the subject.properties that narrows the search for subjects.  This is optional though makes the loader job more efficient
   * @return source id
   */
  public String getLdapSourceId() {
    return this.ldapSourceId;
  }

  /**
   * Source ID from the subject.properties that narrows the search for subjects.  This is optional though makes the loader job more efficient
   * @param ldapSourceId1
   */
  public void setLdapSourceId(String ldapSourceId1) {
    this.ldapSourceId = ldapSourceId1;
  }

  /**The type of subject ID.  This can be either: subjectId (most efficient, default), subjectIdentifier (2nd most efficient), or subjectIdOrIdentifier   */
  private String ldapSubjectIdType;
  
  /**
   * The type of subject ID.  This can be either: subjectId (most efficient, default), subjectIdentifier (2nd most efficient), or subjectIdOrIdentifier 
   * @return subject id type
   */
  public String getLdapSubjectIdType() {
    return this.ldapSubjectIdType;
  }

  /**
   * The type of subject ID.  This can be either: subjectId (most efficient, default), subjectIdentifier (2nd most efficient), or subjectIdOrIdentifier 
   * @param ldapSubjectIdType1
   */
  public void setLdapSubjectIdType(String ldapSubjectIdType1) {
    this.ldapSubjectIdType = ldapSubjectIdType1;
  }

  /** How the deep in the subtree the search will take place.  Can be OBJECT_SCOPE, ONELEVEL_SCOPE, or SUBTREE_SCOPE (default)  */
  private String ldapSearchScope;

  
  /**
   * How the deep in the subtree the search will take place.  Can be OBJECT_SCOPE, ONELEVEL_SCOPE, or SUBTREE_SCOPE (default) 
   * @return search scope
   */
  public String getLdapSearchScope() {
    return this.ldapSearchScope;
  }

  /**
   * How the deep in the subtree the search will take place.  Can be OBJECT_SCOPE, ONELEVEL_SCOPE, or SUBTREE_SCOPE (default) 
   * @param ldapSearchScope1
   */
  public void setLdapSearchScope(String ldapSearchScope1) {
    this.ldapSearchScope = ldapSearchScope1;
  }

  /**
   * start time of job
   */
  private long startTime;
  
  /**
   * type of loader
   */
  private GrouperLoaderType grouperLoaderType;

  /**
   * group name for the job.  If this is a group list, then this is the overall group name
   */
  private String groupNameOverall;
  
  /**
   * attributeDef name for the job
   */
  private String attributeDefName;
  
  /**
   * attributeDef name for the job
   * @return attributeDef name
   */
  public String getAttributeDefName() {
    return attributeDefName;
  }

  /**
   * attributeDef name for the job
   * @param attributeDefName1
   */
  public void setAttributeDefName(String attributeDefName1) {
    this.attributeDefName = attributeDefName1;
  }

  /**
   * database this job runs against
   */
  private GrouperLoaderDb grouperLoaderDb;
  
  /**
   * quert for the job
   */
  private String query;
  
  /**
   * log
   */
  private Hib3GrouperLoaderLog hib3GrouploaderLogOverall;
  
  /**
   * grouper session for the job, probably a root session
   */
  private GrouperSession grouperSession;
  
  /**
   * members must be in these groups to be in the overall group
   */
  private List<Group> andGroups;
  
  /**
   * group types to add to loader managed group
   */
  private List<GroupType> groupTypes;
  
  /**
   * groups with this like DB sql string are managed by the loader.
   * Any group in this list with no memberships and not in the group
   * metadata query will be emptied and if configured deleted
   */
  private String groupLikeString; 
  
  /**
   * query for the job
   */
  private String groupQuery;
  
  /**
   * If empty, then orphans will be left alone (for attributeDefName and attributeDefNameSets).  If %, then all orphans deleted.  If a SQL like string, then only ones in that like string not in loader will be deleted
   */
  private String attributeLoaderAttrsLike;
  
  /**
   * SQL query with at least some of the following columns: attr_name, attr_display_name, attr_description
   */
  private String attributeLoaderAttrQuery;
  
  /**
   * SQL query with at least the following columns: if_has_attr_name, then_has_attr_name
   */
  private String attributeLoaderAttrSetQuery;
  
  /** SQL query with at least the following column: action_name */
  private String attributeLoaderActionQuery;

  /** SQL query with at least the following columns: if_has_action_name, then_has_action_name */
  private String attributeLoaderActionSetQuery;
  
  
  /**
   * SQL query with at least the following column: action_name
   * @return query
   */
  public String getAttributeLoaderActionQuery() {
    return attributeLoaderActionQuery;
  }

  /**
   * SQL query with at least the following column: action_name
   * @param attributeLoaderActionQuery1
   */
  public void setAttributeLoaderActionQuery(String attributeLoaderActionQuery1) {
    this.attributeLoaderActionQuery = attributeLoaderActionQuery1;
  }

  /**
   * SQL query with at least the following columns: if_has_action_name, then_has_action_name
   * @return query
   */
  public String getAttributeLoaderActionSetQuery() {
    return attributeLoaderActionSetQuery;
  }

  /**
   * SQL query with at least the following columns: if_has_action_name, then_has_action_name
   * @param attributeLoaderActionSetQuery1
   */
  public void setAttributeLoaderActionSetQuery(String attributeLoaderActionSetQuery1) {
    this.attributeLoaderActionSetQuery = attributeLoaderActionSetQuery1;
  }

  /**
   * If empty, then orphans will be left alone (for attributeDefName and attributeDefNameSets).  If %, then all orphans deleted.  If a SQL like string, then only ones in that like string not in loader will be deleted
   * @return attrs like
   */
  public String getAttributeLoaderAttrsLike() {
    return attributeLoaderAttrsLike;
  }

  /**
   * If empty, then orphans will be left alone (for attributeDefName and attributeDefNameSets).  If %, then all orphans deleted.  If a SQL like string, then only ones in that like string not in loader will be deleted
   * @param attributeLoaderAttrsLike1
   */
  public void setAttributeLoaderAttrsLike(String attributeLoaderAttrsLike1) {
    this.attributeLoaderAttrsLike = attributeLoaderAttrsLike1;
  }

  /**
   * SQL query with at least some of the following columns: attr_name, attr_display_name, attr_description
   * @return query
   */
  public String getAttributeLoaderAttrQuery() {
    return attributeLoaderAttrQuery;
  }

  /**
   * SQL query with at least some of the following columns: attr_name, attr_display_name, attr_description
   * @param attributeLoaderAttrQuery1
   */
  public void setAttributeLoaderAttrQuery(String attributeLoaderAttrQuery1) {
    this.attributeLoaderAttrQuery = attributeLoaderAttrQuery1;
  }

  /**
   * SQL query with at least the following columns: if_has_attr_name, then_has_attr_name
   * @return sql query
   */
  public String getAttributeLoaderAttrSetQuery() {
    return attributeLoaderAttrSetQuery;
  }

  /**
   * SQL query with at least the following columns: if_has_attr_name, then_has_attr_name
   * @param attributeLoaderAttrSetQuery1
   */
  public void setAttributeLoaderAttrSetQuery(String attributeLoaderAttrSetQuery1) {
    this.attributeLoaderAttrSetQuery = attributeLoaderAttrSetQuery1;
  }

  /**
   * 
   */
  public LoaderJobBean() {
    super();
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public LoaderJobBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * type of job, e.g. group list, or sql simple
   * @return type
   */
  public GrouperLoaderType getGrouperLoaderType() {
    return this.grouperLoaderType;
  }

  /**
   * overall group name (if a group list job, then overall, if sql simple, then the group)
   * @return group name overall
   */
  public String getGroupNameOverall() {
    return this.groupNameOverall;
  }

  /**
   * database this runs against
   * @return loader db
   */
  public GrouperLoaderDb getGrouperLoaderDb() {
    return this.grouperLoaderDb;
  }

  /**
   * query for the job
   * @return query
   */
  public String getQuery() {
    return this.query;
  }

  /**
   * log entry for the job
   * @return log
   */
  public Hib3GrouperLoaderLog getHib3GrouploaderLogOverall() {
    return this.hib3GrouploaderLogOverall;
  }

  /**
   * grouper session (probably a root session)
   * @return session
   */
  public GrouperSession getGrouperSession() {
    return this.grouperSession;
  }

  /**
   * members must be in these groups also to be in the overall group
   * @return and groups
   */
  public List<Group> getAndGroups() {
    return this.andGroups;
  }

  /**
   * group types to add to loader managed group
   * @return group types
   */
  public List<GroupType> getGroupTypes() {
    return this.groupTypes;
  }

  /**
   * 
   * @return group like string
   */
  public String getGroupLikeString() {
    return this.groupLikeString;
  }

  /**
   * group query
   * @return group query
   */
  public String getGroupQuery() {
    return this.groupQuery;
  }

  /**
   * @param grouperLoaderType1
   * @param groupNameOverall1
   * @param grouperLoaderDb1
   * @param query1
   * @param hib3GrouploaderLogOverall1
   * @param grouperSession1
   * @param andGroups1
   * @param groupTypes1
   * @param groupLikeString1 groups with this like DB sql string are managed by the loader.
   * Any group in this list with no memberships and not in the group
   * metadata query will be emptied and if configured deleted
   * @param groupQuery1
   * @param startTime1 
   */
  public LoaderJobBean(GrouperLoaderType grouperLoaderType1,
      String groupNameOverall1, GrouperLoaderDb grouperLoaderDb1, String query1,
      Hib3GrouperLoaderLog hib3GrouploaderLogOverall1,
      GrouperSession grouperSession1, List<Group> andGroups1,
      List<GroupType> groupTypes1, String groupLikeString1, String groupQuery1, long startTime1) {
    this.grouperLoaderType = grouperLoaderType1;
    this.groupNameOverall = groupNameOverall1;
    this.grouperLoaderDb = grouperLoaderDb1;
    this.query = query1;
    this.hib3GrouploaderLogOverall = hib3GrouploaderLogOverall1;
    this.grouperSession = grouperSession1;
    this.andGroups = andGroups1;
    this.groupTypes = groupTypes1;
    this.groupLikeString = groupLikeString1;
    this.groupQuery = groupQuery1;
    this.startTime = startTime1;
  }

  /**
   * constructor for ldap jobs
   * @param ldapType1
   * @param ldapServerId1
   * @param ldapFilter1
   * @param ldapSubjectAttribute1
   * @param ldapSearchDn1
   * @param ldapSourceId1
   * @param ldapSubjectIdType1
   * @param ldapSearchScope1
   * @param startTime1
   * @param grouperLoaderType1
   * @param groupNameOverall1
   * @param hib3GrouploaderLogOverall1
   * @param grouperSession1
   * @param andGroups1
   * @param ldapGroupAttribute1 
   * @param extraAttributes1 
   * @param ldapGroupNameExpression1 
   * @param ldapGroupDisplayExtensionExpression1 
   * @param ldapGroupDescriptionExpression1 
   * @param ldapSubjectExpression1
   * @param groupTypes1 
   * @param ldapGroupReaders1 
   * @param ldapGroupViewers1 
   * @param ldapGroupAdmins1 
   * @param ldapGroupUpdaters1 
   * @param ldapGroupOptins1 
   * @param ldapGroupOptouts1 
   * @param ldapAttributeFilterExpression1
   * @param ldapGroupAttrReaders1
   * @param ldapGroupAttrUpdaters1
   * @param groupsLike1 
   * @param ldapResultsTransformationClass1
   */
  public LoaderJobBean(String ldapType1, String ldapServerId1, String ldapFilter1,
      String ldapSubjectAttribute1, String ldapSearchDn1, String ldapSourceId1,
      String ldapSubjectIdType1, String ldapSearchScope1, long startTime1,
      GrouperLoaderType grouperLoaderType1, String groupNameOverall1,
      Hib3GrouperLoaderLog hib3GrouploaderLogOverall1, GrouperSession grouperSession1, List<Group> andGroups1,
      String ldapGroupAttribute1, String extraAttributes1, 
      String ldapGroupNameExpression1, 
      String ldapGroupDisplayExtensionExpression1, String ldapGroupDescriptionExpression1,
      String ldapSubjectExpression1, List<GroupType> groupTypes1, String ldapGroupReaders1,
      String ldapGroupViewers1, String ldapGroupAdmins1, String ldapGroupUpdaters1, String ldapGroupOptins1,
      String ldapGroupOptouts1, String groupsLike1, String ldapAttributeFilterExpression1, 
      String ldapGroupAttrReaders1, String ldapGroupAttrUpdaters1, String ldapResultsTransformationClass1
      ) {
    super();
    this.ldapType = ldapType1;
    this.ldapServerId = ldapServerId1;
    this.ldapFilter = ldapFilter1;
    this.ldapSubjectAttribute = ldapSubjectAttribute1;
    this.ldapSearchDn = ldapSearchDn1;
    this.ldapSourceId = ldapSourceId1;
    this.ldapSubjectIdType = ldapSubjectIdType1;
    this.ldapSearchScope = ldapSearchScope1;
    this.startTime = startTime1;
    this.grouperLoaderType = grouperLoaderType1;
    this.groupNameOverall = groupNameOverall1;
    this.hib3GrouploaderLogOverall = hib3GrouploaderLogOverall1;
    this.grouperSession = grouperSession1;
    this.andGroups = andGroups1;
    this.ldapAttributeFilterExpression = ldapAttributeFilterExpression1;
    this.ldapGroupAttribute = ldapGroupAttribute1;
    this.ldapExtraAttributes = extraAttributes1;
    this.ldapGroupNameExpression = ldapGroupNameExpression1;
    this.ldapGroupDisplayNameExpression = ldapGroupDisplayExtensionExpression1;
    this.ldapGroupDescriptionExpression = ldapGroupDescriptionExpression1;
    this.ldapSubjectExpression = ldapSubjectExpression1;
    this.groupTypes = groupTypes1;
    this.ldapGroupOptins = ldapGroupOptins1;
    this.ldapGroupOptouts = ldapGroupOptouts1;
    this.ldapGroupAttrReaders = ldapGroupAttrReaders1;
    this.ldapGroupAttrUpdaters = ldapGroupAttrUpdaters1;
    this.ldapGroupViewers = ldapGroupViewers1;
    this.ldapGroupReaders = ldapGroupReaders1;
    this.ldapGroupAdmins = ldapGroupAdmins1;
    this.ldapGroupUpdaters = ldapGroupUpdaters1;
    this.groupLikeString = groupsLike1;
    this.ldapResultsTransformationClass = ldapResultsTransformationClass1;
  }

  /**
   * @param grouperLoaderType1
   * @param attributeDefName 
   * @param grouperLoaderDb1
   * @param hib3GrouploaderLogOverall1
   * @param grouperSession1
   * @param startTime1 
   * @param attributeLoaderAttrQuery1
   * @param attributeLoaderAttrSetQuery1
   * @param attributeLoaderAttrsLike1
   * @param attributeLoaderActionQuery1 
   * @param attributeLoaderActionSetQuery1 
   */
  public LoaderJobBean(GrouperLoaderType grouperLoaderType1, String attributeDefName,
      GrouperLoaderDb grouperLoaderDb1, 
      Hib3GrouperLoaderLog hib3GrouploaderLogOverall1,
      GrouperSession grouperSession1, String attributeLoaderAttrQuery1, 
      String attributeLoaderAttrSetQuery1, String attributeLoaderAttrsLike1,
      String attributeLoaderActionQuery1, String attributeLoaderActionSetQuery1, long startTime1  ) {
    this.attributeDefName = attributeDefName;
    this.grouperLoaderType = grouperLoaderType1;
    this.grouperLoaderDb = grouperLoaderDb1;
    this.hib3GrouploaderLogOverall = hib3GrouploaderLogOverall1;
    this.grouperSession = grouperSession1;
    this.attributeLoaderAttrQuery = attributeLoaderAttrQuery1;
    this.attributeLoaderAttrSetQuery = attributeLoaderAttrSetQuery1;
    this.attributeLoaderAttrsLike = attributeLoaderAttrsLike1;
    this.attributeLoaderActionQuery = attributeLoaderActionQuery1;
    this.attributeLoaderActionSetQuery = attributeLoaderActionSetQuery1;
    this.startTime = startTime1;
  }

  /**
   * type of job, e.g. sql simple or group list
   * @param grouperLoaderType
   */
  public void setGrouperLoaderType(GrouperLoaderType grouperLoaderType) {
    this.grouperLoaderType = grouperLoaderType;
  }

  /**
   * group name for job (if group list, this is the overall name)
   * @param groupNameOverall
   */
  public void setGroupNameOverall(String groupNameOverall) {
    this.groupNameOverall = groupNameOverall;
  }

  /**
   * db this job runs against
   * @param grouperLoaderDb
   */
  public void setGrouperLoaderDb(GrouperLoaderDb grouperLoaderDb) {
    this.grouperLoaderDb = grouperLoaderDb;
  }

  /**
   * query for this job (if runs against query)
   * @param query1
   */
  public void setQuery(String query1) {
    this.query = query1;
  }

  /**
   * 
   * @param hib3GrouploaderLogOverall1
   */
  public void setHib3GrouploaderLogOverall(
      Hib3GrouperLoaderLog hib3GrouploaderLogOverall1) {
    this.hib3GrouploaderLogOverall = hib3GrouploaderLogOverall1;
  }

  /**
   * grouper session, probably a root session
   * @param grouperSession1
   */
  public void setGrouperSession(GrouperSession grouperSession1) {
    this.grouperSession = grouperSession1;
  }

  /**
   * members must be in these groups also to be in the overall group
   * @param andGroups1
   */
  public void setAndGroups(List<Group> andGroups1) {
    this.andGroups = andGroups1;
  }

  /**
   * group types to add to loader managed group
   * @param groupTypes
   */
  public void setGroupTypes(List<GroupType> groupTypes) {
    this.groupTypes = groupTypes;
  }

  /**
   * groups with this like DB sql string are managed by the loader.
   * Any group in this list with no memberships and not in the group
   * metadata query will be emptied and if configured deleted
   * @param groupLikeString
   */
  public void setGroupLikeString(String groupLikeString) {
    this.groupLikeString = groupLikeString;
  }

  /**
   * 
   * @param groupQuery1
   */
  public void setGroupQuery(String groupQuery1) {
    this.groupQuery = groupQuery1;
  }

  /**
   * start time of job
   * @return start time
   */
  public long getStartTime() {
    return this.startTime;
  }

  /**
   * start time of job
   * @param startTime1
   */
  public void setStartTime(long startTime1) {
    this.startTime = startTime1;
  }

  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES
   * Class to transform data from ldap for more advanced transformations (e.g. parsing ldap attribute values into multiple groups)
   * @return the ldapResultsTransformationClass
   */
  public String getLdapResultsTransformationClass() {
    return ldapResultsTransformationClass;
  }

  
  /**
   * optional for LDAP_GROUPS_FROM_ATTRIBUTES
   * Class to transform data from ldap for more advanced transformations (e.g. parsing ldap attribute values into multiple groups)
   * @param ldapResultsTransformationClass1 the ldapResultsTransformationClass1 to set
   */
  public void setLdapResultsTransformationClass(String ldapResultsTransformationClass1) {
    this.ldapResultsTransformationClass = ldapResultsTransformationClass1;
  }

}
