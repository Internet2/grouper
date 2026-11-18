package edu.internet2.middleware.grouper.subj;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security advisory H5 (subjectLookupAttributeAuthorization): subject attributes restricted with
 * subjectApi.source.&lt;configId&gt;.attribute.&lt;n&gt;.requireGroupNameForView must be removed for a
 * caller outside the viewer group on every lookup path:
 * - bulk lookups (findByIds, findByIdentifiers, findByIdsOrIdentifiers), which return through
 *   SubjectFinder.filterSubjects(Map) and used to skip the attribute filter
 * - a single lookup after a bulk lookup (the caller-scoped cache must not hold an unfiltered subject)
 * - WS exact lookups with a source id (WsSubjectLookup used to call the source adapter directly)
 * The subject customizer must also apply to those paths, and identifier-keyed bulk maps keep their keys.
 *
 * The restriction is set on the jdbc test source by assigning the adapter's cached
 * attribute-to-viewer-group map (the config path needs an editable source), and removed in tearDown.
 * Before the fix the bulk, bulk-then-single and WS cases return the restricted value.
 * </pre>
 */
public class SubjectLookupAttributeAuthorizationTest extends GrouperTest {

  /** restricted attribute of the jdbc test source (value id.test.subject.N) */
  private static final String RESTRICTED_ATTRIBUTE = "loginid";

