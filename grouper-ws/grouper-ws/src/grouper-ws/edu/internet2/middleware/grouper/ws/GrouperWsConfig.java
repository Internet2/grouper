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
 * 
 */
package edu.internet2.middleware.grouper.ws;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

/**
 * config constants for WS
 * 
 * @author mchyzer
 * 
 */
public final class GrouperWsConfig extends ConfigPropertiesCascadeBase {

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperClientConfig retrieveConfig() {
    return retrieveConfig(GrouperClientConfig.class);
  }

  /**
   * Get a Grouper configuration parameter.
   * 
   * <pre class="eg">
   * String wheel = GrouperConfig.getProperty(&quot;groups.wheel.group&quot;);
   * </pre>
   * 
   * @param property to lookup
   * @return Value of configuration parameter or an empty string if parameter
   *         is invalid.
   * @since 1.1.0
   * @deprecated use GrouperWsConfig.retrieveConfig().propertyValueString instead
   */
  @Deprecated
  public static String getPropertyString(String property) {
    return retrieveConfig().propertyValueString(property, "");
  }

  /**
   * get all properties
   * @return all properties
   * @deprecated use GrouperWsConfig.retrieveConfig().properties()
   */
  @Deprecated
  public static Properties getProperties() {
    return retrieveConfig().properties();
  }
  
  /**
   * Get a Grouper configuration parameter.
   * 
   * <pre class="eg">
   * String wheel = GrouperConfig.getProperty(&quot;groups.wheel.group&quot;);
   * </pre>
   * 
   * @param property to lookup
   * @param defaultValue is the value if the property isnt found
   * @return Value of configuration parameter or the default value (will trim the value)
   * @since 1.1.0
   * @deprecated use GrouperWsConfig.retrieveConfig().propertyValueString instead
   */
  @Deprecated
  public static String getPropertyString(String property, String defaultValue) {
    return retrieveConfig().propertyValueString(property, defaultValue);
  }

  /**
   * Get a Grouper configuration parameter an integer
   * 
   * @param property to lookup
   * @param defaultValue of the int if not there
   * @return Value of configuration parameter or null if parameter isnt
   *         specified. Exception is thrown if not formatted correcly
   * @deprecated use GrouperWsConfig.retrieveConfig().propertyValueInt instead
   */
  @Deprecated
  public static int getPropertyInt(String property, int defaultValue) {
    return retrieveConfig().propertyValueInt(property, defaultValue);
  }

  /**
   * Get a Grouper configuration parameter as boolean (must be true|t|false|f
   * case-insensitive)
   * 
   * @param property to lookup
   * @param defaultValue if the property is not there
   * @return Value of configuration parameter or null if parameter isnt
   *         specified. Exception is thrown if not formatted correcly
   * @deprecated use GrouperWsConfig.retrieveConfig().propertyValueBoolean instead
   */
  @Deprecated
  public static boolean getPropertyBoolean(String property, boolean defaultValue) {
    return GrouperWsConfig.retrieveConfig().propertyValueBoolean(property, defaultValue);
  }

  /**
   * name of param for add member web service max, default is 1000000
   *  # Max number of subjects to be able to pass to addMember service,
   * default is 1000000 ws.add.member.subjects.max = 20000
   * 
   */
  public static final String WS_ADD_MEMBER_SUBJECTS_MAX = "ws.add.member.subjects.max";

  /**
   * name of param for member change subject web service max, default is 1000000
   *  # Max number of members to pass to memberChangeSubject,
   * default is 1000000 ws.member.change.subject.max = 20000
   * 
   */
  public static final String WS_MEMBER_CHANGE_SUBJECT_MAX = "ws.member.change.subject.max";

  /**
   * name of param for get groups web service max, default is 1000000
   *  # Max number of subjects to be able to pass to getGroups service,
   * default is 1000000 ws.get.groups.subjects.max = 20000
   * 
   */
  public static final String WS_GET_GROUPS_SUBJECTS_MAX = "ws.get.groups.subjects.max";

