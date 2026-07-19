/**
 * @author Grouper - external system references feature
 */
package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames;
import edu.internet2.middleware.grouper.app.reports.GrouperReportSettings;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Finds everywhere in Grouper that a given external system is referenced (used).
 *
 * <p>There are two reference channels, unioned by this finder:</p>
 * <ul>
 *   <li>Config editor references stored in the grouper_config table.  These are
 *   discovered generically: any config attribute that points at an external
 *   system declares the external-system class in its config metadata via
 *   optionValuesFromClass.  We query grouper_config for rows whose value equals
 *   the external system config id, then keep only the rows whose metadata
 *   declares this external system's class.  This is resolved by metadata class,
 *   not by key-name patterns, because the same class is referenced under many
 *   differently named keys (for example the bearer token external system is used
 *   by several app provisioners, each with its own key suffix).</li>
 *   <li>Attribute-value references stored in grouper_attribute_assign_value.
 *   Group/attribute loaders and report configs are stored as attribute values,
 *   not in grouper_config, and do not carry the optionValuesFromClass metadata,
 *   so a small fixed set of attribute def names is scanned -- only the ones whose
 *   external-system class matches the viewed system.</li>
 * </ul>
 *
 * <p>Config references only (not runtime data such as grouper_sync rows).
 * Disabled configs are included; blank/defaulted values are not matched (only
 * explicit stored values), so the built-in "grouper" DB connection does not list
 * every loader/report that omitted the field.</p>
 */
public class GrouperExternalSystemUsageFinder {

  /** default max references to show per type before truncating */
  public static final int DEFAULT_MAX_PER_TYPE = 100;

  /** fully qualified class name of the external system being examined */
  private String externalSystemClassName;

  /** config id of the external system being examined */
  private String configId;

  /** max references to keep per type; extra references are dropped and truncated is set */
  private int maxPerType = DEFAULT_MAX_PER_TYPE;

  /** set to true if any type had more references than maxPerType */
  private boolean truncated = false;

  /**
   * @param externalSystemClassName1 fully qualified class name of the external system
   * @return this for chaining
   */
  public GrouperExternalSystemUsageFinder assignExternalSystemClassName(String externalSystemClassName1) {
    this.externalSystemClassName = externalSystemClassName1;
    return this;
  }

  /**
   * @param configId1 config id of the external system
   * @return this for chaining
   */
  public GrouperExternalSystemUsageFinder assignConfigId(String configId1) {
    this.configId = configId1;
    return this;
  }

  /**
   * @param maxPerType1 max references to keep per type
   * @return this for chaining
   */
  public GrouperExternalSystemUsageFinder assignMaxPerType(int maxPerType1) {
    this.maxPerType = maxPerType1;
    return this;
  }

  /**
   * @return true if any type was truncated (had more than maxPerType references).
   * Only meaningful after findUsages() is called.
   */
  public boolean isTruncated() {
    return this.truncated;
  }

  /**
   * find the references, grouped by type and capped at maxPerType per type.
   * @return the list of usages (references) ordered by type
   */
  public List<GrouperExternalSystemUsage> findUsages() {

    this.truncated = false;

    List<GrouperExternalSystemUsage> all = new ArrayList<GrouperExternalSystemUsage>();

    if (StringUtils.isBlank(this.configId) || StringUtils.isBlank(this.externalSystemClassName)) {
      return all;
    }

    this.addConfigUsages(all);
    this.addAttributeUsages(all);
    this.addMcpUsages(all);

    // group by type, preserving first-seen order, and cap each type at maxPerType
    Map<String, List<GrouperExternalSystemUsage>> byType = new LinkedHashMap<String, List<GrouperExternalSystemUsage>>();
    for (GrouperExternalSystemUsage usage : all) {
      List<GrouperExternalSystemUsage> list = byType.get(usage.getUsageType());
      if (list == null) {
        list = new ArrayList<GrouperExternalSystemUsage>();
        byType.put(usage.getUsageType(), list);
      }
      list.add(usage);
    }

    List<GrouperExternalSystemUsage> result = new ArrayList<GrouperExternalSystemUsage>();
    for (List<GrouperExternalSystemUsage> list : byType.values()) {
      if (list.size() > this.maxPerType) {
        this.truncated = true;
        result.addAll(list.subList(0, this.maxPerType));
      } else {
        result.addAll(list);
      }
    }
    return result;
  }

