/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups;


/**
 * <pre>
 * Class to refer a group
 * 
 * </pre>
 * @author mchyzer
 */
public class AsasApiGroupLookup {

  /**
   * uuid of the group to find
   */
  private String id;

  /** name of the group to find (includes stems, e.g. stem1:stem2:groupName */
  private String name;

  /** handle name of a way to refer to a group */
  private String handleName;
  
  /** handle value of a way to refer to a group */
  private String handleValue;
  
  /**
   * handle name of a way to refer to a group
   * @return the handleName
   */
  public String getHandleName() {
    return handleName;
  }
  
  /**
   * handle name of a way to refer to a group
   * @param handleName the handleName to set
   */
  public void setHandleName(String handleName) {
    this.handleName = handleName;
  }
  
  /**
   * handle value of a way to refer to a group
   * @return the handleValue
   */
  public String getHandleValue() {
    return handleValue;
  }
  
  /**
   * handle value of a way to refer to a group
   * @param handleValue the handleValue to set
   */
  public void setHandleValue(String handleValue) {
    this.handleValue = handleValue;
  }

  /**
   * uuid of the group to find
   * @return the uuid
   */
  public String getId() {
    return this.id;
  }

  /**
   * uuid of the group to find
   * @param uuid1 the uuid to set
   */
  public void setId(String uuid1) {
    this.id = uuid1;
  }

  /**
   * name of the group to find (includes stems, e.g. stem1:stem2:stemName
   * @return the theName
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of the group to find (includes stems, e.g. stem1:stem2:stemName
   * @param theName the theName to set
   */
  public void setName(String theName) {
    this.name = theName;
  }

  /**
   * 
   */
  public AsasApiGroupLookup() {
    //blank
  }

  /**
   * construct with fields
   * @param name1
   * @param id1
   */
  public AsasApiGroupLookup(String name1, String id1) {
    this(name1, id1, null, null);
    this.name = name1;
    this.id = id1;
  }

  /**
   * construct with fields
   * @param name1
   * @param id1
   * @param handleName1
   * @param handleValue1
   */
  public AsasApiGroupLookup(String name1, String id1, String handleName1, String handleValue1) {
    this.name = name1;
    this.id = id1;
    this.handleName = handleName1;
    this.handleValue = handleValue1;
  }

}
