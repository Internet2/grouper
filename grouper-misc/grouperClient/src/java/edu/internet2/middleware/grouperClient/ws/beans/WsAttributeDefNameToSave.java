/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;



/**
 * <pre>
 * Class to save an attribute def name via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsAttributeDefNameToSave {

  /** attribute def name lookup (blank if insert) */
  private WsAttributeDefNameLookup wsAttributeDefNameLookup;

  /** attribute def name to save */
  private WsAttributeDefName wsAttributeDefName;

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
  public WsAttributeDefNameToSave() {
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
   * attribute def name lookup (blank if insert)
   * @return the wsAttributeDefNameLookup
   */
  public WsAttributeDefNameLookup getWsAttributeDefNameLookup() {
    return this.wsAttributeDefNameLookup;
  }

  /**
   * attribute def name lookup (blank if insert)
   * @param wsAttributeDefNameLookup1 the wsAttributeDefNameLookup to set
   */
  public void setWsAttributeDefNameLookup(WsAttributeDefNameLookup wsAttributeDefNameLookup1) {
    this.wsAttributeDefNameLookup = wsAttributeDefNameLookup1;
  }

  /**
   * attribute def name to save
   * @return the wsAttributeDefName
   */
  public WsAttributeDefName getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }

  /**
   * attribute def name to save
   * @param wsAttributeDefName1 the wsAttributeDefName to set
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefName1) {
    this.wsAttributeDefName = wsAttributeDefName1;
  }
}
