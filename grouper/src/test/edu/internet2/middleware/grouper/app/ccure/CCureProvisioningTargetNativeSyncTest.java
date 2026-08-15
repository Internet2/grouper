package edu.internet2.middleware.grouper.app.ccure;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.internet2.middleware.grouper.app.ccure.CCureTargetDao.CCureMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link CCureProvisioningTargetNativeSync}: exercise the raw-JSON build path
 * (Clearance + Personnel) and the DisplayProperties widening in isolation -- no Tomcat, no
 * provisioning cycle, no mock. Mirrors {@code DatadogProvisioningTargetNativeSyncTest}.
 *
 * <p>CCure returns flat PascalCase JSON objects (unlike Datadog's JSON:API envelopes), so pointers
 * are top-level: {@code /GUID}, {@code /Name}, {@code /Int1}, {@code /PartitionID}. The id fields
 * are excluded from the attribute map because they are already the target_group_id /
 * target_user_id columns.
 *
 * <p>The widening tests cover the CCure-specific wrinkle: {@code GetAllWithCriteria} is
 * projection-based, so an operator-configured native attribute has to be added to the request's
 * {@code DisplayProperties} array or it comes back missing and is silently dropped.
 */
public class CCureProvisioningTargetNativeSyncTest extends GrouperTest {

  public CCureProvisioningTargetNativeSyncTest() {
  }

  public CCureProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new CCureProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample CCure Personnel object in the shape {@code CCureUser.fromJson} parses (an element of
   * the array returned by GetAllWithCriteria on Personnel). {@code Text1} and {@code LastName} are
   * NOT modeled by the {@code CCureUser} record -- used to prove raw-JSON capture can reach a field
   * the record would have silently dropped.
   */
  private static final String USER_JSON = "{"
      + "\"ObjectID\":5001,"
      + "\"GUID\":\"11112222-3333-4444-5555-666677778888\","
      + "\"Name\":\"Smith, Dave\","
      + "\"Int1\":\"12345678\","
      + "\"Text1\":\"dsmith\","
      + "\"LastName\":\"Smith\","
      + "\"Disabled\":false,"
      + "\"CredentialCount\":3"
      + "}";

  /**
   * A sample CCure Clearance object in the shape {@code CCureGroup.fromJson} parses.
   * {@code Description} is NOT modeled by the {@code CCureGroup} record.
   */
  private static final String GROUP_JSON = "{"
      + "\"ObjectID\":9001,"
      + "\"GUID\":\"aaaabbbb-cccc-dddd-eeee-ffff00001111\","
      + "\"Name\":\"Library After Hours\","
      + "\"PartitionID\":1,"
      + "\"Description\":\"Access to the library outside posted hours\","
      + "\"DoorCount\":12"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    // neither PersonnelID nor ObjectID -> no target id, so no bean
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"GUID\":\"g\",\"Name\":\"n\"}");
    assertNull("user without PersonnelID or ObjectID should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> CCure entity defaults: GUID, Name, Int1. The id is the target_user_id
   * column, not an attribute; other CCure fields are not captured unless an operator configures
   * them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("5001", bean.getTargetId());                                       // from /ObjectID
    assertEquals("11112222-3333-4444-5555-666677778888", bean.getAttributes().get("GUID"));
    assertEquals("Smith, Dave", bean.getAttributes().get("Name"));
    assertEquals("12345678", bean.getAttributes().get("Int1"));
    assertFalse("ObjectID is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("ObjectID"));
    assertFalse("Text1 is not a default", bean.getAttributes().containsKey("Text1"));
    assertFalse("Disabled is not a default", bean.getAttributes().containsKey("Disabled"));
  }

  /**
   * PersonnelID wins over ObjectID for the target id -- the same either/or {@code CCureUser.fromJson}
   * applies, since which one comes back depends on the endpoint.
   */
  public void testBuildNativeUserPrefersPersonnelIdOverObjectId() {
    JsonNode user = GrouperUtil.jsonJacksonNode(
        "{\"PersonnelID\":7777,\"ObjectID\":8888,\"GUID\":\"g\",\"Name\":\"n\"}");
    assertEquals("7777", defaultsSync().buildNativeUserFromJson(user).getTargetId());
  }

  /** With no PersonnelID, ObjectID is the target id (the GetAllWithCriteria-on-Personnel shape). */
  public void testBuildNativeUserFallsBackToObjectId() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"ObjectID\":8888,\"GUID\":\"g\"}");
    assertEquals("8888", defaultsSync().buildNativeUserFromJson(user).getTargetId());
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"ObjectID\":5002,\"Name\":\"Only Name\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("5002", bean.getTargetId());
    assertEquals("Only Name", bean.getAttributes().get("Name"));
    assertFalse(bean.getAttributes().containsKey("GUID"));
    assertFalse(bean.getAttributes().containsKey("Int1"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any CCure Personnel field --
   * including {@code Text1} and {@code CredentialCount}, which the {@code CCureUser} record does not
   * model. Also proves a declared {@code integer} type coerces the JSON number to a Long and a
   * declared {@code boolean} coerces to a Boolean.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    CCureProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("Text1", "/Text1", null),                        // not on the CCureUser record
        attr("LastName", "/LastName", null),                  // not on the CCureUser record
        attr("Disabled", "/Disabled", "boolean"),
        attr("CredentialCount", "/CredentialCount", "integer")));
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("dsmith", bean.getAttributes().get("Text1"));
    assertEquals("Smith", bean.getAttributes().get("LastName"));
    assertEquals(Boolean.FALSE, bean.getAttributes().get("Disabled"));
    assertEquals(Long.valueOf(3L), bean.getAttributes().get("CredentialCount"));
  }

  /** With no explicit path, the pointer defaults to "/" + name, which is right for CCure's flat JSON. */
  public void testBuildNativeUserDefaultsPointerToSlashName() {
    CCureProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("LastName", null, null)));
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Smith", bean.getAttributes().get("LastName"));
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"GUID\":\"g\",\"Name\":\"n\"}");
    assertNull("group without ObjectID should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> CCure group defaults: GUID, Name, PartitionID. NB the field is
   * PartitionID, which is what CCure returns and what {@code CCureGroup.fromJson} reads.
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("9001", bean.getTargetId());                                       // from /ObjectID
    assertEquals("aaaabbbb-cccc-dddd-eeee-ffff00001111", bean.getAttributes().get("GUID"));
    assertEquals("Library After Hours", bean.getAttributes().get("Name"));
    assertEquals(Long.valueOf(1L), bean.getAttributes().get("PartitionID"));        // number auto-detected
    assertFalse("ObjectID is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("ObjectID"));
    assertFalse("Description is not a default", bean.getAttributes().containsKey("Description"));
  }

  /** Operator-configured group fields, including one the CCureGroup record does not model. */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    CCureProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("Description", "/Description", null),            // not on the CCureGroup record
        attr("DoorCount", "/DoorCount", "integer"),
        attr("PartitionID", "/PartitionID", "string")));      // declared type wins over auto-detect
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("Access to the library outside posted hours", bean.getAttributes().get("Description"));
    assertEquals(Long.valueOf(12L), bean.getAttributes().get("DoorCount"));
    assertEquals("1", bean.getAttributes().get("PartitionID"));
  }

  // ===================== DisplayProperties widening =====================

  /**
   * The CCure-specific piece: a configured attribute that is not already in the projection gets
   * appended, so GetAllWithCriteria actually returns it.
   */
  public void testWidenDisplayPropertiesAddsConfiguredFields() {
    ArrayNode displayProperties = displayProperties("ObjectID", "GUID", "Name", "Int1");

    CCureProvisioningTargetNativeSync.widenDisplayProperties(displayProperties, Arrays.asList(
        attr("Text1", "/Text1", null),
        attr("LastName", "/LastName", null)));

    assertEquals(6, displayProperties.size());
    assertEquals("Text1", displayProperties.get(4).asText());
    assertEquals("LastName", displayProperties.get(5).asText());
  }

  /** A configured attribute that duplicates a built-in projection field is not added twice. */
  public void testWidenDisplayPropertiesDoesNotDuplicate() {
    ArrayNode displayProperties = displayProperties("ObjectID", "GUID", "Name", "Int1");

    CCureProvisioningTargetNativeSync.widenDisplayProperties(displayProperties, Arrays.asList(
        attr("Name", "/Name", null),
        attr("Int1", "/Int1", null),
        attr("Text1", "/Text1", null)));

    assertEquals("only the new field should be appended", 5, displayProperties.size());
    assertEquals("Text1", displayProperties.get(4).asText());
  }

  /** Two configured entries naming the same CCure field collapse to one projection entry. */
  public void testWidenDisplayPropertiesDedupesWithinConfiguredList() {
    ArrayNode displayProperties = displayProperties("ObjectID");

    CCureProvisioningTargetNativeSync.widenDisplayProperties(displayProperties, Arrays.asList(
        attr("Text1", "/Text1", null),
        attr("Text1Again", "/Text1", null)));

    assertEquals(2, displayProperties.size());
    assertEquals("Text1", displayProperties.get(1).asText());
  }

  /** With no path, the config name is the CCure field to request. */
  public void testWidenDisplayPropertiesUsesNameWhenNoPath() {
    ArrayNode displayProperties = displayProperties("ObjectID");

    CCureProvisioningTargetNativeSync.widenDisplayProperties(displayProperties,
        Arrays.asList(attr("LastName", null, null)));

    assertEquals(2, displayProperties.size());
    assertEquals("LastName", displayProperties.get(1).asText());
  }

  /**
   * A nested pointer cannot be expressed as a DisplayProperties entry, so the first segment is
   * requested and the rest of the pointer resolves against whatever CCure returns under it.
   */
  public void testWidenDisplayPropertiesRequestsFirstSegmentOfNestedPointer() {
    ArrayNode displayProperties = displayProperties("ObjectID");

    CCureProvisioningTargetNativeSync.widenDisplayProperties(displayProperties,
        Arrays.asList(attr("badgeNumber", "/Credential/Number", null)));

    assertEquals(2, displayProperties.size());
    assertEquals("Credential", displayProperties.get(1).asText());
  }

  /** A blank-named config is skipped rather than producing an empty projection entry. */
  public void testWidenDisplayPropertiesSkipsBlankNames() {
    ArrayNode displayProperties = displayProperties("ObjectID");

    CCureProvisioningTargetNativeSync.widenDisplayProperties(displayProperties,
        Arrays.asList(attr("", "/Whatever", null), attr(null, null, null)));

    assertEquals(1, displayProperties.size());
  }

  /** Null/empty config list leaves the projection untouched (the no-sync-back-configured case). */
  public void testWidenDisplayPropertiesNullConfigsIsNoOp() {
    ArrayNode displayProperties = displayProperties("ObjectID", "GUID");

    CCureProvisioningTargetNativeSync.widenDisplayProperties(displayProperties, null);

    assertEquals(2, displayProperties.size());
  }

  // ===================== membership capture =====================

  /**
   * A clearance pair maps to the native membership key with no translation: ClearanceID is the
   * native group id and PersonnelID is the native user id, because both are the referenced object's
   * ObjectID.
   */
  public void testCaptureMembershipsBuildsKeysFromClearanceAndPersonnelIds() {
    final java.util.List<String> recorded = new java.util.ArrayList<String>();

    CCureProvisioningTargetNativeSync sync = new CCureProvisioningTargetNativeSync() {
      @Override
      public synchronized void recordTargetNativeMemberships(
          List<edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership> memberships) {
        for (edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership membership
            : memberships) {
          recorded.add(membership.getTargetGroupId() + "/" + membership.getTargetUserId());
        }
      }
    };

    sync.captureMemberships(Arrays.asList(
        new CCureMembership(5001, 9001, 70001),
        new CCureMembership(5002, 9001, 70002)));

    assertEquals(2, recorded.size());
    assertEquals("9001/5001", recorded.get(0));
    assertEquals("9001/5002", recorded.get(1));
  }

  /** Empty and null membership lists are a no-op rather than an error. */
  public void testCaptureMembershipsEmptyIsNoOp() {
    defaultsSync().captureMemberships(null);
    defaultsSync().captureMemberships(new java.util.ArrayList<CCureMembership>());
  }

  // ===================== static dispatchers out of cycle =====================
  // Every static entry point must be safe to call when there is no current provisioner (e.g. the
  // API commands used outside a provisioning run, as the diagnostics screen does).

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    CCureProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(GROUP_JSON));
  }

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    CCureProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureMembershipFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    CCureProvisioningTargetNativeSync.captureMembershipFromCurrentProvisioner(
        new CCureMembership(5001, 9001, 70001));
    CCureProvisioningTargetNativeSync.captureMembershipFromCurrentProvisioner(null);
  }

  public void testCaptureMembershipsFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    CCureProvisioningTargetNativeSync.captureMembershipsFromCurrentProvisioner(
        Arrays.asList(new CCureMembership(5001, 9001, 70001)));
  }

  public void testCaptureMembershipInsertFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    CCureProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner("9001", "5001");
  }

  public void testCaptureMembershipDeleteFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    CCureProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner("9001", "5001");
  }

  /**
   * The widening dispatchers must also be safe out of cycle, and must leave the projection alone
   * (a provisioner-less call means there is nothing configured to widen for).
   */
  public void testWidenDisplayPropertiesFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    ArrayNode displayProperties = displayProperties("ObjectID", "GUID");

    CCureProvisioningTargetNativeSync.widenDisplayPropertiesForEntitiesFromCurrentProvisioner(displayProperties);
    CCureProvisioningTargetNativeSync.widenDisplayPropertiesForGroupsFromCurrentProvisioner(displayProperties);

    assertEquals(2, displayProperties.size());
  }

  // ===================== helpers =====================

  private static GrouperProvisioningNativeAttributeConfig attr(String name, String path, String type) {
    GrouperProvisioningNativeAttributeConfig cfg = new GrouperProvisioningNativeAttributeConfig();
    cfg.setName(name);
    cfg.setPath(path);
    cfg.setType(type);
    return cfg;
  }

  /** A DisplayProperties array preloaded with the given CCure field names. */
  private static ArrayNode displayProperties(String... fieldNames) {
    ArrayNode arrayNode = new com.fasterxml.jackson.databind.ObjectMapper().createArrayNode();
    for (String fieldName : fieldNames) {
      arrayNode.add(fieldName);
    }
    return arrayNode;
  }

  /**
   * Sync that returns the built-in CCure defaults without consulting a live provisioner. The build
   * methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to the
   * protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static CCureProvisioningTargetNativeSync defaultsSync() {
    return new CCureProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return getDefaultNativeAttributeConfigsEntities();
      }
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return getDefaultNativeAttributeConfigsGroups();
      }
    };
  }

  /** Sync whose entity capture list is exactly {@code entityAttrs} (simulates operator config). */
  private static CCureProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new CCureProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static CCureProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new CCureProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