  /**
   * scan for MCP admin external system config that exposes this external system.
   *
   * <p>Unlike the other channels, the MCP admin external system config identifies
   * the external system in the config KEY
   * (grouper.mcp.adminExternalSystem.&lt;configId&gt;.*), not in a value, so the
   * value-based config scan does not find it. Look it up by key prefix here.</p>
   *
   * @param usages list to add references to
   */
  private void addMcpUsages(List<GrouperExternalSystemUsage> usages) {

    String prefix = "grouper.mcp.adminExternalSystem.";

    List<GrouperConfigHibernate> configs = HibernateSession.byHqlStatic()
        .createQuery("from GrouperConfigHibernate gch where gch.configKey like :thePrefix")
        .setString("thePrefix", prefix + "%")
        .list(GrouperConfigHibernate.class);

    for (GrouperConfigHibernate config : GrouperUtil.nonNull(configs)) {
      String key = config.getConfigKey();
      if (StringUtils.isBlank(key) || !key.startsWith(prefix)) {
        continue;
      }
      // key is grouper.mcp.adminExternalSystem.<configId>.<attribute...>
      String remainder = key.substring(prefix.length());
      String mcpConfigId = StringUtils.substringBefore(remainder, ".");
      if (StringUtils.equals(mcpConfigId, this.configId)) {
        usages.add(new GrouperExternalSystemUsage("MCP", "admin_external_system_get",
            "Exposed to AI clients for user lookups via the MCP admin_external_system_get tool", null));
        return;
      }
    }
  }

  /**
   * scan grouper_config for config that references this external system's config id.
   * @param usages list to add references to
   */
  private void addConfigUsages(List<GrouperExternalSystemUsage> usages) {

    // the metadata is the authority on which config keys point at an external
    // system of this class.  Collect the sample keys of every config attribute
    // whose dropdown is driven by this external system's class.  We match by key
    // structure rather than by ConfigFileName.findConfigItemMetdata(realKey),
    // because provisioner (and similar) attribute metadata is keyed by an example
    // config id (e.g. myAzureProvisioner) and has no regex, so a real config id
    // would not resolve.
    List<String> referenceSampleKeys = this.collectReferenceSampleKeys();
    if (referenceSampleKeys.isEmpty()) {
      return;
    }

    Set<GrouperConfigHibernate> configs = GrouperDAOFactory.getFactory().getConfig().findByValue(this.configId);

    for (GrouperConfigHibernate config : GrouperUtil.nonNull(configs)) {

      String key = config.getConfigKey();
      if (StringUtils.isBlank(key)) {
        continue;
      }

      // a real config key references this external system if it has the same
      // shape as one of the sample keys, differing only in the config id token
      boolean isReference = false;
      for (String sampleKey : referenceSampleKeys) {
        if (keyStructurallyMatches(key, sampleKey)) {
          isReference = true;
          break;
        }
      }
      if (!isReference) {
        continue;
      }

      usages.add(configKeyToUsage(key));
    }
  }

  /**
   * collect the sample keys of every config attribute across all config files
   * whose option values are driven by this external system's class.
   * @return the sample keys (e.g. provisioner.myAzureProvisioner.azureExternalSystemConfigId)
   */
  private List<String> collectReferenceSampleKeys() {

    List<String> sampleKeys = new ArrayList<String>();

    for (ConfigFileName configFileName : ConfigFileName.values()) {
      try {
        for (ConfigSectionMetadata section : GrouperUtil.nonNull(configFileName.configFileMetadata().getConfigSectionMetadataList())) {
          for (ConfigItemMetadata itemMetadata : GrouperUtil.nonNull(section.getConfigItemMetadataList())) {
            if (StringUtils.equals(this.externalSystemClassName, itemMetadata.getOptionValuesFromClass())) {
              String sampleKey = itemMetadata.getKeyOrSampleKey();
              if (StringUtils.isNotBlank(sampleKey)) {
                sampleKeys.add(sampleKey);
              }
            }
          }
        }
      } catch (Exception e) {
        // some config files may not be on the classpath in all deployments; skip them
        continue;
      }
    }
    return sampleKeys;
  }

