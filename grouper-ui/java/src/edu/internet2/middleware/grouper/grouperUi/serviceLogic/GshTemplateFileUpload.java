package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfigAndValue;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOwnerType;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateType;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateValidationService;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGshTemplateConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Writes a file uploaded to a GSH template FILE input to disk, safely.
 *
 * Two problems with writing the upload directly (UiV2Template.populateCustomTemplateInputs):
 *   1. The multipart filename comes from the client.  Appending it to a temp directory lets a name like
 *      "../x.jsp" escape the directory and write anywhere the UI process can write.
 *   2. The write happened before the template enabled / owner / who-can-run checks, which only run later in
 *      GshTemplateExec.execute(), so a caller who may not run the template could still cause the write.
 *
 * This class checks first that the logged in user may run the template on the owner (group or folder) of the
 * current request, failing closed, and only then writes the upload under a new server-created temp directory,
 * using only the basename of the client filename and proving the result stays inside that directory.
 *
 * It also deletes those copies again (added 2026-11-11), since nothing else does and each upload would otherwise
 * stay in java.io.tmpdir for good:
 *   - a write that fails after its directory was created removes the directory right away (writeUpload),
 *   - a string input is deleted as soon as it is read, and uploads that no run will use (a validation error, an
 *     input hidden by showEl, a form decoration) are deleted before the request ends (UiV2Template calls
 *     deleteUpload / deleteUploads / deleteUnusedUploads),
 *   - a file input handed to a template run is deleted when the run is done, in the finally block of the task
 *     that runs it, which may be a background thread that outlives the request (UiV2Template).
 * Deleting only ever touches the given file and its own directory, and only under java.io.tmpdir.
 *
 * It only uses APIs that exist in every release it ships with, so the same file is in every file set.
 * </pre>
 */
public class GshTemplateFileUpload {

  /** longest file name kept on disk */
  private static final int MAX_FILE_NAME_LENGTH = 255;

  /** prefix of the temp directory each upload gets */
  static final String TEMP_DIRECTORY_PREFIX = "grouperGshTemplateUpload";

  /**
   * Check that the user may run this template on the owner in the request, then write the upload to a new temp
   * directory with a sanitized basename.
   * @param gshTemplateConfig populated config of the template the upload is for
   * @param loggedInSubject the logged in user
   * @param fileItem the multipart upload
   * @return the file written
   * @throws RuntimeException if not allowed, the file name is not acceptable, or the write fails; nothing is
   * written to disk when not allowed or the name is not acceptable
   */
  public static File writeUploadAfterAuthorization(GshTemplateConfig gshTemplateConfig, Subject loggedInSubject, FileItem fileItem) {

    // owner from the request: the template screens look up the folder or group (with the user's privileges)
    // before populating inputs, and that puts it in the request container
    Stem ownerStem = null;
    Group ownerGroup = null;
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiStem guiStem = grouperRequestContainer.getStemContainer().getGuiStem();
    GuiGroup guiGroup = grouperRequestContainer.getGroupContainer().getGuiGroup();
    if (guiStem != null && guiStem.getStem() != null) {
      ownerStem = guiStem.getStem();
    } else if (guiGroup != null && guiGroup.getGroup() != null) {
      ownerGroup = guiGroup.getGroup();
    }

    if (!allowedToRunTemplate(gshTemplateConfig, loggedInSubject, ownerStem, ownerGroup)) {
      throw new RuntimeException("Not allowed to run this template: '"
          + (gshTemplateConfig == null ? null : gshTemplateConfig.getConfigId()) + "'");
    }

    return writeUpload(fileItem);
  }

