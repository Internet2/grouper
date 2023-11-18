package edu.internet2.middleware.grouper.app.usdu;


public class SubjectResolutionStat {
  
  /**
   * subject source 
   */
  private String source;
  
  /**
   * number of unresolved subjects; this also includes the deleted count
   */
  private long unresolvedCount;
  
  /**
   * number of resolved subjects
   */
  private long resolvedCount;
  
  /**
   * number of subjects marked as deleted
   */
  private long deletedCount;
  
  
  public SubjectResolutionStat(String source, long unresolvedCount, long resolvedCount, long deletedCount) {
    this.source = source;
    this.unresolvedCount = unresolvedCount;
    this.resolvedCount = resolvedCount;
    this.deletedCount = deletedCount;
  }

  /**
   * 
   * @return subject source 
   */
  public String getSource() {
    return source;
  }

  /**
   * 
   * @return number of unresolved subjects; this also includes the deleted count
   */
  public long getUnresolvedCount() {
    return unresolvedCount;
  }

  /**
   * 
   * @return number of resolved subjects
   */
  public long getResolvedCount() {
    return resolvedCount;
  }

  /**
   * 
   * @return number of subjects marked as deleted
   */
  public long getDeletedCount() {
    return deletedCount;
  }
  
}