  /**
   * true if a real config key has the same shape as a metadata sample key,
   * differing only in the config id (exactly one non-terminal token) or not at
   * all.  The config id is never the terminal token of an external-system
   * reference key (those end in the attribute name), so a difference at the last
   * token means a different attribute, not the same reference with another id.
   * @param key real config key
   * @param sampleKey metadata sample key
   * @return true if the real key is a reference matching the sample
   */
  private static boolean keyStructurallyMatches(String key, String sampleKey) {

    String[] keyTokens = StringUtils.split(key, '.');
    String[] sampleTokens = StringUtils.split(sampleKey, '.');

    if (keyTokens.length != sampleTokens.length || keyTokens.length == 0) {
      return false;
    }

    int differingIndex = -1;
    int differingCount = 0;
    for (int i = 0; i < keyTokens.length; i++) {
      if (!StringUtils.equals(keyTokens[i], sampleTokens[i])) {
        differingIndex = i;
        differingCount++;
        if (differingCount > 1) {
          return false;
        }
      }
    }

    // identical key (real config id equals the sample id): a match
    if (differingCount == 0) {
      return true;
    }

    // exactly one differing token: it must be a config id, never the terminal
    // attribute-name token
    return differingIndex != keyTokens.length - 1;
  }

  /**
   * classify a matched config key into a usage row (type, name, description, link).
   * @param key the grouper_config key that references the external system
   * @return the usage row
   */
  private static GrouperExternalSystemUsage configKeyToUsage(String key) {

    String[] tokens = StringUtils.split(key, '.');
    String prefix = tokens.length > 0 ? tokens[0] : key;

    // provisioner.<configId>.<attributeSuffix...>
    if (StringUtils.equals(prefix, "provisioner") && tokens.length >= 3) {
      String owningConfigId = tokens[1];
      String suffix = StringUtils.substringAfter(key, "provisioner." + owningConfigId + ".");
      return new GrouperExternalSystemUsage("Provisioner", owningConfigId,
          "Provisioner references this external system (" + suffix + ")",
          GrouperExternalSystemUsage.LINK_TYPE_PROVISIONER);
    }

    // subjectApi.source.<sourceId>.param.<name>.value
    if (StringUtils.equals(prefix, "subjectApi") && tokens.length >= 3 && StringUtils.equals(tokens[1], "source")) {
      String sourceId = tokens[2];
      return new GrouperExternalSystemUsage("Subject source", sourceId,
          "Subject source references this external system (" + key + ")", null);
    }

    // otherJob.<configId>.<attributeSuffix...>
    if (StringUtils.equals(prefix, "otherJob") && tokens.length >= 3) {
      String owningConfigId = tokens[1];
      return new GrouperExternalSystemUsage("Daemon job", owningConfigId,
          "Daemon job references this external system (" + key + ")", null);
    }

    // grouperDataProviderQuery.<id>.* and grouperDataProviderChangeLogQuery.<id>.*
    if (StringUtils.equals(prefix, "grouperDataProviderQuery") && tokens.length >= 2) {
      return new GrouperExternalSystemUsage("Data provider query", tokens[1],
          "Data provider query references this external system (" + key + ")", null);
    }
    if (StringUtils.equals(prefix, "grouperDataProviderChangeLogQuery") && tokens.length >= 2) {
      return new GrouperExternalSystemUsage("Data provider change log query", tokens[1],
          "Data provider change log query references this external system (" + key + ")", null);
    }

    // entityAttributeResolver.<id>.*
    if (StringUtils.equals(prefix, "entityAttributeResolver") && tokens.length >= 2) {
      return new GrouperExternalSystemUsage("Entity attribute resolver", tokens[1],
          "Entity attribute resolver references this external system (" + key + ")", null);
    }

    // fallback: unknown config family.  Show the config key as text, no link.
    String owningConfigId = tokens.length >= 2 ? tokens[1] : key;
    return new GrouperExternalSystemUsage("Configuration", owningConfigId,
        "Config references this external system (" + key + ")", null);
  }

