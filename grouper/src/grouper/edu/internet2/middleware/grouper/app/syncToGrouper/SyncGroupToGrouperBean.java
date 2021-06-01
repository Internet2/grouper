package edu.internet2.middleware.grouper.app.syncToGrouper;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;

@GcPersistableClass(tableName="testgrouper_syncgr_group", defaultFieldPersist=GcPersist.doPersist)
public class SyncGroupToGrouperBean {

  /**
   * disabled time from 1970
   */
  private Long disabledTimestamp;

  /**
   * disabled time from 1970
   * @param disabledTimestamp1
   * @return 
   */
  public SyncGroupToGrouperBean assignDisabledTimestamp(Long disabledTimestamp1) {
    this.disabledTimestamp = disabledTimestamp1;
    return this;
  }
  
  /**
   * enabled time from 1970
   */
  private Long enabledTimestamp;
  
  /**
   * 
   * @param enabledTimestamp1
   * @return this for chaining
   */
  public SyncGroupToGrouperBean assignEnabledTimestamp(Long enabledTimestamp1) {
    this.enabledTimestamp = enabledTimestamp1;
    return this;
  }
  
  /**
   * disabled time since 1970
   * @return
   */
  public Long getDisabledTimestamp() {
    return disabledTimestamp;
  }

  /**
   * enabled time from 1970
   * @return
   */
  public Long getEnabledTimestamp() {
    return enabledTimestamp;
  }

  /**
   * group, role, entity
   */
  private String typeOfGroup;
  
  /**
   * group, role, entity
   * @return
   */
  public String getTypeOfGroup() {
    return typeOfGroup;
  }

  /**
   * group, role, entity
   * @param typeOfGroup
   */
  public SyncGroupToGrouperBean assignTypeOfGroup(String typeOfGroup) {
    this.typeOfGroup = typeOfGroup;
    return this;
  }

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

  public SyncGroupToGrouperBean() {
  }

  /**
   * 
   * @param name
   */
  public SyncGroupToGrouperBean(String name) {
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
  public SyncGroupToGrouperBean assignName(String name) {
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

  public SyncGroupToGrouperBean assignAlternateName(String alternateName1) {
    this.alternateName = alternateName1;
    return this;
  }
  
  public SyncGroupToGrouperBean assignId(String uuid) {
    this.id = uuid;
    return this;
  }

  public SyncGroupToGrouperBean assignIdIndex(Long idIndex1) {
    this.idIndex = idIndex1;
    return this;
  }

  public SyncGroupToGrouperBean assignDescription(String description1) {
    this.description = description1;
    return this;
  }

  public SyncGroupToGrouperBean assignDisplayName(String displayName1) {
    this.displayName = displayName1;
    return this;
  }

  public void store() {
    new GcDbAccess().storeToDatabase(this);
  }
  
  
}
