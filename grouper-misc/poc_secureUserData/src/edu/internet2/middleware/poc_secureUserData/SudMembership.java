/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.poc_secureUserData;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.poc_secureUserData.util.GcEqualsBuilder;
import edu.internet2.middleware.poc_secureUserData.util.GcHashCodeBuilder;
import edu.internet2.middleware.poc_secureUserData.util.GcDbUtils;
import edu.internet2.middleware.poc_secureUserData.util.GcDbUtils.DbType;


/**
 * holds membership data
 */
public class SudMembership {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    //should be blank:
    
    System.out.println("Should be blank: ");
    for (SudMembership sudMembership : SudMembership.retrieveAllMemberships()) {
      System.out.println(sudMembership);
    }
    
    System.out.println("Insert: ");
    
    SudMembership theSudMembership = new SudMembership();
    theSudMembership.setGroupExtension("students");
    theSudMembership.setPersonid("12345");
    theSudMembership.store();
    
    for (SudMembership sudMembership : SudMembership.retrieveAllMemberships()) {
      System.out.println(sudMembership);
    }
    
    System.out.println("Update: ");
    
    theSudMembership.setGroupExtension("faculty");
    theSudMembership.store();
    
    for (SudMembership sudMembership : SudMembership.retrieveAllMemberships()) {
      System.out.println(sudMembership);
    }

    System.out.println("Delete (should be blank): ");
    
    theSudMembership.delete();
    
    for (SudMembership sudMembership : SudMembership.retrieveAllMemberships()) {
      System.out.println(sudMembership);
    }
  }
  
  /**
   * @see Object#equals(Object) 
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SudMembership)) {
      return false;
    }
    
    SudMembership other = (SudMembership)obj;
    
    return new GcEqualsBuilder().append(this.groupExtension, other.groupExtension)
      .append(this.personid, other.personid).isEquals();
  }

  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new GcHashCodeBuilder().append(this.groupExtension).append(this.personid).toHashCode();
  }

  /**
   * memberships
   * @return all memberships
   */
  public static List<SudMembership> retrieveAllMemberships() {
    
    List<Object[]> rows = GcDbUtils.listSelect(Object[].class, "select id, group_extension, personid from secureuserdata_memberships", 
        GrouperClientUtils.toList(DbType.STRING, DbType.STRING, DbType.STRING));
    
    List<SudMembership> results = new ArrayList<SudMembership>();
    
    for (Object[] row : rows) {
      
      SudMembership sudMembership = new SudMembership();
      results.add(sudMembership);
      
      sudMembership.setId((String)row[0]);
      sudMembership.setGroupExtension((String)row[1]);
      sudMembership.setPersonid((String)row[2]);
      
    }
    return results;
  }
  
  /**
   * memberships
   * @param groupExtension group extension
   * @return all memberships
   */
  public static List<SudMembership> retrieveAllInGroup(String groupExtension) {
    
    List<Object[]> rows = GcDbUtils.listSelect(Object[].class, 
        "select id, group_extension, personid from secureuserdata_memberships where group_extension = ?", 
        GrouperClientUtils.toList(DbType.STRING, DbType.STRING, DbType.STRING), 
        GrouperClientUtils.toList((Object)groupExtension));
    
    List<SudMembership> results = new ArrayList<SudMembership>();
    
    for (Object[] row : rows) {
      
      SudMembership sudMembership = new SudMembership();
      results.add(sudMembership);
      
      sudMembership.setId((String)row[0]);
      sudMembership.setGroupExtension((String)row[1]);
      sudMembership.setPersonid((String)row[2]);
      
    }
    return results;
  }
  
  /**
   * insert/update into database (if there is an ID, then update, if not, then store)
   * @return true if it happened
   */
  public boolean store() {
    
    if (GrouperClientUtils.isBlank(this.id)) {
      //TODO make this a uuid
      this.id = GrouperClientUtils.uniqueId();

      //insert
      return GcDbUtils.executeUpdate("insert into secureuserdata_memberships (id, group_extension, personid) values (?, ?, ?)", 
          GrouperClientUtils.toList((Object)this.id, this.groupExtension, this.personid)) > 0;
      
    }
    
    //update
    return GcDbUtils.executeUpdate("update secureuserdata_memberships set group_extension = ?, personid = ? where id = ?", 
        GrouperClientUtils.toList((Object)this.groupExtension, this.personid, this.id)) > 0;
    
  }
  
  /**
   * delete from database
   * @return the rowcount
   */
  public int delete() {
    
    if (GrouperClientUtils.isBlank(this.id)) {
      throw new RuntimeException("Why are you deleting something with null id? " + this);
    }
    
    //delete
    return GcDbUtils.executeUpdate("delete from secureuserdata_memberships where id = ?", 
        GrouperClientUtils.toList((Object)this.id));
    
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "SudMembership [groupExtension=" + this.groupExtension
        + ", id=" + this.id + ", personid=" + this.personid + "]";
  }

  /** uuid */
  private String id;

  /**
   * uuid
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  /**
   * uuid
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }
  
  /** group extension in a certain folder in grouper */
  private String groupExtension;

  
  /**
   * group extension in a certain folder in grouper
   * @return the group extension
   */
  public String getGroupExtension() {
    return this.groupExtension;
  }

  
  /**
   * group extension in a certain folder in grouper
   * @param groupExtension1 the group extension to set
   */
  public void setGroupExtension(String groupExtension1) {
    this.groupExtension = groupExtension1;
  }
  
  /** the personid in the group */
  private String personid;

  
  /**
   * the personid in the group
   * @return the personid
   */
  public String getPersonid() {
    return this.personid;
  }

  
  /**
   * the personid in the group
   * @param personid1 the personid to set
   */
  public void setPersonid(String personid1) {
    this.personid = personid1;
  }
  
  
  
}