  /**
   * Whether the subject may run the template on the owner: enabled, allowed on that folder or group, and the
   * subject satisfies the securityRunType.  Fails closed: no owner, no subject, or any exception is false.
   * Runs as root so the result does not depend on what the user can VIEW (execution also checks as root).
   * @param gshTemplateConfig populated config
   * @param subject current user
   * @param ownerStem owner folder, or null
   * @param ownerGroup owner group, or null (one of the two must be set)
   * @return true if allowed
   */
  static boolean allowedToRunTemplate(final GshTemplateConfig gshTemplateConfig, final Subject subject,
      final Stem ownerStem, final Group ownerGroup) {

    if (gshTemplateConfig == null || subject == null || (ownerStem == null && ownerGroup == null)) {
      return false;
    }

    try {
      return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          if (!gshTemplateConfig.isEnabled()) {
            return false;
          }

          // build the exec the same way UiV2Template.customTemplateExecute does
          GshTemplateExec gshTemplateExec = new GshTemplateExec();
          gshTemplateExec.assignConfigId(gshTemplateConfig.getConfigId());
          gshTemplateExec.assignCurrentUser(subject);

          if (ownerStem != null) {
            if (!gshTemplateConfig.canFolderRunTemplate(ownerStem)) {
              return false;
            }
            gshTemplateExec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
            gshTemplateExec.assignOwnerStemName(ownerStem.isRootStem() ? ":" : ownerStem.getName());
          } else {
            // abac templates are not limited to certain groups (same as GshTemplateValidationService.validateOwnerType)
            if (gshTemplateConfig.getGshTemplateType() != GshTemplateType.abac
                && !gshTemplateConfig.canGroupRunTemplate(ownerGroup)) {
              return false;
            }
            gshTemplateExec.assignGshTemplateOwnerType(GshTemplateOwnerType.group);
            gshTemplateExec.assignOwnerGroupName(ownerGroup.getName());
          }

          // throws on a securityRunType it does not know (e.g. null), which is caught below and denies
          return new GshTemplateValidationService().canSubjectExecuteTemplate(gshTemplateConfig, gshTemplateExec);
        }
      });
    } catch (RuntimeException re) {
      GrouperUtil.getLog(GshTemplateFileUpload.class).warn("gsh template upload denied: " + gshTemplateConfig.getConfigId(), re);
      return false;
    }
  }

  /**
   * Reduce a client supplied upload file name to a safe basename.  Browsers may send a full path (old IE sends
   * C:\dir\file.txt), so keep only what is after the last / or \.  Then refuse anything that is not a plain
   * name.  A refused name is an error, not silently replaced, since a traversal name is never an honest upload.
   * @param fileName from the multipart request
   * @return the basename
   * @throws RuntimeException if not acceptable
   */
  static String sanitizeFileName(String fileName) {

    String name = StringUtils.defaultString(fileName);

    int lastSeparator = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
    if (lastSeparator >= 0) {
      name = name.substring(lastSeparator + 1);
    }
    name = name.trim();

    if (name.length() == 0 || ".".equals(name) || "..".equals(name)) {
      throw new RuntimeException("Upload file name is not allowed");
    }
    if (name.length() > MAX_FILE_NAME_LENGTH) {
      throw new RuntimeException("Upload file name is too long");
    }
    for (int i = 0; i < name.length(); i++) {
      char theChar = name.charAt(i);
      // control characters (including NUL and newlines) and a drive / stream separator are never needed
      if (theChar < 0x20 || theChar == 0x7f || theChar == ':') {
        throw new RuntimeException("Upload file name has an invalid character");
      }
    }
    return name;
  }

  /**
   * Write the upload into a new temp directory as its sanitized basename, after proving the target is a direct
   * child of that directory.
   * @param fileItem upload
   * @return the file written
   */
  static File writeUpload(FileItem fileItem) {

    if (fileItem == null) {
      throw new RuntimeException("No upload");
    }

    // check the name before anything is created on disk
    String fileName = sanitizeFileName(fileItem.getName());

    Path tempDirectory = null;
    try {
      tempDirectory = Files.createTempDirectory(TEMP_DIRECTORY_PREFIX).toAbsolutePath().normalize();
      Path target = tempDirectory.resolve(fileName).normalize();

      // belt and braces after the sanitizer: the file must be directly in the new directory
      if (!tempDirectory.equals(target.getParent())) {
        throw new RuntimeException("Upload file name is not allowed");
      }

      File file = target.toFile();
      fileItem.write(file);
      return file;
    } catch (RuntimeException re) {
      deleteRejectedUpload(tempDirectory, fileName);
      throw re;
    } catch (Exception e) {
      deleteRejectedUpload(tempDirectory, fileName);
      throw new RuntimeException("Cant write upload", e);
    }
  }

  /**
   * A write that failed after its temp directory was created: remove what it left (a partial file, the directory)
   * so a rejected upload leaves nothing behind.  Only the sanitized name directly in that new directory is touched.
   * @param tempDirectory the directory created for this upload, or null if it was not created
   * @param fileName sanitized basename
   */
  private static void deleteRejectedUpload(Path tempDirectory, String fileName) {
    if (tempDirectory == null) {
      return;
    }
    try {
      File file = new File(tempDirectory.toFile(), fileName);
      if (file.isFile()) {
        file.delete();
      }
      tempDirectory.toFile().delete();
    } catch (RuntimeException re) {
      // do not hide the original error
      GrouperUtil.getLog(GshTemplateFileUpload.class).warn("could not delete rejected gsh template upload: " + tempDirectory, re);
    }
  }

  /**
   * Delete an uploaded copy and the directory made for it.  Works for the copies this class writes and for the
   * ones the code before it wrote (a random directory, or a file directly in java.io.tmpdir in 5.18.0).  To stay
   * safe if called with something else, it only deletes a regular file that is directly in java.io.tmpdir or in
   * a directory directly under java.io.tmpdir, and then that directory only if it is empty.  Never throws: a
   * cleanup problem is logged and must not hide the result of the request or the run.
   * @param file the uploaded copy, may be null or already gone
   */
  public static void deleteUpload(File file) {
    if (file == null) {
      return;
    }
    try {
      Path tempRoot = Paths.get(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize();
      Path path = file.toPath().toAbsolutePath().normalize();
      Path parent = path.getParent();
      if (parent == null) {
        return;
      }
      boolean inTempRoot = tempRoot.equals(parent);
      boolean inUploadDirectory = tempRoot.equals(parent.getParent());
      if (!inTempRoot && !inUploadDirectory) {
        GrouperUtil.getLog(GshTemplateFileUpload.class).warn("not deleting gsh template upload outside of java.io.tmpdir: " + path);
        return;
      }
      if (Files.isRegularFile(path) && !path.toFile().delete()) {
        GrouperUtil.getLog(GshTemplateFileUpload.class).warn("could not delete gsh template upload: " + path);
      }
      // the directory made for this upload; File.delete() leaves it if the template put something else in it
      if (inUploadDirectory) {
        parent.toFile().delete();
      }
    } catch (RuntimeException re) {
      GrouperUtil.getLog(GshTemplateFileUpload.class).warn("could not delete gsh template upload: " + file, re);
    }
  }

  /**
   * Delete the uploaded copies among template inputs: the values of inputs with form element FILE that are files
   * (a string input from a file is read and deleted already, its value is a String).
   * @param gshTemplateInputConfigAndValues inputs as returned by UiV2Template.populateCustomTemplateInputs, may be null
   */
  public static void deleteUploads(Map<String, GshTemplateInputConfigAndValue> gshTemplateInputConfigAndValues) {
    if (gshTemplateInputConfigAndValues == null) {
      return;
    }
    for (GshTemplateInputConfigAndValue gshTemplateInputConfigAndValue : gshTemplateInputConfigAndValues.values()) {
      if (isUpload(gshTemplateInputConfigAndValue)) {
        deleteUpload((File)gshTemplateInputConfigAndValue.getValueObject());
      }
    }
  }

  /**
   * Delete the uploaded copies of the template form in the request (UiV2Template.newTemplateHelper, once the form
   * is decorated and drawn; nothing runs with those inputs).
   * @param guiGshTemplateConfig from the request container, may be null
   */
  public static void deleteUploads(GuiGshTemplateConfig guiGshTemplateConfig) {
    if (guiGshTemplateConfig != null) {
      deleteUploads(guiGshTemplateConfig.getGshTemplateInputConfigAndValues());
    }
  }

  /**
   * At the end of UiV2Template.populateCustomTemplateInputs: delete the uploads no template run will use.  When the
   * inputs added a validation error the caller stops before the run, so all of them; else the ones of inputs that
   * showEl hid, which are not passed to the run.
   * @param allInputs every input with its value, before showEl
   * @param usedInputs the inputs returned to the caller
   * @param validationError true if populating the inputs added a validation error
   */
  public static void deleteUnusedUploads(Map<String, GshTemplateInputConfigAndValue> allInputs,
      Map<String, GshTemplateInputConfigAndValue> usedInputs, boolean validationError) {
    if (allInputs == null) {
      return;
    }
    for (Map.Entry<String, GshTemplateInputConfigAndValue> entry : allInputs.entrySet()) {
      if (validationError || usedInputs == null || usedInputs.get(entry.getKey()) != entry.getValue()) {
        if (isUpload(entry.getValue())) {
          deleteUpload((File)entry.getValue().getValueObject());
        }
      }
    }
  }

  /**
   * @param gshTemplateInputConfigAndValue an input
   * @return true if its value is an uploaded file (form element FILE, value a File)
   */
  private static boolean isUpload(GshTemplateInputConfigAndValue gshTemplateInputConfigAndValue) {
    return gshTemplateInputConfigAndValue != null
        && gshTemplateInputConfigAndValue.getGshTemplateInputConfig() != null
        && gshTemplateInputConfigAndValue.getGshTemplateInputConfig().getConfigItemFormElement() == ConfigItemFormElement.FILE
        && gshTemplateInputConfigAndValue.getValueObject() instanceof File;
  }
}
