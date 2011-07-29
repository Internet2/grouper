/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;


/**
 * <pre>
 * Class to save a group via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsGroupToSave {

  /** stem lookup (blank if insert) */
  private WsGroupLookup wsGroupLookup;

  /** stem to save */
  private WsGroup wsGroup;

  /** T or F (null if F) */
  private String createParentStemsIfNotExist;
  
  /**
   * if should create parent stems if not exist
   * @return T or F or null (F)
   */
  public String getCreateParentStemsIfNotExist() {
    return this.createParentStemsIfNotExist;
  }

  /**
   * if should create parent stems if not exist
   * @param createParentStemsIfNotExist1 T or F or null (F)
   */
  public void setCreateParentStemsIfNotExist(String createParentStemsIfNotExist1) {
    this.createParentStemsIfNotExist = createParentStemsIfNotExist1;
  }

  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private String saveMode;

  /**
   * 
   */
  public WsGroupToSave() {
    // empty constructor
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @return the saveMode
   */
  public String getSaveMode() {
    return this.saveMode;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param saveMode1 the saveMode to set
   */
  public void setSaveMode(String saveMode1) {
    this.saveMode = saveMode1;
  }

  /**
   * @return the wsGroupLookup
   */
  public WsGroupLookup getWsGroupLookup() {
    return this.wsGroupLookup;
  }

  /**
   * @param wsGroupLookup1 the wsGroupLookup to set
   */
  public void setWsGroupLookup(WsGroupLookup wsGroupLookup1) {
    this.wsGroupLookup = wsGroupLookup1;
  }

  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }
}
