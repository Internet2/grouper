package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.duo.GrouperDuoConfiguration;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoGroup;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoProvisioner;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoTargetDao;
import edu.internet2.middleware.grouper.app.zoom.GrouperZoomCommands;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import junit.textui.TestRunner;

/**
 * The Duo and Adobe target DAOs keep the target's groups, and the Azure target DAO keeps owner
 * urls, in static caches shared by every provisioner in the JVM (and Zoom keeps its bearer token
 * in one).  These caches must be keyed by
 * the external system (and the Adobe org, and for Azure the provisioner), so a provisioner never
 * reads or links to a group (or owner url) loaded from another tenant.
 *
 * The caches and their key methods are private, so this uses reflection.  The caches are used
 * raw, so the test also runs (and fails) against the old Boolean-keyed caches.  Adobe is looked
 * up by name because it does not exist in every release; Azure's owner url cache does not exist
 * in 2.6.19.  No database, no target system: a cache miss tries the real API, which fails on
 * the unconfigured external system, and that is treated as "did not get the other tenant's group".
 */
public class ConnectorGroupCacheConfigIdTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ConnectorGroupCacheConfigIdTest("testDuoGroupCacheKeyedByExternalSystem"));
  }

  /**
   * @param name
   */
  public ConnectorGroupCacheConfigIdTest(String name) {
    super(name);
  }

  /**
   * get a private static field
   * @param theClass
   * @param fieldName
   * @return the value or null if the field does not exist
   * @throws Exception
   */
  private static Object staticField(Class<?> theClass, String fieldName) throws Exception {
    Field field;
    try {
      field = theClass.getDeclaredField(fieldName);
    } catch (NoSuchFieldException nsfe) {
      return null;
    }
    field.setAccessible(true);
    return field.get(null);
  }

  /**
   * call a private no-arg or one-arg method, fail with a clear message if it does not exist
   * (it is added by the fix)
   * @param object
   * @param methodName
   * @param args
   * @return the result
   * @throws Exception
   */
  private static Object callPrivate(Object object, String methodName, Object... args) throws Exception {
    Method method = null;
    for (Method current : object.getClass().getDeclaredMethods()) {
      if (current.getName().equals(methodName) && current.getParameterTypes().length == args.length) {
        method = current;
        break;
      }
    }
    assertNotNull(object.getClass().getSimpleName() + "." + methodName
        + " not found: the cache is not keyed by external system config", method);
    method.setAccessible(true);
    return method.invoke(object, args);
  }

  /**
   * retrieve a group by name, return its id, or null if not found or the API call failed
   * @param targetDao
   * @param name
   * @return the id or null
   */
  private static String retrieveGroupIdByName(GrouperProvisionerTargetDaoBase targetDao, String name) {
    TargetDaoRetrieveGroupRequest request = new TargetDaoRetrieveGroupRequest();
    request.setSearchAttribute("name");
    request.setSearchAttributeValue(name);
    try {
      TargetDaoRetrieveGroupResponse response = targetDao.retrieveGroup(request);
      ProvisioningGroup targetGroup = response == null ? null : response.getTargetGroup();
      return targetGroup == null ? null : targetGroup.getId();
    } catch (RuntimeException re) {
      // cache miss went to the (unconfigured) API: not the other tenant's group
      return null;
    }
  }

  /**
   * two Duo provisioners on different Duo external systems: B must not get A's cached group
   * @throws Exception
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void testDuoGroupCacheKeyedByExternalSystem() throws Exception {

    GrouperDuoTargetDao daoA = duoDao("duoProvA162", "duoSysA162");
    GrouperDuoTargetDao daoB = duoDao("duoProvB162", "duoSysB162");

    ExpirableCache cache = (ExpirableCache) staticField(GrouperDuoTargetDao.class, "cacheGroupNameToGroup");
    assertNotNull(cache);

    try {
      cache.clear();

      Object keyA = callPrivate(daoA, "groupCacheKey");
      Object keyB = callPrivate(daoB, "groupCacheKey");
      assertFalse("keys must differ by external system: " + keyA, keyA.equals(keyB));

      GrouperDuoGroup groupA = new GrouperDuoGroup();
      groupA.setGroup_id("groupIdTenantA162");
      groupA.setName("vpnUsers162");
      Map<String, GrouperDuoGroup> nameToGroup = new HashMap<String, GrouperDuoGroup>();
      nameToGroup.put("vpnUsers162", groupA);
      cache.put(keyA, nameToGroup);

      // A reads its own cache
      assertEquals("groupIdTenantA162", retrieveGroupIdByName(daoA, "vpnUsers162"));

      // B must not get tenant A's group
      assertFalse("Duo provisioner B got tenant A's group from the cache",
          "groupIdTenantA162".equals(retrieveGroupIdByName(daoB, "vpnUsers162")));
    } finally {
      cache.clear();
    }
  }

  /**
   * @param provisionerConfigId
   * @param externalSystemConfigId
   * @return the dao wired to a provisioner with that external system
   */
  private static GrouperDuoTargetDao duoDao(String provisionerConfigId, String externalSystemConfigId) {
    GrouperDuoProvisioner provisioner = new GrouperDuoProvisioner();
    provisioner.setConfigId(provisionerConfigId);
    ((GrouperDuoConfiguration) provisioner.retrieveGrouperProvisioningConfiguration())
        .setDuoExternalSystemConfigId(externalSystemConfigId);
    GrouperDuoTargetDao dao = new GrouperDuoTargetDao();
    dao.setGrouperProvisioner(provisioner);
    return dao;
  }

  /**
   * two Adobe provisioners on the same external system but different orgs, and on different
   * external systems: none may get another's cached group
   * @throws Exception
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void testAdobeGroupCacheKeyedByExternalSystemAndOrg() throws Exception {

    Class<?> daoClass;
    try {
      daoClass = Class.forName("edu.internet2.middleware.grouper.app.adobe.GrouperAdobeTargetDao");
    } catch (ClassNotFoundException cnfe) {
      System.out.println("skip testAdobeGroupCacheKeyedByExternalSystemAndOrg: no Adobe provisioner in this release");
      return;
    }

    GrouperProvisionerTargetDaoBase daoA = adobeDao("adobeProvA162", "adobeSysA162", "orgA162");
    GrouperProvisionerTargetDaoBase daoOtherOrg = adobeDao("adobeProvB162", "adobeSysA162", "orgB162");
    GrouperProvisionerTargetDaoBase daoOtherSystem = adobeDao("adobeProvC162", "adobeSysC162", "orgA162");

    ExpirableCache nameCache = (ExpirableCache) staticField(daoClass, "cacheGroupNameToGroup");
    ExpirableCache idCache = (ExpirableCache) staticField(daoClass, "cacheGroupIdToGroup");
    assertNotNull(nameCache);
    assertNotNull(idCache);

    try {
      nameCache.clear();
      idCache.clear();

      Object keyA = callPrivate(daoA, "groupCacheKey");
      assertFalse("keys must differ by org", keyA.equals(callPrivate(daoOtherOrg, "groupCacheKey")));
      assertFalse("keys must differ by external system", keyA.equals(callPrivate(daoOtherSystem, "groupCacheKey")));

      Class<?> groupClass = Class.forName("edu.internet2.middleware.grouper.app.adobe.GrouperAdobeGroup");
      Object groupA = groupClass.getConstructor().newInstance();
      groupClass.getMethod("setId", Long.class).invoke(groupA, Long.valueOf(162162L));
      groupClass.getMethod("setName", String.class).invoke(groupA, "vpnUsers162");
      Map nameToGroup = new HashMap();
      nameToGroup.put("vpnUsers162", groupA);
      Map idToGroup = new HashMap();
      idToGroup.put(Long.valueOf(162162L), groupA);
      nameCache.put(keyA, nameToGroup);
      idCache.put(keyA, idToGroup);

      assertEquals("162162", retrieveGroupIdByName(daoA, "vpnUsers162"));
      assertFalse("Adobe provisioner on another org got org A's group from the cache",
          "162162".equals(retrieveGroupIdByName(daoOtherOrg, "vpnUsers162")));
      assertFalse("Adobe provisioner on another external system got system A's group from the cache",
          "162162".equals(retrieveGroupIdByName(daoOtherSystem, "vpnUsers162")));
    } finally {
      nameCache.clear();
      idCache.clear();
    }
  }

  /**
   * @param provisionerConfigId
   * @param externalSystemConfigId
   * @param orgId
   * @return the dao wired to a provisioner with that external system and org
   * @throws Exception
   */
  private static GrouperProvisionerTargetDaoBase adobeDao(String provisionerConfigId,
      String externalSystemConfigId, String orgId) throws Exception {
    GrouperProvisioner provisioner = (GrouperProvisioner) Class.forName(
        "edu.internet2.middleware.grouper.app.adobe.GrouperAdobeProvisioner").getConstructor().newInstance();
    provisioner.setConfigId(provisionerConfigId);
    Object configuration = provisioner.retrieveGrouperProvisioningConfiguration();
    configuration.getClass().getMethod("setAdobeExternalSystemConfigId", String.class).invoke(configuration, externalSystemConfigId);
    configuration.getClass().getMethod("setOrgId", String.class).invoke(configuration, orgId);
    GrouperProvisionerTargetDaoBase dao = (GrouperProvisionerTargetDaoBase) Class.forName(
        "edu.internet2.middleware.grouper.app.adobe.GrouperAdobeTargetDao").getConstructor().newInstance();
    dao.setGrouperProvisioner(provisioner);
    return dao;
  }

  /**
   * the Azure owner url cache is keyed by provisioner and external system, so an owner url
   * one tenant resolved is not handed to another
   * @throws Exception
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void testAzureOwnerUrlCacheKeyedByProvisionerAndExternalSystem() throws Exception {

    Class<?> daoClass = Class.forName("edu.internet2.middleware.grouper.app.azure.GrouperAzureTargetDao");
    ExpirableCache cache = (ExpirableCache) staticField(daoClass, "ownerIdentifierToOwnerUrl");
    if (cache == null) {
      System.out.println("skip testAzureOwnerUrlCacheKeyedByProvisionerAndExternalSystem: no owner url cache in this release");
      return;
    }

    GrouperProvisionerTargetDaoBase daoA = azureDao("azureProvA162", "azureSysA162");
    GrouperProvisionerTargetDaoBase daoOtherSystem = azureDao("azureProvB162", "azureSysB162");
    GrouperProvisionerTargetDaoBase daoOtherProvisioner = azureDao("azureProvC162", "azureSysA162");

    try {
      cache.clear();
      Object keyA = callPrivate(daoA, "ownerUrlCacheKey", "jsmith162");
      cache.put(keyA, "https://graph.microsoft.com/v1.0/users/tenantAUserId162");

      assertEquals("https://graph.microsoft.com/v1.0/users/tenantAUserId162",
          cache.get(callPrivate(daoA, "ownerUrlCacheKey", "jsmith162")));
      assertNull("Azure provisioner on another tenant got tenant A's owner url",
          cache.get(callPrivate(daoOtherSystem, "ownerUrlCacheKey", "jsmith162")));
      assertNull("another Azure provisioner got provisioner A's owner url",
          cache.get(callPrivate(daoOtherProvisioner, "ownerUrlCacheKey", "jsmith162")));
      // the old key (the bare owner identifier) must not be what the code reads
      assertNull(cache.get("jsmith162"));
    } finally {
      cache.clear();
    }
  }

  /**
   * @param provisionerConfigId
   * @param externalSystemConfigId
   * @return the dao wired to a provisioner with that external system
   * @throws Exception
   */
  private static GrouperProvisionerTargetDaoBase azureDao(String provisionerConfigId,
      String externalSystemConfigId) throws Exception {
    GrouperProvisioner provisioner = (GrouperProvisioner) Class.forName(
        "edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisioner").getConstructor().newInstance();
    provisioner.setConfigId(provisionerConfigId);
    Object configuration = provisioner.retrieveGrouperProvisioningConfiguration();
    configuration.getClass().getMethod("setAzureExternalSystemConfigId", String.class).invoke(configuration, externalSystemConfigId);
    GrouperProvisionerTargetDaoBase dao = (GrouperProvisionerTargetDaoBase) Class.forName(
        "edu.internet2.middleware.grouper.app.azure.GrouperAzureTargetDao").getConstructor().newInstance();
    dao.setGrouperProvisioner(provisioner);
    return dao;
  }

  /**
   * the Zoom bearer token cache is one cache (not a new empty one per call) keyed by the zoom
   * external system config id, so a token cached for one Zoom config is never used for another
   * @throws Exception
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void testZoomBearerTokenCacheKeyedByConfigId() throws Exception {

    ExpirableCache cache = GrouperZoomCommands.bearerTokenCache();
    if (cache == null) {
      System.out.println("skip testZoomBearerTokenCacheKeyedByConfigId: grouper.zoom.cacheJwtForMinutes turns the cache off here");
      return;
    }
    assertSame("bearerTokenCache() must return the same cache each call, else nothing is ever reused",
        cache, GrouperZoomCommands.bearerTokenCache());

    try {
      cache.clear();
      Object keyMethod = GrouperZoomCommands.class.getDeclaredMethod("bearerTokenCacheKey", String.class);
      assertNotNull(keyMethod);
      // the fix keys the cache by the config id itself
      cache.put("zoomSysA162", "tokenTenantA162");

      // A reads its own cached token
      assertEquals("tokenTenantA162", GrouperZoomCommands.retrieveBearerTokenFromCacheOrFresh("zoomSysA162"));

      // B must not get A's token (a miss builds a fresh token, which fails on the unconfigured system)
      String tokenB = null;
      try {
        tokenB = GrouperZoomCommands.retrieveBearerTokenFromCacheOrFresh("zoomSysB162");
      } catch (RuntimeException re) {
        // not configured: not A's token
      }
      assertFalse("Zoom config B got config A's bearer token from the cache", "tokenTenantA162".equals(tokenB));
    } catch (NoSuchMethodException nsme) {
      fail("GrouperZoomCommands.bearerTokenCacheKey not found: the bearer token cache is not keyed by config id");
    } finally {
      cache.clear();
    }
  }

}
