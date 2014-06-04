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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader.ldap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.util.ExpirableCache;


/**
 * utility methods and constants for ldap loader
 */
public class LoaderLdapUtils {

  
  /** stem name of ldap loader attributes */
  private static String grouperLoaderLdapStemName = null;
  
  /**
   * stem name for loader ldap attributes
   * @return stem name
   */
  public static String grouperLoaderLdapStemName() {
    if (grouperLoaderLdapStemName == null) {
      grouperLoaderLdapStemName = GrouperCheckConfig.attributeRootStemName() + ":loaderLdap";
    }
    return grouperLoaderLdapStemName;
  }
  
  /** extension of the attribute def name for the marker attribute for grouper loader */
  public static final String ATTR_DEF_EXTENSION_MARKER = "grouperLoaderLdap";

  /** attribute def name of marker */
  private static String grouperLoaderLdapName;
  
  /**
   * attribute def name of marker attribute
   * @return name
   */
  public static String grouperLoaderLdapName() {
    if (grouperLoaderLdapName == null) {
      grouperLoaderLdapName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_MARKER;
    }
    return grouperLoaderLdapName;
  }
  
  /**
   * return attribute def name for attribute type marker
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapAttributeDefName() {
    return grouperLoaderLdapAttributeDefName(true);
    
  }
  /**
   * return attribute def name for attribute type marker
   * @param exceptionIfNotFound 
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapAttributeDefName(boolean exceptionIfNotFound) {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapName(), exceptionIfNotFound);
  }

  /** extension of the attribute def name for the quartz cron configuration */
  public static final String ATTR_DEF_EXTENSION_QUARTZ_CRON = "grouperLoaderLdapQuartzCron";

  /** attribute def name of quartz cron */
  private static String grouperLoaderLdapQuartzCronName;
  
  /**
   * attribute def name of quartz cron
   * @return name
   */
  public static String grouperLoaderLdapQuartzCronName() {
    if (grouperLoaderLdapQuartzCronName == null) {
      grouperLoaderLdapQuartzCronName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_QUARTZ_CRON;
    }
    return grouperLoaderLdapQuartzCronName;
  }
  
  /**
   * return attribute def name for attribute quartz cron
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapQuartzCronAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapQuartzCronName(), true);
  }
  
  /** loader ldap def extension */
  public static final String LOADER_LDAP_DEF = "grouperLoaderLdapDef";

  /** loader ldap value def extension */
  public static final String LOADER_LDAP_VALUE_DEF = "grouperLoaderLdapValueDef";

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
  public static final String ATTR_DEF_EXTENSION_TYPE = "grouperLoaderLdapType";

  /** attribute def name of type name */
  private static String grouperLoaderLdapTypeName;
  
  /**
   * attribute def name of job type
   * @return name
   */
  public static String grouperLoaderLdapTypeName() {
    if (grouperLoaderLdapTypeName == null) {
      grouperLoaderLdapTypeName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_TYPE;
    }
    return grouperLoaderLdapTypeName;
  }
  
  /**
   * return attribute def name for ldap type
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapTypeAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapTypeName(), true);
  }
  
  /** extension of the attribute def name for the server id of the ldap config (e.g. myLdap) */
  public static final String ATTR_DEF_EXTENSION_SERVER_ID = "grouperLoaderLdapServerId";

  /** attribute def name of server id */
  private static String grouperLoaderLdapServerIdName;
  
  /**
   * attribute def name of server id
   * @return name
   */
  public static String grouperLoaderLdapServerIdName() {
    if (grouperLoaderLdapServerIdName == null) {
      grouperLoaderLdapServerIdName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_SERVER_ID;
    }
    return grouperLoaderLdapServerIdName;
  }
  
  /**
   * return attribute def name for attribute server id
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapServerIdAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapServerIdName(), true);
  }

  /** extension of attribute def name for ldap filter to run to find the objects that have the subject id */
  public static final String ATTR_DEF_EXTENSION_LDAP_FILTER = "grouperLoaderLdapFilter";

