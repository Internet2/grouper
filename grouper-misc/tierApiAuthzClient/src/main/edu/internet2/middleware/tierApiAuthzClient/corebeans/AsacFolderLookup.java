/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzClient.corebeans;


/**
 * <pre>
 * Class to refer a folder
 * 
 * </pre>
 * @author mchyzer
 */
public class AsacFolderLookup {

  /**
   * uuid of the stem to find
   */
  private String id;

  /** name of the stem to find (includes stems, e.g. stem1:stem2:stemName */
  private String name;

  /** handle name of a way to refer to a folder */
  private String handleName;
  
  /** handle value of a way to refer to a folder */
  private String handleValue;
  
  /**
   * handle name of a way to refer to a folder
   * @return the handleName
   */
  public String getHandleName() {
    return handleName;
  }
  
  /**
   * handle name of a way to refer to a folder
   * @param handleName the handleName to set
   */
  public void setHandleName(String handleName) {
    this.handleName = handleName;
  }
  
  /**
   * handle value of a way to refer to a folder
   * @return the handleValue
   */
  public String getHandleValue() {
    return handleValue;
  }
  
  /**
   * handle value of a way to refer to a folder
   * @param handleValue the handleValue to set
   */
  public void setHandleValue(String handleValue) {
    this.handleValue = handleValue;
  }

  /**
   * uuid of the stem to find
   * @return the uuid
   */
  public String getId() {
    return this.id;
  }

  /**
   * uuid of the stem to find
   * @param uuid1 the uuid to set
   */
  public void setId(String uuid1) {
    this.id = uuid1;
  }

  /**
   * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
   * @return the theName
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
   * @param theName the theName to set
   */
  public void setName(String theName) {
    this.name = theName;
  }

  /**
   * 
   */
  public AsacFolderLookup() {
    //blank
  }

  /**
   * construct with fields
   * @param name1
   * @param id1
   */
  public AsacFolderLookup(String name1, String id1) {
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
  public AsacFolderLookup(String name1, String id1, String handleName1, String handleValue1) {
    this.name = name1;
    this.id = id1;
    this.handleName = handleName1;
    this.handleValue = handleValue1;
  }

}
