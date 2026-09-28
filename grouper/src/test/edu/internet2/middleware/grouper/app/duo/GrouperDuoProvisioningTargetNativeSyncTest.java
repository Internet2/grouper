package edu.internet2.middleware.grouper.app.duo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link GrouperDuoProvisioningTargetNativeSync}: exercise the raw-JSON build
 * path (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperAdobeProvisioningTargetNativeSyncTest} /
 * {@code GrouperScim2ProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link GrouperDuoGroup} / {@link GrouperDuoUser} typed beans:
 * the friendlier default key ("userName" from {@code /username}), the target ids drawn from
 * {@code /user_id} and {@code /group_id} (the same JSON fields the old typed-bean build read via
 * getId()/getGroup_id()), exclusion of those id fields from the attribute map, type coercion, and
 * -- the whole point of capturing from raw JSON -- that an operator can capture ANY Duo JSON field
 * by name/path, including one the typed bean does not model at all.
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same parser
 * the production read path uses), so the shapes match what {@code GrouperDuoApiCommands} hands the
 * capture seam (the elements of the {@code response} array / object).
 */
public class GrouperDuoProvisioningTargetNativeSyncTest extends GrouperTest {

  public GrouperDuoProvisioningTargetNativeSyncTest() {
  }

  public GrouperDuoProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDuoProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample Duo user JSON in the shape {@link GrouperDuoUser#fromJson} parses (an element of the
   * {@code response} array from retrieveDuoUsers). Includes {@code enable_auto_prompt}, which the
   * typed bean does NOT model -- used to prove raw-JSON capture can reach a field the bean would
   * have silently dropped. {@code last_login} is a JSON number for coercion.
   */
  private static final String USER_JSON = "{"
      + "\"user_id\":\"abc123\","
      + "\"username\":\"mchyzer\","
      + "\"email\":\"abc@school.edu\","
      + "\"status\":\"active\","
      + "\"firstname\":\"Dave\","
      + "\"lastname\":\"Smith\","
      + "\"realname\":\"Dave Smith\","
      + "\"enable_auto_prompt\":true,"
      + "\"last_login\":1727537850,"
      + "\"is_enrolled\":true,"
      + "\"notes\":\"some notes\""
      + "}";

