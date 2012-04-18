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
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapContext;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.digester.CallMethodRule;
import org.apache.commons.digester.Digester;
import org.opensaml.util.resource.PropertyReplacementResourceFilter;
import org.opensaml.util.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.ConfigurationException;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter.OnNotFound;

/**
 * Class for accessing values from the Auth2Ldap configuration file.
 */
public class ConfigManager implements LdappcConfig {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);

  /**
   * Default configuration file resource name.
   */
  public static final String CONFIG_FILE_RESOURCE = "ldappc.xml";

  /**
   * Default properties file resource name.
   */
  public static final String PROPERTIES_FILE_RESOURCE = "ldappc.properties";

  /**
   * Configuration Schema file resource name.
   */
  public static final String SCHEMA_FILE_RESOURCE = "schema/ldappc.schema.xsd";

  /**
   * JAXP schema language property name.
   */
  public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

  /**
   * JAXP schema source property name.
   */
  public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

  /**
   * JAXP schema language value for W3C XML Schema.
   */
  public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

  /**
   * Property which defines whether or not the range search result handler is used
   */
  public static final String PROPERTY_USE_RANGE_HANDLER = "edu.internet2.middleware.ldappc.useRangeSearchResultHandler";

  /**
   * Flag indicating to get data from ldappc.properties instead of ldappc.xml.
   */
  // private static final String GET_FROM_PROPERTIES_FILE = "GetFromPropertiesFile";

  /**
   * List of SAXParseException objects created while parsing the configuration. file
   */
  private Vector saxParseErrors = new Vector();

  /**
   * Hashtable of LDAP context parameters.
   */
  private Hashtable ldapContextParameters = new Hashtable();

  /**
   * Set of the Group stems for creating subordinate stem queries.
   */
  private Set groupSubordinateStemQueries = new HashSet();

  /**
   * Group attribute name/value pairs for creating matching queries.
   */
  private Map groupAttrMatchingQueries = new Hashtable();

  /**
   * Set of the data connector ids to return the groups to be provisioned.
   */
  private Set<String> groupResolverQueries = new HashSet<String>();

  /**
   * Group DN Structure.
   */
  private GroupDNStructure groupDnStructure;

  /**
   * Group DN root OU.
   */
  private String groupDnRoot;

  /**
   * Group DN object class.
   */
  private String groupDnObjectClass;

  /**
   * Group RDN attribute name.
   */
  private String groupDnRdnAttribute;

  /**
   * Grouper attribute whose value is the Group DN RDN value. Must have a value of
   * {@link edu.internet2.middleware.ldappc.LdappcConfig#GROUPER_ID_ATTRIBUTE} or
   * {@link edu.internet2.middleware.ldappc.LdappcConfig#GROUPER_NAME_ATTRIBUTE} .
   */
  private String groupDnGrouperAttribute;

  /**
   * Object class a Group entry must have to support the Group attribute to LDAP attribute
   * mapping.
   */
  private Set<String> groupAttributeMappingObjectClass;

  /**
   * Group attribute name to LDAP attribute name mapping.
   */
  private Map<String, List<String>> groupAttributeMapping = new HashMap<String, List<String>>();

  /**
   * Associated empty values for the ldap attributes defined in the group attribute
   * mapping key = ldap attribute in upper case, value = empty value (Must be a HashMap to
   * allow null values).
   */
  private Map groupAttributeMappingLdapEmptyValues = new HashMap();

  /**
   * Estimate of size of groups to populate. This is automatically initialized to zero.
   */
  private int groupHashEstimate;

  /**
   * Source to Subject naming attribute mapping for Source Subject Identfiers.
   */
  private Map sourceSubjectNamingAttributes = new Hashtable();

  /**
   * Source to Subject LDAP search filter mapping for Source Subject Identfiers.
   */
  private Map<String, LdapSearchFilter> sourceSubjectLdapFilters = new Hashtable<String, LdapSearchFilter>();

  /**
   * Estimate of size of groups to populate.
   */
  private Map subjectHashEstimates = new Hashtable();

  /**
   * Boolean indicating whether Member LDAP entries contain an attribute holding a listing
   * the Groups to which they belong.
   */
  private boolean memberGroupsListed;

  /**
   * Member LDAP entry object class that contains the attribute t.o hold the list of
   * Groups.
   */
  private String memberGroupsListObjectClass;

  /**
   * Member LDAP entry attribute containing the list of Groups to which it belongs.
   */
  private String memberGroupsListAttribute;

  /**
   * Value placed in member groups listattribute when no groups are stored there.
   */
  // private String memberGroupsListEmptyValue;
  /**
   * Directory for the membership updates file.
   */
  private String memberGroupsListTemporaryDirectory;

  /**
   * Grouper Group naming attribute used to construct the list of Groups to which a Member
   * belongs.
   */
  private String memberGroupsNamingAttribute;

  /**
   * Boolean indicating whether Group LDAP entries contain an attribute holding a listing
   * the Members LDAP entry DN which belong to it.
   */
  private boolean groupMembersDnListed;

  /**
   * Group LDAP entry object class containing the attribute to list members LDAP entry DN.
   */
  private String groupMembersDnListObjectClass;

  /**
   * Group LDAP entry attribute containing the list of Members LDAP entry DNs which belong
   * to it.
   */
  private String groupMembersDnListAttribute;

  /**
   * Value placed in group members dn list attribute when no DNs are stored there.
   */
  private String groupMembersDnListEmptyValue;

  /**
   * Boolean indicating whether Group LDAP entries contain an attribute holding a listing
   * the Members names which belong to it.
   */
  private boolean groupMembersNameListed;

  /**
   * Group LDAP entry object class containing the attribute to list members names.
   */
  private String groupMembersNameListObjectClass;

  /**
   * Group LDAP entry attribute containing the list of Members names which belong to it.
   */
  private String groupMembersNameListAttribute;

  /**
   * Value placed in group members name list attribute when no DNs are stored there.
   */
  private String groupMembersNameListEmptyValue;

  /**
   * Source to Subject attribute mapping used to construct a Member's name in the list of
   * Member names belonging to the Group.
   */
  private Map groupMembersNameListNamingAttributes = new Hashtable();

  /**
   * Boolean indicating if the ldappc element was found while parsing.
   */
  private boolean rootElementFound;

  /**
   * Boolean indicating if groups should be created without members followed by
   * modifications which add member attributes
   */
  private boolean createGroupsThenModifyMembers = false;

  /**
   * Boolean indicating if member groups should be provisioned as members.
   */
  private boolean provisionMemberGroups = true;

  /**
   * Boolean indicating if groups should be provisioned in two steps, first all groups
   * without members, and second all groups with members.
   */
  private boolean provisionGroupsTwoStep = true;

  /**
   * Boolean indicating if a group's attribute modifications should be bundled.
   */
  private boolean bundleModifications = true;

  /**
   * Object class a Group entry must have to support the AttributeResolver attribute to
   * LDAP attribute mapping.
   */
  private Set<String> attributeResolverMappingObjectClass;

  /**
   * AttributeResolver attribute name to LDAP attribute name mapping.
   */
  private Map<String, List<String>> attributeResolverMapping = new HashMap<String, List<String>>();

  /**
   * Associated empty values for the ldap attributes defined in the AttributeResolver
   * attribute mapping key = ldap attribute in upper case, value = empty value (Must be a
   * HashMap to allow null values).
   */
  private Map attributeResolverMappingLdapEmptyValues = new HashMap();

  /**
   * Boolean indicating if the RangeSearchResultHandler should be used.
   */
  private boolean useRangeSearchResultHandler = false;

  /**
   * Boolean indicating if member groups should be provisioned as members even if they are
   * not selected to be provisioned by group-queries.
   */
  private boolean provisionMemberGroupsIgnoreQueries = false;

  public ConfigManager() throws ConfigurationException {

    this(null);
  }

  /**
   * Constructs an instance of ConfigManager using the configuration file identified by
   * the uri.
   * 
   * Constructor is private to enforce singleton pattern.
   * 
   * @param pathToConfig
   *          path to configuration file
   * @throws ConfigurationException
   *           thrown if an error occurs loading the instance from the given uri.
   * 
   */
  public ConfigManager(String pathToConfig) throws ConfigurationException {
    this(pathToConfig, null);
  }

  /**
   * Constructs an instance of ConfigManager using the given configuration file and
   * properties file
   * 
   * @param pathToConfig
   *          path to configuration file
   * @param ldappcPropertiesFile
   *          path to properties file
   * @throws ConfigurationException
   */
  public ConfigManager(String pathToConfig, String pathToProperties)
      throws ConfigurationException {
    //
    // Initialize the ConfigManager instance
    //
    if (pathToConfig == null) {
      pathToConfig = getSystemResourceURL(CONFIG_FILE_RESOURCE, true).getPath();
    }

    if (pathToProperties == null) {
      pathToProperties = getSystemResourceURL(PROPERTIES_FILE_RESOURCE, true).getPath();
    }

    File propertiesFile = new File(pathToProperties);
    if (!propertiesFile.exists() || !propertiesFile.canRead()) {
      LOG.error("Unable to read properties file '" + pathToProperties + "'");
      throw new LdappcException("Unable to find properties file '" + pathToProperties
          + "'");
    }

    File configFile = new File(pathToConfig);
    if (!configFile.exists() || !configFile.canRead()) {
      LOG.error("Unable to read config file '" + pathToConfig + "'");
      throw new ConfigurationException("Unable to locate config file '" + pathToConfig
          + "'");
    }

    init(configFile, propertiesFile);
  }

  /**
   * Initializes the ConfigManager instance from a configuration file and a properties
   * file.
   * 
   * @param config
   *          the configuration File
   * @param properties
   *          the properties File
   * @throws ConfigurationException
   */
  private void init(File config, File properties) throws ConfigurationException {
    LOG.debug("reading configuration from '{}' and properties from '{}'", config,
        properties);
    //
    // Build a Digester for processing the config file
    //
    Digester digester = buildDigester();

    //
    // Push the ConfigManager Broker onto the stack. The broker is used
    // to provide the Digester with access to the necessary methods of the
    // ConfigManager regardless of visibility.
    //
    digester.push(this.new Broker());

    //
    // Set the rules for parsing the config file
    //
    String elementPath = null;

    // Validate sax parsing and parameter values once ldappc
    // element is complete
    elementPath = "ldappc";
    digester.addRule(elementPath, new CallMethodRule("foundRootElement"));
    digester.addRule(elementPath, new CallMethodRule("validateSaxParsing"));
    digester.addRule(elementPath, new CallMethodRule("validateValues"));

    // Save the LDAP parameter values
    elementPath = "ldappc/ldap/context/parameter-list/parameter";
    digester.addCallMethod(elementPath, "addLdapContextParameter", 2);
    digester.addCallParam(elementPath, 0, "name");
    digester.addCallParam(elementPath, 1, "value");

    // Save the Group subordinate stem queries
    elementPath = "ldappc/grouper/group-queries/subordinate-stem-queries/stem-list/stem";
    digester.addCallMethod(elementPath, "addGroupSubordinateStemQuery", 0);

    // Save the Group attribute match queries
    elementPath = "ldappc/grouper/group-queries/attribute-matching-queries/attribute-list/attribute";
    digester.addCallMethod(elementPath, "addGroupAttrMatchQuery", 2);
    digester.addCallParam(elementPath, 0, "name");
    digester.addCallParam(elementPath, 1, "value");
    
    // Save the Group resolver data connector queries
    elementPath = "ldappc/grouper/group-queries/resolver-matching-queries/data-connector-list/data-connector";
    digester.addCallMethod(elementPath, "addResolverQuery", 1);
    digester.addCallParam(elementPath, 0, "id");

    // Save the Source Subject Identifier naming attribute
    elementPath = "ldappc/source-subject-identifiers/source-subject-identifier";
    digester.addCallMethod(elementPath, "addSourceSubjectNamingAttribute", 2);
    digester.addCallParam(elementPath, 0, "source");
    digester.addCallParam(elementPath, 1, "subject-attribute");

    // Save the Subject hash table estimate
    elementPath = "ldappc/source-subject-identifiers/source-subject-identifier";
    digester.addCallMethod(elementPath, "addSubjectHashEstimate", 2);
    digester.addCallParam(elementPath, 0, "source");
    digester.addCallParam(elementPath, 1, "initial-cache-size");

    // Save the Source Subject Identifier LDAP filters
    elementPath = "ldappc/source-subject-identifiers/source-subject-identifier";
    digester.addCallMethod(elementPath, "addSourceSubjectLdapFilter", 6);
    digester.addCallParam(elementPath, 0, "source");

    elementPath = "ldappc/source-subject-identifiers/source-subject-identifier/ldap-search";
    digester.addCallParam(elementPath, 1, "base");
    digester.addCallParam(elementPath, 2, "scope");
    digester.addCallParam(elementPath, 3, "filter");
    digester.addCallParam(elementPath, 4, "on-not-found");
    digester.addCallParam(elementPath, 5, "multiple-results");

    // Save the Group DN structure parameters
    elementPath = "ldappc/grouper/groups";
    digester.addCallMethod(elementPath, "setGroupDnStructure", 1);
    digester.addCallParam(elementPath, 0, "structure");

    digester.addCallMethod(elementPath, "setGroupDnRoot", 1);
    digester.addCallParam(elementPath, 0, "root-dn");

    digester.addCallMethod(elementPath, "setGroupDnObjectClass", 1);
    digester.addCallParam(elementPath, 0, "ldap-object-class");

    digester.addCallMethod(elementPath, "setGroupDnRdnAttribute", 1);
    digester.addCallParam(elementPath, 0, "ldap-rdn-attribute");

    digester.addCallMethod(elementPath, "setGroupDnGrouperAttribute", 1);
    digester.addCallParam(elementPath, 0, "grouper-attribute");

    digester.addCallMethod(elementPath, "setGroupHashEstimate", 1);
    digester.addCallParam(elementPath, 0, "initial-cache-size");

    digester.addCallMethod(elementPath, "setCreateGroupThenModifyMembers", 1);
    digester.addCallParam(elementPath, 0, "create-then-modify-members");

    digester.addCallMethod(elementPath, "setProvisionMemberGroups", 1);
    digester.addCallParam(elementPath, 0, "provision-member-groups");

    digester.addCallMethod(elementPath, "setProvisionMemberGroupsIgnoreQueries", 1);
    digester.addCallParam(elementPath, 0, "provision-member-groups-ignore-queries");

    digester.addCallMethod(elementPath, "setProvisionGroupsTwoStep", 1);
    digester.addCallParam(elementPath, 0, "provision-groups-two-step");

    digester.addCallMethod(elementPath, "setBundleModifications", 1);
    digester.addCallParam(elementPath, 0, "bundle-modifications");

    // Save the Member Group Listing parameters
    elementPath = "ldappc/grouper/memberships/member-groups-list";
    digester.addCallMethod(elementPath, "listMemberGroups");

    digester.addCallMethod(elementPath, "setMemberGroupsListObjectClass", 1);
    digester.addCallParam(elementPath, 0, "list-object-class");

    digester.addCallMethod(elementPath, "setMemberGroupsListAttribute", 1);
    digester.addCallParam(elementPath, 0, "list-attribute");

    digester.addCallMethod(elementPath, "setMemberGroupsNamingAttribute", 1);
    digester.addCallParam(elementPath, 0, "naming-attribute");

    // digester.addCallMethod(elementPath, "setMemberGroupsListEmptyValue", 1);
    // digester.addCallParam(elementPath, 0, "list-empty-value");

    digester.addCallMethod(elementPath, "setMemberGroupsListTemporaryDirectory", 1);
    digester.addCallParam(elementPath, 0, "temporary-directory");

    // Save the Group Member DN Listing parameters
    elementPath = "ldappc/grouper/groups/group-members-dn-list";
    digester.addCallMethod(elementPath, "listGroupMembersDn");

    digester.addCallMethod(elementPath, "setGroupMembersDnListObjectClass", 1);
    digester.addCallParam(elementPath, 0, "list-object-class");

    digester.addCallMethod(elementPath, "setGroupMembersDnListAttribute", 1);
    digester.addCallParam(elementPath, 0, "list-attribute");

    digester.addCallMethod(elementPath, "setGroupMembersDnListEmptyValue", 1);
    digester.addCallParam(elementPath, 0, "list-empty-value");

    // Save the Group Member Name Listing parameters
    elementPath = "ldappc/grouper/groups/group-members-name-list";
    digester.addCallMethod(elementPath, "listGroupMembersName");

    digester.addCallMethod(elementPath, "setGroupMembersNameListObjectClass", 1);
    digester.addCallParam(elementPath, 0, "list-object-class");

    digester.addCallMethod(elementPath, "setGroupMembersNameListAttribute", 1);
    digester.addCallParam(elementPath, 0, "list-attribute");

    digester.addCallMethod(elementPath, "setGroupMembersNameListEmptyValue", 1);
    digester.addCallParam(elementPath, 0, "list-empty-value");

    // Save the Group Members Name List naming attributes
    elementPath = "ldappc/grouper/groups/group-members-name-list/source-subject-name-mapping/source-subject-name-map";
    digester.addCallMethod(elementPath, "addGroupMembersNameListNamingAttribute", 2);
    digester.addCallParam(elementPath, 0, "source");
    digester.addCallParam(elementPath, 1, "subject-attribute");

    // Save the Group Attribute Mapping parameters
    elementPath = "ldappc/grouper/groups/group-attribute-mapping";
    digester.addCallMethod(elementPath, "setGroupAttributeMappingObjectClass", 1);
    digester.addCallParam(elementPath, 0, "ldap-object-class");

    elementPath = "ldappc/grouper/groups/group-attribute-mapping/group-attribute-map";
    digester.addCallMethod(elementPath, "addGroupAttributeMapping", 2);
    digester.addCallParam(elementPath, 0, "group-attribute");
    digester.addCallParam(elementPath, 1, "ldap-attribute");

    elementPath = "ldappc/grouper/groups/group-attribute-mapping/group-attribute-map";
    digester.addCallMethod(elementPath, "addGroupAttributeMappingLdapEmptyValue", 2);
    digester.addCallParam(elementPath, 0, "ldap-attribute");
    digester.addCallParam(elementPath, 1, "ldap-attribute-empty-value");

    // Save the Attribute Resolver Mapping parameters
    elementPath = "ldappc/grouper/groups/resolver-attribute-mapping";
    digester.addCallMethod(elementPath, "setAttributeResolverMappingObjectClass", 1);
    digester.addCallParam(elementPath, 0, "ldap-object-class");

    elementPath = "ldappc/grouper/groups/resolver-attribute-mapping/resolver-attribute-map";
    digester.addCallMethod(elementPath, "addAttributeResolverMapping", 2);
    digester.addCallParam(elementPath, 0, "resolver-attribute");
    digester.addCallParam(elementPath, 1, "ldap-attribute");

    elementPath = "ldappc/grouper/groups/resolver-attribute-mapping/resolver-attribute-map";
    digester.addCallMethod(elementPath, "addAttributeResolverMappingLdapEmptyValue", 2);
    digester.addCallParam(elementPath, 0, "ldap-attribute");
    digester.addCallParam(elementPath, 1, "ldap-attribute-empty-value");

    //
    // Parse the config file
    //
    try {
      //
      // Only expand macros if the properties file is not empty
      //
      Properties props = new Properties();
      props.load(new FileInputStream(properties));
      if (props.isEmpty()) {
        digester.parse(config);
      } else {
        PropertyReplacementResourceFilter prf = new PropertyReplacementResourceFilter(
            properties);
        digester.parse(prf.applyFilter(new FileInputStream(config)));
      }

    } catch (SAXException se) {
      // 
      // Fatal if file can't be parsed
      //
      LOG.error("An error occurred", se);
      throw new ConfigurationException(se);
    } catch (IOException ioe) {
      // 
      // Fatal if a file can't be read
      //
      LOG.error("An error occurred", ioe);
      throw new ConfigurationException(ioe);
    } catch (ResourceException e) {
      // 
      // Fatal if file can't be filtered
      //
      LOG.error("An error occurred", e);
      throw new ConfigurationException(e);
    }

    //
    // If make it here, verify that the ldappc element was found. A valid
    // xml document
    // without an ldappc element will appear to be a valid configuration
    // file.
    //
    if (!isRootElementFound()) {
      throw new ConfigurationException(
          "The ldappc element was not found in the configuration file.");
    }

    //
    // Properties
    //

    try {
      Properties props = new Properties();
      props.load(new FileInputStream(properties));
      useRangeSearchResultHandler = GrouperUtil.propertiesValueBoolean(props,
          PROPERTY_USE_RANGE_HANDLER, false);
    } catch (FileNotFoundException e) {
      throw new ConfigurationException("Unable to find the properties file.", e);
    } catch (IOException e) {
      throw new ConfigurationException("Unable to read the properties file.", e);
    }
  }

  /**
   * This method returns the list, possibly empty, of the
   * {@link org.xml.sax.SAXParseException}s created while parsing the configuration file.
   * 
   * @return {@link java.util.Vector} of {@link org.xml.sax.SAXParseException}s
   */
  public Iterator listSaxParseErrors() {
    return saxParseErrors.iterator();
  }

  /**
   * Add a {@link org.xml.sax.SAXParseException} to current list.
   * 
   * @param e
   *          {@link org.xml.sax.SAXParseException}.
   */
  private void addSaxParseError(SAXParseException e) {
    saxParseErrors.add(e);
  }

  /**
   * This method throws a {@link Auth2LdapConfigurationException} if any SAX errors
   * occured while parsing the configuration file.
   * 
   * @throws ConfigurationException
   *           thrown if validation fails.
   */
  private void validateSaxParsing() throws ConfigurationException {
    if (saxParseErrors.size() == 1) {
      throw new ConfigurationException((SAXParseException) saxParseErrors.elementAt(0));
    } else if (saxParseErrors.size() > 1) {
      throw new ConfigurationException(
          "Multiple SAXParseExceptions generated while parsing the configuration file.  See error log for details.");
    }
  }

  /**
   * This validates the values provided in the configuration file using rules that cannot
   * be defined in the schema. Note although there maybe some overlap between this and the
   * schema, this method is not intended to replace the schema validation.
   * 
   * @throws ConfigurationException
   *           thrown if validation fails.
   */
  private void validateValues() throws ConfigurationException {
    //
    // Validate the grouper-attribute attribute is defined when the
    // groups Dn structure is "flat"
    //
    if (GroupDNStructure.flat.equals(getGroupDnStructure())) {
      if (getGroupDnGrouperAttribute() == null) {
        throw new ConfigurationException(
            "The grouper-attribute must be defined when the group dn structure is "
                + GroupDNStructure.flat);
      }
    }

    // 1.5 20090525 don't allow source-subject-identifier for source "g:gsa"; now built-in
    if (getSourceSubjectNamingAttributes().containsKey(
        SubjectFinder.internal_getGSA().getId())) {
      throw new ConfigurationException(
          "The source-subject-identifier for source '"
              + SubjectFinder.internal_getGSA().getId()
              + "' should be removed. This functionality has been replaced by the provision-member-groups attribute of the <groups /> element.");
    }
  }

  /**
   * This returns a {@link java.net.URL} created by
   * {@link java.lang.ClassLoader#getSystemResource(java.lang.String)} for
   * <code>resource</code>. If the resource is not found (i.e., URL is null) and
   * <code>isRequired</code> is true, this throws a
   * {@link edu.internet2.middleware.ldappc.exception.ConfigurationException}.
   * 
   * @param resource
   *          The resource name
   * @param isRequired
   *          Boolean indicating if the resource is required
   * 
   * @return {@link java.net.URL} for the resource, possibly a <code>null</code> value.
   * @throws ConfigurationException
   *           thrown as defined above.
   */
  public static URL getSystemResourceURL(String resource, boolean isRequired)
      throws ConfigurationException {
    // 20090116 tz doesn't work, strange
    // URL url = ClassLoader.getSystemResource(resource);
    URL url = GrouperUtil.computeUrl(resource, false);
    if (isRequired && url == null) {
      //
      // Fatal error; Throw Auth2LdapRuntimeException
      //
      throw new ConfigurationException("Unable to locate required system resource: "
          + resource);
    }
    return url;
  }

  /**
   * This creates the {@link org.apache.commons.digester.Digester} used for processing the
   * Auth2Ldap configuration file. If the version of JAXP is 1.2 or greater, the digester
   * is configured to validate the configuration file using the configuration file schema.
   * 
   * @return Returns a {@link org.apache.commons.digester.Digester}
   */
  private Digester buildDigester() {
    //
    // Init the digester
    //
    Digester digester = null;

    //
    // Try to build the digester with a parser that will validate the
    // configuration file using the schema.
    //
    try {
      //
      // Get the SAXParserFactory and initialize it accordingly
      //
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setValidating(true);

      //
      // Get a new parser and try to set the properties for supporting
      // schema validation.
      //
      SAXParser parser = factory.newSAXParser();
      parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
      parser.setProperty(JAXP_SCHEMA_SOURCE, getSystemResourceURL(SCHEMA_FILE_RESOURCE,
          true).toString());

      //
      // A validating parser has been created so build the digester
      // using the validating parser, and add the SAXErrorHandler.
      //
      digester = new Digester(parser);
      digester.setErrorHandler(new SaxErrorHandler());
    } catch (Exception se) {
      //
      // If this code is reached, it wasn't possible to create a parser
      // that would validate the configuration file using the schema.
      // Nothing to do but log the exception and continue on.
      //
      LOG.error("Schema validation not supported", se);
    }

    //
    // If digester is null, weren't able to create it so it would validate.
    // Simply use the default.
    //
    if (digester == null) {
      digester = new Digester();
    }

    return digester;
  }

  /**
   * This method returns a {@link java.util.Hashtable} of the LDAP parameters defined to
   * create the {@link javax.naming.InitialContext}. Each of the parameter names from the
   * configuration file that match, ignoring case, a constant name from
   * {@link javax.naming.ldap.LdapContext} have been converted to the actual value of the
   * <code>LdapContext</code> constant. This allows the returned <code>Hashtable</code> to
   * be used directly when creating an initial context.
   * 
   * @return Hashtable with the LDAP initial context parameters.
   */
  public Hashtable getLdapContextParameters() {
    //
    // Return a copy to prevent the original from being
    // unexpectedly changed
    //
    return new Hashtable(ldapContextParameters);
  }

  /**
   * This method adds a LDAP parameter name/value pair to the LDAP parameters hashtable.
   * If the parameter name is the same, ignoring case, as one of the constants found in
   * {@link java.naming.ldap.LdapContext} then the name is converted to the value of the
   * <code>LdapContext</code> constant. This allows the <code>Hashtable</code> returned by
   * {@link #getLdapContextParameters()} to be used directly when creatin an initial
   * context.
   * 
   * @param name
   *          LDAP context parameter name
   * @param value
   *          LDAP context parameter value
   */
  private void addLdapContextParameter(String name, String value) {
    //
    // If name matches (ignoring case) the name of a
    // java.naming.ldap.LdapContext constant,
    // then convert it to the constant's value
    //
    try {
      Field constant = LdapContext.class.getField(name.toUpperCase());
      name = (String) constant.get(LdapContext.class);
    } catch (Exception e) {
      //
      // Add a debug log entry and do nothing else
      //
      LOG.debug("{} is not a valid javax.naming.ldap.LdapContext constant.", name);
    }

    // 2009-10-12 tz use property replacement filter
    //
    // Allow the SECURITY_CREDENTIALS to be obtained from the
    // auth2lap.properties file. Doing this is flagged by placing the
    // the value "GetFromPropertiesFile" in the value for the above key
    // names
    // name in the ldappc.xml file and adding values to the
    // ldappc.properties file.
    //

    // if (GET_FROM_PROPERTIES_FILE.equals(value)) {
    // if (LdapContext.SECURITY_CREDENTIALS.equals(name)) {
    // value = ResourceBundleUtil.getString("securityCredentials");
    // }
    // }

    //
    // Add the name value pair
    //
    ldapContextParameters.put(name, value);
  }

  /**
   * This method returns a possibly empty {@link java.util.Map} of the Group attribute
   * name/value pairs for creating matching queries. The key for the map is the attribute
   * name, and the value is a {@link java.util.Set} of the attribute value strings.
   * 
   * @return Map of the attribute name to the Set of values.
   */
  public Map getGroupAttrMatchingQueries() {
    //
    // Return a copy prevent the original from being
    // unexpectedly changed
    //
    return new Hashtable(this.groupAttrMatchingQueries);
  }

  /**
   * This method adds a Group attribute name/value pair to the Group attribute matching
   * queries map.
   * 
   * @param name
   *          Group attribute name
   * @param value
   *          Group attribute value
   */
  private void addGroupAttrMatchQuery(String name, String value) {
    //
    // If the name isn't already included, add it along with a new set to
    // hold the values
    //
    if (!groupAttrMatchingQueries.containsKey(name)) {
      groupAttrMatchingQueries.put(name, new HashSet());
    }

    //
    // Get the attribute's value set and add the new value
    //
    Set values = (Set) groupAttrMatchingQueries.get(name);
    values.add(value);
  }

  /**
   * This method returns a possibly empty {@link java.util.Set} of the Group stems for
   * creating subordinate stem queries.
   * 
   * @return Set of Group stem strings.
   */
  public Set getGroupSubordinateStemQueries() {
    //
    // Return a copy to prevent the original from being
    // unexpectedly changed
    //
    return new HashSet(this.groupSubordinateStemQueries);
  }

  /**
   * This method returns a new empty {@link java.util.Set} of the Group stems for creating
   * subordinate stem queries. It is used only for testing to override the ldappc.xml file
   * values.
   */
  public void resetGroupSubordinateStemQueries() {
    groupSubordinateStemQueries = new HashSet();
  }

  /**
   * This method adds a Group stem to the Group subordinate stem queries set.
   * 
   * @param stem
   *          Group stem
   */
  private void addGroupSubordinateStemQuery(String stem) {
    groupSubordinateStemQueries.add(stem);
  }

  /**
   * This returns the defined Group DN structure.
   * 
   * @return Group DN structure, either
   *         {@link edu.internet2.middleware.ldappc.LdappcConfig#GROUP_DN_FLAT} or
   *         {@link edu.internet2.middleware.ldappc.LdappcConfig#GROUP_DN_BUSHY}
   */
  public GroupDNStructure getGroupDnStructure() {
    return groupDnStructure;
  }

  /**
   * Set the Group DN structure.
   * 
   * @param structure
   * 
   */
  private void setGroupDnStructure(String structure) {
    this.groupDnStructure = GroupDNStructure.valueOf(structure);
  }

  protected void setGroupDnStructure(GroupDNStructure structure) {
    this.groupDnStructure = structure;
  }

  /**
   * This returns the DN of the root entry being used for Group DNs.
   * 
   * @return DN of the root entry
   */
  public String getGroupDnRoot() {
    return groupDnRoot;
  }

  /**
   * Set the Group DN root DN.
   * 
   * @param root
   *          DN used as the root for Group DNs
   */
  private void setGroupDnRoot(String root) {
    this.groupDnRoot = root;
  }

  /**
   * Returns the name of the object class for the Group entry when
   * {@link #getGroupDnStructure()} returns {@link LdappcConfig#GROUP_DN_FLAT} .
   * 
   * @return Name of the object class for a Group
   */
  public String getGroupDnObjectClass() {
    return groupDnObjectClass;
  }

  /**
   * Set the name of the object class for a Group entry.
   * 
   * @param objectClass
   *          Name of the object class for a Group
   */
  private void setGroupDnObjectClass(String objectClass) {
    this.groupDnObjectClass = objectClass;
  }

  /**
   * Returns the RDN attribute name for the Group entry.
   * 
   * @return RDN attribute name for the Group entry
   */
  public String getGroupDnRdnAttribute() {
    return groupDnRdnAttribute;
  }

  /**
   * Set the name of the RDN attribute for a Group entry.
   * 
   * @param attribute
   *          Name of the RDN attribute for a Group
   */
  private void setGroupDnRdnAttribute(String attribute) {
    this.groupDnRdnAttribute = attribute;
  }

  /**
   * This returns the Grouper group attribute whose value is the Group RND value.
   * 
   * @return The Grouper group attribute whose value is the RND value
   */
  public String getGroupDnGrouperAttribute() {
    return groupDnGrouperAttribute;
  }

  /**
   * Sets the name of the Grouper group value whose value is to be the RND value.
   * 
   * @param attribute
   *          Either
   *          {@link edu.internet2.middleware.ldappc.LdappcConfig#GROUPER_ID_ATTRIBUTE} or
   *          {@link edu.internet2.middleware.ldappc.LdappcConfig#GROUPER_NAME_ATTRIBUTE}
   */
  private void setGroupDnGrouperAttribute(String attribute) {
    this.groupDnGrouperAttribute = attribute;
  }

  /**
   * This gets the LDAP objectclass the Group entry must have to support the Grouper
   * attribute to LDAP attribute mapping.
   * 
   * @return LDAP object class or <code>null</code> if not defined.
   */
  public Set<String> getGroupAttributeMappingObjectClass() {
    return groupAttributeMappingObjectClass;
  }

  /**
   * This sets objectclass that a Group entry must have in order to support the Group
   * attribute to LDAP attribute.
   * 
   * @param objectClass
   *          Object class to support the Group attribute to LDAP attribute map
   */
  private void setGroupAttributeMappingObjectClass(String objectClass) {
    List<String> list = GrouperUtil.splitTrimToList(objectClass, " ");
    if (list != null) {
      this.setGroupAttributeMappingObjectClass(new LinkedHashSet<String>(list));
    }
  }

  private void setGroupAttributeMappingObjectClass(Set<String> objectClass) {
    this.groupAttributeMappingObjectClass = objectClass;
  }

  /**
   * This method returns a possibly empty {@link java.util.Map} of the Group attribute
   * name to LDAP attribute name mapping.
   * 
   * @return Map of Group attribute names to LDAP attribute names.
   */
  public Map getGroupAttributeMapping() {
    //
    // Return a copy to prevent the original from being
    // unexpectedly changed
    //
    return new Hashtable(this.groupAttributeMapping);
  }

  /**
   * This method adds a Group attribute name and LDAP attribute name pair to the Group
   * attribute map.
   * 
   * @param groupAttribute
   *          Group attribute name
   * @param ldapAttribute
   *          LDAP attribute name
   */
  private void addGroupAttributeMapping(String groupAttribute, String ldapAttribute) {
    if (!groupAttributeMapping.containsKey(groupAttribute)) {
      groupAttributeMapping.put(groupAttribute, new ArrayList<String>());
    }
    groupAttributeMapping.get(groupAttribute).add(ldapAttribute);
  }

  /**
   * This method adds an LDAP attribute name and empty value pair to the Group attribute
   * mapping ldap empty value map.
   * 
   * @param ldapAttribute
   *          LDAP attribute name
   * @param value
   *          String value or <code>null</code> if no empty value is desired
   */
  private void addGroupAttributeMappingLdapEmptyValue(String ldapAttribute, String value) {
    groupAttributeMappingLdapEmptyValues.put(convertToUpperCase(ldapAttribute), value);
  }

  /**
   * This gets the value to store in the ldap attribute if there are no grouper attribute
   * values to store there.
   * 
   * @param ldapAttribute
   *          Name of the Ldap Attribute
   * @return String to place in the ldap attribute if no Grouper attribute values are
   *         found to store there, or <code>null</code> if not defined.
   */
  public String getGroupAttributeMappingLdapEmptyValue(String ldapAttribute) {
    return (String) groupAttributeMappingLdapEmptyValues
        .get(convertToUpperCase(ldapAttribute));
  }

  /**
   * Internal helper method to convert strings to upper case.
   * 
   * @param value
   *          String to be converted
   * @return Upper case version of value or <code>null</code> if value is
   *         <code>null</code>
   */
  private String convertToUpperCase(String value) {
    return value == null ? value : value.toUpperCase();
  }

  /**
   * This returns a {@link java.util.Map} of the Source to Subject naming attribute for
   * the Source Subject identifiers.
   * 
   * @return Map of Source Subject naming attribute name/value pairs.
   */
  public Map getSourceSubjectNamingAttributes() {
    //
    // Return a copy to prevent the original from being
    // unexpectedly changed
    //
    return new Hashtable(this.sourceSubjectNamingAttributes);
  }

  /**
   * This returns the Subject naming attribute for the given Source for the Source Subject
   * identifiers.
   * 
   * @param source
   *          Source name
   * 
   * @return Subject naming attribute name or <code>null</code> if the Source is not found
   */
  public String getSourceSubjectNamingAttribute(String source) {
    String value = null;
    if (sourceSubjectNamingAttributes.containsKey(source)) {
      value = (String) sourceSubjectNamingAttributes.get(source);
    }
    return value;
  }

  /**
   * This adds a Source and Subject naming attribute name/value pair to the Source Subject
   * naming attributes map.
   * 
   * @param source
   *          Source name
   * @param attribute
   *          Subject naming attribute name
   */
  private void addSourceSubjectNamingAttribute(String source, String attribute) {
    sourceSubjectNamingAttributes.put(source, attribute);
  }

  /**
   * This returns a {@link java.util.Map} of the Source to Subject LDAP filters for the
   * Source Subject identifiers.
   * 
   * @return Map of Source Subject LDAP filter name/value pairs.
   */
  public Map<String, LdapSearchFilter> getSourceSubjectLdapFilters() {
    //
    // Return a copy to prevent the original from being
    // unexpectedly changed
    //
    return new Hashtable<String, LdapSearchFilter>(this.sourceSubjectLdapFilters);
  }

  /**
   * Sets the estimated size for a hash table listing groups to populate.
   * 
   * @param source
   *          Source name.
   * @param size
   *          Estimated cache size for this source.
   */
  public void addSubjectHashEstimate(String source, int size) {
    subjectHashEstimates.put(source, size);
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, Integer> getSourceSubjectHashEstimates() {
    return subjectHashEstimates;
  }

  /**
   * {@inheritDoc}
   */
  public int getSourceSubjectHashEstimate(String source) {
    return (Integer) subjectHashEstimates.get(source);
  }

  /**
   * This returns the Subject LDAP filter for the given Source for the Source Subject
   * identifiers.
   * 
   * @param source
   *          Source name
   * 
   * @return Subject LDAP filter or <code>null</code> if the Source is not found
   */
  public LdapSearchFilter getSourceSubjectLdapFilter(String source) {
    LdapSearchFilter value = null;
    if (sourceSubjectLdapFilters.containsKey(source)) {
      value = sourceSubjectLdapFilters.get(source);
    }
    return value;
  }

  /**
   * Sets the estimated size for a hash table listing groups to populate.
   * 
   * @param size
   *          Estimated cache size.
   */
  public void setGroupHashEstimate(int size) {
    groupHashEstimate = size;
  }

  /**
   * {@inheritDoc}
   */
  public int getGroupHashEstimate() {
    return groupHashEstimate;
  }

  /**
   * This adds a Source and Subject LDAP filter name/value pair to the Source Subject LDAP
   * filters map.
   * 
   * @param source
   *          Source name
   * @param base
   *          search base
   * @param scope
   *          search scope
   * @param filter
   *          Subject LDAP filter
   * 
   * @throws ConfigurationException
   *           thrown if an invalid scope value is encountered.
   */
  private void addSourceSubjectLdapFilter(String source, String base, String scope,
      String filter, String onNotFound, String multipleResults)
      throws ConfigurationException {
    //
    // If scope matches (ignoring case) the name of a
    // javax.naming.directory.SearchControls constant,
    // then convert it to the constant's value
    //
    int scopeValue = -1;
    try {
      Field constant = SearchControls.class.getField(scope.toUpperCase());
      scopeValue = ((Integer) constant.get(SearchControls.class)).intValue();
    } catch (Throwable t) {
      //
      // This should never happen
      //
      throw new ConfigurationException(t);
    }

    //
    // Create the LdapSearchFilter object
    //
    LdapSearchFilter ldapFilter = new LdapSearchFilter(base, scopeValue, filter,
        OnNotFound.valueOf(onNotFound), Boolean.parseBoolean(multipleResults));

    //
    // Add it to the collection
    //
    sourceSubjectLdapFilters.put(source, ldapFilter);
  }

  /**
   * This returns a boolean indicating if Member Groups list is to be maintained.
   * 
   * @return true if the Groups to which a Member belongs are listed, and false otherwise
   * 
   * @see #getMemberGroupsListAttribute()
   * @see #getMemberGroupsNamingAttribute()
   */
  public boolean isMemberGroupsListed() {
    return memberGroupsListed;
  }

  /**
   * This sets a boolean flag indicating if a Member Groups list is stored in an LDAP
   * attribute of Member.
   * 
   * @param listed
   *          Indicates if a Member Groups list is to be stored in the Member's LDAP entry
   */
  private void setMemberGroupsListed(boolean listed) {
    this.memberGroupsListed = listed;
  }

  /**
   * This sets object class the Member's LDAP entry must have to support the member groups
   * list attribute.
   * 
   * @param objectClass
   *          Object class name
   */
  private void setMemberGroupsListObjectClass(String objectClass) {
    this.memberGroupsListObjectClass = objectClass;
  }

  /**
   * This returns the object class the Member's LDAP entry must have to support the member
   * groups list attribute. If {@link #isMemberGroupsListed()} returns false, the value
   * defined here has no meaning.
   * 
   * @return Object class name or <code>null</code> if not defined.
   */
  public String getMemberGroupsListObjectClass() {
    return memberGroupsListObjectClass;
  }

  /**
   * This gets the LDAP entry attribute name containing the list of Groups to which a
   * Member belongs. If {@link #isMemberGroupsListed()} returns false, the value defined
   * here has no meaning.
   * 
   * @return Name of the LDAP entry attribute containing the list of Groups to which a
   *         Member belongs.
   */
  public String getMemberGroupsListAttribute() {
    return memberGroupsListAttribute;
  }

  /**
   * This sets the Member's LDAP entry attribute that will be used to store the list of
   * Groups to which the Member belongs.
   * 
   * @param attribute
   *          Attribute to store the list of Groups to which a Member belongs
   */
  private void setMemberGroupsListAttribute(String attribute) {
    this.memberGroupsListAttribute = attribute;
  }

  /**
   * This gets the Grouper Group naming attribute to be used when creating the list of
   * Groups to which Member belongs. If {@link #isMemberGroupsListed()} returns false, the
   * value defined here has no meaning.
   * 
   * @return Grouper Group naming attribute to be used to create the list of Groups to
   *         which a Member belongs.
   */
  public String getMemberGroupsNamingAttribute() {
    return memberGroupsNamingAttribute;
  }

  /**
   * This sets the Grouper Group naming attribute to be used to create the list of Groups
   * to which a Member belongs.
   * 
   * @param attribute
   *          Grouper Group naming attribute to be used to create the list of Groups to
   *          which a Member belongs.
   */
  private void setMemberGroupsNamingAttribute(String attribute) {
    this.memberGroupsNamingAttribute = attribute;
  }

  /**
   * This gets the value to store in the member groups list attribute if there are no
   * Groups to store there.
   * 
   * @return String to place in the member groups list attribute if no Groups are found to
   *         store there, or <code>null</code> if not defined.
   */
  // public String getMemberGroupsListEmptyValue()
  // {
  // return memberGroupsListEmptyValue;
  // }
  /**
   * Sets the value to be placed into the member groups list attribute if no groups are
   * stored there.
   * 
   * @param value
   *          String value or <code>null</code> if no value is to be stored
   */
  // private void setMemberGroupsListEmptyValue(String value)
  // {
  // this.memberGroupsListEmptyValue = value;
  // }
  /**
   * Directory for the membership updates temporary file.
   * 
   * The default value is null, causing the files to be placed in the current directory.
   * 
   * @return the temporary directory.
   */
  public String getMemberGroupsListTemporaryDirectory() {
    return memberGroupsListTemporaryDirectory;
  }

  /**
   * Sets the directory for the membership updates temporary file.
   * 
   * @param value
   *          String value or <code>null</code> if no value is to be stored
   */
  private void setMemberGroupsListTemporaryDirectory(String value) {
    this.memberGroupsListTemporaryDirectory = value;
  }

  /**
   * This returns a boolean indicating if a Group Members LDAP entry DN list is to be
   * maintained on the Groups LDAP entry.
   * 
   * @return true if the DNs of Members which belong to the Group are listed, and false
   *         otherwise
   * 
   * @see #getGroupMembersDnListObjectClass()
   * @see #getGroupMembersDnListAttribute()
   */
  public boolean isGroupMembersDnListed() {
    return groupMembersDnListed;
  }

  /**
   * This sets the boolean flag indicating if the DNs of the Members which belong to the
   * Group should be stored in an LDAP attribute.
   * 
   * @param listed
   *          Indicates if the DNs of Members should be listed in the Group's LDAP entry
   */
  private void setGroupMembersDnListed(boolean listed) {
    this.groupMembersDnListed = listed;
  }

  /**
   * This sets the objectclass added to the Group LDAP entry that contains the Group
   * member DN list attribute.
   * 
   * @param objectClass
   *          Object class name or <code>null</code> if not defined.
   */
  private void setGroupMembersDnListObjectClass(String objectClass) {
    this.groupMembersDnListObjectClass = objectClass;
  }

  /**
   * This gets the object class to be added to the Group LDAP entry so support the Group
   * members Dn list attribute. If {@link #isGroupMembersDnListed()} returns false, the
   * value defined here has no meaning.
   * 
   * @return Group members Dn list object class or <code>null</code> if not defined.
   */
  public String getGroupMembersDnListObjectClass() {
    return groupMembersDnListObjectClass;
  }

  /**
   * This gets the LDAP entry attribute containing the list of Member DNs which belong to
   * the Group. If {@link #isGroupMembersDnListed()} returns false, the value defined here
   * has no meaning.
   * 
   * @return LDAP entry attribute containing the list of Members DNs which belong to the
   *         Group.
   */
  public String getGroupMembersDnListAttribute() {
    return groupMembersDnListAttribute;
  }

  /**
   * This sets the Group's LDAP entry attribute that will be used to store the list of
   * Member DNs which belong to the Group.
   * 
   * @param attribute
   *          Attribute to store the list of Member DNs which belong to the Group
   */
  private void setGroupMembersDnListAttribute(String attribute) {
    this.groupMembersDnListAttribute = attribute;
  }

  /**
   * This gets the value to store in the group member DN list attribute if there are no
   * member DNs to store there.
   * 
   * @return String to place in the group members DN list attribute if no DNs are found to
   *         store there, or <code>null</code> if not defined.
   */
  public String getGroupMembersDnListEmptyValue() {
    return groupMembersDnListEmptyValue;
  }

  /**
   * Sets the value to be placed into the group members dn list attribute if no DNs are
   * stored there.
   * 
   * @param value
   *          String value or <code>null</code> if no value is to be stored
   */
  private void setGroupMembersDnListEmptyValue(String value) {
    this.groupMembersDnListEmptyValue = value;
  }

  /**
   * This returns a boolean indicating if a Group Members name list is to be maintained on
   * the Groups LDAP entry.
   * 
   * @return true if the names of Members which belong to the Group are listed, and false
   *         otherwise
   * 
   * @see #getGroupMembersNameListAttribute()
   * @see #getGroupMembersNameListObjectClass()
   * @see #getGroupMembersNameListNamingAttribute(String)
   * @see #getGroupMembersNameListNamingAttributes()
   */
  public boolean isGroupMembersNameListed() {
    return groupMembersNameListed;
  }

  /**
   * This sets the boolean flag indicating if the names of the Members which belong to the
   * Group should be stored in an LDAP attribute.
   * 
   * @param listed
   *          Indicates if the names of Members should be listed in the Group's LDAP entry
   */
  private void setGroupMembersNameListed(boolean listed) {
    this.groupMembersNameListed = listed;
  }

  /**
   * This sets the objectclass added to the Group LDAP entry that contains the Group
   * member names list attribute.
   * 
   * @param objectClass
   *          Object class name
   */
  private void setGroupMembersNameListObjectClass(String objectClass) {
    this.groupMembersNameListObjectClass = objectClass;
  }

  /**
   * This gets the object class to be added to the Group LDAP entry so support the Group
   * members name list attribute.
   * 
   * @return Group members name list object class or <code>null</code> if not defined.
   */
  public String getGroupMembersNameListObjectClass() {
    return groupMembersNameListObjectClass;
  }

  /**
   * This gets the LDAP entry attribute containing the list of Member names which belong
   * to the Group. If {@link #isGroupMembersNameListed()} returns false, the value defined
   * here has no meaning.
   * 
   * @return LDAP entry attribute containing the list of Members names which belong to the
   *         Group.
   */
  public String getGroupMembersNameListAttribute() {
    return groupMembersNameListAttribute;
  }

  /**
   * This sets the Group's LDAP entry attribute that will be used to store the list of
   * Member names which belong to the Group.
   * 
   * @param attribute
   *          Attribute to store the list of Member names which belong to the Group
   */
  private void setGroupMembersNameListAttribute(String attribute) {
    this.groupMembersNameListAttribute = attribute;
  }

  /**
   * This gets the value to store in the group member name list attribute if there are no
   * member names to store there.
   * 
   * @return String to place in the group members name list attribute if no names are
   *         found to store there, or <code>null</code> if not defined.
   */
  public String getGroupMembersNameListEmptyValue() {
    return groupMembersNameListEmptyValue;
  }

  /**
   * Sets the value to be placed into the group members name list attribute if no names
   * are stored there.
   * 
   * @param value
   *          String value or <code>null</code> if no value is to be stored
   */
  private void setGroupMembersNameListEmptyValue(String value) {
    this.groupMembersNameListEmptyValue = value;
  }

  /**
   * This method returns the Subject attribute name for creating the Member's name for the
   * given source name.
   * 
   * @param source
   *          Source name
   * 
   * @return Subject attribute name for the source, or <code>null</code> if the source was
   *         not found.
   */
  public String getGroupMembersNameListNamingAttribute(String source) {
    String subjectAttribute = null;
    if (groupMembersNameListNamingAttributes.containsKey(source)) {
      subjectAttribute = (String) groupMembersNameListNamingAttributes.get(source);
    }
    return subjectAttribute;
  }

  /**
   * This method returns a possibly empty {@link java.util.Map} of the Group members name
   * list source to subject attribute mapping used to determine a members name.
   * 
   * @return Map of Source names to Subject attribute names.
   */
  public Map getGroupMembersNameListNamingAttributes() {
    //
    // Return a copy to prevent the original from being
    // unexpectedly changed
    //
    return new Hashtable(this.groupMembersNameListNamingAttributes);
  }

  /**
   * This method adds a Source name and Subject attribute name pair to the Group members
   * name list naming attribute map.
   * 
   * @param source
   *          Source name
   * @param subjectAttribute
   *          Subject attribute name
   */
  private void addGroupMembersNameListNamingAttribute(String source,
      String subjectAttribute) {
    groupMembersNameListNamingAttributes.put(source, subjectAttribute);
  }

  /**
   * This method returns <code>true</code> if the root element of the configuration file
   * was encountered.
   * 
   * @return <code>true</code> if root element encountered, and <code>false</code>
   *         otherwise.
   */
  public boolean isRootElementFound() {
    return rootElementFound;
  }

  /**
   * This sets the flag indicating if the root element was found in the configuration
   * file.
   * 
   * @param found
   *          Boolean indicating if the configuration file root element was found
   */
  private void setRootElementFound(boolean found) {
    this.rootElementFound = found;
  }

  /*
   * (non-Javadoc)
   * 
   * @seeedu.internet2.middleware.ldappc.GrouperProvisionerConfiguration#
   * getCreateGroupThenModifyMembers()
   */
  public boolean getCreateGroupThenModifyMembers() {
    return createGroupsThenModifyMembers;
  }

  private void setCreateGroupThenModifyMembers(String string) {
    this.createGroupsThenModifyMembers = Boolean.parseBoolean(string);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * edu.internet2.middleware.ldappc.ProvisionerConfiguration#getProvisionMemberGroups()
   */
  public boolean getProvisionMemberGroups() {
    return provisionMemberGroups;
  }

  private void setProvisionMemberGroups(String string) {
    this.provisionMemberGroups = Boolean.parseBoolean(string);
  }

  protected void setProvisionMemberGroups(Boolean provisionMemberGroups) {
    this.provisionMemberGroups = provisionMemberGroups;
  }

  /*
   * (non-Javadoc)
   * 
   * @see edu.internet2.middleware.ldappc.LdappcConfig#getProvisionGroupsTwoStep()
   */
  public boolean getProvisionGroupsTwoStep() {
    return provisionGroupsTwoStep;
  }

  private void setProvisionGroupsTwoStep(String string) {
    this.provisionGroupsTwoStep = Boolean.parseBoolean(string);
  }

  protected void setProvisionGroupsTwoStep(Boolean provisionGroupsTwoStep) {
    this.provisionGroupsTwoStep = provisionGroupsTwoStep;
  }

  /*
   * (non-Javadoc)
   * 
   * @see edu.internet2.middleware.ldappc.LdappcConfig#getBundleModifications()
   */
  public boolean getBundleModifications() {
    return bundleModifications;
  }

  private void setBundleModifications(String string) {
    this.bundleModifications = Boolean.parseBoolean(string);
  }

  protected void setBundleModifications(Boolean bundleModifications) {
    this.bundleModifications = bundleModifications;
  }

  /**
   * This method returns a possibly empty {@link java.util.Map} of the AttributeResolver
   * attribute name to LDAP attribute name mapping.
   * 
   * @return Map of Group attribute names to LDAP attribute names.
   */
  public Map<String, List<String>> getAttributeResolverMapping() {
    //
    // Return a copy to prevent the original from being
    // unexpectedly changed
    //
    return new Hashtable(this.attributeResolverMapping);
  }

  /**
   * This method adds a AttributeResolver attribute name and LDAP attribute name pair to
   * the Group attribute map.
   * 
   * @param resolverAttribute
   *          AttributeResolver attribute name
   * @param ldapAttribute
   *          LDAP attribute name
   */
  private void addAttributeResolverMapping(String resolverAttribute, String ldapAttribute) {
    if (!attributeResolverMapping.containsKey(resolverAttribute)) {
      attributeResolverMapping.put(resolverAttribute, new ArrayList<String>());
    }
    attributeResolverMapping.get(resolverAttribute).add(ldapAttribute);
  }

  /**
   * This gets the value to store in the ldap attribute if there are no AttributeResolver
   * attribute values to store there.
   * 
   * @param ldapAttribute
   *          Name of the Ldap Attribute
   * @return String to place in the ldap attribute if no AttributeResolver attribute
   *         values are found to store there, or <code>null</code> if not defined.
   */
  public String getAttributeResolverMappingLdapEmptyValue(String ldapAttribute) {
    return (String) attributeResolverMappingLdapEmptyValues
        .get(convertToUpperCase(ldapAttribute));
  }

  /**
   * This method adds an LDAP attribute name and empty value pair to the AttributeResolver
   * attribute mapping ldap empty value map.
   * 
   * @param ldapAttribute
   *          LDAP attribute name
   * @param value
   *          String value or <code>null</code> if no empty value is desired
   */
  private void addAttributeResolverMappingLdapEmptyValue(String ldapAttribute,
      String value) {
    attributeResolverMappingLdapEmptyValues.put(convertToUpperCase(ldapAttribute), value);
  }

  /**
   * This sets objectclass that a Group entry must have in order to support the
   * AttributeResolver attribute to LDAP attribute.
   * 
   * @param objectClass
   *          Object class to support the AttributeResolver attribute to LDAP attribute
   *          map
   */
  public Set<String> getAttributeResolverMappingObjectClass() {
    return attributeResolverMappingObjectClass;
  }

  /**
   * This sets objectclass that a Group entry must have in order to support the
   * AttributeResolver attribute to LDAP attribute.
   * 
   * @param objectClass
   *          Object class to support the AttributeResolver attribute to LDAP attribute
   *          map
   */
  private void setAttributeResolverMappingObjectClass(String objectClass) {
    List<String> list = GrouperUtil.splitTrimToList(objectClass, " ");
    if (list != null) {
      this.setAttributeResolverMappingObjectClass(new LinkedHashSet<String>(list));
    }
  }

  private void setAttributeResolverMappingObjectClass(Set<String> objectClass) {
    this.attributeResolverMappingObjectClass = objectClass;
  }

  /**
   * @see edu.internet2.middleware.ldappc.LdappcConfig#useRangeSearchResultHandler()
   */
  public boolean useRangeSearchResultHandler() {
    return useRangeSearchResultHandler;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * edu.internet2.middleware.ldappc.LdappcConfig#getProvisionMemberGroupsIgnoreQueries()
   */
  public boolean getProvisionMemberGroupsIgnoreQueries() {
    return provisionMemberGroupsIgnoreQueries;
  }

  private void setProvisionMemberGroupsIgnoreQueries(String string) {
    this.provisionMemberGroupsIgnoreQueries = Boolean.parseBoolean(string);
  }

  protected void setProvisionMemberGroupsIgnoreQueries(
      Boolean provisionMemberGroupsIgnoreQueries) {
    this.provisionMemberGroupsIgnoreQueries = provisionMemberGroupsIgnoreQueries;
  }

  /*
   * (non-Javadoc)
   * 
   * @see edu.internet2.middleware.ldappc.LdappcConfig#getResolverDataConnectorIds()
   */
  public Set<String> getResolverQueries() {
    return this.groupResolverQueries;
  }
  
  /**
   * This method adds a data connector id to the Group resolver data connector id queries
   * set.
   * 
   * @param id
   *          data connector id
   */
  private void addResolverQuery(String id) {
    this.groupResolverQueries.add(id);
  }

  /**
   * This is class allows the Digester processing the configuration file access to all the
   * necessary methods, regardless of visibility, for setting values in the ConfigManager
   * instance. This class is not intended to be used outside of ConfigManager.
   */
  private class Broker {

    /**
     * Make the default constructor private to prevent anyone else from creating an
     * instance.
     */
    private Broker() {
      // Empty body
    }

    /**
     * Calls {@link ConfigManager#setRootElementFound(boolean)} passing <code>true</code>.
     */
    public void foundRootElement() {
      ConfigManager.this.setRootElementFound(true);
    }

    /**
     * Calls {@link ConfigManager#validateSaxParsing()}.
     * 
     * @throws ConfigurationException
     *           thrown if any non-fatal SAXParsingExceptions were encountered while
     *           parsing the configuration file.
     */
    public void validateSaxParsing() throws ConfigurationException {
      ConfigManager.this.validateSaxParsing();
    }

    /**
     * Calls {@link ConfigManager#validateValues()}.
     * 
     * @throws ConfigurationException
     *           thrown if any non-fatal SAXParsingExceptions were encountered while
     *           parsing the configuration file.
     */
    public void validateValues() throws ConfigurationException {
      ConfigManager.this.validateValues();
    }

    /**
     * Adds an LDAP context parameter.
     * 
     * @param name
     *          the parameter name
     * @param value
     *          the parameter value
     * 
     *          Calls {@link ConfigManager#addLdapContextParameter(String, String)}.
     */
    public void addLdapContextParameter(String name, String value) {
      ConfigManager.this.addLdapContextParameter(name, value);
    }

    /**
     * Adds a group attribute match query.
     * 
     * @param name
     *          the attribute name to query
     * @param value
     *          the required value
     * 
     *          Calls {@link ConfigManager#addGroupAttrMatchQuery(String, String)}.
     */
    public void addGroupAttrMatchQuery(String name, String value) {
      ConfigManager.this.addGroupAttrMatchQuery(name, value);
    }

    /**
     * Adds a group subordinate stem query.
     * 
     * @param stem
     *          the stem
     * 
     *          Calls {@link ConfigManager#addGroupSubordinateStemQuery(String)}.
     */
    public void addGroupSubordinateStemQuery(String stem) {
      ConfigManager.this.addGroupSubordinateStemQuery(stem);
    }

    /**
     * Sets the group DN structure.
     * 
     * @param structure
     * 
     * 
     *          Calls {@link ConfigManager#setGroupDnStructure(String)}.
     */
    public void setGroupDnStructure(String structure) {
      ConfigManager.this.setGroupDnStructure(structure);
    }

    /**
     * Sets the group DN root.
     * 
     * @param root
     *          the root DN
     * 
     *          Calls {@link ConfigManager#setGroupDnRoot(String)}.
     */
    public void setGroupDnRoot(String root) {
      ConfigManager.this.setGroupDnRoot(root);
    }

    /**
     * Sets the group DN object class.
     * 
     * @param objectClass
     *          the object class
     * 
     *          Calls {@link ConfigManager#setGroupDnObjectClass(String)}.
     */
    public void setGroupDnObjectClass(String objectClass) {
      ConfigManager.this.setGroupDnObjectClass(objectClass);
    }

    /**
     * Sets the group DN RDN attribute.
     * 
     * @param attribute
     *          the attribute
     * 
     *          Calls {@link ConfigManager#setGroupDnRdnAttribute(String)}.
     */
    public void setGroupDnRdnAttribute(String attribute) {
      ConfigManager.this.setGroupDnRdnAttribute(attribute);
    }

    /**
     * Sets the group DN grouper attribute.
     * 
     * @param attribute
     *          the attribute
     * 
     *          Calls {@link ConfigManager#setGroupDnGrouperAttribute(String)}.
     */
    public void setGroupDnGrouperAttribute(String attribute) {
      ConfigManager.this.setGroupDnGrouperAttribute(attribute);
    }

    /**
     * Sets the group attribute mapping object class.
     * 
     * @param objectClass
     *          the object class
     * 
     *          Calls {@link
     *          ConfigManager#setGroupAttributeMappingObjectClass(Set<String>)}.
     */
    public void setGroupAttributeMappingObjectClass(String objectClass) {
      ConfigManager.this.setGroupAttributeMappingObjectClass(objectClass);
    }

    /**
     * Sets the group hash table size estimate.
     * 
     * @param sizeString
     *          the size estimate.
     * 
     *          Calls {@link ConfigManager#setGroupHashEstimate(String)}.
     */
    public void setGroupHashEstimate(String sizeString) {
      ConfigManager.this.setGroupHashEstimate(Integer.parseInt(sizeString));
    }

    /**
     * Adds the group attribute mapping.
     * 
     * @param groupAttribute
     *          the group attribute
     * @param ldapAttribute
     *          the value to provision
     * 
     *          Calls {@link ConfigManager#addGroupAttributeMapping(String, String)}.
     */
    public void addGroupAttributeMapping(String groupAttribute, String ldapAttribute) {
      ConfigManager.this.addGroupAttributeMapping(groupAttribute, ldapAttribute);
    }

    /**
     * Adds the group attribute mapping LDAP empty value.
     * 
     * @param ldapAttribute
     *          the LDAP attribute
     * @param value
     *          the value to provision
     * 
     *          Calls
     *          {@link ConfigManager#addGroupAttributeMappingLdapEmptyValue(String,String)}
     *          .
     */
    public void addGroupAttributeMappingLdapEmptyValue(String ldapAttribute, String value) {
      ConfigManager.this.addGroupAttributeMappingLdapEmptyValue(ldapAttribute, value);
    }

    /**
     * Adds a source subject naming attribute.
     * 
     * @param source
     *          the source ID
     * @param attribute
     *          the attribute
     * 
     *          Calls
     *          {@link ConfigManager#addSourceSubjectNamingAttribute(String, String)}.
     */
    public void addSourceSubjectNamingAttribute(String source, String attribute) {
      ConfigManager.this.addSourceSubjectNamingAttribute(source, attribute);
    }

    /**
     * Adds a source subject LDAP filter.
     * 
     * @param source
     *          the source ID
     * @param base
     *          the search base
     * @param scope
     *          the scope string. Must be one of "subtree_scope", or "onelevel_scope".
     * @param filter
     *          the filter string
     * 
     * @throws ConfigurationException
     *           thrown when the configuration file is incorrect.
     * 
     *           Calls
     *           {@link ConfigManager#addSourceSubjectLdapFilter(String, String, String, String)}
     *           .
     */
    public void addSourceSubjectLdapFilter(String source, String base, String scope,
        String filter, String onNotFound, String multipleResults)
        throws ConfigurationException {
      ConfigManager.this.addSourceSubjectLdapFilter(source, base, scope, filter,
          onNotFound, multipleResults);
    }

    /**
     * Adds a subject hash table size estimate.
     * 
     * @param source
     *          the source ID
     * @param sizeString
     *          the size estimate
     * 
     *          Calls {@link ConfigManager#setSubjectHashEstimate(String)}.
     */
    public void addSubjectHashEstimate(String source, String sizeString) {
      int size = 0;
      if (sizeString != null && sizeString.length() > 0) {
        size = Integer.parseInt(sizeString);
      }
      ConfigManager.this.addSubjectHashEstimate(source, size);
    }

    /**
     * Sets the {@link ConfigManager#memberGroupsListed} attribute to true.
     * 
     * @see ConfigManager#setMemberGroupsListed(boolean)
     */
    public void listMemberGroups() {
      ConfigManager.this.setMemberGroupsListed(true);
    }

    /**
     * Sets the member groups list object class.
     * 
     * @param objectClass
     *          the object class
     * 
     *          Calls {@link ConfigManager#setMemberGroupsListObjectClass(String)}.
     */
    public void setMemberGroupsListObjectClass(String objectClass) {
      ConfigManager.this.setMemberGroupsListObjectClass(objectClass);
    }

    /**
     * Sets the member groups list attribute.
     * 
     * @param attribute
     *          the attribute
     * 
     *          Calls {@link ConfigManager#setMemberGroupsListAttribute(String)}.
     */
    public void setMemberGroupsListAttribute(String attribute) {
      ConfigManager.this.setMemberGroupsListAttribute(attribute);
    }

    /**
     * Sets the member groups naming attribute.
     * 
     * @param attribute
     *          the attribute
     * 
     *          Calls {@link ConfigManager#setMemberGroupsNamingAttribute(String)}.
     */
    public void setMemberGroupsNamingAttribute(String attribute) {
      ConfigManager.this.setMemberGroupsNamingAttribute(attribute);
    }

    /**
     * Sets the member groups list empty value.
     * 
     * @param value
     *          the value
     * 
     *          Calls {@link ConfigManager#setMemberGroupsListEmptyValue(String))}.
     */
    // public void setMemberGroupsListEmptyValue(String value)
    // {
    // ConfigManager.this.setMemberGroupsListEmptyValue(value);
    // }
    /**
     * Sets the member groups list temporary directory.
     * 
     * @param value
     *          the value
     * 
     *          Calls {@link ConfigManager#setMemberGroupsListTemporaryDirectory(String))}
     *          .
     */
    public void setMemberGroupsListTemporaryDirectory(String value) {
      ConfigManager.this.setMemberGroupsListTemporaryDirectory(value);
    }

    /**
     * Sets the {@link ConfigManager#groupMembersDnListed} attribute to true.
     * 
     * @see ConfigManager#setGroupMembersDnListed(boolean)
     */
    public void listGroupMembersDn() {
      ConfigManager.this.setGroupMembersDnListed(true);
    }

    /**
     * Sets the group members DN list object class.
     * 
     * @param objectClass
     *          the object class
     * 
     *          Calls {@link ConfigManager#setGroupMembersDnListObjectClass(String)}.
     */
    public void setGroupMembersDnListObjectClass(String objectClass) {
      ConfigManager.this.setGroupMembersDnListObjectClass(objectClass);
    }

    /**
     * Sets the group members DN list attribute.
     * 
     * @param attribute
     *          the attribute
     * 
     *          Calls {@link ConfigManager#setGroupMembersDnListAttribute(String)}.
     */
    public void setGroupMembersDnListAttribute(String attribute) {
      ConfigManager.this.setGroupMembersDnListAttribute(attribute);
    }

    /**
     * Sets group members DN list empty value.
     * 
     * @param value
     *          the value
     * 
     *          Calls {@link ConfigManager#setGroupMembersDnListEmptyValue(String)}.
     */
    public void setGroupMembersDnListEmptyValue(String value) {
      ConfigManager.this.setGroupMembersDnListEmptyValue(value);
    }

    /**
     * Sets the {@link ConfigManager#groupMembersNameListed} attribute to true.
     * 
     * @see ConfigManager#setGroupMembersNameListed(boolean)
     */
    public void listGroupMembersName() {
      ConfigManager.this.setGroupMembersNameListed(true);
    }

    /**
     * Sets the group members name list object class.
     * 
     * @param objectClass
     *          the object class
     * 
     *          Calls {@link ConfigManager#setGroupMembersNameListObjectClass(String)}.
     */
    public void setGroupMembersNameListObjectClass(String objectClass) {
      ConfigManager.this.setGroupMembersNameListObjectClass(objectClass);
    }

    /**
     * Sets the group members name list attribute.
     * 
     * @param attribute
     *          the attribute.
     * 
     *          Calls {@link ConfigManager#setGroupMembersNameListAttribute(String)}.
     */
    public void setGroupMembersNameListAttribute(String attribute) {
      ConfigManager.this.setGroupMembersNameListAttribute(attribute);
    }

    /**
     * Sets the group members name list empty value.
     * 
     * @param value
     *          the value.
     * 
     *          Calls {@link ConfigManager#setGroupMembersNameListEmptyValue(String)}.
     */
    public void setGroupMembersNameListEmptyValue(String value) {
      ConfigManager.this.setGroupMembersNameListEmptyValue(value);
    }

    /**
     * Adds a group members name list nameing attribute.
     * 
     * @param source
     *          the source attribute
     * @param subjectAttribute
     *          the subject attribute
     * 
     *          Calls
     *          {@link ConfigManager#addGroupMembersNameListNamingAttribute(String, String)}
     *          .
     */
    public void addGroupMembersNameListNamingAttribute(String source,
        String subjectAttribute) {
      ConfigManager.this.addGroupMembersNameListNamingAttribute(source, subjectAttribute);
    }

    public void setCreateGroupThenModifyMembers(String string) {
      ConfigManager.this.setCreateGroupThenModifyMembers(string);
    }

    public void setProvisionMemberGroups(String string) {
      ConfigManager.this.setProvisionMemberGroups(string);
    }

    public void setProvisionGroupsTwoStep(String string) {
      ConfigManager.this.setProvisionGroupsTwoStep(string);
    }

    public void setBundleModifications(String string) {
      ConfigManager.this.setBundleModifications(string);
    }

    /**
     * Adds the AttributeResolver attribute mapping.
     * 
     * @param resolverAttribute
     *          the resolver attribute
     * @param ldapAttribute
     *          the value to provision
     * 
     *          Calls {@link ConfigManager#addAttributeResolverMapping(String, String)}.
     */
    public void addAttributeResolverMapping(String resolverAttribute, String ldapAttribute) {
      ConfigManager.this.addAttributeResolverMapping(resolverAttribute, ldapAttribute);
    }

    /**
     * Adds the AttributeResolver attribute mapping LDAP empty value.
     * 
     * @param ldapAttribute
     *          the LDAP attribute
     * @param value
     *          the value to provision
     * 
     *          Calls
     *          {@link ConfigManager#addAttributeResolverMappingLdapEmptyValue(String,String)}
     *          .
     */
    public void addAttributeResolverMappingLdapEmptyValue(String ldapAttribute,
        String value) {
      ConfigManager.this.addAttributeResolverMappingLdapEmptyValue(ldapAttribute, value);
    }

    /**
     * Sets the AttributeResolver attribute mapping object class.
     * 
     * @param objectClass
     *          the object class
     * 
     *          Calls {@link ConfigManager#setAttributeResolverMappingObjectClass(String)}
     *          .
     * 
     */
    public void setAttributeResolverMappingObjectClass(String objectClass) {
      ConfigManager.this.setAttributeResolverMappingObjectClass(objectClass);
    }

    public void setProvisionMemberGroupsIgnoreQueries(String string) {
      ConfigManager.this.setProvisionMemberGroupsIgnoreQueries(string);
    }
    
    public void addResolverQuery(String id) {
      ConfigManager.this.addResolverQuery(id);
    }
  }

  /**
   * This is an internal SAX error handler for ConfigManager. When schema based validation
   * of the configuration file is supported this will log warnings, errors, and fatal
   * errors, but only throw an exception on a fatal error. If an error is encountered and
   * parsing can continue, this will notify the ConfigManager to fail after parsing is
   * complete.
   */
  private class SaxErrorHandler implements ErrorHandler {

    /**
     * Receive notification of a warning.
     * 
     * @param e
     *          The warning information encapsulated in a SAX parse exception.
     * 
     * @throws org.xml.sax.SAXException
     *           Any SAX exception, possibly wrapping another exception.
     */
    public void warning(SAXParseException e) throws SAXException {
      LOG.error(formatMsg(e));
    }

    /**
     * Receive notification of an error.
     * 
     * @param e
     *          The error information encapsulated in a SAX parse exception.
     * 
     * @throws org.xml.sax.SAXException
     *           Any SAX exception, possibly wrapping another exception.
     */
    public void error(SAXParseException e) throws SAXException {
      LOG.error(formatMsg(e));
      ConfigManager.this.addSaxParseError(e);
    }

    /**
     * Receive notification of a fatal error.
     * 
     * @param e
     *          The fatal error information encapsulated in a SAX parse exception.
     * 
     * @throws SAXException
     *           if fatal SAX parse exception is encountered.
     * @throws ConfigurationException
     *           if configuration file is incorrect.
     */
    public void fatalError(SAXParseException e) throws SAXException,
        ConfigurationException {
      LOG.error(formatMsg(e));
      ConfigManager.this.addSaxParseError(e);
      throw new ConfigurationException(e);
    }

    /**
     * Returns the message from the exception in a common format.
     * 
     * @param e
     *          SAXParseException
     * @return SAXParseException message in a standard format
     */
    protected String formatMsg(SAXParseException e) {
      return "[ " + e.getLineNumber() + " : " + e.getColumnNumber() + " ] :: "
          + e.getMessage();
    }
  }
}
