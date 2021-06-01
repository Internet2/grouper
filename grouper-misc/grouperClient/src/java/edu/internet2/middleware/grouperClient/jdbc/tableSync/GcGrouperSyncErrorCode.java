package edu.internet2.middleware.grouperClient.jdbc.tableSync;


public enum GcGrouperSyncErrorCode {

  /** error, exception while provisioning */
  ERR,

  /** invalid data, based on script */
  INV,
  
  /** attribute value is more than maxlength */
  LEN,

  /** required field is missing */
  REQ,

  /** object is missing in the target and not able to be inserted */
  DNE;
}
