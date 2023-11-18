package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderChangeLogQuery;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;

public abstract class GrouperDataProviderChangeLogQueryConfig {

  public GrouperDataProviderChangeLogQueryConfig() {

  }

  private String configId;
  
  
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }
  
  private GrouperDataProviderChangeLogQuery grouperDataProviderChangeLogQuery;

  
  public GrouperDataProviderChangeLogQuery getGrouperDataProviderChangeLogQuery() {
    return grouperDataProviderChangeLogQuery;
  }

  
  public void setGrouperDataProviderChangeLogQuery(GrouperDataProviderChangeLogQuery grouperDataProviderChangeLogQuery) {
    this.grouperDataProviderChangeLogQuery = grouperDataProviderChangeLogQuery;
  }
  
  /**
   * 
   */
  public abstract void configureSpecificSettings();

  /**
   * 
   * @param configId
   * @param grouperConfig optional
   */
  public void configureGenericSettings(String configId, GrouperConfig grouperConfig) {
    
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    
    this.configId = configId;   
    this.providerConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderChangeLogQuery." + configId + ".providerConfigId");
    
    String providerChangeLogQueryTypeString = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderChangeLogQuery." + configId + ".providerChangeLogQueryType");
    GrouperDataProviderChangeLogQueryType grouperDataProviderQueryType = GrouperDataProviderChangeLogQueryType.valueOfIgnoreCase(providerChangeLogQueryTypeString, true);
    this.providerChangeLogQueryType = grouperDataProviderQueryType;
    
    this.providerChangeLogQuerySubjectIdAttribute = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderChangeLogQuery." + configId + ".providerChangeLogQuerySubjectIdAttribute");
    this.providerChangeLogQuerySubjectIdType = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderChangeLogQuery." + configId + ".providerChangeLogQuerySubjectIdType");
    this.providerChangeLogQuerySubjectSourceId = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderChangeLogQuery." + configId + ".providerChangeLogQuerySubjectSourceId");

    this.providerChangeLogQueryPrimaryKeyAttribute = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderChangeLogQuery." + configId + ".providerChangeLogQueryPrimaryKeyAttribute");
    this.providerChangeLogQueryTimestampAttribute = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderChangeLogQuery." + configId + ".providerChangeLogQueryTimestampAttribute");
  }

  /**
   * data provider config id
   */
  private String providerConfigId;

  
  
  /**
   * @return data provider config id
   */
  public String getProviderConfigId() {
    return providerConfigId;
  }

  /**
   * data provider config id
   * @param providerConfigId
   */
  public void setProviderConfigId(String providerConfigId) {
    this.providerConfigId = providerConfigId;
  }

  /**
   * data provider change log query type
   */
  private GrouperDataProviderChangeLogQueryType providerChangeLogQueryType;

  
  
  /**
   * @return data provider change log query type
   */
  public GrouperDataProviderChangeLogQueryType getProviderChangeLogQueryType() {
    return providerChangeLogQueryType;
  }

  /**
   * data provider change log query type
   * @param providerChangeLogQueryType
   */
  public void setProviderChangeLogQueryType(GrouperDataProviderChangeLogQueryType providerChangeLogQueryType) {
    this.providerChangeLogQueryType = providerChangeLogQueryType;
  }

  /**
   * Attribute which links this data to subjects
   */
  private String providerChangeLogQuerySubjectIdAttribute;

  
  
  /**
   * @return Attribute which links this data to subjects
   */
  public String getProviderChangeLogQuerySubjectIdAttribute() {
    return providerChangeLogQuerySubjectIdAttribute;
  }

  /**
   * Attribute which links this data to subjects
   * @param providerChangeLogQuerySubjectIdAttribute
   */
  public void setProviderChangeLogQuerySubjectIdAttribute(String providerChangeLogQuerySubjectIdAttribute) {
    this.providerChangeLogQuerySubjectIdAttribute = providerChangeLogQuerySubjectIdAttribute;
  }

  /**
   * Which type of subject id
   */
  private String providerChangeLogQuerySubjectIdType;

  
  
  /**
   * @return Which type of subject id
   */
  public String getProviderChangeLogQuerySubjectIdType() {
    return providerChangeLogQuerySubjectIdType;
  }

  /**
   * Which type of subject id
   * @param providerChangeLogQuerySubjectIdType
   */
  public void setProviderChangeLogQuerySubjectIdType(String providerChangeLogQuerySubjectIdType) {
    this.providerChangeLogQuerySubjectIdType = providerChangeLogQuerySubjectIdType;
  }

  /**
   * which subject source this is a subject id for
   */
  private String providerChangeLogQuerySubjectSourceId;
  
  
  
  /**
   * @return which subject source this is a subject id for
   */
  public String getProviderChangeLogQuerySubjectSourceId() {
    return providerChangeLogQuerySubjectSourceId;
  }

  /**
   * which subject source this is a subject id for
   * @param providerChangeLogQuerySubjectSourceId
   */
  public void setProviderChangeLogQuerySubjectSourceId(String providerChangeLogQuerySubjectSourceId) {
    this.providerChangeLogQuerySubjectSourceId = providerChangeLogQuerySubjectSourceId;
  }
  
  /**
   * Change log attribute that is the primary key
   */
  private String providerChangeLogQueryPrimaryKeyAttribute;
  
  
  
  /**
   * @return Change log attribute that is the primary key
   */
  public String getProviderChangeLogQueryPrimaryKeyAttribute() {
    return providerChangeLogQueryPrimaryKeyAttribute;
  }


  /**
   * Change log attribute that is the primary key
   * @param providerChangeLogQueryPrimaryKeyAttribute
   */
  public void setProviderChangeLogQueryPrimaryKeyAttribute(String providerChangeLogQueryPrimaryKeyAttribute) {
    this.providerChangeLogQueryPrimaryKeyAttribute = providerChangeLogQueryPrimaryKeyAttribute;
  }


  /**
   * Change log attribute that contains the timestamp for when this row was added, e.g. a timestamp or number field (number of millis since 1970)
   */
  private String providerChangeLogQueryTimestampAttribute;
  
  /**
   * @return Change log attribute that contains the timestamp for when this row was added, e.g. a timestamp or number field (number of millis since 1970)
   */
  public String getProviderChangeLogQueryTimestampAttribute() {
    return providerChangeLogQueryTimestampAttribute;
  }


  /**
   * Change log attribute that contains the timestamp for when this row was added, e.g. a timestamp or number field (number of millis since 1970)
   * @param providerChangeLogQueryTimestampAttribute
   */
  public void setProviderChangeLogQueryTimestampAttribute(String providerChangeLogQueryTimestampAttribute) {
    this.providerChangeLogQueryTimestampAttribute = providerChangeLogQueryTimestampAttribute;
  }
}
