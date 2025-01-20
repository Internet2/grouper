package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperDataGlobalAssignDao {
  
  public GrouperDataGlobalAssignDao() {
  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataGlobalAssign.class)).executeSql();
  }
  
  public static List<GrouperDataGlobalAssign> selectByDataFieldInternalId(long dataFieldInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_global_assign where data_field_internal_id = ?")
        .addBindVar(dataFieldInternalId)
        .selectList(GrouperDataGlobalAssign.class);

  }
  
  public static List<GrouperDataGlobalAssign> selectByDataFieldInternalIds(Set<Long> dataFieldInternalIds) {
    
    if (dataFieldInternalIds == null || dataFieldInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
//    List<List<Object>> batchBindVars = new ArrayList<>();
//    
//    for (Long dataFieldInternalId: dataFieldInternalIds) {
//      batchBindVars.add(GrouperClientUtils.toList(dataFieldInternalId));
//    }
    
    return new GcDbAccess().sql("select * from grouper_data_global_assign ")
        .selectMultipleColumnName("data_field_internal_id")
        .bindVars(new ArrayList<Long>(dataFieldInternalIds))
//        .batchBindVars(batchBindVars)
        .selectList(GrouperDataGlobalAssign.class);
  }

  /**
   * @param grouperDataGlobalAssign
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDataGlobalAssign grouperDataGlobalAssign) {
    
    GrouperUtil.assertion(grouperDataGlobalAssign != null, "grouperDataGlobalAssign is null");
    
    grouperDataGlobalAssign.storePrepare();

    boolean changed = new GcDbAccess().storeToDatabase(grouperDataGlobalAssign);
    return changed;

  }  
  
  public static List<GrouperDataGlobalAssign> selectByProvider(Long dataProviderInternalId) {

    if (dataProviderInternalId == null) {
      throw new NullPointerException();
    }
    
    List<GrouperDataGlobalAssign> grouperDataGlobalAssigns = new GcDbAccess()
        .sql("select * from grouper_data_global_assign where data_provider_internal_id = ? ")
        .addBindVar(dataProviderInternalId).selectList(GrouperDataGlobalAssign.class);
    return grouperDataGlobalAssigns;
  }
  
  public static List<GrouperDataGlobalAssign> selectByDataProviderInternalIds(Set<Long> dataProviderInternalIds) {
    if (dataProviderInternalIds == null || dataProviderInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select * from grouper_data_global_assign ")
        .selectMultipleColumnName("data_provider_internal_id")
        .bindVars(new ArrayList<Long>(dataProviderInternalIds))
        .selectList(GrouperDataGlobalAssign.class);
    
  }
  
  /**
   * 
   * @param grouperDataGlobalAssign 
   */
  public static void delete(GrouperDataGlobalAssign grouperDataGlobalAssign) {
    grouperDataGlobalAssign.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataGlobalAssign);
  }
  
  public static void delete(Collection<GrouperDataGlobalAssign> grouperDataGlobalAssigns) {
    for (GrouperDataGlobalAssign grouperDataGlobalAssign: grouperDataGlobalAssigns) {      
      grouperDataGlobalAssign.storePrepare();
    }
    new GcDbAccess().deleteFromDatabaseMultiple(grouperDataGlobalAssigns);
  }

}
