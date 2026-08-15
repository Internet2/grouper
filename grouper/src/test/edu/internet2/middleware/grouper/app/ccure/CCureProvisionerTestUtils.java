package edu.internet2.middleware.grouper.app.ccure;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.db.ConfigDatabaseLogic;

/**
 * Test utils for the CCure provisioner.
 *
 * <p>The provisioner config written here mirrors the reference configuration on GRP-7181: CCure is
 * a membership-only provisioner, so groups (Clearances) and entities (Personnel) are selected but
 * never inserted, updated, or deleted -- only the PersonnelClearancePair memberships are written.
 */
public class CCureProvisionerTestUtils {

  /**
   * External system config id used by the tests.
   *
   * <p>Deliberately the placeholder configId, NOT a test-specific one.  The mock service runs in the
   * Tomcat JVM and resolves which config to validate credentials against via
   * {@code grouperTest.ccure.mock.configId} in <b>grouper.properties</b>, falling back to this
   * placeholder.  grouper.properties refreshes on {@code grouper.config.secondsBetweenUpdateChecks}
   * (600s by default, and typically not overridden), so a test-specific configId written from this
   * JVM would not be visible to Tomcat for up to ten minutes -- the mock would fall back to the
   * placeholder, find no username configured, and reject every login with 401 "User not in system".
   *
   * <p>Using the placeholder means the fallback is already correct and nothing has to be written to
   * grouper.properties at all.  Everything the mock needs then lives in grouper-loader.properties,
   * which refreshes far more often (see {@link #awaitTomcatConfigRefresh()}).
   */
  public static final String EXTERNAL_SYSTEM_CONFIG_ID = CCureExternalSystem.CONFIGID_PLACEHOLDER;

  /** credentials the mock service validates the login against */
  public static final String USERNAME = "ccureUser";
  public static final String PASSWORD = "ccurePassword";
  public static final String CLIENT_NAME = "Internet2 - Grouper - Integration";
  public static final String CLIENT_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";
  public static final String CLIENT_VERSION = "2.9";

  /**
   * A second config id, credentials identical to the main one but with tiny page sizes, so the
   * paging loops can be exercised without seeding thousands of rows.
   *
   * <p>This exists as a separate config written once up front, rather than by rewriting the page
   * size on the main config mid-run, because ANY write to grouper-loader.properties triggers a
   * config reload in the Tomcat JVM where the mock lives -- and a test that writes config and then
   * immediately authenticates races that reload.  When it loses, the mock reads no username for the
   * config it validates against and rejects the login with 401 "User not in system".
   *
   * <p>The credentials match the main config, so the mock (which always validates against
   * {@link #EXTERNAL_SYSTEM_CONFIG_ID}) accepts a login posted from this one.
   */
  public static final String SMALL_PAGES_CONFIG_ID = "myCCureSmallPages";

  /** page size used by {@link #SMALL_PAGES_CONFIG_ID}; small enough that a few rows span pages */
  public static final int SMALL_PAGE_SIZE = 2;