  /** attribute def name of filter name */
  private static String grouperLoaderLdapFilterName;

  /**
   * attribute def name of filter name
   * @return name
   */
  public static String grouperLoaderLdapFilterName() {
    if (grouperLoaderLdapFilterName == null) {
      grouperLoaderLdapFilterName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_FILTER;
    }
    return grouperLoaderLdapFilterName;
  }

  /**
   * return attribute def name for attribute ldap filter
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapFilterAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapFilterName(), true);
  }

  /** extension of attribute def name for ldap filter run in a dn (optional though recommended) */
  public static final String ATTR_DEF_EXTENSION_LDAP_SEARCH_DN = "grouperLoaderLdapSearchDn";

  /** attribute def name of search dn */
  private static String grouperLoaderLdapSearchDnName;

  /**
   * attribute def name of search dn
   * @return name
   */
  public static String grouperLoaderLdapSearchDnName() {
    if (grouperLoaderLdapSearchDnName == null) {
      grouperLoaderLdapSearchDnName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_SEARCH_DN;
    }
    return grouperLoaderLdapSearchDnName;
  }
  
  /**
   * return attribute def name for attribute search dn
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapSearchDnAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapSearchDnName(), true);
  }

  /** extension of attribute def name for ldap "and groups" (must be in these comma separated group names) */
  public static final String ATTR_DEF_EXTENSION_LDAP_AND_GROUPS = "grouperLoaderLdapAndGroups";

  /** attribute def name of priority */
  private static String grouperLoaderLdapPriorityName;

  /**
   * attribute def name of priority
   * @return name
   */
  public static String grouperLoaderLdapPriorityName() {
    if (grouperLoaderLdapPriorityName == null) {
      grouperLoaderLdapPriorityName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_PRIORITY;
    }
    return grouperLoaderLdapPriorityName;
  }
  
  /**
   * return attribute def name for attribute quartz priority
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapPriorityAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapPriorityName(), true);
  }

  /** Quartz has a fixed threadpool (max configured in the grouper-loader.properties), and when the max is reached, 
   * then jobs are prioritized by this integer.  The higher the better, and the default if not set is 5. */
  public static final String ATTR_DEF_EXTENSION_LDAP_PRIORITY = "grouperLoaderLdapPriority";

  /** attribute def name of and groups */
  private static String grouperLoaderLdapAndGroupsName;

  /**
   * attribute def name of "and groups" (must be in these comma separated group names)
   * @return name
   */
  public static String grouperLoaderLdapAndGroupsName() {
    if (grouperLoaderLdapAndGroupsName == null) {
      grouperLoaderLdapAndGroupsName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_AND_GROUPS;
    }
    return grouperLoaderLdapAndGroupsName;
  }
  
  /**
   * return attribute def name for attribute "and groups"
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapAndGroupsAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapAndGroupsName(), true);
  }

  /** extension of attribute def name for the name of the attribute in the ldap object 
   * that is returned by the ldap filter which has the subject
   * id or identifier in it.  e.g. hasMember */
  public static final String ATTR_DEF_EXTENSION_SUBJECT_ATTRIBUTE = "grouperLoaderLdapSubjectAttribute";

  /** attribute def name of subject attribute */
  private static String grouperLoaderLdapSubjectAttributeName;

  /**
   * attribute def name of subject attribute
   * @return name
   */
  public static String grouperLoaderLdapSubjectAttributeName() {
    if (grouperLoaderLdapSubjectAttributeName == null) {
      grouperLoaderLdapSubjectAttributeName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_SUBJECT_ATTRIBUTE;
    }
    return grouperLoaderLdapSubjectAttributeName;
  }
  
  /**
   * return attribute def name for attribute subject attribute
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapSubjectAttributeAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapSubjectAttributeName(), true);
  }

  /** extension of the attribute def name for the source id of all subjects inside */
  public static final String ATTR_DEF_EXTENSION_SOURCE_ID = "grouperLoaderLdapSourceId";

