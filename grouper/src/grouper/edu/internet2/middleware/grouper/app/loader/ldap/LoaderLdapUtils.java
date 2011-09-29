/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader.ldap;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


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

  /** extension of the attribute def name for type of ldap loader (e.g. LDAP_SIMPLE) */
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


}
