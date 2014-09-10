package edu.internet2.middleware.grouperVoot.beans;

/**
 * response for get groups request
 * @author mchyzer
 *
 */
public class VootGetGroupsResponse extends VootResponse {
  
  /**
   * result body
   */
  private VootGroup[] entry;

  /**
   * results
   * @return the results
   */
  public VootGroup[] getEntry() {
    return this.entry;
  }

  /**
   * results
   * @param entry1
   */
  public void setEntry(VootGroup[] entry1) {
    this.entry = entry1;
  }
  
  
}