  /** attribute def name of source id */
  private static String grouperLoaderLdapSourceIdName;

  /**
   * attribute def name of source id
   * @return name
   */
  public static String grouperLoaderLdapSourceIdName() {
    if (grouperLoaderLdapSourceIdName == null) {
      grouperLoaderLdapSourceIdName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_SOURCE_ID;
    }
    return grouperLoaderLdapSourceIdName;
  }
  
  /**
   * return attribute def name for attribute source id
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapSourceIdAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapSourceIdName(), true);
  }

  /** extension of the attribute def name for subjectId, subjectIdentifier, or subjectIdOrIdentifier (default) */
  public static final String ATTR_DEF_EXTENSION_SUBJECT_ID_TYPE = "grouperLoaderLdapSubjectIdType";

  /** attribute def name of subject id type */
  private static String grouperLoaderLdapSubjectIdTypeName;

  /**
   * attribute def name of subject id type
   * @return name
   */
  public static String grouperLoaderLdapSubjectIdTypeName() {
    if (grouperLoaderLdapSubjectIdTypeName == null) {
      grouperLoaderLdapSubjectIdTypeName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_SUBJECT_ID_TYPE;
    }
    return grouperLoaderLdapSubjectIdTypeName;
  }
  
  /**
   * return attribute def name for attribute subject id type
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapSubjectIdTypeAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapSubjectIdTypeName(), true);
  }

  /** extension of the attribute def name for search scope, needs to be one of: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE */
  public static final String ATTR_DEF_EXTENSION_SEARCH_SCOPE = "grouperLoaderLdapSearchScope";

  /** attribute def name of search scope name */
  private static String grouperLoaderLdapSearchScopeName;

  /**
   * attribute def name of search scope name
   * @return name
   */
  public static String grouperLoaderLdapSearchScopeName() {
    if (grouperLoaderLdapSearchScopeName == null) {
      grouperLoaderLdapSearchScopeName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_SEARCH_SCOPE;
    }
    return grouperLoaderLdapSearchScopeName;
  }
  
  /**
   * return attribute def name for attribute
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapSearchScopeAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapSearchScopeName(), true);
  }
  
  /** extension of the attribute def name for groups like string for sql to remove orphans of LDAP_GROUP_LIST */
  public static final String ATTR_DEF_EXTENSION_GROUPS_LIKE = "grouperLoaderLdapGroupsLike";

  /** attribute def name of groups like string for sql to remove orphans of LDAP_GROUP_LIST */
  private static String grouperLoaderLdapGroupsLikeName;

  /**
   * attribute def name of groups like string for sql to remove orphans of LDAP_GROUP_LIST
   * @return name
   */
  public static String grouperLoaderLdapGroupsLikeName() {
    if (grouperLoaderLdapGroupsLikeName == null) {
      grouperLoaderLdapGroupsLikeName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_GROUPS_LIKE;
    }
    return grouperLoaderLdapGroupsLikeName;
  }
  
  /**
   * return attribute def name for attribute groups like string for sql to remove orphans of LDAP_GROUP_LIST
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapGroupsLikeAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapGroupsLikeName(), true);
  }
  
  /**
   * return the stem name where the limit attributes go, without colon on end
   * @return stem name
   */
  public static String attributeLoaderLdapStemName() {
    String rootStemName = GrouperCheckConfig.attributeRootStemName();
    
    //namespace this separate from other builtins
    rootStemName += ":loaderLdap";
    return rootStemName;
  }


  /** Attribute name of the filter object result that holds the group name  */
  public static final String ATTR_DEF_EXTENSION_LDAP_GROUP_ATTRIBUTE = "grouperLoaderLdapGroupAttribute";

  /** attribute def name of group attribute */
  private static String grouperLoaderLdapGroupAttributeName;

