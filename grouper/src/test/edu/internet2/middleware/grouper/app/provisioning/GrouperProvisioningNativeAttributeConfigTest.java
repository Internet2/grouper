package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig.NativeAttributeConfigException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Tests for {@link GrouperProvisioningNativeAttributeConfig#parseAndValidate(String, String)}.
 * Covers both the comma-separated and JSON-array forms, plus error cases.
 */
public class GrouperProvisioningNativeAttributeConfigTest extends GrouperTest {

  public GrouperProvisioningNativeAttributeConfigTest() {
  }

  public GrouperProvisioningNativeAttributeConfigTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperProvisioningNativeAttributeConfigTest("testParseCsvSimple"));
  }

  // ===================== CSV form =====================

  public void testParseCsvSimple() {
    List<GrouperProvisioningNativeAttributeConfig> result =
        GrouperProvisioningNativeAttributeConfig.parseAndValidate("sn, mail, telephoneNumber", "label");
    assertEquals(3, result.size());

    assertEquals("sn", result.get(0).getName());
    assertNull(result.get(0).getPath());
    assertNull(result.get(0).getType());

    assertEquals("mail", result.get(1).getName());
    assertEquals("telephoneNumber", result.get(2).getName());
  }

  public void testParseCsvSingleAttribute() {
    List<GrouperProvisioningNativeAttributeConfig> result =
        GrouperProvisioningNativeAttributeConfig.parseAndValidate("uid", "label");
    assertEquals(1, result.size());
    assertEquals("uid", result.get(0).getName());
  }

  public void testParseCsvIgnoresEmptyTokensAndWhitespace() {
    // trailing commas, extra spaces, and a stray empty token in the middle
    List<GrouperProvisioningNativeAttributeConfig> result =
        GrouperProvisioningNativeAttributeConfig.parseAndValidate("  cn ,  description , , gidNumber, ", "label");
    assertEquals(3, result.size());
    assertEquals("cn", result.get(0).getName());
    assertEquals("description", result.get(1).getName());
    assertEquals("gidNumber", result.get(2).getName());
  }

  public void testParseCsvBlankInputReturnsEmptyList() {
    assertEquals(0, GrouperProvisioningNativeAttributeConfig.parseAndValidate(null, "label").size());
    assertEquals(0, GrouperProvisioningNativeAttributeConfig.parseAndValidate("", "label").size());
    assertEquals(0, GrouperProvisioningNativeAttributeConfig.parseAndValidate("   ", "label").size());
  }

  public void testParseCsvRejectsDuplicateNames() {
    try {
      GrouperProvisioningNativeAttributeConfig.parseAndValidate("sn, mail, sn", "myLabel");
      fail("expected exception for duplicate name");
    } catch (RuntimeException e) {
      assertTrue("message should name the label: " + e.getMessage(), e.getMessage().contains("myLabel"));
      assertTrue("message should mention duplicate: " + e.getMessage(), e.getMessage().contains("sn"));
    }
  }

  public void testParseCsvRejectsJsonLikeTokens() {
    // a half-JSON typo shouldn't silently become an "attribute name"
    try {
      GrouperProvisioningNativeAttributeConfig.parseAndValidate("sn, {\"name\":\"mail\"}", "myLabel");
      fail("expected exception for JSON-ish token in CSV form");
    } catch (RuntimeException e) {
      assertTrue("message should mention myLabel: " + e.getMessage(), e.getMessage().contains("myLabel"));
    }
  }

  // ===================== JSON form =====================

  public void testParseJsonBasic() {
    String json = "[{\"name\":\"active\"},{\"name\":\"displayName\"}]";
    List<GrouperProvisioningNativeAttributeConfig> result =
        GrouperProvisioningNativeAttributeConfig.parseAndValidate(json, "label");
    assertEquals(2, result.size());
    assertEquals("active", result.get(0).getName());
    assertNull(result.get(0).getPath());
    assertNull(result.get(0).getType());
    assertEquals("displayName", result.get(1).getName());
  }

  public void testParseJsonWithPathAndType() {
    String json = "[{\"name\":\"lastModified\",\"path\":\"/meta/lastModified\",\"type\":\"timestamp\"}]";
    List<GrouperProvisioningNativeAttributeConfig> result =
        GrouperProvisioningNativeAttributeConfig.parseAndValidate(json, "label");
    assertEquals(1, result.size());
    GrouperProvisioningNativeAttributeConfig entry = result.get(0);
    assertEquals("lastModified", entry.getName());
    assertEquals("/meta/lastModified", entry.getPath());
    assertEquals("timestamp", entry.getType());
  }

  public void testParseJsonLowercasesType() {
    String json = "[{\"name\":\"flag\",\"type\":\"BOOLEAN\"}]";
    List<GrouperProvisioningNativeAttributeConfig> result =
        GrouperProvisioningNativeAttributeConfig.parseAndValidate(json, "label");
    assertEquals("boolean", result.get(0).getType());
  }

  public void testParseJsonEmptyArrayReturnsEmptyList() {
    List<GrouperProvisioningNativeAttributeConfig> result =
        GrouperProvisioningNativeAttributeConfig.parseAndValidate("[]", "label");
    assertEquals(0, result.size());
  }

  public void testParseJsonAllSupportedTypes() {
    String json = "[{\"name\":\"a\",\"type\":\"string\"},"
        + "{\"name\":\"b\",\"type\":\"integer\"},"
        + "{\"name\":\"c\",\"type\":\"boolean\"},"
        + "{\"name\":\"d\",\"type\":\"timestamp\"}]";
    List<GrouperProvisioningNativeAttributeConfig> result =
        GrouperProvisioningNativeAttributeConfig.parseAndValidate(json, "label");
    assertEquals(4, result.size());
    assertEquals("string", result.get(0).getType());
    assertEquals("integer", result.get(1).getType());
    assertEquals("boolean", result.get(2).getType());
    assertEquals("timestamp", result.get(3).getType());
  }

  public void testParseJsonRejectsInvalidJson() {
    try {
      GrouperProvisioningNativeAttributeConfig.parseAndValidate("[{not json", "myLabel");
      fail("expected exception for malformed JSON");
    } catch (RuntimeException e) {
      assertTrue("message should mention myLabel: " + e.getMessage(), e.getMessage().contains("myLabel"));
    }
  }

  public void testParseJsonRejectsNonArrayTopLevel() {
    try {
      GrouperProvisioningNativeAttributeConfig.parseAndValidate("[\"sn\"]", "myLabel");
      fail("expected exception for non-object entry");
    } catch (RuntimeException e) {
      assertTrue("message should mention myLabel: " + e.getMessage(), e.getMessage().contains("myLabel"));
    }
  }

  public void testParseJsonRejectsMissingName() {
    try {
      GrouperProvisioningNativeAttributeConfig.parseAndValidate("[{\"path\":\"foo\"}]", "myLabel");
      fail("expected exception for missing name");
    } catch (RuntimeException e) {
      assertTrue("message should mention name: " + e.getMessage(), e.getMessage().contains("name"));
    }
  }

  public void testParseJsonRejectsDuplicateNames() {
    String json = "[{\"name\":\"sn\"},{\"name\":\"sn\"}]";
    try {
      GrouperProvisioningNativeAttributeConfig.parseAndValidate(json, "myLabel");
      fail("expected exception for duplicate name");
    } catch (RuntimeException e) {
      assertTrue("message should mention duplicate: " + e.getMessage(), e.getMessage().contains("sn"));
    }
  }

  public void testParseJsonRejectsInvalidType() {
    String json = "[{\"name\":\"sn\",\"type\":\"binary\"}]";
    try {
      GrouperProvisioningNativeAttributeConfig.parseAndValidate(json, "myLabel");
      fail("expected exception for invalid type");
    } catch (RuntimeException e) {
      assertTrue("message should mention type: " + e.getMessage(), e.getMessage().contains("type"));
    }
  }

  // ===================== Form-detection =====================

  public void testFormDetectionBracketLeadsToJsonPath() {
    // input starts with '[' so it's parsed as JSON — a malformed array surfaces a JSON error,
    // NOT a CSV "no name" error. Sanity-check the dispatcher.
    try {
      GrouperProvisioningNativeAttributeConfig.parseAndValidate("[oops", "myLabel");
      fail("expected exception");
    } catch (RuntimeException e) {
      assertTrue("should be a JSON error: " + e.getMessage(),
          e.getMessage().toLowerCase().contains("json"));
    }
  }

  public void testFormDetectionNoBracketUsesCsvPath() {
    // input without leading '[' — CSV path treats this as a single name
    List<GrouperProvisioningNativeAttributeConfig> result =
        GrouperProvisioningNativeAttributeConfig.parseAndValidate("just_one_name", "label");
    assertEquals(1, result.size());
    assertEquals("just_one_name", result.get(0).getName());
  }

  // ===================== typed exception (textKey + args) =====================
  // These tests verify the contract the UI validator relies on: every failure path throws
  // NativeAttributeConfigException with a stable textKey and a named args map. If a textKey
  // changes here, the corresponding i18n key in grouper.textNg.en.us.base.properties must
  // change too (or the message falls back to English via getMessage()).

  /** helper: expect parseAndValidate to throw NativeAttributeConfigException with given textKey */
  private static NativeAttributeConfigException expectError(String input, String label,
      String expectedTextKey) {
    try {
      GrouperProvisioningNativeAttributeConfig.parseAndValidate(input, label);
      fail("expected NativeAttributeConfigException with textKey='" + expectedTextKey + "'");
      return null; // unreachable
    } catch (NativeAttributeConfigException ex) {
      assertEquals("textKey mismatch (message=" + ex.getMessage() + ")",
          expectedTextKey, ex.getTextKey());
      return ex;
    }
  }

  public void testTypedExceptionCsvLooksLikeJson() {
    NativeAttributeConfigException ex = expectError("sn, {\"name\":\"mail\"}",
        "myLabel", "csvLooksLikeJson");
    Map<String, String> args = ex.getArgs();
    assertEquals("myLabel", args.get("configLabel"));
    assertEquals("{\"name\":\"mail\"}", args.get("token"));
  }

  public void testTypedExceptionCsvDuplicateName() {
    NativeAttributeConfigException ex = expectError("sn, mail, sn",
        "myLabel", "csvDuplicateName");
    Map<String, String> args = ex.getArgs();
    assertEquals("myLabel", args.get("configLabel"));
    assertEquals("sn", args.get("name"));
  }

  public void testTypedExceptionInvalidJson() {
    NativeAttributeConfigException ex = expectError("[{not json", "myLabel", "invalidJson");
    Map<String, String> args = ex.getArgs();
    assertEquals("myLabel", args.get("configLabel"));
    assertNotNull("detail arg should carry the Jackson parse error", args.get("detail"));
    assertNotNull("cause should be the Jackson exception", ex.getCause());
  }

  // NOTE: the 'notArray' branch is reachable only if the JSON dispatcher (startsWith("["))
  // produces something that doesn't parse to an array. Since "[..." always parses to an
  // array in valid JSON, that branch is a defensive guard with no realistic input — skip.

  public void testTypedExceptionEntryNotObject() {
    // "[\"sn\"]" is an array of scalars → entry 0 isn't a JSON object
    NativeAttributeConfigException ex = expectError("[\"sn\"]", "myLabel", "entryNotObject");
    Map<String, String> args = ex.getArgs();
    assertEquals("myLabel", args.get("configLabel"));
    assertEquals("0", args.get("index"));
  }

  public void testTypedExceptionMissingName() {
    NativeAttributeConfigException ex = expectError("[{\"path\":\"foo\"}]",
        "myLabel", "missingName");
    Map<String, String> args = ex.getArgs();
    assertEquals("myLabel", args.get("configLabel"));
    assertEquals("0", args.get("index"));
  }

  public void testTypedExceptionDuplicateName() {
    NativeAttributeConfigException ex = expectError(
        "[{\"name\":\"sn\"},{\"name\":\"mail\"},{\"name\":\"sn\"}]",
        "myLabel", "duplicateName");
    Map<String, String> args = ex.getArgs();
    assertEquals("myLabel", args.get("configLabel"));
    assertEquals("sn", args.get("name"));
    assertEquals("2", args.get("index"));
  }

  public void testTypedExceptionInvalidType() {
    NativeAttributeConfigException ex = expectError(
        "[{\"name\":\"foo\",\"type\":\"binary\"}]",
        "myLabel", "invalidType");
    Map<String, String> args = ex.getArgs();
    assertEquals("myLabel", args.get("configLabel"));
    assertEquals("0", args.get("index"));
    assertEquals("foo", args.get("name"));
    assertEquals("binary", args.get("type"));
    assertNotNull("validTypes arg should list the allowed types", args.get("validTypes"));
    assertTrue("validTypes should list string|integer|boolean|timestamp",
        args.get("validTypes").contains("string") && args.get("validTypes").contains("timestamp"));
  }

  public void testTypedExceptionEnglishFallbackPopulated() {
    // getMessage() must still carry the English text so the daemon log stays readable
    // even when no UI/i18n is involved
    NativeAttributeConfigException ex = expectError(
        "[{\"name\":\"sn\"},{\"name\":\"sn\"}]", "myLabel", "duplicateName");
    assertNotNull("English fallback message must be populated", ex.getMessage());
    assertTrue("English fallback should mention the label",
        ex.getMessage().contains("myLabel"));
    assertTrue("English fallback should mention the duplicate name",
        ex.getMessage().contains("sn"));
  }

  // ===================== validator i18n rendering =====================
  // Spot-check that GrouperProvisioningConfigurationValidation.renderNativeAttributesValidationMessage
  // looks up the i18n key, substitutes $$configLabel$$ with the localized field label, and
  // substitutes the named args. Uses the live text container loaded by GrouperTest setUp.

  public void testRenderValidationMessageSubstitutesArgsAndLocalizesLabel() {
    // pretend duplicateName fired at entry index 2 with name='sn'
    NativeAttributeConfigException ex = new NativeAttributeConfigException(
        "duplicateName",
        argMap("configLabel", "nativeAttributesEntities", "name", "sn", "index", "2"),
        "english fallback (should not be used when i18n key resolves)");

    String rendered = edu.internet2.middleware.grouper.app.provisioning
        .GrouperProvisioningConfigurationValidation
        .renderNativeAttributesValidationMessage("nativeAttributesEntities", ex);

    assertNotNull(rendered);
    // configLabel should be the LOCALIZED field label, not the raw config suffix
    assertTrue("rendered message should use the localized label 'Native entity attributes', "
        + "got: " + rendered,
        rendered.contains("Native entity attributes"));
    // raw suffix should NOT appear (configLabel was replaced with the localized version)
    assertFalse("rendered message should not contain the raw config suffix, got: " + rendered,
        rendered.contains("nativeAttributesEntities"));
    // named args should be substituted
    assertTrue("rendered message should contain the duplicate name, got: " + rendered,
        rendered.contains("sn"));
    assertTrue("rendered message should contain the entry index, got: " + rendered,
        rendered.contains("2"));
    // no leftover $$tokens$$ should remain
    assertFalse("rendered message should not contain unresolved $$ tokens, got: " + rendered,
        rendered.contains("$$"));
  }

  public void testRenderValidationMessageFallsBackToEnglishWhenI18nMissing() {
    // make up a textKey that doesn't have an i18n entry — should fall through to getMessage()
    NativeAttributeConfigException ex = new NativeAttributeConfigException(
        "noSuchKeyExistsForThisTest_xyz",
        argMap("configLabel", "nativeAttributesGroups"),
        "this is the english fallback");

    String rendered = edu.internet2.middleware.grouper.app.provisioning
        .GrouperProvisioningConfigurationValidation
        .renderNativeAttributesValidationMessage("nativeAttributesGroups", ex);

    assertEquals("this is the english fallback", rendered);
  }

  /** tiny helper mirroring the private one in the production class, for compact test setup */
  private static java.util.LinkedHashMap<String, String> argMap(String... namesAndValues) {
    java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<String, String>();
    for (int i = 0; i + 1 < namesAndValues.length; i += 2) {
      map.put(namesAndValues[i], namesAndValues[i + 1]);
    }
    return map;
  }

}