  /** viewer group for the restricted attribute */
  private static final String VIEWER_GROUP_NAME = "test:subjectAttributeViewers";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new SubjectLookupAttributeAuthorizationTest("testBulkLookupsDenied"));
  }

  /**
   * @param name
   */
  public SubjectLookupAttributeAuthorizationTest(String name) {
    super(name);
  }

  /**
   * restrict the attribute and make the viewer group with SUBJ1 in it
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group viewers = new GroupSave(grouperSession).assignName(VIEWER_GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
    viewers.addMember(SubjectTestHelper.SUBJ1, false);

    Map<String, String> attributeNameToViewerGroupName = new HashMap<String, String>();
    attributeNameToViewerGroupName.put(RESTRICTED_ATTRIBUTE, VIEWER_GROUP_NAME);
    assignViewerGroupMap(attributeNameToViewerGroupName);
    SubjectFinder.flushCache();
  }

  /**
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    // null makes the adapter read the config again
    assignViewerGroupMap(null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("subjects.customizer.className");
    clearSubjectCustomizerCache();
    SubjectFinder.flushCache();
    super.tearDown();
  }

  /**
   * set the cached attribute-to-viewer-group map of the jdbc test source
   * @param attributeNameToViewerGroupName map or null to reset
   */
  private static void assignViewerGroupMap(Map<String, String> attributeNameToViewerGroupName) {
    Source source = SourceManager.getInstance().getSource("jdbc");
    assertTrue(source instanceof BaseSourceAdapter);
    GrouperUtil.assignField(BaseSourceAdapter.class, source, "attributeNameToViewerGroupName",
        attributeNameToViewerGroupName, true, true, false, null);
  }

  /**
   * the subject customizer class is cached for a few minutes, clear it after changing the config
   */
  @SuppressWarnings("rawtypes")
  private static void clearSubjectCustomizerCache() {
    ExpirableCache cache = (ExpirableCache)GrouperUtil.fieldValue(SubjectFinder.class, null,
        "subjectCustomizerClassCache", false, true, false);
    cache.clear();
  }

  /**
   * @param subject
   * @return the restricted attribute value
   */
  private static String restrictedValue(Subject subject) {
    assertNotNull(subject);
    return subject.getAttributeValue(RESTRICTED_ATTRIBUTE);
  }

  /**
   * run every bulk lookup for SUBJ2 and assert the attribute is there or not
   * @param expectValue true if the caller may see the attribute
   */
  private static void assertBulkLookups(boolean expectValue) {
    String expected = expectValue ? SubjectTestHelper.SUBJ2_IDENTIFIER : null;
    Set<String> ids = GrouperUtil.toSet(SubjectTestHelper.SUBJ2_ID);
    Set<String> identifiers = GrouperUtil.toSet(SubjectTestHelper.SUBJ2_IDENTIFIER);

    assertEquals(expected, restrictedValue(SubjectFinder.findByIds(ids).get(SubjectTestHelper.SUBJ2_ID)));
    assertEquals(expected, restrictedValue(SubjectFinder.findByIds(ids, "jdbc").get(SubjectTestHelper.SUBJ2_ID)));
    assertEquals(expected, restrictedValue(SubjectFinder.findByIdentifiers(identifiers).get(SubjectTestHelper.SUBJ2_IDENTIFIER)));
    assertEquals(expected, restrictedValue(SubjectFinder.findByIdentifiers(identifiers, "jdbc").get(SubjectTestHelper.SUBJ2_IDENTIFIER)));
    assertEquals(expected, restrictedValue(SubjectFinder.findByIdsOrIdentifiers(identifiers).get(SubjectTestHelper.SUBJ2_IDENTIFIER)));
    assertEquals(expected, restrictedValue(SubjectFinder.findByIdsOrIdentifiers(identifiers, "jdbc").get(SubjectTestHelper.SUBJ2_IDENTIFIER)));
  }

  /**
   * WS exact lookups with a source id, by id and by identifier
   * @param expectValue true if the caller may see the attribute
   */
  private static void assertWsSourceLookups(boolean expectValue) {
    String expected = expectValue ? SubjectTestHelper.SUBJ2_IDENTIFIER : null;
    assertEquals(expected, restrictedValue(new WsSubjectLookup(SubjectTestHelper.SUBJ2_ID, "jdbc", null).retrieveSubject()));
    assertEquals(expected, restrictedValue(new WsSubjectLookup(null, "jdbc", SubjectTestHelper.SUBJ2_IDENTIFIER).retrieveSubject()));
  }

  /**
   * control: the normal single lookup filters for SUBJ0
   */
  public void testSingleLookupDeniedControl() {
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    assertNull(restrictedValue(SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ2_ID, "jdbc", true)));
  }

  /**
   * every bulk lookup removes the attribute for a caller outside the viewer group
   */
  public void testBulkLookupsDenied() {
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    assertBulkLookups(false);
  }

  /**
   * a bulk lookup first must not seed the caller cache with an unfiltered subject
   */
  public void testBulkThenSingleDenied() {
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    SubjectFinder.findByIds(GrouperUtil.toSet(SubjectTestHelper.SUBJ2_ID), "jdbc");
    assertNull(restrictedValue(SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ2_ID, "jdbc", true)));
    SubjectFinder.findByIdentifiers(GrouperUtil.toSet(SubjectTestHelper.SUBJ2_IDENTIFIER), "jdbc");
    assertNull(restrictedValue(SubjectFinder.findByIdentifierAndSource(SubjectTestHelper.SUBJ2_IDENTIFIER, "jdbc", true)));
  }

  /**
   * root looks up first (sees the value), then the denied caller must not get it
   */
  public void testRootPrimesCacheThenDenied() {
    GrouperSession.startRootSession();
    assertBulkLookups(true);
    assertEquals(SubjectTestHelper.SUBJ2_IDENTIFIER, restrictedValue(SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ2_ID, "jdbc", true)));

    GrouperSession.start(SubjectTestHelper.SUBJ0);
    assertBulkLookups(false);
    assertWsSourceLookups(false);
    assertNull(restrictedValue(SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ2_ID, "jdbc", true)));
  }

  /**
   * WS exact lookups with a source id remove the attribute
   */
  public void testWsSourceLookupsDenied() {
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    assertWsSourceLookups(false);
  }

  /**
   * a caller in the viewer group, and root, see the attribute on every path
   */
  public void testAllowedAndRoot() {
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    assertBulkLookups(true);
    assertWsSourceLookups(true);

    GrouperSession.startRootSession();
    assertBulkLookups(true);
    assertWsSourceLookups(true);
  }

  /**
   * the subject customizer applies to bulk maps (keys kept) and to WS lookups with a source id
   */
  public void testSubjectCustomizerAppliesAndKeysKept() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.customizer.className",
        HideSubject3Customizer.class.getName());
    clearSubjectCustomizerCache();
    SubjectFinder.flushCache();

    GrouperSession.start(SubjectTestHelper.SUBJ0);

    Map<String, Subject> byIdentifier = SubjectFinder.findByIdentifiers(
        GrouperUtil.toSet(SubjectTestHelper.SUBJ2_IDENTIFIER, SubjectTestHelper.SUBJ3_IDENTIFIER), "jdbc");
    // identifier-keyed map stays keyed by identifier, the hidden subject is gone
    assertEquals(1, byIdentifier.size());
    assertTrue(byIdentifier.containsKey(SubjectTestHelper.SUBJ2_IDENTIFIER));
    assertNull(restrictedValue(byIdentifier.get(SubjectTestHelper.SUBJ2_IDENTIFIER)));

    Map<String, Subject> byId = SubjectFinder.findByIds(
        GrouperUtil.toSet(SubjectTestHelper.SUBJ2_ID, SubjectTestHelper.SUBJ3_ID), "jdbc");
    assertEquals(1, byId.size());
    assertTrue(byId.containsKey(SubjectTestHelper.SUBJ2_ID));

    // WS with a source id: hidden subject not found
    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(SubjectTestHelper.SUBJ3_ID, "jdbc", null);
    assertNull(wsSubjectLookup.retrieveSubject());
    assertFalse(wsSubjectLookup.retrieveSubjectFindResult().isSuccess());
    wsSubjectLookup = new WsSubjectLookup(null, "jdbc", SubjectTestHelper.SUBJ3_IDENTIFIER);
    assertNull(wsSubjectLookup.retrieveSubject());
    assertFalse(wsSubjectLookup.retrieveSubjectFindResult().isSuccess());
  }

  /**
   * test customizer: hides test.subject.3 from everyone but root/wheel
   */
  public static class HideSubject3Customizer extends SubjectCustomizerBase {

    /**
     * @see SubjectCustomizerBase#filterSubjects(GrouperSession, Set, String)
     */
    @Override
    public Set<Subject> filterSubjects(GrouperSession grouperSession, Set<Subject> subjects, String findSubjectsInStemName) {
      if (grouperSession != null && edu.internet2.middleware.grouper.privs.PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
        return subjects;
      }
      Set<Subject> result = new java.util.LinkedHashSet<Subject>();
      for (Subject subject : GrouperUtil.nonNull(subjects)) {
        if (!SubjectTestHelper.SUBJ3_ID.equals(subject.getId())) {
          result.add(subject);
        }
      }
      return result;
    }
  }
}
