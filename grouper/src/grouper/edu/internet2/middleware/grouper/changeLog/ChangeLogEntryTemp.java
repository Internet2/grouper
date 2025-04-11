package edu.internet2.middleware.grouper.changeLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouperClient.jdbc.GcDbVersionable;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;

@SuppressWarnings("serial")
@GcPersistableClass(tableName="grouper_change_log_entry_temp", defaultFieldPersist=GcPersist.doPersist)
public class ChangeLogEntryTemp extends ChangeLogEntry implements GcDbVersionable {

  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private String id;
  
  /**
   * optional sequence for ordering
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Long sequenceNumber;
  
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Long hibernateVersionNumber;

  
  public ChangeLogEntryTemp() {
  }
  
  /**
   * construct, assign an id
   * @param changeLogTypeIdentifier points to changeLog type
   * @param labelNamesAndValues alternate label name and value
   */
  public ChangeLogEntryTemp(ChangeLogTypeIdentifier changeLogTypeIdentifier, String... labelNamesAndValues) {
    super(true, changeLogTypeIdentifier, labelNamesAndValues);
  }
  
  public void storePrepare() {
    this.truncate();
    
    if (this.getCreatedOnDb() == null) {
      this.setCreatedOnDb(ChangeLogId.changeLogId());
    }
    if (StringUtils.isBlank(this.getContextId())) {
      this.setContextId(GrouperContext.retrieveContextId(true));
    }
    
    //assign id if not there
    if (StringUtils.isBlank(this.getId())) {
      this.setId(GrouperUuid.getUuid());
    }
  }
  
  public ChangeLogEntryTemp getDbVersion() {
    return this.dbVersion;
  }
  
  /**
   * version from db
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private ChangeLogEntryTemp dbVersion;
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public ChangeLogEntryTemp clone() {
  
    ChangeLogEntryTemp changeLogEntryTemp = new ChangeLogEntryTemp();
  
    //dbVersion  DONT CLONE
    changeLogEntryTemp.setId(this.getId());
    changeLogEntryTemp.setChangeLogTypeId(this.getChangeLogTypeId());
    changeLogEntryTemp.setContextId(this.getContextId());
    changeLogEntryTemp.setString01(this.getString01());
    changeLogEntryTemp.setString02(this.getString02());
    changeLogEntryTemp.setString03(this.getString03());
    changeLogEntryTemp.setString04(this.getString04());
    changeLogEntryTemp.setString05(this.getString05());
    changeLogEntryTemp.setString06(this.getString06());
    changeLogEntryTemp.setString07(this.getString07());
    changeLogEntryTemp.setString08(this.getString08());
    changeLogEntryTemp.setString09(this.getString09());
    changeLogEntryTemp.setString10(this.getString10());
    changeLogEntryTemp.setString11(this.getString11());
    changeLogEntryTemp.setString12(this.getString12());
    changeLogEntryTemp.setCreatedOnDb(this.getCreatedOnDb());

    return changeLogEntryTemp;
  }

  /**
   *
   */
  public boolean equalsDeep(Object obj) {
    if (this==obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ChangeLogEntryTemp)) {
      return false;
    }
    ChangeLogEntryTemp other = (ChangeLogEntryTemp) obj;

    return new EqualsBuilder()
        //dbVersion  DONT EQUALS
        .append(this.getCreatedOnDb(), other.getCreatedOnDb())
        .append(this.getId(), other.getId())
        .append(this.getChangeLogTypeId(), other.getChangeLogTypeId())
        .append(this.getContextId(), other.getContextId())
        .append(this.getString01(), other.getString01())
        .append(this.getString02(), other.getString02())
        .append(this.getString03(), other.getString03())
        .append(this.getString04(), other.getString04())
        .append(this.getString05(), other.getString05())
        .append(this.getString06(), other.getString06())
        .append(this.getString07(), other.getString07())
        .append(this.getString08(), other.getString08())
        .append(this.getString09(), other.getString09())
        .append(this.getString10(), other.getString10())
        .append(this.getString11(), other.getString11())
        .append(this.getString12(), other.getString12())
        .isEquals();
  }
  
  /**
   * db version
   */
  @Override
  public void dbVersionDelete() {
    this.dbVersion = null;
  }
  /**
   * if we need to update this object
   * @return if needs to update this object
   */
  @Override
  public boolean dbVersionDifferent() {
    return !this.equalsDeep(this.dbVersion);
  }
  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = this.clone();
  }
  
  /**
   * uuid for temp object
   * @return uuid for temp object
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * set uuid for temp object
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }
}
