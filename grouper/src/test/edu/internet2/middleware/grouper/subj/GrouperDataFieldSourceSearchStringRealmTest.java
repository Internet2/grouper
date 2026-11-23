package edu.internet2.middleware.grouper.subj;

import java.lang.reflect.Method;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * The data field subject source stores search strings (grouper_members.search_stringN) built only
 * from publicly readable data fields, unless the site restricted who can search that search string;
 * search terms match % and _ literally.
 *
 * Uses the data of GrouperDataFieldSourceAdapterTest (setupData, called by reflection since its
 * signature differs by release): search attribute 0 is descriptionPrivate (private realm, readers
 * test:dataFieldPrivateReaders = test.subject.1 and test.subject.2), search attribute 1 is
 * descriptionPublic (null for test.subject.8), and search string 0 is restricted to the readers group.
 * Goes in grouper/src/test/edu/internet2/middleware/grouper/subj/.
 */
public class GrouperDataFieldSourceSearchStringRealmTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDataFieldSourceSearchStringRealmTest("testUnrestrictedSearchStringHasNoPrivateFields"));
  }

  /**
   *
   */
  public GrouperDataFieldSourceSearchStringRealmTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperDataFieldSourceSearchStringRealmTest(String name) {
    super(name);
  }

  /** source id of the data field source in GrouperDataFieldSourceAdapterTest */
  private static final String SOURCE_ID = "dataFieldSubjectSource";

  /** subject whose private description is its only searchable value */
  private static final String SUBJECT_8 = "test.subject.8";

  /**
   * load the data field source and its data
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("member.search.defaultIndexOrder", "0");
    setupDataFromAdapterTest();
  }

  /**
   * remove overrides
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("member.search.defaultIndexOrder");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string0.allowOnlyGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string0.wheelOnly");
    super.tearDown();
  }

  /**
   * search string 0 open to everyone: the stored value has no private description, so a user who
   * cannot read the private realm cannot find test.subject.8 by it (before the fix it could)
   */
  public void testUnrestrictedSearchStringHasNoPrivateFields() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.allowOnlyGroup", "");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.wheelOnly", "false");

    String searchString0 = rewriteSearchStringsAsRoot(SUBJECT_8);
    assertFalse("no private description in the open search string: " + searchString0,
        searchString0 != null && searchString0.contains(SUBJECT_8));

    assertEquals("non-reader cannot find by the private description", 0, searchCount("test.subject.0", SUBJECT_8));

    // the subject itself still has its private value for a reader (attributes are unchanged)
    Subject subject8 = SubjectFinder.findByIdAndSource(SUBJECT_8, SOURCE_ID, true);
    assertNotNull("root still sees the private attribute", subject8.getAttributeValue("descriptionPrivate"));
  }

  /**
   * search string 0 restricted to the readers group: it keeps the private description, readers can
   * search it, others cannot
   */
  public void testRestrictedSearchStringKeepsValues() {
    String readersGroupName = "test:dataFieldPrivateReaders";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.allowOnlyGroup", readersGroupName);

    String searchString0 = rewriteSearchStringsAsRoot(SUBJECT_8);
    assertTrue("restricted search string keeps the private description: " + searchString0,
        searchString0 != null && searchString0.contains(SUBJECT_8));

    assertEquals("reader finds it", 1, searchCount("test.subject.1", SUBJECT_8));
    assertEquals("non-reader does not", 0, searchCount("test.subject.0", SUBJECT_8));
  }

  /**
   * % and _ in a term match literally: "test_subject_8" does not match "test.subject.8"
   */
  public void testLikeWildcardsEscaped() {
    String readersGroupName = "test:dataFieldPrivateReaders";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.allowOnlyGroup", readersGroupName);
    rewriteSearchStringsAsRoot(SUBJECT_8);

    assertEquals("plain term", 1, searchCount("test.subject.1", SUBJECT_8));
    assertEquals("underscore is not a wildcard", 0, searchCount("test.subject.1", "test_subject_8"));
    assertEquals("percent is not a wildcard", 0, searchCount("test.subject.1", "test%8"));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * GrouperDataFieldSourceAdapterTest.setupData() (5.x) or setupData(fullSyncFull) (6.x on)
   */
  private void setupDataFromAdapterTest() {
    try {
      GrouperDataFieldSourceAdapterTest adapterTest = new GrouperDataFieldSourceAdapterTest("testGetSubjectCache");
      for (Method method : GrouperDataFieldSourceAdapterTest.class.getMethods()) {
        if (!"setupData".equals(method.getName())) {
          continue;
        }
        if (method.getParameterTypes().length == 0) {
          method.invoke(adapterTest);
          return;
        }
        if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isEnum()) {
          Object fullSync = null;
          for (Object constant : method.getParameterTypes()[0].getEnumConstants()) {
            if ("fullSyncFull".equals(((Enum<?>)constant).name())) {
              fullSync = constant;
            }
          }
          method.invoke(adapterTest, fullSync);
          return;
        }
      }
      fail("no setupData on GrouperDataFieldSourceAdapterTest");
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * resolve the subject as root (as the data provider sync does), write its member attributes, and
   * return the stored search_string0
   * @param subjectId
   * @return search_string0
   */
  private static String rewriteSearchStringsAsRoot(String subjectId) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Subject subject = SubjectFinder.findByIdAndSource(subjectId, SOURCE_ID, true);
      Member member = MemberFinder.findBySubject(grouperSession, subject, true);
      member.updateMemberAttributes(subject, true);
      return new GcDbAccess().sql("select search_string0 from grouper_members where subject_id = ? and subject_source = ?")
          .addBindVar(subjectId).addBindVar(SOURCE_ID).select(String.class);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * search the data field source as a jdbc test subject
   * @param searcherSubjectId
   * @param term
   * @return number of subjects found
   */
  private static int searchCount(String searcherSubjectId, final String term) {
    // callbackGrouperSessionBySubjectId returns nothing, so the count comes back in a holder
    final int[] count = new int[1];
    GrouperSession.callbackGrouperSessionBySubjectId(searcherSubjectId, "jdbc", new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Set<Subject> subjects = SubjectFinder.findAll(term, SOURCE_ID);
        count[0] = GrouperUtil.length(subjects);
        return null;
      }
    });
    return count[0];
  }
}
