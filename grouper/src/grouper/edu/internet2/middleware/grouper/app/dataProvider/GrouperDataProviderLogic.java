package edu.internet2.middleware.grouper.app.dataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssign;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldStructure;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataMemberWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataProvider;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryFieldMappingType;
import edu.internet2.middleware.grouper.dataField.GrouperDataRow;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssign;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssign;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssignDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssignWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowWrapper;
import edu.internet2.middleware.grouper.dictionary.GrouperDictionaryDao;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 */
public class GrouperDataProviderLogic {
  
  private GrouperDataProviderSync grouperDataProviderSync;

  public void setGrouperDataProviderSync(GrouperDataProviderSync grouperDataProviderSync) {
    this.grouperDataProviderSync = grouperDataProviderSync;
  }

  /**
   * 
   */
  public void syncFull() {
    String dataProviderConfigId = grouperDataProviderSync.getConfigId();
    
    if (grouperDataProviderSync.getGrouperDataEngine() == null) {
      grouperDataProviderSync.setGrouperDataEngine(new GrouperDataEngine());
    }
    
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    GrouperDataEngine.syncDataProviders(grouperConfig);
    GrouperDataEngine.syncDataFields(grouperConfig);
    GrouperDataEngine.syncDataRows(grouperConfig);
    GrouperDataEngine.syncDataAliases(grouperConfig);

    GrouperDataProvider grouperDataProvider = GrouperDataProviderDao.selectByText(dataProviderConfigId);

    dataEngine.loadFieldsAndRows(grouperConfig);

    // maybe things in DB arent in sync with the config yet
    if (!dataEngine.getProviderConfigByConfigId().containsKey(dataProviderConfigId)) {
      grouperDataProviderSync.getDebugMap().put("dataProviderConfigNotFound", dataProviderConfigId);
      return;
    }

    // wrapper object for fields, rows, and columns

    // get all dictionary text for field and row assignments for this data provider
    dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().putAll(GrouperDictionaryDao.selectByDataProvider(grouperDataProvider.getInternalId()));
    
    // get all the members that are assigned in a data provider to fields or rows
    for (Long memberInternalId : GrouperUtil.nonNull(GrouperDAOFactory.getFactory().getMember().selectByDataProvider(grouperDataProvider.getInternalId()))) {

      GrouperDataMemberWrapper grouperDataMemberWrapper = new GrouperDataMemberWrapper(dataEngine, memberInternalId);
      dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().put(memberInternalId, grouperDataMemberWrapper);
    }
    
    {
      // get field assignments in the database for this provider
      List<GrouperDataFieldAssign> grouperDataFieldAssigns = GrouperDataFieldAssignDao.selectByProvider(grouperDataProvider.getInternalId());
  
      for (GrouperDataFieldAssign grouperDataFieldAssign : grouperDataFieldAssigns) {
        GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper = new GrouperDataFieldAssignWrapper(dataEngine, grouperDataFieldAssign);
        
        dataEngine.getGrouperDataProviderIndex().getFieldAssignWrapperByInternalId().put(grouperDataFieldAssign.getInternalId(), grouperDataFieldAssignWrapper);
        
        grouperDataFieldAssignWrapper.setGrouperDataFieldWrapper(dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(grouperDataFieldAssign.getDataFieldInternalId()));

        grouperDataFieldAssignWrapper.setMemberWrapper(dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(grouperDataFieldAssign.getMemberInternalId()));

        if (grouperDataFieldAssign.getValueDictionaryInternalId() != null) {
          // TODO fix race conditions here
          String textValue = dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().get(grouperDataFieldAssign.getValueDictionaryInternalId());
          GrouperUtil.assertion(!StringUtils.isBlank(textValue), "Cant find text: " + grouperDataFieldAssign.getValueDictionaryInternalId());
          grouperDataFieldAssignWrapper.setTextValue(textValue);
        }
      }
    }
    
    {
      // get row assignments in the database for this provider
      List<GrouperDataRowAssign> grouperDataRowAssigns = GrouperUtil.nonNull(GrouperDataRowAssignDao.selectByProvider(grouperDataProvider.getInternalId()));
      
      for (GrouperDataRowAssign grouperDataRowAssign : grouperDataRowAssigns) {
        GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = new GrouperDataRowAssignWrapper(dataEngine, grouperDataRowAssign);
        
        dataEngine.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().put(grouperDataRowAssign.getInternalId(), grouperDataRowAssignWrapper);
        
        grouperDataRowAssignWrapper.setGrouperDataRowWrapper(dataEngine.getGrouperDataProviderIndex().getRowWrapperByInternalId().get(grouperDataRowAssign.getDataRowInternalId()));

        grouperDataRowAssignWrapper.setMemberWrapper(dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(grouperDataRowAssign.getMemberInternalId()));
        
      }
    }    

    {
      List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns = GrouperUtil.nonNull(GrouperDataRowFieldAssignDao.selectByProvider(grouperDataProvider.getInternalId()));
  
      for (GrouperDataRowFieldAssign grouperDataRowFieldAssign : grouperDataRowFieldAssigns) {
  
        GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper = new GrouperDataRowFieldAssignWrapper(dataEngine, grouperDataRowFieldAssign);
        
        dataEngine.getGrouperDataProviderIndex().getRowFieldAssignWrapperByInternalId().put(grouperDataRowFieldAssign.getInternalId(), grouperDataRowFieldAssignWrapper);
  
        grouperDataRowFieldAssignWrapper.setGrouperDataFieldWrapper(dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(grouperDataRowFieldAssign.getDataFieldInternalId()));
        
        if (grouperDataRowFieldAssign.getValueDictionaryInternalId() != null) {
          // TODO fix race conditions here
          String textValue = dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().get(grouperDataRowFieldAssign.getValueDictionaryInternalId());
          GrouperUtil.assertion(!StringUtils.isBlank(textValue), "Cant find text: " + grouperDataRowFieldAssign.getValueDictionaryInternalId());
          grouperDataRowFieldAssignWrapper.setTextValue(textValue);
        }
  
        grouperDataRowFieldAssignWrapper.setGrouperDataRowAssignWrapper(dataEngine.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().get(grouperDataRowFieldAssign.getDataRowAssignInternalId()));
        
      }
    }
    
    // index attribute assignments by person
    for (GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper  : dataEngine.getGrouperDataProviderIndex().getFieldAssignWrapperByInternalId().values()) {
      
      Long memberInternalId = grouperDataFieldAssignWrapper.getMemberWrapper().getInternalId();
      GrouperDataMemberWrapper grouperDataMemberWrapper = dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
      
      long dataFieldInternalId = grouperDataFieldAssignWrapper.getGrouperDataFieldAssign().getDataFieldInternalId();
      List<GrouperDataFieldAssignWrapper> grouperDataFieldAssignWrappers = grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().get(dataFieldInternalId);
      
      if (grouperDataFieldAssignWrappers == null) {
        grouperDataFieldAssignWrappers = new ArrayList<>();
        grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().put(dataFieldInternalId, grouperDataFieldAssignWrappers);
      }
      grouperDataFieldAssignWrappers.add(grouperDataFieldAssignWrapper);
    }
    
    // index rows by user
    // index attribute assignments by person
    for (GrouperDataRowAssignWrapper grouperDataRowAssignWrapper  : dataEngine.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().values()) {
      
      Long memberInternalId = grouperDataRowAssignWrapper.getMemberWrapper().getInternalId();
      GrouperDataMemberWrapper grouperDataMemberWrapper = dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
      
      long dataRowInternalId = grouperDataRowAssignWrapper.getGrouperDataRowAssign().getDataRowInternalId();
      List<GrouperDataRowAssignWrapper> grouperDataRowAssignWrappers = grouperDataMemberWrapper.getRowAssignWrappersByRowInternalId().get(dataRowInternalId);

      if (grouperDataRowAssignWrappers == null) {
        grouperDataRowAssignWrappers = new ArrayList<>();
        grouperDataMemberWrapper.getRowAssignWrappersByRowInternalId().put(dataRowInternalId, grouperDataRowAssignWrappers);
      }
      grouperDataRowAssignWrappers.add(grouperDataRowAssignWrapper);
    }
    
    // put the row fields in the rows
    // index attribute assignments by person
    for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper  : dataEngine.getGrouperDataProviderIndex().getRowFieldAssignWrapperByInternalId().values()) {
      
      Long rowAssignId = grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getDataRowAssignInternalId();
      GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = dataEngine.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().get(rowAssignId);
      
      long dataFieldInternalId = grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getDataFieldInternalId();
      List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers = grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().get(dataFieldInternalId);
      
      if (grouperDataRowFieldAssignWrappers == null) {
        grouperDataRowFieldAssignWrappers = new ArrayList<>();
        grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().put(dataFieldInternalId, grouperDataRowFieldAssignWrappers);
      }
      grouperDataRowFieldAssignWrappers.add(grouperDataRowFieldAssignWrapper);
    }

