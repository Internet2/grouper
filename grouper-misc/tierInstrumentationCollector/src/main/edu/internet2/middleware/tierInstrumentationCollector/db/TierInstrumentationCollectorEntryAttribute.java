package edu.internet2.middleware.tierInstrumentationCollector.db;

import java.sql.Timestamp;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * attribute of entry
 * @author mchyzer
 *
 */
@GcPersistableClass(tableName="tic_entry_attr", defaultFieldPersist=GcPersist.doPersist)
public class TierInstrumentationCollectorEntryAttribute {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TierInstrumentationCollectorEntryAttribute entryAttr = new TierInstrumentationCollectorEntryAttribute();
    entryAttr.setUuid(GrouperClientUtils.uuid());
    entryAttr.setAttributeName("someAttr");
    entryAttr.setAttributeType("string");
    entryAttr.setAttributeValueFloating(1.2);
    entryAttr.setAttributeValueInteger(4321L);
    entryAttr.setAttributeValueString("abc");
    entryAttr.setAttributeValueTimestamp(new Timestamp(System.currentTimeMillis()));
    entryAttr.setEntryUuid("da46ea35ed8e4ef183c88eb2bb4b7f6a");
    new GcDbAccess().storeToDatabase(entryAttr);
  }

  /**
   * uuid of this record
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private String uuid;

  /**
   * uuid of the entry
   */
  private String entryUuid;

  /**
   * attribute name
   */
  private String attributeName;
  
  /**
   * boolean_type, floating_type, integer_type, string_type, timestamp_type
   */
  private String attributeType;
  
  /**
   * string attribute value
   */
  private String attributeValueString;

  /**
   * integer attribute value
   */
  private Long attributeValueInteger;

  /**
   * floating attribute value
   */
  private Double attributeValueFloating;
  
  /**
   * timestamp attribute value
   */
  private Timestamp attributeValueTimestamp;

  /**
   * timestamp attribute value
   * @return timestamp
   */
  public Timestamp getAttributeValueTimestamp() {
    return this.attributeValueTimestamp;
  }

  /**
   * timestamp attribute value
   * @param attributeValueTimestamp1
   */
  public void setAttributeValueTimestamp(Timestamp attributeValueTimestamp1) {
    this.attributeValueTimestamp = attributeValueTimestamp1;
  }


  /**
   * uuid of this record
   * @return uuid
   */
  public String getUuid() {
    return this.uuid;
  }


  /**
   * uuid of this record
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * uuid of the entry
   * @return entry uuid
   */
  public String getEntryUuid() {
    return this.entryUuid;
  }

  /**
   * uuid of the entry
   * @param entryUuid1
   */
  public void setEntryUuid(String entryUuid1) {
    this.entryUuid = entryUuid1;
  }

  /**
   * attribute name
   * @return attribute name
   */
  public String getAttributeName() {
    return this.attributeName;
  }

  /**
   * attribute name
   * @param attributeName1
   */
  public void setAttributeName(String attributeName1) {
    this.attributeName = attributeName1;
  }

  /**
   * boolean_type, floating_type, integer_type, string_type, timestamp_type
   * @return type
   */
  public String getAttributeType() {
    return this.attributeType;
  }

  /**
   * boolean_type, floating_type, integer_type, string_type, timestamp_type
   * @param attributeType1
   */
  public void setAttributeType(String attributeType1) {
    this.attributeType = attributeType1;
  }

  /**
   * string attribute value
   * @return value
   */
  public String getAttributeValueString() {
    return this.attributeValueString;
  }

  /**
   * string attribute value
   * @param attributeValueString1
   */
  public void setAttributeValueString(String attributeValueString1) {
    this.attributeValueString = attributeValueString1;
  }

  /**
   * integer attribute value
   * @return value
   */
  public Long getAttributeValueInteger() {
    return this.attributeValueInteger;
  }

  /**
   * integer attribute value
   * @param attributeValueLong1
   */
  public void setAttributeValueInteger(Long attributeValueLong1) {
    this.attributeValueInteger = attributeValueLong1;
  }

  /**
   * floating attribute value
   * @return value
   */
  public Double getAttributeValueFloating() {
    return this.attributeValueFloating;
  }

  /**
   * floating attribute value
   * @param attributeValueFloating1
   */
  public void setAttributeValueFloating(Double attributeValueFloating1) {
    this.attributeValueFloating = attributeValueFloating1;
  }
  
  
}