  /**
   * attribute def name of group attribute
   * @return name
   */
  public static String grouperLoaderLdapGroupAttributeName() {
    if (grouperLoaderLdapGroupAttributeName == null) {
      grouperLoaderLdapGroupAttributeName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_GROUP_ATTRIBUTE;
    }
    return grouperLoaderLdapGroupAttributeName;
  }
  
  /**
   * return attribute def name for attribute group attribute
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapGroupAttributeAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapGroupAttributeName(), true);
  }


  
  
  /** attribute def name of attribute filter expression  */
  public static final String ATTR_DEF_EXTENSION_LDAP_ATTRIBUTE_FILTER_EXPRESSION = "grouperLoaderLdapAttributeFilterExpression";

  /** attribute def name of attribute filter expression */
  private static String grouperLoaderLdapAttributeFilterExpression;

  /**
   * attribute def name of attribute filter expression
   * @return name
   */
  public static String grouperLoaderLdapAttributeFilterExpressionName() {
    if (grouperLoaderLdapAttributeFilterExpression == null) {
      grouperLoaderLdapAttributeFilterExpression = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_ATTRIBUTE_FILTER_EXPRESSION;
    }
    return grouperLoaderLdapAttributeFilterExpression;
  }
  
  /**
   * return attribute def name of attribute filter expression
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapAttributeFilterExpressionAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapAttributeFilterExpressionName(), true);
  }


  
  
  
  /** Attribute name of the filter object result that holds the extra attributes  */
  public static final String ATTR_DEF_EXTENSION_LDAP_EXTRA_ATTRIBUTES = "grouperLoaderLdapExtraAttributes";

  /** attribute def name of extra attributes */
  private static String grouperLoaderLdapExtraAttributesName;

  /**
   * attribute def name of extra attributes
   * @return name
   */
  public static String grouperLoaderLdapExtraAttributesName() {
    if (grouperLoaderLdapExtraAttributesName == null) {
      grouperLoaderLdapExtraAttributesName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_EXTRA_ATTRIBUTES;
    }
    return grouperLoaderLdapExtraAttributesName;
  }
  
  /**
   * return attribute def name for attribute extra attributes
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapExtraAttributesAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapExtraAttributesName(), true);
  }
  

  
  
  /** Attribute name of true/false for error unresolvable  */
  public static final String ATTR_DEF_EXTENSION_LDAP_ERROR_UNRESOLVABLE = "grouperLoaderLdapErrorUnresolvable";

  /** attribute def name of error unresolvable */
  private static String grouperLoaderLdapErrorUnresolvableName;

  /**
   * attribute def name of error unresolvable
   * @return name
   */
  public static String grouperLoaderLdapErrorUnresolvableName() {
    if (grouperLoaderLdapErrorUnresolvableName == null) {
      grouperLoaderLdapErrorUnresolvableName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_ERROR_UNRESOLVABLE;
    }
    return grouperLoaderLdapErrorUnresolvableName;
  }
  
  /**
   * return attribute def name for attribute error unresolvable
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapErrorUnresolvableAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapErrorUnresolvableName(), true);
  }


  

  
  
  /** Attribute name of name expression  */
  public static final String ATTR_DEF_EXTENSION_LDAP_GROUP_NAME_EXPRESSION = "grouperLoaderLdapGroupNameExpression";

  /** attribute def name of name expression */
  private static String grouperLoaderLdapGroupNameExpressionName;

  /**
   * attribute def name of group name expression
   * @return name
   */
  public static String grouperLoaderLdapGroupNameExpressionName() {
    if (grouperLoaderLdapGroupNameExpressionName == null) {
      grouperLoaderLdapGroupNameExpressionName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_GROUP_NAME_EXPRESSION;
    }
    return grouperLoaderLdapGroupNameExpressionName;
  }
  
