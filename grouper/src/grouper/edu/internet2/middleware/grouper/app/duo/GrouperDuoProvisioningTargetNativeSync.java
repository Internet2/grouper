package edu.internet2.middleware.grouper.app.duo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Duo-specific {@link GrouperProvisioningTargetNativeSync}: builds native target reporting
 * beans for sync-back to the generic grouper_prov_group / grouper_prov_user tables.
 *
 * <p>Both <b>groups</b> and <b>users</b> are captured from the raw Duo JSON (JSON Pointer paths,
 * like SCIM and Adobe), hooked at the API-commands seam ({@code GrouperDuoApiCommands} read
 * methods such as {@code retrieveDuoGroups} / {@code retrieveDuoUsers} / {@code retrieveDuoUser})
 * where the per-element JSON node is in scope. This avoids losing any Duo field that the
 * {@link GrouperDuoGroup} / {@link GrouperDuoUser} typed beans do not model; operators can capture
 * any JSON field via {@code nativeAttributesGroups} / {@code nativeAttributesEntities} with a
 * {@code name} and optional JSON-Pointer {@code path}.
 *
 * <p>The default keys are chosen so each default's captured value matches what the OLD typed-bean
 * getter returned: the Duo user JSON field is {@code username} (stored under the friendlier key
 * {@code userName}, matching {@code GrouperDuoUser.getUserName()}), and the target ids come from
 * {@code /user_id} (user) and {@code /group_id} (group) -- the same JSON fields the old
 * typed-bean build methods read via {@code getId()} / {@code getGroup_id()}.
 *
 * <p>Memberships are still derived from the {@link GrouperDuoUser} typed bean's inline
 * {@code groups} set ({@link #captureMembershipsFromUser}); each {@link GrouperDuoGroup} in that
 * set carries its own {@code group_id}, so no name->id index resolution is needed. Only the
 * group/user object capture is JSON-based.
 */
public class GrouperDuoProvisioningTargetNativeSync extends GrouperProvisioningTargetNativeSync {

  /**
   * Default per-attribute capture list for Duo users when {@code nativeAttributesEntities}
   * is not configured. JSON-Pointer based (reads the raw Duo user JSON). Excludes {@code user_id}
   * (already the target_user_id column). Operators can capture any other Duo user JSON field
   * (firstname, lastname, realname, notes, ...) via {@code nativeAttributesEntities} with a
   * {@code name} and optional {@code path}.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_ENTITY_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Duo JSON field is "username"; store it under the friendlier key "userName"
          // (matches the old GrouperDuoUser.getUserName() default)
          attrConfigWithPath("userName", "/username"),
          attrConfig("email"),
          attrConfig("status"),
          // GRP-7384: epoch seconds of the last Duo authentication, and whether the user has
          // at least one auth device registered (independent of status, e.g. active but not enrolled)
          attrConfigWithPathAndType("lastLogin", "/last_login", "integer"),
          attrConfigWithPathAndType("isEnrolled", "/is_enrolled", "boolean")));

  /**
   * Default per-attribute capture list for Duo groups when {@code nativeAttributesGroups}
   * is not configured. JSON-Pointer based (reads the raw Duo group JSON). Excludes
   * {@code group_id} (already the target_group_id column). Operators can configure any other Duo
   * group JSON field (desc, status, ...) via {@code nativeAttributesGroups} with a {@code name}
   * (and optional {@code path}), since capture now reads the full JSON rather than the typed bean.
   */
  private static final List<GrouperProvisioningNativeAttributeConfig> DEFAULT_GROUP_ATTRS =
      Collections.unmodifiableList(Arrays.asList(
          // Duo JSON field is "name" (matches the old GrouperDuoGroup.getName() default)
          attrConfig("name")));

  private static GrouperProvisioningNativeAttributeConfig attrConfig(String name) {
    return attrConfigWithPath(name, null);
  }

  /**
   * Build a native-attribute config with an explicit JSON Pointer {@code path}. When {@code path}
   * is null the JSON path defaults to {@code "/" + name} (see {@link #populateAttributesFromJson}).
   */
  private static GrouperProvisioningNativeAttributeConfig attrConfigWithPath(String name, String path) {
    return attrConfigWithPathAndType(name, path, null);
  }

  /**
   * Build a native-attribute config with an explicit JSON Pointer {@code path} and declared
   * {@code type} (string|integer|boolean|timestamp, or null to auto-detect).
   */
  private static GrouperProvisioningNativeAttributeConfig attrConfigWithPathAndType(String name, String path, String type) {
    GrouperProvisioningNativeAttributeConfig cfg = new GrouperProvisioningNativeAttributeConfig();
    cfg.setName(name);
    cfg.setPath(path);
    cfg.setType(type);
    return cfg;
  }

  // ----- GRP-7384: derived auth method attributes ----------------------------------------

  /** activated phones with Duo Mobile push */
  public static final String ATTR_PUSH_PHONE_COUNT = "pushPhoneCount";

  /** activated phones with Duo Mobile passcodes */
  public static final String ATTR_MOBILE_OTP_PHONE_COUNT = "mobileOtpPhoneCount";

  /** latest Duo {@code last_seen} across activated push phones (Duo's ISO 8601 string, as returned) */
  public static final String ATTR_PUSH_PHONE_LAST_SEEN = "pushPhoneLastSeen";

  /** phones that can receive SMS passcodes */
  public static final String ATTR_SMS_PHONE_COUNT = "smsPhoneCount";

  /** phones that can receive phone callbacks */
  public static final String ATTR_VOICE_PHONE_COUNT = "voicePhoneCount";

  /** Duo-issued D-100 hardware tokens (token type d1) */
  public static final String ATTR_DUO_HARDWARE_TOKEN_COUNT = "duoHardwareTokenCount";

  /** HOTP tokens (token type h6 or h8) */
  public static final String ATTR_HOTP_TOKEN_COUNT = "hotpTokenCount";

  /** TOTP tokens (token type t6 or t8) */
  public static final String ATTR_TOTP_TOKEN_COUNT = "totpTokenCount";

  /** YubiKey OTP tokens (token type yk) */
  public static final String ATTR_YUBIKEY_OTP_TOKEN_COUNT = "yubikeyOtpTokenCount";

  /** WebAuthn credentials (platform or roaming, Duo does not say which) */
  public static final String ATTR_WEBAUTHN_COUNT = "webauthnCount";

  /** legacy U2F security keys */
  public static final String ATTR_U2F_COUNT = "u2fCount";

  /**
   * GRP-7384: if the derived auth method attributes should be captured for users. Reads the Duo
   * provisioner config {@code nativeAttributesEntitiesIncludeAuthMethods}. Isolated as a seam so
   * tests can turn it on without standing up a provisioner.
   * @return true to capture auth method attributes
   */
  protected boolean isIncludeAuthMethods() {
    GrouperProvisioner provisioner = this.getGrouperProvisioner();
    if (provisioner == null) {
      return false;
    }
    Object configuration = provisioner.retrieveGrouperProvisioningConfiguration();
    return configuration instanceof GrouperDuoConfiguration
        && ((GrouperDuoConfiguration) configuration).isNativeAttributesEntitiesIncludeAuthMethods();
  }

  /**
   * GRP-7384: derive per-user auth method counts from the raw Duo user JSON ({@code phones},
   * {@code tokens}, {@code webauthncredentials}, {@code u2ftokens}). These are arrays, which the
   * scalar JSON Pointer capture cannot store, so they are summarized into counts here. Phone
   * numbers, serials, and credential names are deliberately not captured.
   *
   * <p>Push and mobile passcode only count on activated phones (an unactivated Duo Mobile cannot
   * receive a push). SMS and voice do not depend on Duo Mobile activation, so every phone counts.
   * Every count is always returned (0 when none), so a 0 is distinguishable from "not captured".
   *
   * @param userNode raw Duo user JSON
   * @return attribute name to value (Long counts, String last seen), never null
   */
  public static Map<String, Object> computeAuthMethodAttributes(JsonNode userNode) {

    long pushPhoneCount = 0;
    long mobileOtpPhoneCount = 0;
    long smsPhoneCount = 0;
    long voicePhoneCount = 0;
    String pushPhoneLastSeen = null;

    JsonNode phonesNode = userNode == null ? null : userNode.get("phones");
    if (phonesNode != null && phonesNode.isArray()) {
      for (JsonNode phoneNode : phonesNode) {
        boolean activated = phoneNode.path("activated").asBoolean(false);
        Set<String> capabilities = new HashSet<String>();
        JsonNode capabilitiesNode = phoneNode.get("capabilities");
        if (capabilitiesNode != null && capabilitiesNode.isArray()) {
          for (JsonNode capabilityNode : capabilitiesNode) {
            capabilities.add(capabilityNode.asText());
          }
        }
        if (activated && capabilities.contains("push")) {
          pushPhoneCount++;
          // Duo last_seen is an ISO 8601 string (or blank), so the lexical max is the latest
          String lastSeen = StringUtils.trimToNull(phoneNode.path("last_seen").asText(null));
          if (lastSeen != null && (pushPhoneLastSeen == null || lastSeen.compareTo(pushPhoneLastSeen) > 0)) {
            pushPhoneLastSeen = lastSeen;
          }
        }
        if (activated && capabilities.contains("mobile_otp")) {
          mobileOtpPhoneCount++;
        }
        if (capabilities.contains("sms")) {
          smsPhoneCount++;
        }
        if (capabilities.contains("phone")) {
          voicePhoneCount++;
        }
      }
    }

    long duoHardwareTokenCount = 0;
    long hotpTokenCount = 0;
    long totpTokenCount = 0;
    long yubikeyOtpTokenCount = 0;

    // Duo token types: d1 = Duo-D100, h6/h8 = HOTP 6/8 digit, t6/t8 = TOTP 6/8 digit, yk = YubiKey OTP
    JsonNode tokensNode = userNode == null ? null : userNode.get("tokens");
    if (tokensNode != null && tokensNode.isArray()) {
      for (JsonNode tokenNode : tokensNode) {
        String type = StringUtils.defaultString(tokenNode.path("type").asText(null));
        if ("d1".equals(type)) {
          duoHardwareTokenCount++;
        } else if ("h6".equals(type) || "h8".equals(type)) {
          hotpTokenCount++;
        } else if ("t6".equals(type) || "t8".equals(type)) {
          totpTokenCount++;
        } else if ("yk".equals(type)) {
          yubikeyOtpTokenCount++;
        }
      }
    }

    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put(ATTR_PUSH_PHONE_COUNT, pushPhoneCount);
    result.put(ATTR_MOBILE_OTP_PHONE_COUNT, mobileOtpPhoneCount);
    if (pushPhoneLastSeen != null) {
      result.put(ATTR_PUSH_PHONE_LAST_SEEN, pushPhoneLastSeen);
    }
    result.put(ATTR_SMS_PHONE_COUNT, smsPhoneCount);
    result.put(ATTR_VOICE_PHONE_COUNT, voicePhoneCount);
    result.put(ATTR_DUO_HARDWARE_TOKEN_COUNT, duoHardwareTokenCount);
    result.put(ATTR_HOTP_TOKEN_COUNT, hotpTokenCount);
    result.put(ATTR_TOTP_TOKEN_COUNT, totpTokenCount);
    result.put(ATTR_YUBIKEY_OTP_TOKEN_COUNT, yubikeyOtpTokenCount);
    result.put(ATTR_WEBAUTHN_COUNT, (long) arraySize(userNode, "webauthncredentials"));
    result.put(ATTR_U2F_COUNT, (long) arraySize(userNode, "u2ftokens"));
    return result;
  }

  /** size of a JSON array field, or 0 if missing / not an array */
  private static int arraySize(JsonNode node, String fieldName) {
    JsonNode arrayNode = node == null ? null : node.get(fieldName);
    return arrayNode != null && arrayNode.isArray() ? arrayNode.size() : 0;
  }

  @Override
  protected List<GrouperProvisioningNativeAttributeConfig> getDefaultNativeAttributeConfigsEntities() {
    return DEFAULT_ENTITY_ATTRS;
  }

  @Override
  protected List<GrouperProvisioningNativeAttributeConfig> getDefaultNativeAttributeConfigsGroups() {
    return DEFAULT_GROUP_ATTRS;
  }

  // ----- build (raw Duo JSON -> native-reporting bean) ---------------------------------

  /**
   * Build a native group bean from the raw Duo group JSON. {@code targetId} is read from
   * {@code /group_id} (the same field the old typed-bean build read via getGroup_id()); the
   * attributes map is populated for each entry in {@link #effectiveNativeAttributeConfigsGroups()}
   * (operator-configured or default) by JSON Pointer. Returns null when the JSON is missing or has
   * no {@code group_id}.
   */
  public GrouperProvisioningTargetNativeGroup buildNativeGroupFromJson(JsonNode groupNode) {
    if (groupNode == null || groupNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(groupNode, "/group_id");
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeGroup bean = new GrouperProvisioningTargetNativeGroup();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), groupNode, effectiveNativeAttributeConfigsGroups());
    return bean;
  }

  /**
   * Build a native user bean from the raw Duo user JSON. {@code targetId} is read from
   * {@code /user_id} (the same field the old typed-bean build read via getId()); the attributes
   * map is populated for each entry in {@link #effectiveNativeAttributeConfigsEntities()}
   * (operator-configured or default) by JSON Pointer. Returns null when the JSON is missing or has
   * no {@code user_id}.
   */
  public GrouperProvisioningTargetNativeUser buildNativeUserFromJson(JsonNode userNode) {
    if (userNode == null || userNode.isMissingNode()) {
      return null;
    }
    String targetId = resolveScalarAsString(userNode, "/user_id");
    if (targetId == null) {
      return null;
    }
    GrouperProvisioningTargetNativeUser bean = new GrouperProvisioningTargetNativeUser();
    bean.setTargetId(targetId);
    populateAttributesFromJson(bean.getAttributes(), userNode, effectiveNativeAttributeConfigsEntities());
    // GRP-7384: derived auth method attributes are appended to whatever list was captured above
    // (defaults or operator-configured), they do not replace it
    if (this.isIncludeAuthMethods()) {
      bean.getAttributes().putAll(computeAuthMethodAttributes(userNode));
    }
    return bean;
  }

  /**
   * For each attribute config, resolve its JSON Pointer ({@code path}, or {@code "/" + name})
   * against the raw Duo JSON (group or user) and put the coerced value under {@code cfg.getName()}.
   * Missing / null nodes are skipped (no attribute row written).
   */
  private static void populateAttributesFromJson(
      Map<String, Object> destinationAttributes,
      JsonNode resourceNode,
      List<GrouperProvisioningNativeAttributeConfig> nativeAttributeConfigs) {
    if (destinationAttributes == null || resourceNode == null) {
      return;
    }
    for (GrouperProvisioningNativeAttributeConfig cfg : GrouperUtil.nonNull(nativeAttributeConfigs)) {
      // JSON Pointer per RFC 6901: explicit path wins, else "/" + name (e.g. /email, /status)
      String pointer = StringUtils.defaultIfBlank(cfg.getPath(), "/" + cfg.getName());
      JsonNode node = resourceNode.at(pointer);
      if (node == null || node.isMissingNode() || node.isNull()) {
        continue;
      }
      Object value = coerceJsonValue(node, cfg.getType());
      if (value != null) {
        destinationAttributes.put(cfg.getName(), value);
      }
    }
  }

  private static String resolveScalarAsString(JsonNode resourceNode, String jsonPointer) {
    if (resourceNode == null) {
      return null;
    }
    JsonNode node = resourceNode.at(jsonPointer);
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    return node.asText();
  }

  /**
   * Coerce a JsonNode to a scalar Object for storage in the attribute map. The declared type
   * ({@code "string"|"integer"|"boolean"|"timestamp"}) wins when present; otherwise the node's
   * intrinsic JSON type drives the choice.
   */
  private static Object coerceJsonValue(JsonNode node, String declaredType) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    if (StringUtils.equalsIgnoreCase(declaredType, "integer")) {
      return Long.valueOf(node.asLong());
    }
    if (StringUtils.equalsIgnoreCase(declaredType, "boolean")) {
      return Boolean.valueOf(node.asBoolean());
    }
    if (StringUtils.equalsIgnoreCase(declaredType, "timestamp")) {
      // store as the source string; downstream coercion handled by the dictionary path
      return node.asText();
    }
    if (StringUtils.equalsIgnoreCase(declaredType, "string")) {
      return node.asText();
    }
    // auto-detect by intrinsic JSON type
    if (node.isBoolean()) {
      return Boolean.valueOf(node.asBoolean());
    }
    if (node.isIntegralNumber()) {
      return Long.valueOf(node.asLong());
    }
    if (node.isNumber()) {
      return Double.valueOf(node.asDouble());
    }
    return node.asText();
  }

  // ----- capture convenience (build + record) ------------------------------------------

  /** Build + record a Duo group from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureGroupJson(JsonNode groupNode) {
    this.recordTargetNativeGroup(this.buildNativeGroupFromJson(groupNode));
  }

  /** Build + record a Duo user from its raw JSON. No-op when sync-back is off or id-less. */
  public void captureUserJson(JsonNode userNode) {
    this.recordTargetNativeUser(this.buildNativeUserFromJson(userNode));
  }

  /**
   * Translate a Duo user's inline {@code groups} set ({@link GrouperDuoGroup} beans with
   * their own {@code group_id}) into native membership beans and record them. No-op if
   * reporting is off or the user has no groups.
   */
  public void captureMembershipsFromUser(GrouperDuoUser grouperDuoUser) {
    if (grouperDuoUser == null || grouperDuoUser.getId() == null) {
      return;
    }
    Set<GrouperDuoGroup> userGroups = grouperDuoUser.getGroups();
    if (userGroups == null || userGroups.isEmpty()) {
      return;
    }
    List<GrouperProvisioningTargetNativeMembership> memberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();
    for (GrouperDuoGroup duoGroup : userGroups) {
      if (duoGroup == null || duoGroup.getGroup_id() == null) {
        continue;
      }
      GrouperProvisioningTargetNativeMembership membership = new GrouperProvisioningTargetNativeMembership();
      membership.setTargetGroupId(duoGroup.getGroup_id());
      membership.setTargetUserId(grouperDuoUser.getId());
      memberships.add(membership);
    }
    this.recordTargetNativeMemberships(memberships);
  }

  // ----- static dispatchers (called from GrouperDuoApiCommands / GrouperDuoTargetDao) ----

  /**
   * Capture a Duo group (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't a Duo one. Called from the
   * commands seam (GrouperDuoApiCommands read methods) for every group parsed from a read.
   */
  public static void captureGroupJsonFromCurrentProvisioner(JsonNode groupNode) {
    GrouperDuoProvisioningTargetNativeSync duoSync = duoSyncForCurrentProvisioner();
    if (duoSync == null) {
      return;
    }
    duoSync.captureGroupJson(groupNode);
  }

  /**
   * Capture a Duo user (from its raw JSON) against the current provisioner's sync. No-op if
   * there's no current provisioner or the active provisioner isn't a Duo one. Called from the
   * commands seam (GrouperDuoApiCommands read methods) for every user parsed from a read.
   */
  public static void captureUserJsonFromCurrentProvisioner(JsonNode userNode) {
    GrouperDuoProvisioningTargetNativeSync duoSync = duoSyncForCurrentProvisioner();
    if (duoSync == null) {
      return;
    }
    duoSync.captureUserJson(userNode);
  }

  /**
   * Translate a Duo user's inline {@code groups} set into native membership records and
   * record them on the current provisioner.
   */
  public static void captureMembershipsFromUserForCurrentProvisioner(GrouperDuoUser grouperDuoUser) {
    GrouperDuoProvisioningTargetNativeSync duoSync = duoSyncForCurrentProvisioner();
    if (duoSync == null) {
      return;
    }
    duoSync.captureMembershipsFromUser(grouperDuoUser);
  }

  /**
   * Write-track a successful Duo membership add ({@code associateUserToGroup}) against the current
   * provisioner: record {@code (groupTargetId, userTargetId)} in the native membership mirror.
   * Unlike groups/users (re-read via the drain), memberships are tracked purely from our own
   * successful writes -- never re-read -- so grouper_prov_mship stays current for GRP-7048 (full
   * sync serving memberships from the sync-back cache). No-op out of cycle or for a non-Duo
   * provisioner; the record call itself is a no-op when membership sync-back is off.
   */
  public static void captureMembershipInsertFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperDuoProvisioningTargetNativeSync duoSync = duoSyncForCurrentProvisioner();
    if (duoSync == null) {
      return;
    }
    duoSync.recordTargetNativeMembershipInsert(groupTargetId, userTargetId);
  }

  /**
   * Write-track a successful Duo membership remove ({@code disassociateUserFromGroup}) against the
   * current provisioner: drop {@code (groupTargetId, userTargetId)} from the native membership
   * mirror so the end-of-run flush deletes its grouper_prov_mship row. No-op out of cycle or for a
   * non-Duo provisioner; the record call itself is a no-op when membership sync-back is off.
   */
  public static void captureMembershipDeleteFromCurrentProvisioner(String groupTargetId, String userTargetId) {
    GrouperDuoProvisioningTargetNativeSync duoSync = duoSyncForCurrentProvisioner();
    if (duoSync == null) {
      return;
    }
    duoSync.recordTargetNativeMembershipDelete(groupTargetId, userTargetId);
  }

  private static GrouperDuoProvisioningTargetNativeSync duoSyncForCurrentProvisioner() {
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (provisioner == null) {
      return null;
    }
    GrouperProvisioningTargetNativeSync sync = provisioner.retrieveGrouperProvisioningTargetNativeSync();
    if (sync instanceof GrouperDuoProvisioningTargetNativeSync) {
      return (GrouperDuoProvisioningTargetNativeSync) sync;
    }
    return null;
  }

}
