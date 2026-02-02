package edu.internet2.middleware.grouperClient.jdbc;

public interface GcDbVersionable {

  /**
   * if we need to update this object
   * @return if needs to update this object
   */
  public boolean dbVersionDifferent();
  
  /**
   * keep a copy of the state in the DB so we know if we need to store again
   */
  public void dbVersionReset();

  /**
   * record was deleted
   */
  public void dbVersionDelete();

}
