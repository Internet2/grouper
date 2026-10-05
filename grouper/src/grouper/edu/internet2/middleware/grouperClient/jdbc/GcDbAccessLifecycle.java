package edu.internet2.middleware.grouperClient.jdbc;

/**
 * <pre>Optional lifecycle callbacks for beans persisted with GcDbAccess (storeToDatabase, storeBatchToDatabase,
 * storeListToDatabase, deleteFromDatabase, deleteFromDatabaseMultiple), like the hibernate
 * onPreSave / onPreUpdate / onPreDelete callbacks in GrouperAPI.  This lets a bean set its own
 * context id, created / last updated timestamps, etc instead of every DAO doing it.
 *
 * dbPreStore is called after GcDbAccess decides insert vs update, and before it reads the field values,
 * so changes the bean makes to its persisted fields are stored.  It is not called if the store is skipped
 * (e.g. a GcDbVersionable that did not change).
 *
 * Note: these can be called more than once for the same store, e.g. a defaultUpdate class whose update
 * found no row and falls back to insert (dbPreStore(false) then dbPreStore(true)), or a batch store that
 * is retried in smaller batches.  So implementations should be idempotent (setting fields is fine,
 * do not e.g. increment counters or send messages).
 *
 * The methods have empty defaults so a bean only implements what it needs.</pre>
 */
public interface GcDbAccessLifecycle {

  /**
   * called before the bean is inserted or updated, after GcDbAccess decides which one,
   * and before the field values are read for the sql
   * @param isInsert true if insert, false if update
   */
  public default void dbPreStore(boolean isInsert) {
  }

  /**
   * called before the bean is deleted (single or batch delete)
   */
  public default void dbPreDelete() {
  }

}
