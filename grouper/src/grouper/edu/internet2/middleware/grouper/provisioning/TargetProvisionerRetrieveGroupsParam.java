package edu.internet2.middleware.grouper.provisioning;

import java.util.Collection;

/**
 * retrieve group
 * @author mchyzer
 *
 */
public class TargetProvisionerRetrieveGroupsParam {

  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   */
  private Collection<String> ids;

  /**
   * name of group in target system.  could be group system name, extension, or other
   */
  private Collection<String> names;
  
  /**
   * id index in target (optional)
   */
  private Collection<Long> idIndexes;

  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   * @return id
   */
  public Collection<String> getIds() {
    return this.ids;
  }

  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   * @param id1
   */
  public void setIds(Collection<String> ids1) {
    this.ids = ids1;
  }

  /**
   * name of group in target system.  could be group system name, extension, or other
   * @return name
   */
  public Collection<String> getName() {
    return this.names;
  }

  /**
   * name of group in target system.  could be group system name, extension, or other
   * @param name1
   */
  public void setName(Collection<String> names1) {
    this.names = names1;
  }

  /**
   * id index in target (optional)
   * @return
   */
  public Collection<Long> getIdIndexes() {
    return idIndexes;
  }

  /**
   * id index in target (optional)
   * @param idIndex
   */
  public void setIdIndex(Collection<Long> idIndex1) {
    this.idIndexes = idIndex1;
  }


  
}
