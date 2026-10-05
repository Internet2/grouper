package edu.internet2.middleware.grouperClient.jdbc;

/**
 * Thrown by GcDbAccess when an object with an optimisticLockVersion field
 * (see GcPersistableField) is updated or deleted, but no row matched the primary key and
 * the version the object was loaded with.  That means someone else changed (or deleted)
 * the row since it was read, the caller should reload the object and make the change again.
 * This lives in grouperClient so GcDbAccess has no dependency on Grouper, Grouper DAOs
 * can translate it into a GrouperStaleObjectStateException.
 */
@SuppressWarnings("serial")
public class GcStaleObjectException extends RuntimeException {

  /**
   *
   */
  public GcStaleObjectException() {
  }

  /**
   * @param message
   */
  public GcStaleObjectException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public GcStaleObjectException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public GcStaleObjectException(String message, Throwable cause) {
    super(message, cause);
  }

}