  /**
   * name of param for group delete, max groups to be able to delete at once,
   * default is 1000000
   *  # Max number of groups to be able to pass to groupDelete service,
   * default is 1000000 ws.group.delete.max = 20000
   * 
   */
  public static final String WS_GROUP_DELETE_MAX = "ws.group.delete.max";

  /**
   * name of param for attributedefname delete, max attributedefnames to be able to delete at once,
   * default is 1000000
   *  # Max number of attributedefnames to be able to pass to attributedefnameDelete service,
   * default is 1000000 ws.attributedefname.delete.max = 20000
   * 
   */
  public static final String WS_ATTRIBUTE_DEF_NAME_DELETE_MAX = "ws.attributeDefName.delete.max";

  /**
   * name of param for group save, max groups to be able to save at once,
   * default is 1000000
   *  # Max number of groups to be able to pass to groupSave service,
   * default is 1000000 ws.group.save.max = 20000
   * 
   */
  public static final String WS_GROUP_SAVE_MAX = "ws.group.save.max";

  /**
   * name of param for attributeDefName save, max attributeDefNames to be able to save at once,
   * default is 1000000
   *  # Max number of attributeDefNames to be able to pass to attributeDefNameSave service,
   * default is 1000000 ws.attributeDefName.save.max = 20000
   * 
   */
  public static final String WS_ATTRIBUTE_DEF_NAME_SAVE_MAX = "ws.attributeDefName.save.max";

  /**
   * name of param for stem delete, max stems to be able to delete at once,
   * default is 1000000
   *  # Max number of stems to be able to pass to stemDelete service,
   * default is 1000000 ws.stem.delete.max = 20000
   * 
   */
  public static final String WS_STEM_DELETE_MAX = "ws.stem.delete.max";

  
  
  
  /**
   * name of param for assignAttributeDefNameInheritance save, max assignAttributeDefNameInheritance to be able to save at once,
   * default is 1000000
   *  # Max number of assignAttributeDefNameInheritance to be able to pass to assignAttributeDefNameInheritance service,
   * default is 1000000 ws.assign.attributeDefName.inheritance.max = 20000
   * 
   */
  public static final String WS_ASSIGN_ATTRIBUTE_DEF_NAME_INHERITANCE_MAX = "ws.assign.attributeDefName.inheritance.max";

  /**
   * name of param for stem save, max stems to be able to save at once,
   * default is 1000000
   *  # Max number of stems to be able to pass to stemSave service,
   * default is 1000000 ws.stem.save.max = 20000
   * 
   */
  public static final String WS_STEM_SAVE_MAX = "ws.stem.save.max";

  /**
   * name of param for group attribute, max groups to be able to view/edit attributes at once,
   * default is 1000000
   *  # Max number of subjects to be able to pass to addMember service,
   * default is 1000000 ws.group.save.max = 20000
   * 
   */
  public static final String WS_GROUP_ATTRIBUTE_MAX = "ws.group.attribute.max";

  /**
   * name of param for delete member web service max, default is 1000000
   *  # Max number of subjects to be able to pass to deleteMember service,
   * default is 1000000 ws.delete.member.subjects.max = 20000
   * 
   */
  public static final String WS_DELETE_MEMBER_SUBJECTS_MAX = "ws.delete.member.subjects.max";

  /**
   * name of param for has member web service max, default is 1000000
   *  # Max number of subjects to be able to pass to addMember service,
   * default is 1000000 ws.has.member.subjects.max = 20000
   */
  public static final String WS_HAS_MEMBER_SUBJECTS_MAX = "ws.has.member.subjects.max";

  /**
   * name of param for subject result attribute names when extended data is requested
   *  # subject result attribute names when extended data is requested (comma separated)
   * default is name, description
   * note, these will be in addition to ws.subject.result.attribute.names
   */
  public static final String WS_SUBJECT_RESULT_DETAIL_ATTRIBUTE_NAMES = "ws.subject.result.detail.attribute.names";

  /**
   * subject attribute names to send back when a WsSubject is sent, comma separated
   * e.g. name, netid
   * default is none
   */
  public static final String WS_SUBJECT_RESULT_ATTRIBUTE_NAMES = "ws.subject.result.attribute.names";

  /** when packing things in a single param, this is the separator */
  public static final String WS_SEPARATOR = "::::";