  /**
   * return attribute def name for group name expression
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapGroupNameExpressionAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapGroupNameExpressionName(), true);
  }

  
  

  
  
  /** Attribute name of display name expression  */
  public static final String ATTR_DEF_EXTENSION_LDAP_GROUP_DISPLAY_NAME_EXPRESSION = "grouperLoaderLdapGroupDisplayNameExpression";

  /** attribute def name of display name expression */
  private static String grouperLoaderLdapGroupDisplayNameExpressionName;

  /**
   * attribute def name of group display name expression
   * @return name
   */
  public static String grouperLoaderLdapGroupDisplayNameExpressionName() {
    if (grouperLoaderLdapGroupDisplayNameExpressionName == null) {
      grouperLoaderLdapGroupDisplayNameExpressionName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_GROUP_DISPLAY_NAME_EXPRESSION;
    }
    return grouperLoaderLdapGroupDisplayNameExpressionName;
  }
  
  /**
   * return attribute def name for group dislpay name expression
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapGroupDisplayNameExpressionAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapGroupDisplayNameExpressionName(), true);
  }

  
  
  
  
  /** Attribute name of description expression  */
  public static final String ATTR_DEF_EXTENSION_LDAP_GROUP_DESCRIPTION_EXPRESSION = "grouperLoaderLdapGroupDescriptionExpression";

  /** attribute def name of description expression */
  private static String grouperLoaderLdapGroupDescriptionExpressionName;

  /**
   * attribute def name of group description expression
   * @return name
   */
  public static String grouperLoaderLdapGroupDescriptionExpressionName() {
    if (grouperLoaderLdapGroupDescriptionExpressionName == null) {
      grouperLoaderLdapGroupDescriptionExpressionName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_GROUP_DESCRIPTION_EXPRESSION;
    }
    return grouperLoaderLdapGroupDescriptionExpressionName;
  }
  
  /**
   * return attribute def name for group description expression
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapGroupDescriptionExpressionAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapGroupDescriptionExpressionName(), true);
  }

  
  
  /** limit logic map */
  static ExpirableCache<Boolean, Set<Class<?>>> ldapLoaderElClassesMap = new ExpirableCache<Boolean, Set<Class<?>>>(5);

  /**
   * custom el instances to add to the variable map for ldap loader EL
   * @return the map
   */
  public static Map<String, Object> limitLoaderElClasses() {

    //grouper.permissions.limits.el.classes

    Set<Class<?>> ldapLoaderElClasses = ldapLoaderElClassesMap.get(Boolean.TRUE);

    if (ldapLoaderElClasses == null) {

      synchronized (PermissionLimitUtils.class) {

        ldapLoaderElClasses = ldapLoaderElClassesMap.get(Boolean.TRUE);

        if (ldapLoaderElClasses == null) {

          ldapLoaderElClasses = new HashSet<Class<?>>();

          //middleware.grouper.rules.MyRuleUtils
          String customElClasses = GrouperLoaderConfig.retrieveConfig().propertyValueString("loader.ldap.el.classes");

          if (!StringUtils.isBlank(customElClasses)) {
            String[] customElClassesArray = GrouperUtil.splitTrim(customElClasses, ",");
            for (String customElClass : customElClassesArray) {
              Class<?> customClassClass = GrouperUtil.forName(customElClass);
              ldapLoaderElClasses.add(customClassClass);
            }
          }


          //lets add standard entries, do this first so they can be overridden
          ldapLoaderElClassesMap.put(Boolean.TRUE, ldapLoaderElClasses);

        }
      }
    }
    
    //get new instances each time
    Map<String,Object> result = new HashMap<String, Object>();
    for (Class<?> customClassClass : ldapLoaderElClasses) {
      String simpleName = StringUtils.uncapitalize(customClassClass.getSimpleName());
      result.put(simpleName, GrouperUtil.newInstance(customClassClass));
      
    }

    return result;
    
  }

