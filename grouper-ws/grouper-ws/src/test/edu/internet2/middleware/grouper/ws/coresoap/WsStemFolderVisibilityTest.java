package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.HashSet;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.filter.StemNameAnyFilter;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup.StemFindResult;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.subject.Subject;

/**
 * WS findStems (every query filter type, and wsStemLookups), stemDelete and the folder of a WS query
 * filter resolved folders with the unsecured finders, so a caller could list or probe folders it may
 * not see (security.folders.are.viewable.by.all false, the default).
 *
 * The fix filters GrouperQuery.getStems (behind every stem query filter) and treats a folder the
 * caller may not see as not found in WsStemLookup, WsStemQueryFilter.retrieveParentStem and
 * WsQueryFilter.retrieveStem.  The rule is the UI one (StemViewAuthorization): a naming privilege on
 * the folder or something visible inside it.
 */
public class WsStemFolderVisibilityTest extends GrouperTest {

  /** hidden folder */
  private static final String HIDDEN = "test191:hidden191";

  /** folder SUBJ0 has a naming privilege on */
  private static final String VISIBLE = "test191:visible191";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsStemFolderVisibilityTest("testWsStemLookupHiddenIsNotFound"));
  }

  /**
   *
   */
  public WsStemFolderVisibilityTest() {
    super();
  }

  /**
   * @param name
   */
  public WsStemFolderVisibilityTest(String name) {
    super(name);
  }

  /**
   * folders are not viewable by all (the default), set here so the test JVM sees it now
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.folders.are.viewable.by.all", "false");
  }

  /**
   * remove the config override
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.folders.are.viewable.by.all");
    super.tearDown();
  }

  /**
   * as root: a hidden folder (nobody has a privilege on it, nothing inside) and a folder SUBJ0 may see
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Stem hidden = new StemSave(rootSession).assignName(HIDDEN).assignDisplayExtension("SECRET_WS_FOLDER_191")
          .assignDescription("CONFIDENTIAL_FOLDER_DESC_191").assignCreateParentStemsIfNotExist(true).save();
      Stem visible = new StemSave(rootSession).assignName(VISIBLE).assignCreateParentStemsIfNotExist(true).save();
      // make sure nothing is visible to everyone, whatever the create-grant-all config says
      for (Stem stem : new Stem[] {hidden, visible}) {
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_VIEW, false);
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.CREATE, false);
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_ATTR_READ, false);
      }
      visible.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * names of the folders a stem query filter returns
   * @param subject caller
   * @return the names
   */
  private static Set<String> approximateQueryAs(Subject subject) {
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      Stem root = StemFinder.findRootStem(grouperSession);
      Set<String> names = new HashSet<String>();
      for (Stem stem : GrouperQuery.createQuery(grouperSession, new StemNameAnyFilter("191", root)).getStems()) {
        names.add(stem.getName());
      }
      return names;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * findStems FIND_BY_STEM_NAME_APPROXIMATE (and every other stem query filter) goes through
   * GrouperQuery.getStems: the hidden folder is only returned to root
   */
  public void testStemQueryFilterHidesHiddenFolder() {
    Set<String> subj0Names = approximateQueryAs(SubjectTestHelper.SUBJ0);
    assertFalse(subj0Names.toString(), subj0Names.contains(HIDDEN));
    assertTrue(subj0Names.toString(), subj0Names.contains(VISIBLE));

    Set<String> rootNames = approximateQueryAs(SubjectFinder.findRootSubject());
    assertTrue(rootNames.toString(), rootNames.contains(HIDDEN));
    assertTrue(rootNames.toString(), rootNames.contains(VISIBLE));
  }

  /**
   * WsStemLookup (findStems wsStemLookups, stemDelete, and every other WS folder lookup): a hidden
   * folder is STEM_NOT_FOUND with no stem, the same as a folder that does not exist
   */
  public void testWsStemLookupHiddenIsNotFound() {
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      WsStemLookup hiddenLookup = new WsStemLookup(HIDDEN, null);
      hiddenLookup.retrieveStemIfNeeded(grouperSession, false);
      assertNull(hiddenLookup.retrieveStem());
      assertEquals(StemFindResult.STEM_NOT_FOUND, hiddenLookup.retrieveStemFindResult());

      // by uuid too
      String hiddenUuid = (String)GrouperSession.internal_callbackRootGrouperSession(
          new edu.internet2.middleware.grouper.misc.GrouperSessionHandler() {
            public Object callback(GrouperSession rootSession) {
              return StemFinder.findByName(rootSession, HIDDEN, true).getUuid();
            }
          });
      WsStemLookup hiddenUuidLookup = new WsStemLookup(null, hiddenUuid);
      hiddenUuidLookup.retrieveStemIfNeeded(grouperSession, false);
      assertNull(hiddenUuidLookup.retrieveStem());
      assertEquals(StemFindResult.STEM_NOT_FOUND, hiddenUuidLookup.retrieveStemFindResult());

      WsStemLookup visibleLookup = new WsStemLookup(VISIBLE, null);
      visibleLookup.retrieveStemIfNeeded(grouperSession, false);
      assertNotNull(visibleLookup.retrieveStem());
      assertEquals(StemFindResult.SUCCESS, visibleLookup.retrieveStemFindResult());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    // root still finds it
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      WsStemLookup hiddenLookup = new WsStemLookup(HIDDEN, null);
      hiddenLookup.retrieveStemIfNeeded(rootSession, false);
      assertNotNull(hiddenLookup.retrieveStem());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * findStems FIND_BY_PARENT_STEM_NAME and findGroups with a stemName: a hidden folder gives the
   * same "Cant find stem" error as a folder that does not exist
   */
  public void testQueryFilterFolderHiddenIsNotFound() {
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      WsStemQueryFilter wsStemQueryFilter = new WsStemQueryFilter();
      wsStemQueryFilter.assignGrouperSession(grouperSession);
      wsStemQueryFilter.setParentStemName(HIDDEN);
      try {
        wsStemQueryFilter.retrieveParentStem();
        fail("hidden parent folder should not be found");
      } catch (WsInvalidQueryException wiqe) {
        // ok, same as a missing folder
      }
      wsStemQueryFilter.setParentStemName(VISIBLE);
      assertNotNull(wsStemQueryFilter.retrieveParentStem());

      WsQueryFilter wsQueryFilter = new WsQueryFilter();
      wsQueryFilter.assignGrouperSession(grouperSession);
      wsQueryFilter.setStemName(HIDDEN);
      try {
        wsQueryFilter.retrieveStem();
        fail("hidden folder should not be found");
      } catch (WsInvalidQueryException wiqe) {
        // ok, same as a missing folder
      }
      wsQueryFilter.setStemName(VISIBLE);
      assertNotNull(wsQueryFilter.retrieveStem());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
}
