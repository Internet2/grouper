package edu.internet2.middleware.grouper.app.usdu;


public class SubjectResolutionStat {
  
  /**
   * subject source 
   */
  private String source;
  
  /**
   * number of unresolved subjects
   */
  private long unresolvedCount;
  
  /**
   * number of resolved subjects
   */
  private long resolvedCount;
  
  public SubjectResolutionStat(String source, long unresolvedCount, long resolvedCount) {
    this.source = source;
    this.unresolvedCount = unresolvedCount;
    this.resolvedCount = resolvedCount;
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
   * @return number of unresolved subjects
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

}