  /**
   * substitute expression
   * @param expression
   * @param loaderEnvVars 
   * @return the evaluation
   */
  public static String substituteEl(String expression, Map<String, Object> loaderEnvVars) {
    
    if (StringUtils.isBlank(expression)) {
      return expression;
    }
    
    loaderEnvVars.put("loaderLdapElUtils", new LoaderLdapElUtils());
    
    //get custom el classes to add
    Map<String, Object> customElClasses = limitLoaderElClasses();
    
    loaderEnvVars.putAll(GrouperUtil.nonNull(customElClasses));
    
    String result = null;
    //dont be lenient on undefined variables
    result = GrouperUtil.substituteExpressionLanguage(expression, loaderEnvVars, false, false, false);
    
    return result;
  }
  
  
  /** Attribute name of subject expression  */
  public static final String ATTR_DEF_EXTENSION_LDAP_SUBJECT_EXPRESSION = "grouperLoaderLdapSubjectExpression";

  /** attribute def name of subject expression */
  private static String grouperLoaderLdapSubjectExpressionName;

  /**
   * attribute def name of subject expression
   * @return name
   */
  public static String grouperLoaderLdapSubjectExpressionName() {
    if (grouperLoaderLdapSubjectExpressionName == null) {
      grouperLoaderLdapSubjectExpressionName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_SUBJECT_EXPRESSION;
    }
    return grouperLoaderLdapSubjectExpressionName;
  }
  
  /**
   * return attribute def name for subject expression
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapSubjectExpressionAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapSubjectExpressionName(), true);
  }

  
  /** Attribute name of group types  */
  public static final String ATTR_DEF_EXTENSION_LDAP_GROUP_TYPES = "grouperLoaderLdapGroupTypes";

  /** attribute def name of group types */
  private static String grouperLoaderLdapGroupTypesName;

  /**
   * attribute def name of group types
   * @return name
   */
  public static String grouperLoaderLdapGroupTypesName() {
    if (grouperLoaderLdapGroupTypesName == null) {
      grouperLoaderLdapGroupTypesName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_GROUP_TYPES;
    }
    return grouperLoaderLdapGroupTypesName;
  }
  
  /**
   * return attribute def name for group types
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapGroupTypesAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapGroupTypesName(), true);
  }

  

  /** Attribute name of readers  */
  public static final String ATTR_DEF_EXTENSION_LDAP_READERS = "grouperLoaderLdapReaders";

  /** attribute def name of readers */
  private static String grouperLoaderLdapReadersName;

  /**
   * attribute def name of readers
   * @return name
   */
  public static String grouperLoaderLdapReadersName() {
    if (grouperLoaderLdapReadersName == null) {
      grouperLoaderLdapReadersName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_READERS;
    }
    return grouperLoaderLdapReadersName;
  }
  
  /**
   * return attribute def name for readers
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapReadersAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapReadersName(), true);
  }

  
  /** Attribute name of viewers  */
  public static final String ATTR_DEF_EXTENSION_LDAP_VIEWERS = "grouperLoaderLdapViewers";

  /** attribute def name of viewers */
  private static String grouperLoaderLdapViewersName;

  /**
   * attribute def name of viewers
   * @return name
   */
  public static String grouperLoaderLdapViewersName() {
    if (grouperLoaderLdapViewersName == null) {
      grouperLoaderLdapViewersName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_VIEWERS;
    }
    return grouperLoaderLdapViewersName;
  }
  
  /**
   * return attribute def name for viewers
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapViewersAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapViewersName(), true);
  }

  
    
  /** Attribute name of updaters  */
  public static final String ATTR_DEF_EXTENSION_LDAP_UPDATERS = "grouperLoaderLdapUpdaters";

  /** attribute def name of udpaters */
  private static String grouperLoaderLdapUpdatersName;

  /**
   * attribute def name of updaters
   * @return name
   */
  public static String grouperLoaderLdapUpdatersName() {
    if (grouperLoaderLdapUpdatersName == null) {
      grouperLoaderLdapUpdatersName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_UPDATERS;
    }
    return grouperLoaderLdapUpdatersName;
  }
  