    // get the values and index per user
    // remove invalid types
    // type cast by field type
    // have a map of values
    for (GrouperDataMemberWrapper grouperDataMemberWrapper : dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().values()) {

      for (Long fieldInternalId : grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().keySet()) {
        
        List<GrouperDataFieldAssignWrapper> dataFieldAssignWrappers = GrouperUtil.nonNull(grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().get(fieldInternalId));
        GrouperDataFieldWrapper grouperDataFieldWrapper = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(fieldInternalId);

        String dataFieldConfigId = grouperDataFieldWrapper.getGrouperDataField().getConfigId();
        GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(dataFieldConfigId);
        
        Set<Object> values = grouperDataMemberWrapper.getFieldIdToValues().get(fieldInternalId);
        
        if (values == null) {
          values = new HashSet<>();
          grouperDataMemberWrapper.getFieldIdToValues().put(fieldInternalId, values);
        }

        HashMap<Object, GrouperDataFieldAssignWrapper> valueToFieldAssignWrapper = new HashMap<>();
        grouperDataMemberWrapper.getFieldIdToValueToFieldAssignWrapper().put(fieldInternalId, valueToFieldAssignWrapper);
        
        for (GrouperDataFieldAssignWrapper dataFieldAssignWrapper : dataFieldAssignWrappers) {
          Object value = grouperDataFieldConfig.getFieldDataType().convertValue(
              dataFieldAssignWrapper.getGrouperDataFieldAssign().getValueInteger(),
              dataFieldAssignWrapper.getTextValue());
          
          // cant have same value
          if (valueToFieldAssignWrapper.containsKey(value)) {
            GrouperDataFieldAssignDao.delete(dataFieldAssignWrapper.getGrouperDataFieldAssign());
            grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(1);
            continue;
          }
          
          if (!grouperDataFieldConfig.isFieldMultiValued() && valueToFieldAssignWrapper.size() >= 1) {
            GrouperDataFieldAssignDao.delete(dataFieldAssignWrapper.getGrouperDataFieldAssign());
            grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(1);
            continue;
          }
          values.add(value);
          valueToFieldAssignWrapper.put(value, dataFieldAssignWrapper);
        }
      }
    }
    
    
    
