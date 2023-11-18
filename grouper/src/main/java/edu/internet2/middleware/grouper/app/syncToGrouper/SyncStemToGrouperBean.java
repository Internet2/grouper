package edu.internet2.middleware.grouper.app.syncToGrouper;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;

@GcPersistableClass(tableName="testgrouper_syncgr_stem", defaultFieldPersist=GcPersist.doPersist)
public class SyncStemToGrouperBean {

  private String displayName;
  
  public String getDisplayName() {
    return displayName;
  }

  /**
   * 
   */
  private String alternateName;

  private String description;

  private Long idIndex;

  
  public String getAlternateName() {
    return alternateName;
  }

  
  public String getDescription() {
    return description;
  }

  
  public Long getIdIndex() {
    return idIndex;
  }

  public SyncStemToGrouperBean() {
  }

  /**
   * 
   * @param name
   */
  public SyncStemToGrouperBean(String name) {
    super();
    this.name = name;
  }

  /**
   * stem name
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private String name;

  /**
   * stem name
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * stem name
   * @param name
   */
  public SyncStemToGrouperBean assignName(String name) {
    this.name = name;
    return this;
  }

  /**
   * 
   */
  private String id;

  
  public String getId() {
    return id;
  }

  public SyncStemToGrouperBean assignAlternateName(String alternateName1) {
    this.alternateName = alternateName1;
    return this;
  }
  
  public SyncStemToGrouperBean assignId(String uuid) {
    this.id = uuid;
    return this;
  }

  public SyncStemToGrouperBean assignIdIndex(Long idIndex1) {
    this.idIndex = idIndex1;
    return this;
  }

  public SyncStemToGrouperBean assignDescription(String description1) {
    this.description = description1;
    return this;
  }

  public SyncStemToGrouperBean assignDisplayName(String displayName1) {
    this.displayName = displayName1;
    return this;
  }

  public void store() {
    new GcDbAccess().storeToDatabase(this);
  }
  
  
}
