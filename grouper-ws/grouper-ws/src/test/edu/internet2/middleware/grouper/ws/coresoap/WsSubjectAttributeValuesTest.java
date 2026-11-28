package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveMapImpl;
import edu.internet2.middleware.subject.provider.SubjectImpl;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * GRP-5430: WS subject attributeValues return all values of multivalued attributes for client version
 * v6_7_000 to 6.x or v7_9_000+ (v6 and v7 only, not v4), joined with ", " where a comma in a value is U+002C and the U of a literal U+ is U+0055.
 * Older clients get the first value unencoded.  No database needed
 */
public class WsSubjectAttributeValuesTest extends TestCase {

  static {
    // init GrouperUtil before GrouperVersion, else GrouperVersion static init calls GrouperUtil whose static
    // init calls back into GrouperVersion.currentVersion() before GrouperVersion.LOG is set (NPE).  In a running
    // app GrouperUtil is always loaded first
    GrouperUtil.length((Object)null);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WsSubjectAttributeValuesTest.class);
  }

  /**
   * @param name
   */
  public WsSubjectAttributeValuesTest(String name) {
    super(name);
  }

  /**
   * make sure the client version thread local does not leak into other tests
   */
  @Override
  protected void tearDown() throws Exception {
    GrouperWsVersionUtils.removeCurrentClientVersion();
    super.tearDown();
  }

  /**
   * encode a list of values
   * @param values
   * @return the encoded string
   */
  private static String encode(String... values) {
    return GrouperClientUtils.wsSubjectAttributeValuesEncode(Arrays.asList(values));
  }

  /**
   * values with no comma or U+ are unchanged, and multiple values are joined with comma space
   */
  public void testEncodePlain() {
    assertEquals("A", encode("A"));
    assertEquals("A, C, D", encode("A", "C", "D"));
    assertEquals("", encode(""));
    assertNull(GrouperClientUtils.wsSubjectAttributeValuesEncode(null));
    assertNull(GrouperClientUtils.wsSubjectAttributeValuesEncode(new ArrayList<String>()));
    // nulls are skipped
    assertEquals("A, B", encode("A", null, "B"));
  }

  /**
   * commas and the escape itself are encoded
   */
  public void testEncodeEscapes() {
    assertEquals("AndersonU+002C Bob", encode("Anderson, Bob"));
    assertEquals("U+0055+002C", encode("U+002C"));
    assertEquals("U+0055+U+002C", encode("U+,"));
    assertEquals("U+0055+0055", encode("U+0055"));
    assertEquals("aU+002CU+002Cb, U+002C", encode("a,,b", ","));
  }

  /**
   * decode reverses encode exactly, including values that look like escapes
   */
  public void testRoundTrip() {
    List<List<String>> cases = new ArrayList<List<String>>();
    cases.add(Arrays.asList("A"));
    cases.add(Arrays.asList("A", "C", "D"));
    cases.add(Arrays.asList("Anderson, Bob", "x"));
    cases.add(Arrays.asList("U+002C"));
    cases.add(Arrays.asList("U+,"));
    cases.add(Arrays.asList("U+0055"));
    cases.add(Arrays.asList("U+0055+002C", "U+", "U", "+002C"));
    cases.add(Arrays.asList(",", ", ", " ,", "a, b, c"));
    cases.add(Arrays.asList("", "a", ""));
    cases.add(Arrays.asList(" leading", "trailing "));

    for (List<String> values : cases) {
      String encoded = GrouperClientUtils.wsSubjectAttributeValuesEncode(values);
      assertEquals("encoded: " + encoded, values, GrouperClientUtils.wsSubjectAttributeValuesDecode(encoded));
    }
  }

  /**
   * null or empty decodes to no values
   */
  public void testDecodeEmpty() {
    assertEquals(0, GrouperClientUtils.wsSubjectAttributeValuesDecode(null).size());
    assertEquals(0, GrouperClientUtils.wsSubjectAttributeValuesDecode("").size());
  }

  /**
   * assert if a client version encodes multivalued values
   * @param clientVersion
   * @param expected
   */
  private static void assertEncode(String clientVersion, boolean expected) {
    GrouperWsVersionUtils.currentClientVersion.set(GrouperVersion.valueOfIgnoreCase(clientVersion));
    assertEquals(clientVersion, expected, WsSubject.encodeMultivaluedAttributeValues());
  }

  /**
   * only client versions 6.7 to 6.x and 7.9+ get encoded multivalued values (v6 and v7 only, not v4)
   */
  public void testClientVersionGate() {
    GrouperWsVersionUtils.removeCurrentClientVersion();
    assertFalse("not a ws request", WsSubject.encodeMultivaluedAttributeValues());

    // v4 and v5 never
    assertEncode("v2_4_000", false);
    assertEncode("v4_9_000", false);
    assertEncode("v4_28_000", false);
    assertEncode("v5_21_000", false);

    // v6 starting at 6.7
    assertEncode("v6_6_000", false);
    assertEncode("v6_6_9", false);
    assertEncode("v6_7_000", true);
    assertEncode("v6_9_000", true);
    assertEncode("v6_99_000", true);

    // v7 starting at 7.9
    assertEncode("v7_0_000", false);
    assertEncode("v7_8_000", false);
    assertEncode("v7_8_5", false);
    assertEncode("v7_9_000", true);
    assertEncode("v7_10_000", true);

    // later majors
    assertEncode("v8_0_000", true);
  }

  /**
   * in memory subject with a multivalued affiliation and a name with a comma
   * @return the subject
   */
  private static SubjectImpl subject() {
    Map<String, Set<String>> attributes = new SubjectCaseInsensitiveMapImpl<String, Set<String>>();
    // insertion order is not sorted, the ws should sort
    attributes.put("edupersonaffiliation", new LinkedHashSet<String>(Arrays.asList("D", "A", "C")));
    attributes.put("mail", new LinkedHashSet<String>(Arrays.asList("bob@example.edu")));
    return new SubjectImpl("800001147", "Anderson, Bob", "Anderson, Bob - Staff", "person", "g:isa", attributes);
  }

  /**
   * old clients get the first value and no encoding, same as before GRP-5430
   */
  public void testWsSubjectOldClient() {
    GrouperWsVersionUtils.currentClientVersion.set(GrouperVersion.valueOfIgnoreCase("v4_9_000"));
    WsSubject wsSubject = new WsSubject(subject(), new String[] {"edupersonaffiliation", "mail", "name", "notThere"}, null);
    String[] attributeValues = wsSubject.getAttributeValues();
    // just one of the values, which one depends on the set order
    assertTrue(attributeValues[0], Arrays.asList("A", "C", "D").contains(attributeValues[0]));
    assertEquals("bob@example.edu", attributeValues[1]);
    assertEquals("Anderson, Bob", attributeValues[2]);
    assertEquals("", attributeValues[3]);
  }

  /**
   * new clients get all values sorted and encoded, including single valued fallbacks like name
   */
  public void testWsSubjectNewClient() {
    GrouperWsVersionUtils.currentClientVersion.set(GrouperVersion.valueOfIgnoreCase("v7_9_000"));
    WsSubject wsSubject = new WsSubject(subject(), new String[] {"edupersonaffiliation", "mail", "name", "notThere"}, null);
    String[] attributeValues = wsSubject.getAttributeValues();
    assertEquals("A, C, D", attributeValues[0]);
    assertEquals("bob@example.edu", attributeValues[1]);
    assertEquals("AndersonU+002C Bob", attributeValues[2]);
    assertEquals("", attributeValues[3]);
    assertEquals(Arrays.asList("Anderson, Bob"), GrouperClientUtils.wsSubjectAttributeValuesDecode(attributeValues[2]));
  }
}
