package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.Arrays;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import junit.textui.TestRunner;

/**
 * GRP-5430: call GrouperServiceLogic.getSubjects (no http) with an old and a new client version and make
 * sure the client version passed in decides whether subject attributeValues are encoded.
 * Multivalued joining is covered in WsSubjectAttributeValuesTest since the test jdbc source is single valued
 */
public class WsSubjectAttributeValuesLogicTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsSubjectAttributeValuesLogicTest("testGetSubjectsClientVersion"));
  }

  /**
   * @param name
   */
  public WsSubjectAttributeValuesLogicTest(String name) {
    super(name);
  }

  /**
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    // logic methods start the session from this in unit tests
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
  }

  /**
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperServiceUtils.testSession = null;
    GrouperWsVersionUtils.removeCurrentClientVersion();
    super.tearDown();
  }

  /**
   * get the name attribute of a subject with a comma in its name, from the ws logic with a client version
   * @param clientVersion e.g. v4_9_000
   * @return the attribute value
   */
  private static String retrieveNameAttributeValue(String clientVersion) {
    WsGetSubjectsResults wsGetSubjectsResults = GrouperServiceLogic.getSubjects(
        GrouperVersion.valueOfIgnoreCase(clientVersion),
        new WsSubjectLookup[] {new WsSubjectLookup("testCommaName", "jdbc", null)}, null, false,
        new String[] {"name"}, null, null, null, null, null, false, null);

    assertEquals(wsGetSubjectsResults.getResultMetadata().getResultMessage(), "T",
        wsGetSubjectsResults.getResultMetadata().getSuccess());
    assertEquals(1, wsGetSubjectsResults.getWsSubjects().length);
    WsSubject wsSubject = wsGetSubjectsResults.getWsSubjects()[0];
    assertEquals("testCommaName", wsSubject.getId());
    assertEquals(1, wsSubject.getAttributeValues().length);
    return wsSubject.getAttributeValues()[0];
  }

  /**
   * old client versions get the value as is, v6_7_000 to 6.x and v7_9_000+ get it encoded and it decodes back
   */
  public void testGetSubjectsClientVersion() {

    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testCommaName", "person", "Anderson, Bob");

    // old clients: unchanged (v4 never encodes, v6 before 6.7, v7 before 7.9)
    assertEquals("Anderson, Bob", retrieveNameAttributeValue("v4_9_000"));
    assertEquals("Anderson, Bob", retrieveNameAttributeValue("v6_6_000"));
    assertEquals("Anderson, Bob", retrieveNameAttributeValue("v7_8_000"));

    // new clients: comma encoded
    for (String clientVersion : new String[] {"v6_7_000", "v7_9_000"}) {
      String encoded = retrieveNameAttributeValue(clientVersion);
      assertEquals(clientVersion, "AndersonU+002C Bob", encoded);
      assertEquals(clientVersion, Arrays.asList("Anderson, Bob"), GrouperClientUtils.wsSubjectAttributeValuesDecode(encoded));
    }
  }
}