  /** endpoint the configs should point at, recomputed each call and compared to what is readable */
  private static String mockEndpoint() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    // NB no /api/... suffix, the commands add that themselves
    return (ssl ? "https://" : "http://") + domainName + ":" + port + "/grouper/mockServices/ccure";
  }

  /**
   * Make sure the CCure external systems point at the mock service, writing them only if they are
   * not already readable.
   *
   * <p>This is deliberately self-verifying rather than either "write every time" or "write once":
   *
   * <ul>
   *   <li><b>Not every time.</b> The mock validates posted credentials against this config but reads
   *       it from the <b>Tomcat</b> JVM.  Every write to grouper-loader.properties makes Tomcat
   *       reload that config, and a test that writes and then immediately authenticates races the
   *       reload -- when it loses, the mock sees no username and rejects the login with 401 "User not
   *       in system".</li>
   *   <li><b>Not once per JVM either.</b> The test harness resets state between tests, so config
   *       that was written for an earlier test is not guaranteed to still be readable -- a stale
   *       "already written" flag produces a null endpoint and "Target host is not specified".</li>
   * </ul>
   *
   * <p>So: clear the caches, read the config back, and write only when it is actually missing or
   * wrong.  In a healthy run that means one write and one wait for the whole class.
   *
   * <p>Clearing caches here is necessary but NOT sufficient for the mock; it does nothing for the
   * Tomcat JVM, which is what {@link #awaitTomcatConfigRefresh()} covers.
   */
  public static void setupCcureExternalSystem() {

    // the harness clears config caches and resets between tests, so re-read before deciding
    ConfigDatabaseLogic.clearCache();
    ConfigPropertiesCascadeBase.clearCache();

    String endpoint = mockEndpoint();

    if (configReadsBackCorrectly(endpoint)) {
      return;
    }

    // the config the mock validates against, and that most tests post from
    storeCredentials(EXTERNAL_SYSTEM_CONFIG_ID, endpoint, PASSWORD);

    // same credentials, tiny page sizes, for the tests that exercise the paging loops
    String smallPagesPrefix = storeCredentials(SMALL_PAGES_CONFIG_ID, endpoint, PASSWORD);
    storeLoaderConfig(smallPagesPrefix + "personnelPageSize", "" + SMALL_PAGE_SIZE);
    storeLoaderConfig(smallPagesPrefix + "clearancePairPageSize", "" + SMALL_PAGE_SIZE);

    // deliberately wrong password, for the test that proves the mock actually validates
    storeCredentials(BAD_PASSWORD_CONFIG_ID, endpoint, "theWrongPassword");

    // NB nothing is written to grouper.properties on purpose -- see EXTERNAL_SYSTEM_CONFIG_ID

    ConfigDatabaseLogic.clearCache();
    ConfigPropertiesCascadeBase.clearCache();

    awaitTomcatConfigRefresh();
  }

  /**
   * Whether all three configs read back with the expected endpoint and username, i.e. there is
   * nothing to write.  Checking the username too, not just the endpoint, so a half-written config
   * is repaired rather than trusted.
   * @param endpoint
   * @return true if every config this class needs is already in place
   */
  private static boolean configReadsBackCorrectly(String endpoint) {
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

    for (String configId : new String[] {EXTERNAL_SYSTEM_CONFIG_ID, SMALL_PAGES_CONFIG_ID, BAD_PASSWORD_CONFIG_ID}) {
      String prefix = "grouper.CCureExternalSystem." + configId + ".";
      if (!StringUtils.equals(endpoint, grouperLoaderConfig.propertyValueString(prefix + "endpoint"))
          || !StringUtils.equals(USERNAME, grouperLoaderConfig.propertyValueString(prefix + "username"))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Write one external system's credentials and return its property prefix.
   * @param configId
   * @param endpoint
   * @param password
   * @return the property prefix, for writing any further properties on the same config
   */
  private static String storeCredentials(String configId, String endpoint, String password) {
    String prefix = "grouper.CCureExternalSystem." + configId + ".";
    storeLoaderConfig(prefix + "endpoint", endpoint);
    storeLoaderConfig(prefix + "username", USERNAME);
    storeLoaderConfig(prefix + "password", password);
    storeLoaderConfig(prefix + "clientName", CLIENT_NAME);
    storeLoaderConfig(prefix + "clientId", CLIENT_ID);
    storeLoaderConfig(prefix + "clientVersion", CLIENT_VERSION);
    return prefix;
  }

  /**
   * A config id whose password does NOT match what the mock expects.
   *
   * <p>The mock validates posted credentials against {@link #EXTERNAL_SYSTEM_CONFIG_ID}'s config --
   * the very config the client normally posts from.  Editing the password there moves BOTH sides and
   * the login still succeeds, so the only way to produce a real credential mismatch is to post from a
   * different config while the mock keeps validating against the good one.
   */
  public static final String BAD_PASSWORD_CONFIG_ID = "myCCureBadPassword";

  /**
   * Wait long enough for the Tomcat JVM to re-read grouper-loader.properties from the database, so
   * credential config written here is in effect for the mock service.
   *
   * <p>The wait is derived from {@code loader.config.secondsBetweenUpdateChecks} (5 in the standard
   * dev grouper-loader.properties) plus a margin, rather than a magic number.  It is capped, because
   * a site on the 600 second base default should not make the suite sleep for ten minutes -- in that
   * case the credentials will usually already be in the database from a previous run, and if they
   * are not the login fails with a clear 401 rather than hanging.
   */
  public static void awaitTomcatConfigRefresh() {
    int secondsBetweenUpdateChecks = GrouperLoaderConfig.retrieveConfig()
        .propertyValueInt("loader.config.secondsBetweenUpdateChecks", 600);

    long millis = Math.min((secondsBetweenUpdateChecks * 1000L) + 1000L, 15000L);

    GrouperUtil.sleep(millis);
  }

  private static void storeLoaderConfig(String propertyName, String value) {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(propertyName).value(value).store();
  }

  /**
   * @param provisioningTestConfigInput
   * @param suffix
   * @param value
   */
  public static void configureProvisionerSuffix(CCureProvisionerTestConfigInput provisioningTestConfigInput, String suffix, String value) {
    if (!provisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }

  private static void configureProvisioner(CCureProvisionerTestConfigInput provisioningTestConfigInput) {
    GrouperUtil.assertion(!StringUtils.isBlank(provisioningTestConfigInput.getConfigId()), "Config ID required");

    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.ccure.CCureProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "externalSystemConfigId", EXTERNAL_SYSTEM_CONFIG_ID);
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");

    // entity config - Personnel exist in CCure already, Grouper never changes them
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");

    // group config - Clearances exist in CCure already, Grouper never creates or removes them
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");

    // membership config - the clearance pairs are the only thing Grouper writes
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    // set explicitly rather than leaning on the default: deleteMembershipsIfNotExistInGrouper is
    // gated on deleteMemberships, so a default change would silently disable the delete tests
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");

    if (provisioningTestConfigInput.getGroupOfUsersToProvision() != null) {
      configureProvisionerSuffix(provisioningTestConfigInput, "entity2advanced", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupIdOfUsersToProvision",
          provisioningTestConfigInput.getGroupOfUsersToProvision().getUuid());
    }

    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");

    // entity attributes: PersonnelID (the target id), Int1 (holds the subject id)
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "PersonnelID");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "Int1");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");

    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "PersonnelID");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute1name", "Int1");

    // entity attribute value cache 0 = PersonnelID, so a membership can translate from it
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "PersonnelID");

    // group attributes: ObjectID (the target id), Name (matched against the group display extension)
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "ObjectID");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "Name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "displayExtension");

    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "ObjectID");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute1name", "Name");

    // group attribute value cache 0 = ObjectID, so a membership can translate from it
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "ObjectID");

    // membership attributes: PersonnelID and ClearanceID come from the caches above; ObjectID is
    // assigned by CCure on insert and is what a delete is keyed on
    configureProvisionerSuffix(provisioningTestConfigInput, "membership2AdvancedOptions", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "membershipMatchingIdExpression",
        "${new('edu.internet2.middleware.grouperClient.collections.MultiKey', "
        + "targetMembership.retrieveAttributeValueString('ClearanceID'), "
        + "targetMembership.retrieveAttributeValueString('PersonnelID'))}");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfMembershipAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.name", "PersonnelID");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.name", "ClearanceID");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateFromGrouperProvisioningGroupField", "groupAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.2.name", "ObjectID");
  }

  /**
   * @param provisioningTestConfigInput
   */
  public static void configureCcureProvisioner(CCureProvisionerTestConfigInput provisioningTestConfigInput) {

    configureProvisioner(provisioningTestConfigInput);

    for (String key : provisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
            .propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".provisionerJobSyncType").value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.debug").value("true").store();

    ConfigPropertiesCascadeBase.clearCache();
  }

}
