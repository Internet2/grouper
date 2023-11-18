package edu.internet2.middleware.grouper.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * dao for dictionaries
 * @author mchyzer
 *
 */
public class GrouperDictionaryDao {


  public GrouperDictionaryDao() {
  }

  /**
   * get dictionary items by data provider for field and row assignments
   * @param dataProviderInternalId
   * @return internal id to value
   */
  public static Map<Long, String> selectByDataProvider(Long dataProviderInternalId) {

    if (dataProviderInternalId == null) {
      throw new NullPointerException();
    }
    
    
    List<Object[]> internalIdAndTexts = new GcDbAccess()
        .sql("select gd.internal_id, gd.the_text from grouper_dictionary gd where gd.internal_id in ( "
            + " select gdfa.value_dictionary_internal_id from grouper_data_field_assign gdfa where data_provider_internal_id = ? ) "
            + " union select gd.internal_id, gd.the_text from grouper_dictionary gd where gd.internal_id in ( "
            + " select gdrfa.value_dictionary_internal_id from grouper_data_row_field_assign gdrfa where exists "
            + " (select 1 from grouper_data_row_assign gdra where gdrfa.data_row_assign_internal_id = gdra.internal_id and gdra.data_provider_internal_id = ? ))")
        .addBindVar(dataProviderInternalId).addBindVar(dataProviderInternalId).selectList(Object[].class);

    Map<Long, String> result = new HashMap<Long, String>();
    
    for (Object[] internalIdAndText : GrouperUtil.nonNull(internalIdAndTexts)) {
      result.put(GrouperUtil.longObjectValue(internalIdAndText[0], false), (String)internalIdAndText[1]);
    }

    return result;
  }