  /**
   * name of param
   * # Web service users who are in the following group can use the actAs field to act as someone else
   * # You can put multiple groups separated by commas.  e.g. a:b:c, e:f:g
   * # You can put a single entry as the group the calling user has to be in, and the grouper the actAs has to be in
   * # separated by 4 colons
   * # e.g. if the configured values is:       a:b:c, e:f:d :::: r:e:w, x:e:w
   * # then if the calling user is in a:b:c or x:e:w, then the actAs can be anyone
   * # if not, then if the calling user is in e:f:d, then the actAs must be in r:e:w.  If multiple rules, then 
   * # if one passes, then it is a success, if they all fail, then fail.
   *
   * field to act as someone else ws.act.as.group = aStem:aGroup
   */
  public static final String WS_ACT_AS_GROUP = "ws.act.as.group";

  /**
   * name of param: ws.act.as.cache.seconds
   * cache the decision to allow a user to actAs another, so it doesnt have to be calculated each time
   * defaults to 30 minutes: 
   */
  public static final String WS_ACT_AS_CACHE_MINUTES = "ws.act.as.cache.minutes";

  /**
   * name of param: ws.logged.in.subject.default.source
   * if you have subject namespace overlap (or not), set the default subject 
   * source to lookup the user if none specified in user name
   */
  public static final String WS_LOGGED_IN_SUBJECT_DEFAULT_SOURCE = "ws.logged.in.subject.default.source";

  /**
   * name of param for save privileges web service max, default is 1000000
   *  # Max number of subjects to be able to pass to savePrivileges service,
   * default is 1000000 ws.view.or.edit.privileges.subjects.max = 20000
   */
  public static final String WS_VIEW_OR_EDIT_PRIVILEGES_SUBJECTS_MAX = "ws.view.or.edit.privileges.subjects.max";

  /**
   * name of param: ws.rest.default.response.content.type
   * if the request has no content type (http params), and the response content type is not
   * specified in the url, then put it here.  must be a valid value of WsLiteResponseContentType
   * defaults to xhtml if blank
   */
  public static final String WS_REST_DEFAULT_RESPONSE_CONTENT_TYPE 
    = "ws.rest.default.response.content.type";
  
  /**
   * to provide custom authentication (instead of the default httpServletRequest.getUserPrincipal()
   * for non-Rampart authentication.  Class must implement the interface:
   * edu.internet2.middleware.grouper.ws.security.WsCustomAuthentication
   * class must be fully qualified.  e.g. edu.school.whatever.MyAuthenticator
   * blank means use default: edu.internet2.middleware.grouper.ws.security.WsGrouperDefaultAuthentication
   * ws.security.non-rampart.authentication.class = 
   */
  public static final String WS_SECURITY_NON_RAMPART_AUTHENTICATION_CLASS = 
    "ws.security.non-rampart.authentication.class";

  /**
   * to provide rampart authentication, Class must implement the interface:
   * edu.internet2.middleware.grouper.ws.security.GrouperWssecAuthentication
   * class must be fully qualified.  e.g. edu.school.whatever.MyAuthenticator
   * blank means rampart will throw 404 status code
   */
  public static final String WS_SECURITY_RAMPART_AUTHENTICATION_CLASS = 
    "ws.security.rampart.authentication.class";
  
  /**
   * name of param: ws.client.user.group.name
   * If there is an entry here for group name, then all web service client 
   * users must be in this group (before the actAs)
   * e.g. etc:webServiceClientUsers
   */
  public static final String WS_CLIENT_USER_GROUP_NAME = "ws.client.user.group.name";

  /**
   * name of param: ws.client.user.group.cache.minutes
   * cache the decision to allow a user to user web services, so it doesnt have to be calculated each time
   * defaults to 30 minutes: 
   */
  public static final String WS_CLIENT_USER_GROUP_CACHE_MINUTES = "ws.client.user.group.cache.minutes";

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues
   */
  @Override
  public void clearCachedCalculatedValues() {
    //nothing to do at this point
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "ws.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper-ws.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper-ws.example.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "ws.config.secondsBetweenUpdateChecks";
  }


}
