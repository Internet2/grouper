package edu.internet2.middleware.grouper.provisioning;

/**
 * retrieve group
 * @author mchyzer
 *
 */
public class TargetProvisionerRetrieveGroupParam {

  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   */
  private String id;

  /**
   * name of group in target system.  could be group system name, extension, or other
   */
  private String name;
  
  /**
   * id index in target (optional)
   */
  private Long idIndex;

  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * name of group in target system.  could be group system name, extension, or other
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of group in target system.  could be group system name, extension, or other
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * id index in target (optional)
   * @return
   */
  public Long getIdIndex() {
    return idIndex;
  }

  /**
   * id index in target (optional)
   * @param idIndex
   */
  public void setIdIndex(Long idIndex) {
    this.idIndex = idIndex;
  }


  
}