    // from db
    // wrapper object for attributes, rows, and columns
    // map by user to objects
    
    // remove attributes not there
    // remove attributes not assigned
    // add row composite key attributes (to config)
    // match rows
    
    Map<String, Map<String, Integer>> queryConfigIdToLowerColumnNameToZeroIndex = new HashMap<String, Map<String, Integer>>();

    for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {
      GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();

      Map<String, Integer> lowerColumnNameToZeroIndex = new HashMap<String, Integer>();
      queryConfigIdToLowerColumnNameToZeroIndex.put(grouperDataProviderQueryConfig.getConfigId(), lowerColumnNameToZeroIndex);
      
      List<Object[]> rows = grouperDataProviderQuery.retrieveGrouperDataProviderQueryTargetDao().selectData(lowerColumnNameToZeroIndex);
      
      String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute().toLowerCase();
      String sourceIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectSourceId();
      Integer subjectIdZeroIndex = queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId()).get(subjectIdAttribute);
      
      GrouperUtil.assertion(subjectIdZeroIndex != null, "Cannot find subject id attribute column: " + subjectIdAttribute);

      for (Object[] row : rows) {
        
        String subjectId = GrouperUtil.stringValue(row[subjectIdZeroIndex]);
        
        Subject subject = StringUtils.isBlank(sourceIdAttribute) ? SubjectFinder.findById(subjectId, true)
            : SubjectFinder.findByIdAndSource(subjectId, sourceIdAttribute, true);
        
        Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);

        Long memberInternalId = member.getInternalId();
        
        GrouperDataMemberWrapper grouperDataMemberWrapper = dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
        
