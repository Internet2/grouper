package edu.internet2.middleware.grouperClient.jdbc.tableSync;

/**
 * which DML phases a full-sync invocation should execute. Lets callers
 * split the standard insert/update/delete pass when ordering matters
 * for foreign-key integrity (e.g. parent-table inserts before child sync,
 * parent-table deletes after child sync).
 */
public enum GcTableSyncPhase {

  /** standard behavior: insert, update, and delete in one call */
  INSERTS_UPDATES_DELETES,

  /** insert and update only; defer deletes for a later call */
  INSERTS_UPDATES_ONLY,

  /** delete only; assumes inserts/updates were applied earlier */
  DELETES_ONLY;

}