  /**
   * return attribute def name for updaters
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapUpdatersAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapUpdatersName(), true);
  }

  
    
  
  /** Attribute name of admins  */
  public static final String ATTR_DEF_EXTENSION_LDAP_ADMINS = "grouperLoaderLdapAdmins";

  /** attribute def name of admins */
  private static String grouperLoaderLdapAdminsName;

  /**
   * attribute def name of admins
   * @return name
   */
  public static String grouperLoaderLdapAdminsName() {
    if (grouperLoaderLdapAdminsName == null) {
      grouperLoaderLdapAdminsName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_ADMINS;
    }
    return grouperLoaderLdapAdminsName;
  }
  
  /**
   * return attribute def name for admins
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapAdminsAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapAdminsName(), true);
  }

  
    
  
  /** Attribute name of optins  */
  public static final String ATTR_DEF_EXTENSION_LDAP_OPTINS = "grouperLoaderLdapOptins";

  /** attribute def name of optins */
  private static String grouperLoaderLdapOptinsName;

  /**
   * attribute def name of optins
   * @return name
   */
  public static String grouperLoaderLdapOptinsName() {
    if (grouperLoaderLdapOptinsName == null) {
      grouperLoaderLdapOptinsName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_OPTINS;
    }
    return grouperLoaderLdapOptinsName;
  }
  
  /**
   * return attribute def name for optins
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapOptinsAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapOptinsName(), true);
  }

  
    
  /** Attribute name of optouts  */
  public static final String ATTR_DEF_EXTENSION_LDAP_OPTOUTS = "grouperLoaderLdapOptouts";

  /** attribute def name of optouts */
  private static String grouperLoaderLdapOptoutsName;

  /**
   * attribute def name of optouts
   * @return name
   */
  public static String grouperLoaderLdapOptoutsName() {
    if (grouperLoaderLdapOptoutsName == null) {
      grouperLoaderLdapOptoutsName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_OPTOUTS;
    }
    return grouperLoaderLdapOptoutsName;
  }
  
  /**
   * return attribute def name for optouts
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapOptoutsAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapOptoutsName(), true);
  }

  
  
  /** Attribute name of groupAttrReaders  */
  public static final String ATTR_DEF_EXTENSION_LDAP_GROUP_ATTR_READERS = "grouperLoaderLdapGroupAttrReaders";

  /** attribute def name of groupAttrReaders */
  private static String grouperLoaderLdapGroupAttrReadersName;

  /**
   * attribute def name of groupAttrReaders
   * @return name
   */
  public static String grouperLoaderLdapGroupAttrReadersName() {
    if (grouperLoaderLdapGroupAttrReadersName == null) {
      grouperLoaderLdapGroupAttrReadersName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_GROUP_ATTR_READERS;
    }
    return grouperLoaderLdapGroupAttrReadersName;
  }
  
  /**
   * return attribute def name for groupAttrReaders
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapGroupAttrReadersAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapGroupAttrReadersName(), true);
  }
  
  
  /** Attribute name of groupAttrUpdaters  */
  public static final String ATTR_DEF_EXTENSION_LDAP_GROUP_ATTR_UPDATERS = "grouperLoaderLdapGroupAttrUpdaters";

  /** attribute def name of groupAttrUpdaters */
  private static String grouperLoaderLdapGroupAttrUpdatersName;

  /**
   * attribute def name of groupAttrUpdaters
   * @return name
   */
  public static String grouperLoaderLdapGroupAttrUpdatersName() {
    if (grouperLoaderLdapGroupAttrUpdatersName == null) {
      grouperLoaderLdapGroupAttrUpdatersName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_GROUP_ATTR_UPDATERS;
    }
    return grouperLoaderLdapGroupAttrUpdatersName;
  }
  
  /**
   * return attribute def name for groupAttrUpdaters
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapGroupAttrUpdatersAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapGroupAttrUpdatersName(), true);
  }
}
