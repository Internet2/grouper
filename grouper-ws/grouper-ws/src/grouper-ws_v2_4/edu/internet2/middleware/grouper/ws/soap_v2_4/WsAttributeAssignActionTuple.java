package edu.internet2.middleware.grouper.ws.soap_v2_4;

/**
 * an item in the attribute assign action result list
 * 
 * @author vsachdeva
 */
public class WsAttributeAssignActionTuple {
	
  /** action name in the attribute definition **/
  private String action;
	
  /** attribute definition id **/
  private String attributeDefId;
    
  /** name of the attribute definition **/
  private String nameOfAttributeDef;
    
  /**
   * @param action1
   * @param attributeDefId1
   * @param nameOfAttributeDef1
   */
  public WsAttributeAssignActionTuple(String action1, String attributeDefId1, String nameOfAttributeDef1) {
  	this.action = action1;
   	this.attributeDefId = attributeDefId1;
   	this.nameOfAttributeDef = nameOfAttributeDef1;
  }
    
  /**
   *  needed for marshalling 
   */
  public WsAttributeAssignActionTuple() {}
    
  /**
   * action in the attribute definition
   * @param action1
   */
  public void setAction(String action1) {
	this.action = action1;
  }
	
  /**
   * @return action
   */
  public String getAction() {
	return this.action;
  }

  /**
   * attribute definition id
   * @param attributeDefId1
   */
  public void setAttributeDefId(String attributeDefId1) {
	this.attributeDefId = attributeDefId1;
  }

  /**
   * @return attributeDefId
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  /**
   * name of attribute definition
   * @param nameOfAttributeDef1
   */
  public void setNameOfAttributeDef(String nameOfAttributeDef1) {
	this.nameOfAttributeDef = nameOfAttributeDef1;
  }

	
  /**
   * @return nameOfAttributeDef
   */
  public String getNameOfAttributeDef() {
	return this.nameOfAttributeDef;
  }    

}