  /**
   * A sample Duo group JSON in the shape {@link GrouperDuoGroup#fromJson} parses (an element of the
   * {@code response} array from retrieveDuoGroups). {@code status} is not modeled by the typed
   * bean; {@code member_count} is a JSON number for coercion.
   */
  private static final String GROUP_JSON = "{"
      + "\"group_id\":\"DGCXPKWT7MJ7WLQT7CMQ\","
      + "\"name\":\"EarlyAdopters\","
      + "\"desc\":\"the early adopters group\","
      + "\"status\":\"Active\","
      + "\"member_count\":7"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"username\":\"alice\"}");
    assertNull("user without /user_id should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> Duo user defaults: userName (from {@code /username}), email, status.
   * {@code user_id} is the target_user_id column (not an attribute); other JSON fields are not
   * captured unless an operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("abc123", bean.getTargetId());                            // from /user_id
    assertEquals("mchyzer", bean.getAttributes().get("userName"));         // from /username
    assertEquals("abc@school.edu", bean.getAttributes().get("email"));
    assertEquals("active", bean.getAttributes().get("status"));
    assertEquals("GRP-7384: lastLogin default from /last_login",
        Long.valueOf(1727537850L), bean.getAttributes().get("lastLogin"));
    assertEquals("GRP-7384: isEnrolled default from /is_enrolled",
        Boolean.TRUE, bean.getAttributes().get("isEnrolled"));
    assertFalse("GRP-7384: auth method attributes are off by default",
        bean.getAttributes().containsKey("pushPhoneCount"));
    assertFalse("user_id is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("user_id"));
    assertFalse("username is captured under userName, not username",
        bean.getAttributes().containsKey("username"));
    assertFalse("firstname is not a default", bean.getAttributes().containsKey("firstname"));
    assertFalse("enable_auto_prompt is not a default",
        bean.getAttributes().containsKey("enable_auto_prompt"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"user_id\":\"u-2\",\"email\":\"x@y.edu\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("email"));
    assertFalse(bean.getAttributes().containsKey("userName"));
    assertFalse(bean.getAttributes().containsKey("status"));
    assertFalse(bean.getAttributes().containsKey("lastLogin"));
    assertFalse(bean.getAttributes().containsKey("isEnrolled"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any Duo user JSON field by
   * name/path -- including {@code enable_auto_prompt}, which the {@link GrouperDuoUser} typed bean
   * does not model and the old switch-on-getter capture could never have reached. A declared
   * {@code integer} type coerces the JSON number to a Long.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    GrouperDuoProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("firstname", null, null),
        attr("enable_auto_prompt", null, null),       // not on the typed bean
        attr("last_login", null, "integer")));        // coerce JSON number -> Long
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Dave", bean.getAttributes().get("firstname"));
    assertEquals(Boolean.TRUE, bean.getAttributes().get("enable_auto_prompt"));
    assertEquals(Long.valueOf(1727537850L), bean.getAttributes().get("last_login"));
  }

  // ===================== GRP-7384: auth method attributes =====================

  /**
   * A Duo user in the real Admin API shape with one of everything: an activated smartphone
   * (push, sms, voice, mobile passcode), an unactivated smartphone that also advertises push and
   * mobile passcode (must NOT count for those), a landline (voice only), and an sms-only phone; a
   * D-100, two HOTP (one 8 digit), a TOTP, a YubiKey OTP, and an unknown token type; two WebAuthn
   * credentials and one U2F key.
   */
  private static final String AUTH_METHODS_USER_JSON = "{"
      + "\"user_id\":\"u-auth\","
      + "\"username\":\"jsmith\","
      + "\"status\":\"active\","
      + "\"is_enrolled\":true,"
      + "\"last_login\":1727537850,"
      + "\"phones\":["
      + "{\"activated\":true,\"capabilities\":[\"auto\",\"push\",\"sms\",\"phone\",\"mobile_otp\"],"
      +   "\"last_seen\":\"2024-09-28T21:28:48\",\"number\":\"+12155550001\",\"type\":\"Mobile\"},"
      + "{\"activated\":true,\"capabilities\":[\"auto\",\"push\",\"sms\",\"mobile_otp\"],"
      +   "\"last_seen\":\"2025-01-02T03:04:05\",\"number\":\"+12155550002\",\"type\":\"Mobile\"},"
      + "{\"activated\":false,\"capabilities\":[\"auto\",\"push\",\"sms\",\"mobile_otp\"],"
      +   "\"last_seen\":\"2026-01-01T00:00:00\",\"number\":\"+12155550003\",\"type\":\"Mobile\"},"
      + "{\"activated\":false,\"capabilities\":[\"auto\",\"phone\"],"
      +   "\"last_seen\":\"\",\"number\":\"+12155550004\",\"type\":\"Landline\"},"
      + "{\"activated\":false,\"capabilities\":[\"sms\"],"
      +   "\"last_seen\":\"\",\"number\":\"+12155550005\",\"type\":\"Mobile\"}"
      + "],"
      + "\"tokens\":["
      + "{\"serial\":\"D100-1\",\"token_id\":\"t1\",\"type\":\"d1\"},"
      + "{\"serial\":\"legacy__hotp__0\",\"token_id\":\"t2\",\"totp_step\":null,\"type\":\"h6\"},"
      + "{\"serial\":\"hotp8\",\"token_id\":\"t3\",\"totp_step\":null,\"type\":\"h8\"},"
      + "{\"serial\":\"legacy__totp__30\",\"token_id\":\"t4\",\"totp_step\":30,\"type\":\"t6\"},"
      + "{\"serial\":\"yk1\",\"token_id\":\"t5\",\"type\":\"yk\"},"
      + "{\"serial\":\"x1\",\"token_id\":\"t6\",\"type\":\"zz\"}"
      + "],"
      + "\"webauthncredentials\":["
      + "{\"credential_name\":\"Touch ID\",\"date_added\":1699543237,\"label\":\"Chrome on Mac\",\"webauthnkey\":\"w1\"},"
      + "{\"credential_name\":\"Security Key\",\"date_added\":1712852003,\"label\":\"YubiKey\",\"webauthnkey\":\"w2\"}"
      + "],"
      + "\"u2ftokens\":[{\"date_added\":1600000000,\"registration_id\":\"r1\"}]"
      + "}";

  /**
   * Every derived count, with push and mobile passcode only counting activated phones, sms and
   * voice counting every phone, tokens bucketed by Duo type (unknown types ignored), and the push
   * last seen being the latest across ACTIVATED push phones only.
   */
  public void testComputeAuthMethodAttributes() {
    Map<String, Object> attrs = GrouperDuoProvisioningTargetNativeSync.computeAuthMethodAttributes(
        GrouperUtil.jsonJacksonNode(AUTH_METHODS_USER_JSON));

    assertEquals(Long.valueOf(2), attrs.get("pushPhoneCount"));
    assertEquals(Long.valueOf(2), attrs.get("mobileOtpPhoneCount"));
    assertEquals("latest last_seen among activated push phones (the unactivated 2026 one is ignored)",
        "2025-01-02T03:04:05", attrs.get("pushPhoneLastSeen"));
    assertEquals(Long.valueOf(4), attrs.get("smsPhoneCount"));
    assertEquals(Long.valueOf(2), attrs.get("voicePhoneCount"));
    assertEquals(Long.valueOf(1), attrs.get("duoHardwareTokenCount"));
    assertEquals(Long.valueOf(2), attrs.get("hotpTokenCount"));
    assertEquals(Long.valueOf(1), attrs.get("totpTokenCount"));
    assertEquals(Long.valueOf(1), attrs.get("yubikeyOtpTokenCount"));
    assertEquals(Long.valueOf(2), attrs.get("webauthnCount"));
    assertEquals(Long.valueOf(1), attrs.get("u2fCount"));
    assertEquals("exactly the 11 documented attributes", 11, attrs.size());

    // nothing identifying leaks into the attributes
    for (Object value : attrs.values()) {
      assertFalse("phone number leaked: " + value, String.valueOf(value).contains("+1215"));
    }
  }

  /**
   * A stuck user: only SMS / voice phones and legacy HOTP / TOTP tokens. All the "safe" counts are
   * 0 (not missing), and there is no push last seen.
   */
  public void testComputeAuthMethodAttributesSmsVoiceOnly() {
    String json = "{\"user_id\":\"u-stuck\","
        + "\"phones\":[{\"activated\":false,\"capabilities\":[\"auto\",\"sms\",\"phone\"],\"last_seen\":\"\"}],"
        + "\"tokens\":[{\"type\":\"h6\"},{\"type\":\"t6\"}],"
        + "\"webauthncredentials\":[],\"u2ftokens\":[]}";
    Map<String, Object> attrs = GrouperDuoProvisioningTargetNativeSync.computeAuthMethodAttributes(
        GrouperUtil.jsonJacksonNode(json));

    assertEquals(Long.valueOf(1), attrs.get("smsPhoneCount"));
    assertEquals(Long.valueOf(1), attrs.get("voicePhoneCount"));
    assertEquals(Long.valueOf(1), attrs.get("hotpTokenCount"));
    assertEquals(Long.valueOf(1), attrs.get("totpTokenCount"));
    assertEquals(Long.valueOf(0), attrs.get("pushPhoneCount"));
    assertEquals(Long.valueOf(0), attrs.get("mobileOtpPhoneCount"));
    assertEquals(Long.valueOf(0), attrs.get("duoHardwareTokenCount"));
    assertEquals(Long.valueOf(0), attrs.get("yubikeyOtpTokenCount"));
    assertEquals(Long.valueOf(0), attrs.get("webauthnCount"));
    assertEquals(Long.valueOf(0), attrs.get("u2fCount"));
    assertFalse(attrs.containsKey("pushPhoneLastSeen"));
  }

  /** Missing arrays (not just empty) all count 0 and do not throw. */
  public void testComputeAuthMethodAttributesNoArrays() {
    Map<String, Object> attrs = GrouperDuoProvisioningTargetNativeSync.computeAuthMethodAttributes(
        GrouperUtil.jsonJacksonNode("{\"user_id\":\"u-none\"}"));
    assertEquals(10, attrs.size());
    for (Map.Entry<String, Object> entry : attrs.entrySet()) {
      assertEquals(entry.getKey(), Long.valueOf(0), entry.getValue());
    }
    assertEquals(10, GrouperDuoProvisioningTargetNativeSync.computeAuthMethodAttributes(null).size());
  }

  /** With the flag on, auth method attributes are ADDED to the defaults (not replacing them). */
  public void testBuildNativeUserIncludeAuthMethodsAddsToDefaults() {
    GrouperDuoProvisioningTargetNativeSync sync = new GrouperDuoProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return getDefaultNativeAttributeConfigsEntities();
      }
      @Override
      protected boolean isIncludeAuthMethods() {
        return true;
      }
    };
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(AUTH_METHODS_USER_JSON));

    assertEquals("jsmith", bean.getAttributes().get("userName"));
    assertEquals("active", bean.getAttributes().get("status"));
    assertEquals(Long.valueOf(1727537850L), bean.getAttributes().get("lastLogin"));
    assertEquals(Boolean.TRUE, bean.getAttributes().get("isEnrolled"));
    assertEquals(Long.valueOf(2), bean.getAttributes().get("pushPhoneCount"));
    assertEquals(Long.valueOf(1), bean.getAttributes().get("duoHardwareTokenCount"));
    assertFalse("raw phones array is never captured", bean.getAttributes().containsKey("phones"));
  }

  /** With the flag on, auth method attributes are ADDED to an operator-configured list too. */
  public void testBuildNativeUserIncludeAuthMethodsAddsToConfigured() {
    GrouperDuoProvisioningTargetNativeSync sync = new GrouperDuoProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return Arrays.asList(attr("userName", "/username", null));
      }
      @Override
      protected boolean isIncludeAuthMethods() {
        return true;
      }
    };
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(AUTH_METHODS_USER_JSON));

    assertEquals("jsmith", bean.getAttributes().get("userName"));
    assertFalse("configured list replaced the defaults", bean.getAttributes().containsKey("status"));
    assertEquals(Long.valueOf(4), bean.getAttributes().get("smsPhoneCount"));
    assertEquals(Long.valueOf(2), bean.getAttributes().get("webauthnCount"));
  }

  /** No provisioner attached means the flag reads as off (no NPE). */
  public void testIsIncludeAuthMethodsFalseWithoutProvisioner() {
    assertFalse(new GrouperDuoProvisioningTargetNativeSync().isIncludeAuthMethods());
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"name\":\"g\"}");
    assertNull("group without /group_id should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> Duo group defaults: name (from {@code /name}). targetId comes from
   * {@code /group_id} (not captured as an attribute).
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("DGCXPKWT7MJ7WLQT7CMQ", bean.getTargetId());            // from /group_id
    assertEquals("EarlyAdopters", bean.getAttributes().get("name"));     // from /name
    assertFalse("group_id is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("group_id"));
    assertFalse("desc is not a default", bean.getAttributes().containsKey("desc"));
    assertFalse("status is not a default", bean.getAttributes().containsKey("status"));
    assertFalse("member_count is not a default", bean.getAttributes().containsKey("member_count"));
  }

  /**
   * Operator-configured group fields: an unmodeled field ({@code status}) is captured, and a
   * declared {@code integer} type coerces the JSON number to a Long.
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    GrouperDuoProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("member_count", null, "integer"),
        attr("status", null, null)));        // not on the typed bean
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals(Long.valueOf(7L), bean.getAttributes().get("member_count"));
    assertEquals("Active", bean.getAttributes().get("status"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperDuoProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperDuoProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(GROUP_JSON));
  }

  // ===================== helpers =====================

  private static GrouperProvisioningNativeAttributeConfig attr(String name, String path, String type) {
    GrouperProvisioningNativeAttributeConfig cfg = new GrouperProvisioningNativeAttributeConfig();
    cfg.setName(name);
    cfg.setPath(path);
    cfg.setType(type);
    return cfg;
  }

  /**
   * Sync that returns the built-in Duo defaults without consulting a live provisioner. The build
   * methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to the
   * protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static GrouperDuoProvisioningTargetNativeSync defaultsSync() {
    return new GrouperDuoProvisioningTargetNativeSync() {
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
  private static GrouperDuoProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new GrouperDuoProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static GrouperDuoProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new GrouperDuoProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
