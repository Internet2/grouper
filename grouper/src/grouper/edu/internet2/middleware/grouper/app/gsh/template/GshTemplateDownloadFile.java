package edu.internet2.middleware.grouper.app.gsh.template;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.file.GrouperFile;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAOFactory;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * GRP-7438: files that a GSH template saves in grouper_file for the user to download.
 *
 * <p>Rows are stored as plain (unencrypted) text with system_name {@link #SYSTEM_NAME} and
 * file_path /gshTemplateDownload/&lt;templateConfigId&gt;/&lt;yyyy-MM-dd&gt;/&lt;fileName&gt;.
 * file_path has a unique index, so the path is the key: a template can cheaply check whether
 * the file for a day already exists and reuse it instead of recomputing.  The date in the path
 * is also what the retention cleanup goes by, since grouper_file has no timestamp column.</p>
 *
 * <p>There is no central cleanup: each template deletes its own old files with
 * {@link GshTemplateOutput#deleteExpiredDownloadFiles(int)}, which only sees that template's paths.</p>
 *
 * <p>Templates normally use this through {@link GshTemplateOutput#retrieveDownloadFileId(String, String)}
 * and {@link GshTemplateOutput#assignDownloadFile(String, String, String)}, which fill in the
 * template config id.</p>
 */
public class GshTemplateDownloadFile {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GshTemplateDownloadFile.class);

  /** grouper_file.system_name for files saved by gsh templates for download */
  public static final String SYSTEM_NAME = "gshTemplateDownload";

  /** start of grouper_file.file_path for these files */
  private static final String FILE_PATH_PREFIX = "/" + SYSTEM_NAME + "/";

  /** grouper_file.file_name is varchar(100) */
  private static final int FILE_NAME_MAX_LENGTH = 100;

  /** grouper_file.file_path is varchar(400) */
  private static final int FILE_PATH_MAX_LENGTH = 400;

  /** yyyy-MM-dd, checked before parsing so the path segment is exactly 10 chars */
  private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

  /**
   * build the grouper_file.file_path for a template, date, and file name.  Validates all three
   * since they become path segments.
   * @param templateConfigId gsh template config id
   * @param date yyyy-MM-dd, the day this file is for
   * @param fileName name the browser saves the file as, e.g. myReport_2026-10-04.csv
   * @return the path, e.g. /gshTemplateDownload/myReport/2026-10-04/myReport_2026-10-04.csv
   */
  public static String filePath(String templateConfigId, String date, String fileName) {
    validateTemplateConfigId(templateConfigId);
    parseDate(date);
    validateFileName(fileName);

    String filePath = FILE_PATH_PREFIX + templateConfigId + "/" + date + "/" + fileName;
    if (filePath.length() > FILE_PATH_MAX_LENGTH) {
      throw new RuntimeException("File path is longer than " + FILE_PATH_MAX_LENGTH + " chars: " + filePath);
    }
    return filePath;
  }

  /**
   * find the file a template saved for a date.  This loads the contents, use findIdByDate() to
   * just check if it is there
   * @param templateConfigId
   * @param date yyyy-MM-dd
   * @param fileName
   * @return the file or null if it is not there
   */
  public static GrouperFile findByDate(String templateConfigId, String date, String fileName) {
    String filePath = filePath(templateConfigId, date, fileName);
    GrouperFile grouperFile = Hib3DAOFactory.getFactory().getGrouperFile().findByFilePath(filePath, false);

    // path is namespaced by the prefix, but make sure it is really one of ours
    if (grouperFile != null && !StringUtils.equals(SYSTEM_NAME, grouperFile.getSystemName())) {
      throw new RuntimeException("File at path '" + filePath + "' has system name '"
          + grouperFile.getSystemName() + "', expected '" + SYSTEM_NAME + "'");
    }
    return grouperFile;
  }

  /**
   * find the id of the file a template saved for a date, without loading the contents.  Use this
   * to check if the file is already there
   * @param templateConfigId
   * @param date yyyy-MM-dd
   * @param fileName
   * @return the grouper_file id or null if it is not there
   */
  public static String findIdByDate(String templateConfigId, String date, String fileName) {
    String filePath = filePath(templateConfigId, date, fileName);
    return Hib3DAOFactory.getFactory().getGrouperFile().findIdBySystemNameAndFilePath(SYSTEM_NAME, filePath);
  }

  /**
   * save the contents for a template, date, and file name.  If the row is already there its
   * contents are replaced, so the id stays the same.  Stored unencrypted.
   * @param templateConfigId
   * @param date yyyy-MM-dd
   * @param fileName
   * @param contents text contents, e.g. csv
   * @return the saved row
   */
  public static GrouperFile save(String templateConfigId, String date, String fileName, String contents) {
    if (contents == null) {
      throw new RuntimeException("Contents cannot be null for file: " + fileName);
    }

    GrouperFile grouperFile = findByDate(templateConfigId, date, fileName);

    if (grouperFile == null) {
      grouperFile = new GrouperFile();
      grouperFile.setId(GrouperUuid.getUuid());
      grouperFile.setSystemName(SYSTEM_NAME);
      grouperFile.setFilePath(filePath(templateConfigId, date, fileName));
      grouperFile.setFileName(fileName);
    }

    // goes to varchar or clob based on size
    grouperFile.setValueToSave(contents);

    Hib3DAOFactory.getFactory().getGrouperFile().saveOrUpdate(grouperFile);
    return grouperFile;
  }

  /**
   * delete one template's download files whose date is more than retentionDays before today.
   * Only rows with system_name gshTemplateDownload under this template's path are looked at, so
   * other templates' files, files from other features, and existing files a template pointed at
   * are never deleted.
   * @param templateConfigId gsh template whose files to clean up
   * @param today yyyy-MM-dd
   * @param retentionDays 0 keeps only today, 1 keeps today and yesterday, etc
   * @return number of files deleted
   */
  public static int deleteExpired(String templateConfigId, String today, int retentionDays) {
    validateTemplateConfigId(templateConfigId);
    if (retentionDays < 0) {
      throw new RuntimeException("Retention days cannot be negative: " + retentionDays);
    }
    LocalDate cutoff = parseDate(today).minusDays(retentionDays);

    // escape like wildcards in the config id so the prefix match is exact
    String templatePathPrefix = FILE_PATH_PREFIX + templateConfigId + "/";
    String likePrefix = templatePathPrefix.replace("!", "!!").replace("%", "!%").replace("_", "!_");

    // only select id and path, no need to pull the contents
    List<Object[]> idsAndPaths = new GcDbAccess()
        .sql("select id, file_path from grouper_file where system_name = ? and file_path like ? escape '!'")
        .addBindVar(SYSTEM_NAME).addBindVar(likePrefix + "%").selectList(Object[].class);

    int deletedCount = 0;
    for (Object[] idAndPath : GrouperUtil.nonNull(idsAndPaths)) {
      String id = (String)idAndPath[0];
      String filePath = (String)idAndPath[1];

      // double check in java in case the database like is case insensitive (mysql)
      if (filePath == null || !filePath.startsWith(templatePathPrefix)) {
        continue;
      }

      LocalDate fileDate = dateFromFilePath(filePath);
      if (fileDate == null) {
        LOG.warn("Cant parse date from gsh template download file path, not deleting: " + filePath);
        continue;
      }
      if (!fileDate.isBefore(cutoff)) {
        continue;
      }

      // delete without loading the contents
      deletedCount += Hib3DAOFactory.getFactory().getGrouperFile().deleteById(id);
    }
    if (deletedCount > 0) {
      LOG.info("Deleted " + deletedCount + " gsh template download files for template '" + templateConfigId + "' before " + cutoff);
    }
    return deletedCount;
  }

  /**
   * get the date segment from /gshTemplateDownload/&lt;templateConfigId&gt;/&lt;yyyy-MM-dd&gt;/&lt;fileName&gt;
   * @param filePath
   * @return the date or null if the path is not in that format
   */
  static LocalDate dateFromFilePath(String filePath) {
    if (filePath == null || !filePath.startsWith(FILE_PATH_PREFIX)) {
      return null;
    }
    // templateConfigId, date, fileName
    String[] segments = filePath.substring(FILE_PATH_PREFIX.length()).split("/");
    if (segments.length != 3) {
      return null;
    }
    try {
      return parseDate(segments[1]);
    } catch (RuntimeException re) {
      return null;
    }
  }

  /**
   * parse yyyy-MM-dd strictly (e.g. 2026-10-4 and 2026-13-01 fail)
   * @param date
   * @return the date
   */
  private static LocalDate parseDate(String date) {
    if (date == null || !DATE_PATTERN.matcher(date).matches()) {
      throw new RuntimeException("Date must be yyyy-MM-dd: '" + date + "'");
    }
    try {
      return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException dtpe) {
      throw new RuntimeException("Date must be yyyy-MM-dd: '" + date + "'", dtpe);
    }
  }

  /**
   * @param templateConfigId must be there and not have a slash since it is a path segment
   */
  private static void validateTemplateConfigId(String templateConfigId) {
    if (StringUtils.isBlank(templateConfigId) || templateConfigId.contains("/")) {
      throw new RuntimeException("Invalid template config id: '" + templateConfigId + "'");
    }
  }

  /**
   * @param fileName must be there, fit the column, and not have a slash since it is a path segment
   */
  private static void validateFileName(String fileName) {
    if (StringUtils.isBlank(fileName) || fileName.contains("/") || fileName.contains("\\")) {
      throw new RuntimeException("Invalid file name: '" + fileName + "'");
    }
    if (fileName.length() > FILE_NAME_MAX_LENGTH) {
      throw new RuntimeException("File name is longer than " + FILE_NAME_MAX_LENGTH + " chars: '" + fileName + "'");
    }
  }

}
