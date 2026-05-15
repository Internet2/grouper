package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;

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

}
