package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import junit.textui.TestRunner;

/**
 * Security advisory C3: a GSH template FILE input upload was written to tempDir + separator + client file name
 * (so "../x" escaped the temp dir), and the write happened before the template's enabled / owner / who-can-run
 * checks.  GshTemplateFileUpload checks first and writes only a sanitized basename inside a new temp dir.
 * <p>
 * writeUploadAfterAuthorization reads the owner from the UI request container, which needs the UI filter thread
 * locals, so this test calls its two halves (allowedToRunTemplate, writeUpload) directly.
 * </p>
 */
public class GshTemplateFileUploadTest extends GrouperTest {

  /** template config id */
  private static final String CONFIG_ID = "gshTest05upload";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateFileUploadTest("testTraversalNameStaysInTempDirectory"));
  }

  /**
   *
   */
  public GshTemplateFileUploadTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateFileUploadTest(String name) {
    super(name);
  }

  /**
   * @param key after grouperGshTemplate.configId.
   * @param value
   */
  private static void config(String key, String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + CONFIG_ID + "." + key, value);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    // V1 template with one file input read as a string, like the reproduction
    config("enabled", "true");
    config("templateType", "gsh");
    config("templateName", CONFIG_ID);
    config("templateDescription", CONFIG_ID);
    config("runAsType", "currentUser");
    config("securityRunType", "everyone");
    config("showOnFolders", "true");
    config("folderShowType", "allFolders");
    config("showOnGroups", "true");
    config("groupShowType", "allGroups");
    config("gshTemplate", "gsh_builtin_gshTemplateOutput.addOutputLine(\"Finished\");");
    config("numberOfInputs", "1");
    config("input.0.name", "gsh_input_upload");
    config("input.0.label", "Upload");
    config("input.0.description", "Upload");
    config("input.0.type", "string");
    config("input.0.formElementType", "file");
    config("input.0.required", "false");
    config("input.0.validationType", "none");
  }

  /**
   * @return populated config
   */
  private static GshTemplateConfig templateConfig() {
    GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(CONFIG_ID);
    gshTemplateConfig.populateConfiguration();
    return gshTemplateConfig;
  }

  /**
   * @param fileName client file name
   * @return an upload holding a little text
   * @throws Exception
   */
  private static FileItem upload(String fileName) throws Exception {
    FileItem fileItem = new DiskFileItemFactory().createItem("config_gsh_input_upload", "text/plain", false, fileName);
    OutputStream outputStream = fileItem.getOutputStream();
    outputStream.write("harmless".getBytes(StandardCharsets.UTF_8));
    outputStream.close();
    return fileItem;
  }

  /**
   * the client file name is reduced to a basename, and anything that is not a plain name is refused
   */
  public void testSanitizeFileName() {
    assertEquals("a.txt", GshTemplateFileUpload.sanitizeFileName("a.txt"));
    assertEquals("a.txt", GshTemplateFileUpload.sanitizeFileName("C:\\Users\\x\\a.txt"));
    assertEquals("a.txt", GshTemplateFileUpload.sanitizeFileName("/home/x/a.txt"));
    assertEquals("proof.txt", GshTemplateFileUpload.sanitizeFileName("../proof.txt"));
    assertEquals("proof.txt", GshTemplateFileUpload.sanitizeFileName("..\\proof.txt"));
    assertEquals("proof.txt", GshTemplateFileUpload.sanitizeFileName("../../../../tmp/proof.txt"));

    for (String bad : new String[] {null, "", " ", ".", "..", "../", "a/..", "a\u0000b", "a\nb", "c:proof.txt"}) {
      try {
        GshTemplateFileUpload.sanitizeFileName(bad);
        fail("should refuse: '" + bad + "'");
      } catch (RuntimeException re) {
        // good
      }
    }
  }

  /**
   * a traversal name is written as a direct child of a new temp dir, not next to it
   * @throws Exception
   */
  public void testTraversalNameStaysInTempDirectory() throws Exception {

    String proofName = "gshUploadProof" + GrouperUuid.getUuid() + ".txt";
    File escaped = new File(System.getProperty("java.io.tmpdir"), proofName);
    assertFalse(escaped.exists());

    File file = GshTemplateFileUpload.writeUpload(upload("../" + proofName));
    try {
      assertTrue(file.exists());
      assertEquals(proofName, file.getName());
      assertTrue(file.getParentFile().getName().startsWith(GshTemplateFileUpload.TEMP_DIRECTORY_PREFIX));
      assertEquals(new File(System.getProperty("java.io.tmpdir")).getCanonicalFile(),
          file.getParentFile().getParentFile().getCanonicalFile());
      assertFalse("must not escape the temp dir", escaped.exists());
    } finally {
      file.delete();
      file.getParentFile().delete();
    }
  }

  /**
   * a name that is refused writes nothing
   * @throws Exception
   */
  public void testRefusedNameWritesNothing() throws Exception {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    int before = countUploadDirs(tmpDir);
    try {
      GshTemplateFileUpload.writeUpload(upload(".."));
      fail("should refuse");
    } catch (RuntimeException re) {
      // good
    }
    assertEquals("no temp dir created", before, countUploadDirs(tmpDir));
  }

  /**
   * @param tmpDir
   * @return number of upload temp dirs
   */
  private static int countUploadDirs(File tmpDir) {
    int count = 0;
    File[] files = tmpDir.listFiles();
    for (File file : files == null ? new File[0] : files) {
      if (file.getName().startsWith(GshTemplateFileUpload.TEMP_DIRECTORY_PREFIX)) {
        count++;
      }
    }
    return count;
  }

  /**
   * who may upload: the same rules as who may run the template, checked before anything is written
   */
  public void testAllowedToRunTemplate() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem ownerStem = new StemSave(grouperSession).assignName("test:gshTest05folder").assignCreateParentStemsIfNotExist(true).save();
    Stem otherStem = new StemSave(grouperSession).assignName("test:gshTest05other").assignCreateParentStemsIfNotExist(true).save();
    Group ownerGroup = new GroupSave(grouperSession).assignName("test:gshTest05group").assignCreateParentStemsIfNotExist(true).save();
    assertFalse("precondition", PrivilegeHelper.isWheelOrRoot(SubjectTestHelper.SUBJ0));

    // everyone, enabled, allowed on all folders and groups
    assertTrue(GshTemplateFileUpload.allowedToRunTemplate(templateConfig(), SubjectTestHelper.SUBJ0, ownerStem, null));
    assertTrue(GshTemplateFileUpload.allowedToRunTemplate(templateConfig(), SubjectTestHelper.SUBJ0, null, ownerGroup));

    // no owner, no subject: denied
    assertFalse(GshTemplateFileUpload.allowedToRunTemplate(templateConfig(), SubjectTestHelper.SUBJ0, null, null));
    assertFalse(GshTemplateFileUpload.allowedToRunTemplate(templateConfig(), null, ownerStem, null));

    // wheel only: ordinary user denied, root allowed
    config("securityRunType", "wheel");
    assertFalse(GshTemplateFileUpload.allowedToRunTemplate(templateConfig(), SubjectTestHelper.SUBJ0, ownerStem, null));
    assertTrue(GshTemplateFileUpload.allowedToRunTemplate(templateConfig(), SubjectFinder.findRootSubject(), ownerStem, null));
    config("securityRunType", "everyone");

    // folder the template is not allowed on
    config("folderShowType", "certainFolders");
    config("folderUuidToShow", otherStem.getName());
    config("folderShowOnDescendants", "certainFolders");
    assertFalse(GshTemplateFileUpload.allowedToRunTemplate(templateConfig(), SubjectTestHelper.SUBJ0, ownerStem, null));
    assertTrue(GshTemplateFileUpload.allowedToRunTemplate(templateConfig(), SubjectTestHelper.SUBJ0, otherStem, null));
    config("folderShowType", "allFolders");

    // disabled
    config("enabled", "false");
    assertFalse(GshTemplateFileUpload.allowedToRunTemplate(templateConfig(), SubjectTestHelper.SUBJ0, ownerStem, null));
  }
}