  /**
   * get dictionary items by data provider and members for field and row assignments
   * @param dataProviderInternalId
   * @param memberInternalIds
   * @return internal id to value
   */
  public static Map<Long, String> selectByDataProviderAndMembers(Long dataProviderInternalId, Set<Long> memberInternalIds) {

    if (dataProviderInternalId == null) {
      throw new NullPointerException();
    }
    
    Map<Long, String> result = new HashMap<Long, String>();

    if (memberInternalIds.size() == 0) {
      return result;
    }
    
    int batchSize = 200;
    List<Long> memberInternalIdsList = new ArrayList<Long>(memberInternalIds);
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberInternalIdsList.size(), batchSize, true);
    for (int i=0;i<numberOfBatches;i++) {
      GcDbAccess gcDbAccess = new GcDbAccess();
      List<Long> batchMemberInternalIds = GrouperUtil.batchList(memberInternalIdsList, batchSize, i);

      StringBuilder sql = new StringBuilder("select gd.internal_id, gd.the_text from grouper_dictionary gd where gd.internal_id in ( "
              + " select gdfa.value_dictionary_internal_id from grouper_data_field_assign gdfa where data_provider_internal_id = ? and member_internal_id in (");
      gcDbAccess.addBindVar(dataProviderInternalId);
      GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberInternalIds));
      for (Long memberId : batchMemberInternalIds) {
        gcDbAccess.addBindVar(memberId);
      }
      
      sql.append(") ) "
              + " union select gd.internal_id, gd.the_text from grouper_dictionary gd where gd.internal_id in ( "
              + " select gdrfa.value_dictionary_internal_id from grouper_data_row_field_assign gdrfa where exists "
              + " (select 1 from grouper_data_row_assign gdra where gdrfa.data_row_assign_internal_id = gdra.internal_id and gdra.data_provider_internal_id = ? and gdra.member_internal_id in (");
      gcDbAccess.addBindVar(dataProviderInternalId);
      GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberInternalIds));
      for (Long memberId : batchMemberInternalIds) {
        gcDbAccess.addBindVar(memberId);
      }
      
      sql.append(") ))");
      
      List<Object[]> internalIdAndTexts = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
      
      for (Object[] internalIdAndText : GrouperUtil.nonNull(internalIdAndTexts)) {
        result.put(GrouperUtil.longObjectValue(internalIdAndText[0], false), (String)internalIdAndText[1]);
      }
    }
    
    return result;
  }
  
  /**
   * @param grouperDictionary
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDictionary grouperDictionary) {
    
    GrouperUtil.assertion(grouperDictionary != null, "grouperDictionary is null");
    
    boolean isInsert = grouperDictionary.getInternalId() == -1;

    grouperDictionary.storePrepare();

    if (!isInsert) {
      boolean changed = new GcDbAccess().storeToDatabase(grouperDictionary);
      return changed;
    }

    RuntimeException runtimeException = null;
    // might be other places saving the same dictionary
    for (int i=0;i<5;i++) {
      try {
        new GcDbAccess().storeToDatabase(grouperDictionary);
        return true;
      } catch (RuntimeException re) {
        runtimeException = re;
        GrouperUtil.sleep(100 * (i+1));
        GrouperDictionary grouperDictionaryNew = selectByText(grouperDictionary.getTheText());
        if (grouperDictionaryNew != null) {
          return false;
        }
        if (i==4) {
          throw re;
        }
      }
    }
    // this should never happen :)
    throw runtimeException;
  }  

  public static GrouperDictionary selectByText(String theText) {
    if (StringUtils.isBlank(theText)) {
      return null;
    }
    GrouperDictionary grouperDictionary = new GcDbAccess().sql("select * from grouper_dictionary where the_text = ?")
        .addBindVar(theText).select(GrouperDictionary.class);
    return grouperDictionary;
  }
  
  /**
   * 
   * @param connectionName
   */
  public static void delete(GrouperDictionary grouperDictionary) {
    grouperDictionary.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDictionary);
  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDictionary.class)).executeSql();
  }

  /**
   * cache, use the method to get this
   */
  private static ExpirableCache<String, Long> textToInternalIdCache = null;
  
  /**
   * max terms in memory
   */
  private static int maxTermsInMemoryCache = 50000;
  
  /**
   * dictionary cache
   * @return the cache
   */
  private static ExpirableCache<String, Long> textToInternalIdCache() {
    if (textToInternalIdCache == null) {
      maxTermsInMemoryCache = GrouperConfig.retrieveConfig().propertyValueInt("grouper.dictionary.maxTermsInMemoryCache ", 50000);
      int cacheStoreForMinutes = GrouperConfig.retrieveConfig().propertyValueInt("grouper.dictionary.cacheStoreForMinutes");
      textToInternalIdCache = new ExpirableCache<String, Long>(cacheStoreForMinutes);
    }
    return textToInternalIdCache;
  }
  
  /**
   * cache, use the method to get this
   */
  private static ExpirableCache<Long, String> internalIdToTextCache = null;
  
  /**
   * dictionary cache
   * @return the cache
   */
  private static ExpirableCache<Long, String> internalIdToTextCache() {
    if (internalIdToTextCache == null) {
      int cacheStoreForMinutes = GrouperConfig.retrieveConfig().propertyValueInt("grouper.dictionary.cacheStoreForMinutes");
      internalIdToTextCache = new ExpirableCache<Long, String>(cacheStoreForMinutes);
    }
    return internalIdToTextCache;
  }
  
  /**
   * @param string
   * @return the dictionary
   */
  public static Long findOrAdd(String string) {
    if (StringUtils.isBlank(string)) {
      return null;
    }

    Long internalId = textToInternalIdCache().get(string);
    if (internalId == null) {
      GrouperDictionary grouperDictionary = selectByText(string);
      
      if (grouperDictionary == null) {
      
        grouperDictionary = new GrouperDictionary();
        grouperDictionary.setTheText(string);
        store(grouperDictionary);
      }
      internalId = grouperDictionary.getInternalId();
      
      if (textToInternalIdCache().size(false) < maxTermsInMemoryCache) {
        textToInternalIdCache().put(string, internalId);
        internalIdToTextCache().put(internalId, string);
      }
    }
    return internalId;
  }
  

}
