package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.jdbc.GcTransactionCallback;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * dao for data field assign
 * @author mchyzer
 *
 */
public class GrouperDataFieldAssignDao {


  public GrouperDataFieldAssignDao() {
  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataFieldAssign.class)).executeSql();
  }
  
  public static List<GrouperDataFieldAssign> selectByDataFieldInternalId(long dataFieldInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_field_assign where data_field_internal_id = ?")
        .addBindVar(dataFieldInternalId)
        .selectList(GrouperDataFieldAssign.class);

  }
  
  public static List<GrouperDataFieldAssign> selectByDataFieldInternalIds(Set<Long> dataFieldInternalIds) {
    
    if (dataFieldInternalIds == null || dataFieldInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select * from grouper_data_field_assign ")
        .selectMultipleColumnName("data_field_internal_id")
        .bindVars(new ArrayList<Long>(dataFieldInternalIds))
        .selectList(GrouperDataFieldAssign.class);
  }
  
 public static List<GrouperDataFieldAssign> selectByDataProviderInternalIds(Set<Long> dataProviderInternalIds) {
    
    if (dataProviderInternalIds == null || dataProviderInternalIds.size() == 0) {
      return new ArrayList<>();
      
    }
    
    return new GcDbAccess().sql("select * from grouper_data_field_assign ")
        .selectMultipleColumnName("data_provider_internal_id")
        .bindVars(new ArrayList<Long>(dataProviderInternalIds))
        .selectList(GrouperDataFieldAssign.class);
  }

  /**
   * @param grouperDataFieldAssign
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDataFieldAssign grouperDataFieldAssign) {
    
    GrouperUtil.assertion(grouperDataFieldAssign != null, "grouperDataFieldAssign is null");
    
    grouperDataFieldAssign.storePrepare();

    boolean changed = new GcDbAccess().storeToDatabase(grouperDataFieldAssign);
    return changed;

  }  

  public static List<GrouperDataFieldAssign> selectByMarker(Long memberInternalId, Long dataFieldInternalId) {

    if (memberInternalId == null) {
      throw new NullPointerException();
    }
    if (dataFieldInternalId == null) {
      throw new NullPointerException();
    }
    
    List<GrouperDataFieldAssign> grouperDataFieldAssigns = new GcDbAccess()
        .sql("select * from grouper_data_field_assign where member_internal_id = ? and data_field_internal_id = ? "
            + " and value_integer is null and value_dictionary_internal_id is null ")
        .addBindVar(memberInternalId).addBindVar(dataFieldInternalId).selectList(GrouperDataFieldAssign.class);
    return grouperDataFieldAssigns;
  }
  
  public static List<GrouperDataFieldAssign> selectByProvider(Long dataProviderInternalId) {

    if (dataProviderInternalId == null) {
      throw new NullPointerException();
    }
    
    List<GrouperDataFieldAssign> grouperDataFieldAssigns = new GcDbAccess()
        .sql("select * from grouper_data_field_assign where data_provider_internal_id = ? ")
        .addBindVar(dataProviderInternalId).selectList(GrouperDataFieldAssign.class);
    return grouperDataFieldAssigns;
  }
  
  public static List<GrouperDataFieldAssign> selectByProviderAndMembers(Long dataProviderInternalId, Set<Long> memberInternalIds) {

    if (dataProviderInternalId == null) {
      throw new NullPointerException();
    }

    List<GrouperDataFieldAssign> grouperDataFieldAssigns = new ArrayList<GrouperDataFieldAssign>();

    if (memberInternalIds.size() == 0) {
      return grouperDataFieldAssigns;
    }

    int batchSize = 200;
    List<Long> memberInternalIdsList = new ArrayList<Long>(memberInternalIds);

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberInternalIdsList.size(), batchSize, true);
    for (int i=0;i<numberOfBatches;i++) {
      GcDbAccess gcDbAccess = new GcDbAccess();
      List<Long> batchMemberInternalIds = GrouperUtil.batchList(memberInternalIdsList, batchSize, i);

      StringBuilder sql = new StringBuilder("select * from grouper_data_field_assign where data_provider_internal_id = ? and member_internal_id in (");
      gcDbAccess.addBindVar(dataProviderInternalId);
      GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberInternalIds));
      for (Long memberId : batchMemberInternalIds) {
        gcDbAccess.addBindVar(memberId);
      }

      sql.append(")");

      List<GrouperDataFieldAssign> currGrouperDataFieldAssigns = gcDbAccess.sql(sql.toString()).selectList(GrouperDataFieldAssign.class);
      grouperDataFieldAssigns.addAll(currGrouperDataFieldAssigns);
    }
   
    return grouperDataFieldAssigns;
  }
  
  public static void delete(GrouperDataFieldAssign grouperDataFieldAssign) {
    delete(grouperDataFieldAssign, true);
  }
  
  public static void delete(GrouperDataFieldAssign grouperDataFieldAssign, boolean addHistory) {
    grouperDataFieldAssign.storePrepare();

    new GcDbAccess().callbackTransaction(new GcTransactionCallback<Boolean>() {
      
      @Override
      public Boolean callback(GcDbAccess dbAccessForStorage) {
  
        dbAccessForStorage.deleteFromDatabase(grouperDataFieldAssign);
        
        if (addHistory) {
          // hack?
          dbAccessForStorage.sql(null);
          dbAccessForStorage.bindVars();
          
          GrouperDataFieldAssignHst grouperDataFieldAssignHst = getHstInstanceFromObjectBeingDelete(grouperDataFieldAssign);
          GrouperDataFieldAssignHstDao.store(grouperDataFieldAssignHst);
        }
        
        return null;
      }
    });
  }
  
  public static void delete(Collection<GrouperDataFieldAssign> grouperDataFieldAssigns) {
    delete(grouperDataFieldAssigns, true);
  }
  
  public static void delete(Collection<GrouperDataFieldAssign> grouperDataFieldAssigns, boolean addHistory) {
    for (GrouperDataFieldAssign grouperDataFieldAssign: grouperDataFieldAssigns) {      
      grouperDataFieldAssign.storePrepare();
    }
        
    new GcDbAccess().callbackTransaction(new GcTransactionCallback<Boolean>() {
      
      @Override
      public Boolean callback(GcDbAccess dbAccessForStorage) {
        dbAccessForStorage.deleteFromDatabaseMultiple(grouperDataFieldAssigns);

        if (addHistory) {
          List<GrouperDataFieldAssignHst> grouperDataFieldAssignHsts = new ArrayList<>();
          for (GrouperDataFieldAssign grouperDataFieldAssign: grouperDataFieldAssigns) {                
            GrouperDataFieldAssignHst grouperDataFieldAssignHst = getHstInstanceFromObjectBeingDelete(grouperDataFieldAssign);
            grouperDataFieldAssignHsts.add(grouperDataFieldAssignHst);
          }
          GrouperDataFieldAssignHstDao.store(grouperDataFieldAssignHsts);
        }
        
        return null;
      }
    });
  }
  
  private static GrouperDataFieldAssignHst getHstInstanceFromObjectBeingDelete(GrouperDataFieldAssign grouperDataFieldAssign) {
    GrouperDataFieldAssignHst grouperDataFieldAssignHst = new GrouperDataFieldAssignHst();
    grouperDataFieldAssignHst.setDataFieldInternalId(grouperDataFieldAssign.getDataFieldInternalId());
    grouperDataFieldAssignHst.setMemberInternalId(grouperDataFieldAssign.getMemberInternalId());
    grouperDataFieldAssignHst.setValueInteger(grouperDataFieldAssign.getValueInteger());
    grouperDataFieldAssignHst.setValueDictionaryInternalId(grouperDataFieldAssign.getValueDictionaryInternalId());
    grouperDataFieldAssignHst.setStartTime(grouperDataFieldAssign.getCreatedOn().getTime() * 1000L);
    grouperDataFieldAssignHst.setEndTime(System.currentTimeMillis() * 1000L);
    
    return grouperDataFieldAssignHst;
  }
  
  public static List<GrouperDataFieldAssignView> retrieveDataFieldAssignments(Subject subject) {
    
    List<GrouperDataFieldAssignView> result = new ArrayList<GrouperDataFieldAssignView>();
    
    String sql = "select data_field_config_id, value_integer, value_text from grouper_data_field_assign_v where subject_id = ? and subject_source_id = ?";
    List<Object[]> objects = new GcDbAccess().sql(sql).addBindVar(subject.getId()).addBindVar(subject.getSourceId())
        .selectList(Object[].class);
    
    for (Object[] object: objects) {
      result.add(new GrouperDataFieldAssignView(GrouperUtil.stringValue(object[0]), GrouperUtil.longValue(object[1]), GrouperUtil.stringValue(object[2])));
    }
    
    return result;
  }

}