        if (grouperDataMemberWrapper == null) {
          grouperDataMemberWrapper = new GrouperDataMemberWrapper(dataEngine, memberInternalId);
          dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().put(memberInternalId, grouperDataMemberWrapper);
        }

        List<Object[]> userRowsforQuery = grouperDataMemberWrapper.getQueryConfigIdToRowData().get(grouperDataProviderQueryConfig.getConfigId());
        if (userRowsforQuery == null) {
          userRowsforQuery = new ArrayList<Object[]>();
          grouperDataMemberWrapper.getQueryConfigIdToRowData().put(grouperDataProviderQueryConfig.getConfigId(), userRowsforQuery);
        }
        
        userRowsforQuery.add(row);
      }
      
    }
    
    // go through each user, index and convert the data
    for (GrouperDataMemberWrapper grouperDataMemberWrapper : dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().values()) {
      
      // go through each query
      for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {
        
        GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();
        String queryConfigId = grouperDataProviderQueryConfig.getConfigId();
        
        List<Object[]> providerRows = GrouperUtil.nonNull(grouperDataMemberWrapper.getQueryConfigIdToRowData().get(queryConfigId));
        
        String rowConfigId = grouperDataProviderQueryConfig.getProviderQueryRowConfigId();

        //GrouperDataFieldConfig grouperDataRowConfig = null;
        
        GrouperDataRowWrapper grouperDataRowWrapper = null;
        List<Map<Long, List<Object>>> rowsOfFieldInternalIdToValues = null;
        
        // if this is a direct assignment
        if (!StringUtils.isBlank(rowConfigId)) {
          
          //grouperDataRowConfig = this.fieldConfigByConfigId.get(rowConfigId);
          
          // if this is a row assignment
          grouperDataRowWrapper = dataEngine.getGrouperDataProviderIndex().getRowWrapperByConfigId().get(rowConfigId);
          
          rowsOfFieldInternalIdToValues = grouperDataMemberWrapper.getDataProviderDataByDataRowInternalId().get(grouperDataRowWrapper.getGrouperDataRow().getInternalId());
          if (rowsOfFieldInternalIdToValues == null) {
            rowsOfFieldInternalIdToValues = new ArrayList<>();
            grouperDataMemberWrapper.getDataProviderDataByDataRowInternalId().put(grouperDataRowWrapper.getGrouperDataRow().getInternalId(), rowsOfFieldInternalIdToValues);
          }
        }
        

        List<GrouperDataProviderQueryFieldConfig> grouperDataProviderQueryFieldConfigs =
            grouperDataProviderQueryConfig.getGrouperDataProviderQueryFieldConfigs();

        for (Object[] row : providerRows) {

          Map<Long, List<Object>> rowDataFieldInternalIdToValues = null;
          
          if (!StringUtils.isBlank(rowConfigId)) {
            rowDataFieldInternalIdToValues = new HashMap<>();
            rowsOfFieldInternalIdToValues.add(rowDataFieldInternalIdToValues);
          }

          for (GrouperDataProviderQueryFieldConfig grouperDataProviderQueryFieldConfig : GrouperUtil.nonNull(grouperDataProviderQueryFieldConfigs)) {
          
            GrouperDataProviderQueryFieldMappingType providerDataFieldMappingType = 
                grouperDataProviderQueryFieldConfig.getProviderDataFieldMappingType();
            
            // could be the subject attribute?
            if (providerDataFieldMappingType == null) {
              continue;
            }
            
            // this is really the only option right now
            if (providerDataFieldMappingType == GrouperDataProviderQueryFieldMappingType.attribute) {
              
              String columnName = grouperDataProviderQueryFieldConfig.getProviderDataFieldAttribute();
              String dataFieldConfigId = grouperDataProviderQueryFieldConfig.getProviderDataFieldConfigId();
  
              GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(dataFieldConfigId);
              
              GrouperDataFieldWrapper grouperDataFieldWrapper = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByConfigId().get(dataFieldConfigId);
                
              Integer rowIndex = queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId()).get(columnName.toLowerCase());
              if (rowIndex == null) {
                throw new RuntimeException("Unable to find index for configId=" + grouperDataProviderQueryConfig.getConfigId() + ", columnName=" + columnName.toLowerCase());
              }
              
              Object value = row[rowIndex];
              
              if (value instanceof Set) {
                if (((Set)value).size() > 0) {
                  List<Object> data = grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().get(grouperDataFieldWrapper.getGrouperDataField().getInternalId());
                  if (data == null) {
                    data = new ArrayList<>();
                    grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().put(grouperDataFieldWrapper.getGrouperDataField().getInternalId(), data);
                  }
                  
                  for (Object currentValue : (Set)value) {
                    currentValue = grouperDataFieldConfig.getFieldDataType().convertValue(currentValue);
                    
                    if (currentValue != null && currentValue != Void.TYPE) {
                      data.add(currentValue);
                    }
                  }
                }
              } else {
                value = grouperDataFieldConfig.getFieldDataType().convertValue(value);
                
                // if this is a direct assignment
                if (StringUtils.isBlank(rowConfigId)) {

                  List<Object> data = grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().get(grouperDataFieldWrapper.getGrouperDataField().getInternalId());
                  if (data == null) {
                    data = new ArrayList<>();
                    grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().put(grouperDataFieldWrapper.getGrouperDataField().getInternalId(), data);
                  }
                  
                  if (value != null && value != Void.TYPE) {
                    data.add(value);
                  }
                } else {
                  // if this is a row
                  List<Object> values = rowDataFieldInternalIdToValues.get(grouperDataFieldWrapper.getGrouperDataField().getInternalId());
                  if (values == null) {
                    values = new ArrayList<>();
                    rowDataFieldInternalIdToValues.put(grouperDataFieldWrapper.getGrouperDataField().getInternalId(), values);
                  }
                  values.add(value);
                } 
              }
            }
              
          }          
        }

      }

      {
        // change the database for fields
        // go through each dataFieldConfigId where there is provider or grouper data
        Set<Long> dataFieldInternalIds = new HashSet<Long>();
        dataFieldInternalIds.addAll(grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().keySet());
        dataFieldInternalIds.addAll(grouperDataMemberWrapper.getFieldIdToValues().keySet());
        Map<Long, Map<Object, GrouperDataFieldAssignWrapper>> fieldIdToValueToFieldAssignWrapper = grouperDataMemberWrapper.getFieldIdToValueToFieldAssignWrapper();
        
        for (Long dataFieldInternalId : dataFieldInternalIds) {
  
          GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
          GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(grouperDataField.getConfigId());

          if (grouperDataFieldConfig.getFieldDataStructure() == GrouperDataFieldStructure.attribute) {
            Map<Object, GrouperDataFieldAssignWrapper> valueToFieldAssignWrapper = GrouperUtil.nonNull(fieldIdToValueToFieldAssignWrapper.get(dataFieldInternalId));
            
            Set<Object> dataFromProvider = new HashSet<>(GrouperUtil.nonNull(grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().get(dataFieldInternalId)));
            Set<Object> dataFromGrouper = new HashSet<>(GrouperUtil.nonNull(grouperDataMemberWrapper.getFieldIdToValues().get(dataFieldInternalId)));
            
            Set<Object> dataToDelete = new HashSet<>(dataFromGrouper);
            dataToDelete.removeAll(dataFromProvider);
            
            for (Object value : dataToDelete) {
              GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper = valueToFieldAssignWrapper.get(value);
              GrouperDataFieldAssignDao.delete(grouperDataFieldAssignWrapper.getGrouperDataFieldAssign());
              grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(1);
            }
            
            Set<Object> dataToInsert = new HashSet<>(dataFromProvider);
            dataToInsert.removeAll(dataFromGrouper);
    
            
            for (Object value : dataToInsert) {
              GrouperDataFieldAssign grouperDataFieldAssign = new GrouperDataFieldAssign();
              grouperDataFieldAssign.setDataFieldInternalId(dataFieldInternalId);
              grouperDataFieldAssign.setDataProviderInternalId(grouperDataProvider.getInternalId());
              grouperDataFieldAssign.setMemberInternalId(grouperDataMemberWrapper.getInternalId());
              grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataFieldAssign, value);
              GrouperDataFieldAssignDao.store(grouperDataFieldAssign);
              grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(1);
            }
          }
        }
      }

      { 
        //change database for rows
        Set<Long> dataRowInternalIds = new HashSet<Long>();
        dataRowInternalIds.addAll(grouperDataMemberWrapper.getDataProviderDataByDataRowInternalId().keySet());
        Map<Long, List<GrouperDataRowAssignWrapper>> rowAssignWrappersByRowInternalId = grouperDataMemberWrapper.getRowAssignWrappersByRowInternalId();
        dataRowInternalIds.addAll(GrouperUtil.nonNull(rowAssignWrappersByRowInternalId).keySet());
        
        // go through each row id: (dataRowInternalIds is null)
        for (Long dataRowInternalId : dataRowInternalIds) {
          
          GrouperDataRow grouperDataRow = dataEngine.getGrouperDataProviderIndex().getRowWrapperByInternalId().get(dataRowInternalId).getGrouperDataRow();
          GrouperDataRowConfig grouperDataRowConfig = dataEngine.getRowConfigByConfigId().get(grouperDataRow.getConfigId());
          List<GrouperDataRowAssignWrapper> grouperDataRowAssignWrappers = GrouperUtil.nonNull(rowAssignWrappersByRowInternalId.get(dataRowInternalId));
          Map<MultiKey, GrouperDataRowAssignWrapper> grouperDataRowKeyToRowAssignWrapper = new HashMap<>();
          for (GrouperDataRowAssignWrapper grouperDataRowAssignWrapper : GrouperUtil.nonNull(grouperDataRowAssignWrappers)) {
            MultiKey rowKey = grouperDataRowAssignWrapper.rowKey();
            grouperDataRowKeyToRowAssignWrapper.put(rowKey, grouperDataRowAssignWrapper);
          }
          
          List<Map<Long, List<Object>>> providerRowsOfDataFieldInternalIdToListOfValues = GrouperUtil.nonNull(grouperDataMemberWrapper.getDataProviderDataByDataRowInternalId().get(dataRowInternalId));
          Map<MultiKey, Map<Long, List<Object>>> providerDataRowKeyToDataFieldInternalIdsAndValues = new HashMap<>();
          for (Map<Long, List<Object>> providerDataFieldInternalIdToValues : GrouperUtil.nonNull(providerRowsOfDataFieldInternalIdToListOfValues)) {
            Object[] keyValues = new Object[GrouperUtil.length(grouperDataRowConfig.getRowKeyFieldConfigIds())];
            int i = 0;

            for (String rowKeyFieldConfigId : grouperDataRowConfig.getRowKeyFieldConfigIds()) {

                GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(rowKeyFieldConfigId);
                GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByConfigId().get(rowKeyFieldConfigId).getGrouperDataField();
                List<Object> values = providerDataFieldInternalIdToValues.get(grouperDataField.getInternalId());

                if (GrouperUtil.length(values) != 1) {
                  throw new RuntimeException("Provider row field key must have one value: " + grouperDataRowConfig.getConfigId() 
                    + ", field: " + grouperDataFieldConfig.getConfigId() + ", " + GrouperUtil.stringValue(values));
                }

                keyValues[i] = grouperDataFieldConfig.getFieldDataType().convertValue(values.iterator().next());

                GrouperUtil.assertion(keyValues[i] != null && keyValues[i] != Void.TYPE, 
                    "Data row field key must not have a null value: " + grouperDataRowConfig.getConfigId() 
                    + ", rowAssignId: " + grouperDataRow.getInternalId() + ", field: " + grouperDataFieldConfig.getConfigId());
                i++;
              }
              MultiKey rowKey = new MultiKey(keyValues);
              providerDataRowKeyToDataFieldInternalIdsAndValues.put(rowKey, providerDataFieldInternalIdToValues);
            }
          
            Set<MultiKey> rowKeyFieldsToDeletes = new HashSet<>(grouperDataRowKeyToRowAssignWrapper.keySet());
            rowKeyFieldsToDeletes.removeAll(providerDataRowKeyToDataFieldInternalIdsAndValues.keySet());
            
            for (MultiKey rowKeyFieldsToDelete : rowKeyFieldsToDeletes) {
              GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = grouperDataRowKeyToRowAssignWrapper.get(rowKeyFieldsToDelete);
              for (List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers : grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().values()) {
                for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper : grouperDataRowFieldAssignWrappers) {
                  GrouperDataRowFieldAssignDao.delete(grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign());
                }
              }
              
              GrouperDataRowAssignDao.delete(grouperDataRowAssignWrapper.getGrouperDataRowAssign());
              grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(1);
            }

            Set<MultiKey> rowKeyFieldsToInserts = new HashSet<>(providerDataRowKeyToDataFieldInternalIdsAndValues.keySet());
            rowKeyFieldsToInserts.removeAll(grouperDataRowKeyToRowAssignWrapper.keySet());
    
            
            for (MultiKey rowKeyFieldsToInsert : rowKeyFieldsToInserts) {
              GrouperDataRowAssign grouperDataRowAssign = new GrouperDataRowAssign();
              grouperDataRowAssign.setDataRowInternalId(dataRowInternalId);
              grouperDataRowAssign.setDataProviderInternalId(grouperDataProvider.getInternalId());
              grouperDataRowAssign.setMemberInternalId(grouperDataMemberWrapper.getInternalId());
              GrouperDataRowAssignDao.store(grouperDataRowAssign);
              
              Map<Long, List<Object>> dataFieldInternalIdToValues = providerDataRowKeyToDataFieldInternalIdsAndValues.get(rowKeyFieldsToInsert);
              for (Long dataFieldInternalId : GrouperUtil.nonNull(dataFieldInternalIdToValues.keySet())) {

                GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
                GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(grouperDataField.getConfigId());

                List<Object> values = dataFieldInternalIdToValues.get(dataFieldInternalId);
                for (Object value : values) {
                  GrouperDataRowFieldAssign grouperDataRowFieldAssign = new GrouperDataRowFieldAssign();
                  grouperDataRowFieldAssign.setDataFieldInternalId(dataFieldInternalId);
                  grouperDataRowFieldAssign.setDataRowAssignInternalId(grouperDataRowAssign.getInternalId());
                  grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataRowFieldAssign, value);

                  GrouperDataRowFieldAssignDao.store(grouperDataRowFieldAssign);
                  
                }
              }
              
              grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(1);
            }

            // do the updates
            for (MultiKey grouperDataRowKey : grouperDataRowKeyToRowAssignWrapper.keySet()) {
              if (providerDataRowKeyToDataFieldInternalIdsAndValues.containsKey(grouperDataRowKey)) {
                GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = grouperDataRowKeyToRowAssignWrapper.get(grouperDataRowKey);
                Map<Long, List<Object>> providerDataFieldInternalIdsAndValues = providerDataRowKeyToDataFieldInternalIdsAndValues.get(grouperDataRowKey);
                
                for (Long dataFieldInternalId : GrouperUtil.nonNull(providerDataFieldInternalIdsAndValues.keySet())) {

                  GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
                  GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(grouperDataField.getConfigId());

                  List<Object> providerValues = providerDataFieldInternalIdsAndValues.get(dataFieldInternalId);
                  List<Object> grouperValuesConverted = new ArrayList<Object>();
                  List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers = GrouperUtil.nonNull(grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().get(dataFieldInternalId));
                  for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper : grouperDataRowFieldAssignWrappers) {
                    Object grouperValueConverted = grouperDataFieldConfig.getFieldDataType().convertValue(
                        grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getValueInteger(),
                        grouperDataRowFieldAssignWrapper.getTextValue());
                    if (providerValues.contains(grouperValueConverted)) {
                      grouperValuesConverted.add(grouperValueConverted);
                    } else {
                      GrouperDataRowFieldAssignDao.delete(grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign());
                      grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(1);
                    }
                  }
                  
                  Set<Object> valuesToAdd = new HashSet<Object>(providerValues);
                  valuesToAdd.removeAll(grouperValuesConverted);
                  
                  for (Object valueToAdd : valuesToAdd) {
                    if (valueToAdd != null && valueToAdd != Void.TYPE) {
                      GrouperDataRowFieldAssign grouperDataRowFieldAssign = new GrouperDataRowFieldAssign();
                      grouperDataRowFieldAssign.setDataFieldInternalId(dataFieldInternalId);
                      grouperDataRowFieldAssign.setDataRowAssignInternalId(grouperDataRowAssignWrapper.getGrouperDataRowAssign().getInternalId());
                      grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataRowFieldAssign, valueToAdd);
                      GrouperDataRowFieldAssignDao.store(grouperDataRowFieldAssign);
                      grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(1);
                    }
                  }
                }
              }
            }
          }

          
        }
    }
    
  }
  
  /**
   * 
   */
  public void syncIncremental() {
    
  }
}
