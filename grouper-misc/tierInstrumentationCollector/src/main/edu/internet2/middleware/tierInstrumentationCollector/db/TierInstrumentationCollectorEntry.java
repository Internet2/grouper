package edu.internet2.middleware.tierInstrumentationCollector.db;

import java.sql.Timestamp;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * entry in database
 * @author mchyzer
 *
 */
@GcPersistableClass(tableName="tic_entry", defaultFieldPersist=GcPersist.doPersist)
public class TierInstrumentationCollectorEntry {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TierInstrumentationCollectorEntry entry = new TierInstrumentationCollectorEntry();
    entry.setUuid(GrouperClientUtils.uuid());
    entry.setComponent("grouper");
    entry.setVersion("2.3.0");
    entry.setReportFormat(1L);
    entry.setEnvironment("prod");
    entry.setInstitution("Penn");
    entry.setTheTimestamp(new Timestamp(System.currentTimeMillis()));
    new GcDbAccess().storeToDatabase(entry);
  }
  
  /**
   * uuid of entry
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private String uuid;

  /**
   * timestamp of entry
   */
  private Timestamp theTimestamp;

  /**
   * uuid of entry
   * @return uuid
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * uuid of entry
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * uuid of entry
   * @return uuid
   */
  public Timestamp getTheTimestamp() {
    return theTimestamp;
  }

  /**
   * uuid of entry
   * @param theTimestamp1
   */
  public void setTheTimestamp(Timestamp theTimestamp1) {
    this.theTimestamp = theTimestamp1;
  }
  
  /**
   * component name, e.g. grouper
   */
  private String component;

  /**
   * version of entry, e.g. 1
   */
  private Long reportFormat;
  
  /**
   * component name, e.g. grouper
   * @return name
   */
  public String getComponent() {
    return this.component;
  }

  /**
   * component name, e.g. grouper
   * @param componentName1
   */
  public void setComponent(String componentName1) {
    this.component = componentName1;
  }

  /**
   * version of entry, e.g. 1
   * @return version
   */
  public Long getReportFormat() {
    return this.reportFormat;
  }

  /**
   * version of entry, e.g. 1
   * @param entryVersion1
   */
  public void setReportFormat(Long entryVersion1) {
    this.reportFormat = entryVersion1;
  }

  /**
   * which institution this env is running
   */
  private String institution;

  /**
   * which environment e.g. dev/test/prod
   */
  private String environment;

  /**
   * which institution this env is running
   * @return inst
   */
  public String getInstitution() {
    return this.institution;
  }

  /**
   * which institution this env is running
   * @param institution1
   */
  public void setInstitution(String institution1) {
    this.institution = institution1;
  }

  /**
   * which environment e.g. dev/test/prod
   * @return env
   */
  public String getEnvironment() {
    return environment;
  }

  /**
   * which environment e.g. dev/test/prod
   * @param environment1
   */
  public void setEnvironment(String environment1) {
    this.environment = environment1;
  }
  
  /**
   * version of the component
   */
  private String version;

  /**
   * version of the component
   * @return version
   */
  public String getVersion() {
    return this.version;
  }

  /**
   * version of the component
   * @param componentVersion1
   */
  public void setVersion(String componentVersion1) {
    this.version = componentVersion1;
  }
  
  
}