  /**
   * scan attribute values (loaders, reports) that reference this external system.
   * Only db and ldap external systems are referenced from attribute data.
   * @param usages list to add references to
   */
  private void addAttributeUsages(List<GrouperExternalSystemUsage> usages) {

    boolean isDb = StringUtils.equals(this.externalSystemClassName, DatabaseGrouperExternalSystem.class.getName());
    boolean isLdap = StringUtils.equals(this.externalSystemClassName, LdapGrouperExternalSystem.class.getName());

    if (!isDb && !isLdap) {
      return;
    }

    if (isDb) {

      // SQL group loader stores the db connection in the legacy grouperLoaderDbName attribute
      String legacyBaseStem = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
      String legacyAttributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");
      this.addAttributeUsagesForDefName(legacyBaseStem + ":" + legacyAttributePrefix + "grouperLoaderDbName",
          "SQL group loader", "loader database connection", usages);

      // attribute (permission) loader
      this.addAttributeUsagesForDefName(GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderDbName",
          "Attribute loader", "loader database connection", usages);

      // report config
      this.addAttributeUsagesForDefName(GrouperReportSettings.reportConfigStemName() + ":"
          + GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SQL_CONFIG,
          "Report", "report database connection", usages);
    }

    if (isLdap) {

      // LDAP group loader stores the ldap server in the grouperLoaderLdapServerId attribute
      this.addAttributeUsagesForDefName(LoaderLdapUtils.grouperLoaderLdapServerIdName(),
          "LDAP group loader", "loader LDAP server", usages);
    }
  }

  /**
   * find attribute assignments of the given attribute def name whose value equals
   * the external system config id, and add a usage row for each owner.
   * @param attributeDefNameName full attribute def name
   * @param usageType human readable type label
   * @param roleDescription short phrase describing how the external system is used
   * @param usages list to add references to
   */
  private void addAttributeUsagesForDefName(String attributeDefNameName, String usageType,
      String roleDescription, List<GrouperExternalSystemUsage> usages) {

    if (StringUtils.isBlank(attributeDefNameName)) {
      return;
    }

    // the attribute may not exist in this registry (e.g. reports not installed)
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, false);
    if (attributeDefName == null) {
      return;
    }

    // the value is stored on an attribute assign (assign-on-assign under the marker).
    // find the value assigns whose string value is the external system config id.
    List<AttributeAssign> valueAssigns = HibernateSession.byHqlStatic()
        .createQuery("select valueAssign from AttributeAssignValue value, AttributeAssign valueAssign "
            + "where value.attributeAssignId = valueAssign.id "
            + "and valueAssign.attributeDefNameId = :theAttributeDefNameId "
            + "and value.valueString = :theValue")
        .setString("theAttributeDefNameId", attributeDefName.getId())
        .setString("theValue", this.configId)
        .list(AttributeAssign.class);

    for (AttributeAssign valueAssign : GrouperUtil.nonNull(valueAssigns)) {

      // walk up to the owning object; the value assign owner is the marker assign
      AttributeAssign markerAssign = valueAssign.getOwnerAttributeAssign();
      AttributeAssign ownerAssign = markerAssign != null ? markerAssign : valueAssign;

      String ownerName;
      String linkType;

      if (ownerAssign.getOwnerGroupId() != null) {
        Group group = ownerAssign.getOwnerGroup();
        ownerName = group.getName();
        linkType = GrouperExternalSystemUsage.LINK_TYPE_GROUP;
      } else if (ownerAssign.getOwnerStemId() != null) {
        Stem stem = ownerAssign.getOwnerStem();
        ownerName = stem.getName();
        linkType = GrouperExternalSystemUsage.LINK_TYPE_STEM;
      } else if (ownerAssign.getOwnerAttributeDefId() != null) {
        AttributeDef attributeDef = ownerAssign.getOwnerAttributeDef();
        ownerName = attributeDef.getName();
        // no dedicated simple view page target here; render as text
        linkType = null;
      } else {
        continue;
      }

      usages.add(new GrouperExternalSystemUsage(usageType, ownerName,
          usageType + " uses this external system as the " + roleDescription, linkType));
    }
  }

}
