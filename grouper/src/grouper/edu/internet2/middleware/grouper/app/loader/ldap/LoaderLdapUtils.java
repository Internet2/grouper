/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader.ldap;

import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;


/**
 * utility methods and constants for ldap loader
 */
public class LoaderLdapUtils {

  /** extension of the attribute def name for the marker attribute for grouper loader */
  public static final String ATTR_DEF_EXTENSION_MARKER = "grouperLoaderLdap";

  /** loader ldap def extension */
  public static final String LOADER_LDAP_DEF = "grouperLoaderLdapDef";

  /** loader ldap value def extension */
  public static final String LOADER_LDAP_VALUE_DEF = "grouperLoaderLdapValueDef";

  /** extension of the attribute def name for type of ldap loader (e.g. LDAP_SIMPLE) */
  public static final String ATTR_DEF_EXTENSION_TYPE = "grouperLoaderLdapType";

  /** extension of the attribute def name for type of ldap loader (e.g. LDAP_SIMPLE) */
  public static final String ATTR_DEF_EXTENSION_SERVER_ID = "grouperLoaderLdapServerId";

  /** extension of attribute def name for ldap filter to run to find the objects that have the subject id */
  public static final String ATTR_DEF_EXTENSION_LDAP_FILTER = "grouperLoaderLdapFilter";

  /** extension of attribute def name for the name of the attribute in the ldap object 
   * that is returned by the ldap filter which has the subject
   * id or identifier in it.  e.g. hasMember */
  public static final String ATTR_DEF_EXTENSION_SUBJECT_ATTRIBUTE = "grouperLoaderLdapSubjectAttribute";

  /** extension of the attribute def name for type of ldap loader (e.g. LDAP_SIMPLE) */
  public static final String ATTR_DEF_EXTENSION_SOURCE_ID = "grouperLoaderLdapSourceId";

  /** extension of the attribute def name for subjectId, subjectIdentifier, or subjectIdOrIdentifier (default) */
  public static final String ATTR_DEF_EXTENSION_SUBJECT_ID_TYPE = "grouperLoaderLdapSubjectIdType";

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
