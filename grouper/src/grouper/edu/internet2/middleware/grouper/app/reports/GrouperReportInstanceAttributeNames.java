package edu.internet2.middleware.grouper.app.reports;

/**
 * 
 */
public class GrouperReportInstanceAttributeNames {
  
  /**
   * main attribute definition assigned to groups, folders
   */
  public static final String GROUPER_REPORT_INSTANCE_DEF = "reportInstanceDef";
  
  /**
   * main attribute name assigned to reportInstanceDef
   */
  public static final String GROUPER_REPORT_INSTANCE_ATTRIBUTE_NAME = "reportInstanceMarker";
  
  /**
   * attribute definition for name value pairs assigned to assignment on groups or folders
   */
  public static final String GROUPER_REPORT_INSTANCE_VALUE_DEF = "reportInstanceValueDef";

  
  /**
   * SUCCESS means link to the report from screen, ERROR means didnt execute successfully
   */
  public static final String GROUPER_REPORT_INSTANCE_STATUS = "reportInstanceStatus";
  
  /**
   * number of millis it took to generate this report
   */
  public static final String GROUPER_REPORT_INSTANCE_MILLIS_ELAPSED = "reportElapsedMillis";
  
  /**
   * Attribute assign ID of the marker attribute of the config (same owner as this attribute, but there could be many reports configured on one owner)
   */
  public static final String GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID = "reportInstanceConfigMarkerAssignmentId";
  
  /**
   * millis since 1970 that this report was run. This must match the timestamp in the report name and storage
   */
  public static final String GROUPER_REPORT_INSTANCE_MILLIS_SINCE_1970 = "reportInstanceMillisSince1970";
  
  /**
   * number of bytes of the unencrypted report
   */
  public static final String GROUPER_REPORT_INSTANCE_SIZE_BYTES = "reportInstanceSizeBytes";
  
  /**
   * filename of report
   */
  public static final String GROUPER_REPORT_INSTANCE_FILE_NAME = "reportInstanceFilename";
  
  /**
   * depending on storage type, this is a pointer to the report in storage, e.g. the S3 address.
   *  note the S3 address is .csv suffix, but change to __metadata.json for instance metadata
   */
  public static final String GROUPER_REPORT_INSTANCE_FILE_POINTER = "reportInstanceFilePointer";
  
  /**
   * number of times this report was downloaded (note update this in try/catch and a for loop so concurrency doesnt cause problems)
   */
  public static final String GROUPER_REPORT_INSTANCE_DOWNLOAD_COUNT = "reportInstanceDownloadCount";
  
  /**
   * randomly generated 16 char alphanumeric encryption key (never allow display or edit of this)
   */
  public static final String GROUPER_REPORT_INSTANCE_ENCRYPTION_KEY = "reportInstanceEncryptionKey";
  
  /**
   * number of rows returned in report
   */
  public static final String GROUPER_REPORT_INSTANCE_ROWS = "reportInstanceRows";
  
  /**
   * source::::subjectId1, source2::::subjectId2 list for subjects who were were emailed successfully (cant be more than 4k chars)
   */
  public static final String GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS = "reportInstanceEmailToSubjects";
  
  /**
   * source::::subjectId1, source2::::subjectId2 list for subjects who were were NOT emailed successfully, dont include g:gsa groups (cant be more than 4k chars)
   */
  public static final String GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS_ERROR = "reportInstanceEmailToSubjectsError";
}
