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
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapName(), true);
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

  
  

  
  
  /** Attribute name of display extension expression  */
  public static final String ATTR_DEF_EXTENSION_LDAP_GROUP_DISPLAY_EXTENSION_EXPRESSION = "grouperLoaderLdapGroupDisplayExtensionExpression";

  /** attribute def name of display extension expression */
  private static String grouperLoaderLdapGroupDisplayExtensionExpressionName;

  /**
   * attribute def name of group display extension expression
   * @return name
   */
  public static String grouperLoaderLdapGroupDisplayExtensionExpressionName() {
    if (grouperLoaderLdapGroupDisplayExtensionExpressionName == null) {
      grouperLoaderLdapGroupDisplayExtensionExpressionName = grouperLoaderLdapStemName() + ":" + ATTR_DEF_EXTENSION_LDAP_GROUP_DISPLAY_EXTENSION_EXPRESSION;
    }
    return grouperLoaderLdapGroupDisplayExtensionExpressionName;
  }
  
  /**
   * return attribute def name for group dislpay extension expression
   * @return attribute def name
   */
  public static AttributeDefName grouperLoaderLdapGroupDisplayExtensionExpressionAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(grouperLoaderLdapGroupDisplayExtensionExpressionName(), true);
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
          String customElClasses = GrouperLoaderConfig.getPropertyString("loader.ldap.el.classes", false);

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
    
    //dont be lenient on undefined variables
    String result = GrouperUtil.substituteExpressionLanguage(expression, loaderEnvVars, false, false, false);
    
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

  

    
  
}
