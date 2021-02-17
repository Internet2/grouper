package edu.internet2.middleware.grouper.app.syncToGrouper;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;

@GcPersistableClass(tableName="testgrouper_syncgr_composite", defaultFieldPersist=GcPersist.doPersist)
public class SyncCompositeToGrouperBean implements GcSqlAssignPrimaryKey {

  public SyncCompositeToGrouperBean() {
  }

  private String ownerName;
  
  public String getOwnerName() {
    return ownerName;
  }

  /**
   * 
   * @param ownerName1
   * @return this for chaining
   */
  public SyncCompositeToGrouperBean assignOwnerName(String ownerName1) {
    this.ownerName = ownerName1;
    return this;
  }
  
  private String leftFactorName;

  public String getLeftFactorName() {
    return leftFactorName;
  }

  public SyncCompositeToGrouperBean assignLeftFactorName(String leftFactorName1) {
    this.leftFactorName = leftFactorName1;
    return this;
  }
  
  private String rightFactorName;
  
  public String getRightFactorName() {
    return rightFactorName;
  }

  public SyncCompositeToGrouperBean assignRightFactorName(String rightFactorName1) {
    this.rightFactorName = rightFactorName1;
    return this;
  }

  private String type;
  
  public String getType() {
    return type;
  }

  public SyncCompositeToGrouperBean assignType(String theType) {
    this.type = theType;
    return this;
  }
  
  /**
   * if inserting and not wanting an assigned id
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private String idForInsert;
  
  public SyncCompositeToGrouperBean assignIdForInsert(String theId) {
    this.idForInsert = theId;
    return this;
  }
  
  
  /**
   * composite id
   */
  @GcPersistableField(primaryKey=true)
  private String id;

  
  public String getId() {
    return id;
  }

  public SyncCompositeToGrouperBean assignId(String id1) {
    this.id = id1;
    return this;
  }

  public void store() {
    new GcDbAccess().storeToDatabase(this);
  }

  @Override
  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
    if (StringUtils.isBlank(this.id)) {
      if (!StringUtils.isBlank(this.idForInsert)) {
        this.id = this.idForInsert;
      } else {
        this.id = GrouperUuid.getUuid();
      }
      return true;
    }
    return false;
  }
  
  
}
