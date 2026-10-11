package edu.internet2.middleware.grouperClient.jdbc.tableSync;


public enum GcGrouperSyncErrorCode {

  /** error, exception while provisioning */
  ERR,

  /** unexpected exception while processing a single object (GRP-7458).  Counted per run and only fails the
   * run past a configurable threshold (errorHandlingUnexpectedErrorMaxCount / errorHandlingUnexpectedErrorMaxPercent) */
  UNE,

  /** invalid data, based on script */
  INV,
  
  /** attribute value is more than maxlength */
  LEN,

  /** required field is missing */
  REQ,

  /** object is missing in the target and not able to be inserted */
  DNE,
  
  /** grouper object matches multiple target objects */
  MAT,
  
  /** group has no members, and configured to be not provisionable */
  MEM,
  
  /** group skipped because it already exists in target on first encounter */
  SKP;

  /**
   * Tolerant lookup of an error code read from the database.  Returns null for null/blank or for an unrecognized
   * value instead of throwing.  This protects callers that read raw error_code strings (e.g. the UI / WS) from
   * throwing IllegalArgumentException on a code written by a newer version during a rolling upgrade or rollback.
   * @param errorCode the stored error code string
   * @return the enum value, or null if null/blank/unrecognized
   */
  public static GcGrouperSyncErrorCode valueOfOrNull(String errorCode) {
    if (errorCode == null || errorCode.trim().length() == 0) {
      return null;
    }
    try {
      return GcGrouperSyncErrorCode.valueOf(errorCode.trim());
    } catch (IllegalArgumentException iae) {
      return null;
    }
  }
}
