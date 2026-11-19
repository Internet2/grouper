package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateDownloadFile;
import edu.internet2.middleware.grouper.file.GrouperFile;
import edu.internet2.middleware.grouper.file.GrouperFileDao;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * advisory C3: a GSH template download file is keyed by template, date, and file
 * name, so a second user running the same template re-saves the same grouper_file row (same id).  A download
 * token issued to the first user must not then serve the second user's contents: it is bound to the row's
 * version and fails closed (null, which the servlet turns into a 404) once the row changes.
 *
 * <p>Before the fix, token A returns user B's contents, so testTextResaveDoesNotLeak and
 * testBinaryResaveDoesNotLeak fail (and the version overload of registerDownload does not exist).</p>
 *
 * <p>Uses an in-memory proxy for the http session, like UiV2DownloadFileTest, so no servlet container is needed.</p>
 */
public class UiV2DownloadFileVersionTest extends GrouperTest {

  /** template config id for the files this test saves, cleaned up in tearDown */
  private static final String TEMPLATE_CONFIG_ID = "downloadVersionTest109";

  /** date segment of the shared path */
  private static final String DATE = "2026-11-04";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2DownloadFileVersionTest("testTextResaveDoesNotLeak"));
  }

  /**
   *
   */
  public UiV2DownloadFileVersionTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2DownloadFileVersionTest(String name) {
    super(name);
  }

  /**
   * remove this test's download files
   */
  @Override
  protected void tearDown() {
    // delete by path so files saved within the last microsecond are not missed (deleteOlderThanMinutes is by time)
    new GcDbAccess().sql("delete from grouper_file where system_name = ? and file_path like ?")
        .addBindVar(GshTemplateDownloadFile.SYSTEM_NAME)
        .addBindVar("/" + GshTemplateDownloadFile.SYSTEM_NAME + "/" + TEMPLATE_CONFIG_ID + "/%").executeSql();
    super.tearDown();
  }

  /**
   * @return an http session backed by a map (only attribute methods are supported)
   */
  private static HttpSession newHttpSession() {
    final Map<String, Object> attributes = new HashMap<String, Object>();
    return (HttpSession)Proxy.newProxyInstance(UiV2DownloadFileVersionTest.class.getClassLoader(),
        new Class<?>[] {HttpSession.class}, new InvocationHandler() {

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getAttribute".equals(method.getName())) {
              return attributes.get(args[0]);
            }
            if ("setAttribute".equals(method.getName())) {
              attributes.put((String)args[0], args[1]);
              return null;
            }
            if ("hashCode".equals(method.getName())) {
              return System.identityHashCode(proxy);
            }
            if ("equals".equals(method.getName())) {
              return proxy == args[0];
            }
            throw new UnsupportedOperationException(method.getName());
          }
        });
  }

  /**
   * @param httpSession
   * @param token
   * @param subjectIndex 0 or 1
   * @return the contents the download would serve, or null for a 404
   */
  private static String download(HttpSession httpSession, String token, int subjectIndex) {
    GrouperFile grouperFile = UiV2DownloadFile.retrieveGrouperFileForDownload(httpSession, token,
        subjectIndex == 0 ? SubjectTestHelper.SUBJ0 : SubjectTestHelper.SUBJ1);
    return grouperFile == null ? null : new String(grouperFile.retrieveBytes(), StandardCharsets.UTF_8);
  }

  /**
   * user A gets a link to contents A; user B re-saves the same template file with contents B; A's link must not
   * return B (it returns not found), B's link returns B
   */
  public void testTextResaveDoesNotLeak() {

    // given: user A runs the template
    HttpSession sessionA = newHttpSession();
    GrouperFile fileA = GshTemplateDownloadFile.save(TEMPLATE_CONFIG_ID, DATE, "shared.txt", "PRIVATE_FOR_A");
    String tokenA = UiV2DownloadFile.registerDownload(sessionA, fileA.getId(), SubjectTestHelper.SUBJ0);
    assertEquals("PRIVATE_FOR_A", download(sessionA, tokenA, 0));
    // still good on a second click
    assertEquals("PRIVATE_FOR_A", download(sessionA, tokenA, 0));

    // when: user B runs the same template, same date and file name
    HttpSession sessionB = newHttpSession();
    GrouperFile fileB = GshTemplateDownloadFile.save(TEMPLATE_CONFIG_ID, DATE, "shared.txt", "PRIVATE_FOR_B");
    assertEquals("same row is reused, that is the precondition of the bug", fileA.getId(), fileB.getId());
    String tokenB = UiV2DownloadFile.registerDownload(sessionB, fileB.getId(), SubjectTestHelper.SUBJ1);

    // then
    assertEquals("PRIVATE_FOR_B", download(sessionB, tokenB, 1));
    assertNull("A's old link must not serve B's file", download(sessionA, tokenA, 0));
    // the stale token is dropped
    assertNull(UiV2DownloadFile.retrieveGrouperFileId(sessionA, tokenA, SubjectTestHelper.SUBJ0));
  }

  /**
   * same as the text test with the binary (blob) overload of save
   */
  public void testBinaryResaveDoesNotLeak() {

    HttpSession sessionA = newHttpSession();
    GrouperFile fileA = GshTemplateDownloadFile.save(TEMPLATE_CONFIG_ID, DATE, "shared.zip",
        "BYTES_FOR_A".getBytes(StandardCharsets.UTF_8));
    String tokenA = UiV2DownloadFile.registerDownload(sessionA, fileA.getId(), SubjectTestHelper.SUBJ0);
    assertEquals("BYTES_FOR_A", download(sessionA, tokenA, 0));

    HttpSession sessionB = newHttpSession();
    GrouperFile fileB = GshTemplateDownloadFile.save(TEMPLATE_CONFIG_ID, DATE, "shared.zip",
        "BYTES_FOR_B".getBytes(StandardCharsets.UTF_8));
    assertEquals(fileA.getId(), fileB.getId());
    String tokenB = UiV2DownloadFile.registerDownload(sessionB, fileB.getId(), SubjectTestHelper.SUBJ1);

    assertEquals("BYTES_FOR_B", download(sessionB, tokenB, 1));
    assertNull("A's old link must not serve B's file", download(sessionA, tokenA, 0));
  }

  /**
   * a token registered for an explicit version (what the template saved) fails closed if the row has already
   * moved on, which closes the window between the template's save and the link registration
   */
  public void testExplicitStaleVersion() {

    GrouperFile fileA = GshTemplateDownloadFile.save(TEMPLATE_CONFIG_ID, DATE, "race.txt", "PRIVATE_FOR_A");
    Long versionA = fileA.getHibernateVersionNumber();
    assertNotNull(versionA);

    // someone else re-saves before A's link is registered
    GrouperFile fileB = GshTemplateDownloadFile.save(TEMPLATE_CONFIG_ID, DATE, "race.txt", "PRIVATE_FOR_B");
    assertFalse("a re-save bumps the version", versionA.equals(fileB.getHibernateVersionNumber()));

    HttpSession sessionA = newHttpSession();
    String staleToken = UiV2DownloadFile.registerDownload(sessionA, fileA.getId(), versionA, SubjectTestHelper.SUBJ0);
    assertNull(download(sessionA, staleToken, 0));

    String currentToken = UiV2DownloadFile.registerDownload(sessionA, fileB.getId(),
        fileB.getHibernateVersionNumber(), SubjectTestHelper.SUBJ0);
    assertEquals("PRIVATE_FOR_B", download(sessionA, currentToken, 0));
  }

  /**
   * a token for a row that is not there (or was deleted) is a 404, and the session and subject binding still holds
   */
  public void testMissingRowAndOtherSubject() {

    HttpSession session = newHttpSession();
    String missingToken = UiV2DownloadFile.registerDownload(session, "noSuchFileId109", SubjectTestHelper.SUBJ0);
    assertNull(download(session, missingToken, 0));

    GrouperFile grouperFile = GshTemplateDownloadFile.save(TEMPLATE_CONFIG_ID, DATE, "mine.txt", "MINE");
    String token = UiV2DownloadFile.registerDownload(session, grouperFile.getId(), SubjectTestHelper.SUBJ0);
    assertNull("other subject", download(session, token, 1));
    assertNull("other session", download(newHttpSession(), token, 0));
    assertEquals("MINE", download(session, token, 0));

    GrouperFileDao.deleteById(grouperFile.getId());
    assertNull("deleted", download(session, token, 0));
  }
}
