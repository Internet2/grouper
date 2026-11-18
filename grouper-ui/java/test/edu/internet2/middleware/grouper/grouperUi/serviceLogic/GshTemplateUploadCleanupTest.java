package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfigAndValue;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory C3 (temporaryUploadFilesNotCleaned): a GSH template upload was copied to a new directory
 * under java.io.tmpdir and never deleted, after a run or after a rejection.  The copies are now deleted:
 * a write that fails removes its directory right away (GshTemplateFileUpload.writeUpload), a string input is
 * deleted once read, uploads no run will use (validation error, input hidden by showEl, form decoration) are
 * deleted before the request ends, and a file input is deleted in the finally block of the task that runs the
 * template (UiV2Template, possibly a background thread).  The loader config import deletes its temp
 * directory in a finally block (UiV2GrouperLoader.importLoaderConfigSubmit, checked by the http test).
 * <p>
 * The UiV2Template call sites need the UI request and filter thread locals, so this test calls the
 * GshTemplateFileUpload cleanup methods they use directly.  No database is needed.
 * </p>
 */
public class GshTemplateUploadCleanupTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GshTemplateUploadCleanupTest.class);
  }

  /**
   *
   */
  public GshTemplateUploadCleanupTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateUploadCleanupTest(String name) {
    super(name);
  }

  /**
   * @return java.io.tmpdir
   */
  private static File tmpDir() {
    return new File(System.getProperty("java.io.tmpdir"));
  }

  /**
   * @param fileName client file name
   * @return an upload holding a little text
   * @throws Exception
   */
  private static FileItem upload(String fileName) throws Exception {
    FileItem fileItem = new DiskFileItemFactory().createItem("config_gsh_input_upload", "text/plain", false, fileName);
    OutputStream outputStream = fileItem.getOutputStream();
    outputStream.write("harmless secPatch145".getBytes(StandardCharsets.UTF_8));
    outputStream.close();
    return fileItem;
  }

  /**
   * @param name
   * @return every file with that name directly in java.io.tmpdir or one level below it
   */
  private static int countCopies(String name) {
    int count = new File(tmpDir(), name).exists() ? 1 : 0;
    File[] children = tmpDir().listFiles();
    for (File child : children == null ? new File[0] : children) {
      if (child.isDirectory() && new File(child, name).exists()) {
        count++;
      }
    }
    return count;
  }

  /**
   * @return number of upload temp dirs
   */
  private static int countUploadDirs() {
    int count = 0;
    File[] files = tmpDir().listFiles();
    for (File file : files == null ? new File[0] : files) {
      if (file.getName().startsWith(GshTemplateFileUpload.TEMP_DIRECTORY_PREFIX)) {
        count++;
      }
    }
    return count;
  }

  /**
   * @param name input name
   * @param formElement form element of the input
   * @param value value object
   * @return input config with value, as populateCustomTemplateInputs builds it
   */
  private static GshTemplateInputConfigAndValue input(String name, ConfigItemFormElement formElement, Object value) {
    GshTemplateInputConfig gshTemplateInputConfig = new GshTemplateInputConfig();
    gshTemplateInputConfig.setName(name);
    gshTemplateInputConfig.setConfigItemFormElement(formElement);
    gshTemplateInputConfig.setGshTemplateInputType(value instanceof File ? GshTemplateInputType.FILE : GshTemplateInputType.STRING);
    GshTemplateInputConfigAndValue gshTemplateInputConfigAndValue = new GshTemplateInputConfigAndValue();
    gshTemplateInputConfigAndValue.setGshTemplateInputConfig(gshTemplateInputConfig);
    gshTemplateInputConfigAndValue.setValueObject(value);
    return gshTemplateInputConfigAndValue;
  }

  /**
   * an upload whose write fails after its directory was created leaves nothing behind (rejected upload)
   * @throws Exception
   */
  public void testFailedWriteLeavesNothing() throws Exception {
    String name = "secPatch145failed" + GrouperUuid.getUuid() + ".txt";
    int dirsBefore = countUploadDirs();

    DiskFileItem failingItem = new DiskFileItem("config_gsh_input_upload", "text/plain", false, name, 10000, null) {

      /** */
      private static final long serialVersionUID = 1L;

      @Override
      public void write(File file) throws Exception {
        // a partial file, then the failure (disk full, client went away)
        OutputStream outputStream = new FileOutputStream(file);
        outputStream.write(1);
        outputStream.close();
        throw new IOException("secPatch145 write fails");
      }
    };

    try {
      GshTemplateFileUpload.writeUpload(failingItem);
      fail("write should fail");
    } catch (RuntimeException re) {
      // good
    }
    assertEquals("no copy of the failed upload is left", 0, countCopies(name));
    assertEquals("no upload directory is left", dirsBefore, countUploadDirs());
  }

  /**
   * a name that is refused writes nothing and creates no directory
   * @throws Exception
   */
  public void testRefusedNameLeavesNothing() throws Exception {
    int dirsBefore = countUploadDirs();
    try {
      GshTemplateFileUpload.writeUpload(upload(".."));
      fail("should refuse");
    } catch (RuntimeException re) {
      // good
    }
    assertEquals("no upload directory is left", dirsBefore, countUploadDirs());
  }

  /**
   * after a run (the task's finally calls deleteUploads with the run's inputs) neither the copy nor its
   * directory remain; string values and inputs that are not file inputs are left alone
   * @throws Exception
   */
  public void testDeleteUploadsAfterRun() throws Exception {
    String name = "secPatch145run" + GrouperUuid.getUuid() + ".txt";
    int dirsBefore = countUploadDirs();
    File file = GshTemplateFileUpload.writeUpload(upload(name));
    File directory = file.getParentFile();
    assertTrue(file.isFile());
    assertEquals(dirsBefore + 1, countUploadDirs());

    // a file the template was given some other way (not a FILE form element) is not ours to delete
    File notAnUpload = File.createTempFile("secPatch145notUpload", ".txt");
    try {
      Map<String, GshTemplateInputConfigAndValue> inputs = new LinkedHashMap<String, GshTemplateInputConfigAndValue>();
      inputs.put("gsh_input_upload", input("gsh_input_upload", ConfigItemFormElement.FILE, file));
      inputs.put("gsh_input_text", input("gsh_input_text", ConfigItemFormElement.TEXT, "some text"));
      inputs.put("gsh_input_other", input("gsh_input_other", ConfigItemFormElement.TEXT, notAnUpload));
      inputs.put("gsh_input_noFile", input("gsh_input_noFile", ConfigItemFormElement.FILE, null));

      GshTemplateFileUpload.deleteUploads(inputs);

      assertFalse("upload deleted after the run", file.exists());
      assertFalse("its directory deleted after the run", directory.exists());
      assertEquals(0, countCopies(name));
      assertEquals(dirsBefore, countUploadDirs());
      assertTrue("a file that is not an upload is not deleted", notAnUpload.exists());

      // a second cleanup (e.g. the form redraw after the run) of the same inputs is harmless
      GshTemplateFileUpload.deleteUploads(inputs);
      GshTemplateFileUpload.deleteUploads((Map<String, GshTemplateInputConfigAndValue>)null);
    } finally {
      notAnUpload.delete();
    }
  }

  /**
   * a string input from a file is read and then deleteUpload removes the copy and its directory
   * @throws Exception
   */
  public void testDeleteUploadOfStringInput() throws Exception {
    String name = "secPatch145string" + GrouperUuid.getUuid() + ".txt";
    File file = GshTemplateFileUpload.writeUpload(upload(name));
    assertEquals("harmless secPatch145", new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
    GshTemplateFileUpload.deleteUpload(file);
    assertFalse(file.exists());
    assertFalse(file.getParentFile().exists());
    assertEquals(0, countCopies(name));

    // already gone, or null: no error
    GshTemplateFileUpload.deleteUpload(file);
    GshTemplateFileUpload.deleteUpload(null);
  }

  /**
   * the copy the code before GshTemplateFileUpload wrote (a random directory under java.io.tmpdir, or a file
   * directly in java.io.tmpdir as in 5.18.0) is deleted too, but never java.io.tmpdir itself
   * @throws Exception
   */
  public void testDeleteUploadOfOlderLayouts() throws Exception {
    File randomDirectory = Files.createTempDirectory(GrouperUuid.getUuid()).toFile();
    File inRandomDirectory = new File(randomDirectory, "secPatch145old.txt");
    Files.write(inRandomDirectory.toPath(), "x".getBytes(StandardCharsets.UTF_8));
    GshTemplateFileUpload.deleteUpload(inRandomDirectory);
    assertFalse(inRandomDirectory.exists());
    assertFalse(randomDirectory.exists());

    File direct = File.createTempFile("secPatch145direct", "");
    GshTemplateFileUpload.deleteUpload(direct);
    assertFalse(direct.exists());
    assertTrue("java.io.tmpdir is never deleted", tmpDir().isDirectory());
  }

  /**
   * only a file in java.io.tmpdir or one directory below is deleted, and a directory that still holds other
   * files is kept
   * @throws Exception
   */
  public void testDeleteUploadStaysInTempDirectory() throws Exception {
    File outer = Files.createTempDirectory("secPatch145outer").toFile();
    File inner = new File(outer, "inner");
    assertTrue(inner.mkdir());
    File deep = new File(inner, "deep.txt");
    Files.write(deep.toPath(), "x".getBytes(StandardCharsets.UTF_8));
    File sibling = new File(outer, "sibling.txt");
    Files.write(sibling.toPath(), "x".getBytes(StandardCharsets.UTF_8));
    File inOuter = new File(outer, "upload.txt");
    Files.write(inOuter.toPath(), "x".getBytes(StandardCharsets.UTF_8));
    try {
      GshTemplateFileUpload.deleteUpload(deep);
      assertTrue("two levels below java.io.tmpdir is not an upload: not deleted", deep.exists());

      GshTemplateFileUpload.deleteUpload(inOuter);
      assertFalse(inOuter.exists());
      assertTrue("a directory that still has other files is kept", outer.isDirectory());
      assertTrue(sibling.exists());

      // a directory is never deleted as if it were the upload
      GshTemplateFileUpload.deleteUpload(inner);
      assertTrue(inner.isDirectory());
    } finally {
      deep.delete();
      inner.delete();
      sibling.delete();
      inOuter.delete();
      outer.delete();
    }
  }

  /**
   * at the end of populateCustomTemplateInputs: with a validation error every upload is deleted (the caller
   * stops before the run); without one, only those of inputs that showEl hid (not passed to the run)
   * @throws Exception
   */
  public void testDeleteUnusedUploads() throws Exception {
    File used = GshTemplateFileUpload.writeUpload(upload("secPatch145used" + GrouperUuid.getUuid() + ".txt"));
    File hidden = GshTemplateFileUpload.writeUpload(upload("secPatch145hidden" + GrouperUuid.getUuid() + ".txt"));
    try {
      Map<String, GshTemplateInputConfigAndValue> allInputs = new LinkedHashMap<String, GshTemplateInputConfigAndValue>();
      allInputs.put("used", input("used", ConfigItemFormElement.FILE, used));
      allInputs.put("hidden", input("hidden", ConfigItemFormElement.FILE, hidden));
      Map<String, GshTemplateInputConfigAndValue> usedInputs = new LinkedHashMap<String, GshTemplateInputConfigAndValue>(allInputs);
      usedInputs.remove("hidden");

      // no validation error: the hidden input's upload goes, the used one stays for the run
      GshTemplateFileUpload.deleteUnusedUploads(allInputs, usedInputs, false);
      assertFalse("upload of a hidden input deleted", hidden.exists());
      assertFalse(hidden.getParentFile().exists());
      assertTrue("upload the run will use is kept", used.exists());

      // validation error: no run, so every upload goes
      GshTemplateFileUpload.deleteUnusedUploads(allInputs, usedInputs, true);
      assertFalse("upload deleted on a validation error", used.exists());
      assertFalse(used.getParentFile().exists());
    } finally {
      GshTemplateFileUpload.deleteUpload(used);
      GshTemplateFileUpload.deleteUpload(hidden);
    }
  }
}
