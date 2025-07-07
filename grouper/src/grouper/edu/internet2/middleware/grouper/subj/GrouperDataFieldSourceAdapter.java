package edu.internet2.middleware.grouper.subj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealmConfig;
//import edu.internet2.middleware.grouper.subj.GrouperDataFieldSourceAdapter.DataFieldSubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.InvalidQueryRuntimeException;
import edu.internet2.middleware.subject.provider.SubjectImpl;
import edu.internet2.middleware.subject.util.SubjectApiUtils;

public class GrouperDataFieldSourceAdapter extends BaseSourceAdapter {
  
  /** logger */
  private static Log log = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(GrouperDataFieldSourceAdapter.class);
  
  @Override
  public boolean isEditable() {
    return true;
  }
  
  @Override
  public boolean isEnabled() {
    return SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source."+getConfigId()+".enabled", true);
  }
  
  @Override
  public Map<Integer, String> getSortAttributes() {
    return this.sortAttributes;
  }

  @Override
  public Map<Integer, String> getSearchAttributes() {
    return this.searchAttributes;
  }


  @Override
  public void loggingStart() {
  }

  @Override
  public String loggingStop() {
    return null;
  }

  @Override
  public void checkConfig() {
    // TODO Auto-generated method stub
  }

  @Override
  public String printConfig() {
    // TODO Auto-generated method stub
    return null;
  }
  
  
  private Set<String> retrieveDataFieldConfigsSubjectCanAccess(Subject subject, DataFieldCache dataFieldCache) {
    
    MultiKey sourceIdSubjectId = new MultiKey(subject.getSourceId(), subject.getId());
    Set<String> dataFieldConfigIds = dataFieldCache.sourceIdSubjectIdToDataFieldConfigIds.get(sourceIdSubjectId);
    
    if (dataFieldConfigIds != null) {
      return dataFieldConfigIds;
    }
    
    Set<String> result = new HashSet<String>();
    
    Map<String, GrouperPrivacyRealmConfig> privacyRealmConfigByConfigId = dataFieldCache.dataEngine.getPrivacyRealmConfigByConfigId();
    
    Set<String> groupsToLookup = new HashSet<String>();
    for (GrouperDataFieldConfig dataFieldConfig: dataFieldCache.allDataFieldConfigIds) {
      String grouperPrivacyRealmConfigId = dataFieldConfig.getGrouperPrivacyRealmConfigId();
      
      GrouperPrivacyRealmConfig privacyRealmConfig = privacyRealmConfigByConfigId.get(grouperPrivacyRealmConfigId);
      if (privacyRealmConfig == null) {
        String errorMessage = "Privacy Realm Config Id not found: '"+grouperPrivacyRealmConfigId+"' for data field config id: '"+dataFieldConfig.getConfigId()+"'";
        log.error(errorMessage);
        throw new RuntimeException(errorMessage);
      }
      
      String highestLevelAccess = dataFieldCache.dataEngine.calculateHighestLevelAccess(privacyRealmConfig, subject);
      if (StringUtils.equalsAny(highestLevelAccess, "update", "read")) {
        result.add(dataFieldConfig.getConfigId());
      }
      
//      String readersGroupName = privacyRealmConfig.getPrivacyRealmReadersGroupName();
//      String updatersGroupName = privacyRealmConfig.getPrivacyRealmUpdatersGroupName();
//      String viewersGroupName = privacyRealmConfig.getPrivacyRealmViewersGroupName();
//      
//      if (StringUtils.isNotBlank(readersGroupName)) {        
//        groupsToLookup.add(readersGroupName);
//      }
//      if (StringUtils.isNotBlank(updatersGroupName)) {             
//        groupsToLookup.add(updatersGroupName);
//      }
//      if (StringUtils.isNotBlank(viewersGroupName)) {             
//        groupsToLookup.add(viewersGroupName);
//      }
    }
    
//    String sql = """
//        SELECT gg.name AS group_name
//           FROM grouper_sql_cache_group gscg,
//            grouper_sql_cache_mship gscm,
//            grouper_fields gf,
//            grouper_groups gg,
//            grouper_members gm
//          WHERE gscg.group_internal_id = gg.internal_id
//          AND gscg.field_internal_id = gf.internal_id 
//          AND gscm.sql_cache_group_internal_id = gscg.internal_id 
//          AND gscm.member_internal_id = gm.internal_id
//          and gf.name = 'members'
//          and gm.subject_source = ?
//          and gm.subject_id = ?
//          and gg.name in (
//        """ + GrouperClientUtils.appendQuestions(GrouperUtil.length(groupsToLookup)) + ")";
//    
//    GcDbAccess gcDbAccess = new GcDbAccess().sql(sql).addBindVar(subject.getSourceId()).addBindVar(subject.getId());
//    
//    for (String groupName: groupsToLookup) {
//      gcDbAccess.addBindVar(groupName);
//    }
    
//    Set<String> groupNamesSubjectSessionIsIn = new HashSet<String>(gcDbAccess.selectList(String.class));
    
//    for (GrouperDataFieldConfig dataFieldConfig: dataFieldConfigs) {
//      String grouperPrivacyRealmConfigId = dataFieldConfig.getGrouperPrivacyRealmConfigId();
//      
//      GrouperPrivacyRealmConfig privacyRealmConfig = privacyRealmConfigByConfigId.get(grouperPrivacyRealmConfigId);
//      
//      String readersGroupName = privacyRealmConfig.getPrivacyRealmReadersGroupName();
//      String updatersGroupName = privacyRealmConfig.getPrivacyRealmUpdatersGroupName();
//      
//      if (groupNamesSubjectSessionIsIn.contains(readersGroupName)) {
//        result.add(dataFieldConfig.getConfigId());
//        continue;
//      }
//      
//      if (groupNamesSubjectSessionIsIn.contains(updatersGroupName)) {
//        result.add(dataFieldConfig.getConfigId());
//        continue;
//      }
//      
//    }
    
    dataFieldCache.sourceIdSubjectIdToDataFieldConfigIds.put(sourceIdSubjectId, result);
    
    return result;
  }
  
  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubjectsByIds(java.util.Collection)
   */
  @Override
  public Map<String, Subject> getSubjectsByIds(Collection<String> subjectIds) {

    if (subjectIds == null) {
      return null;
    }

    DataFieldCache dataFieldCache = dataFieldCache();
    
    GrouperSession currentSession = GrouperSession.staticGrouperSession();
    
    Subject subjectForSession = currentSession.getSubject();
    
    //TODO return no subjects if not allowed to see?
    Set<String> fieldConfigs = retrieveDataFieldConfigsSubjectCanAccess(subjectForSession, dataFieldCache);
    GrouperUtil.assertion(GrouperUtil.length(fieldConfigs) > 0, "No data field config ids!!");
    
    Map<String, Subject> results = new HashMap<String, Subject>();
    
    if (subjectIds.size() > 0) {
      
      int batchSize = 800;
      int numberOfBatches = SubjectApiUtils.batchNumberOfBatches(subjectIds, batchSize);
      
      List<String> idsList = new ArrayList<String>(subjectIds);
      
      for (int i=0;i<numberOfBatches;i++) {

        List<String> idsBatch = SubjectApiUtils.batchList(idsList, batchSize, i);        

        List<String> args = new ArrayList<String>();
       
        //TODO make this a query and not a view
        StringBuilder query = new StringBuilder("select gm.subject_id, gdfa.data_field_config_id, gdfa.value_text from grouper_members gm, grouper_data_field_assign_v gdfa where "
            + "gm.id = gdfa.member_id and gdfa.subject_source_id = ? and ( ");
        
        args.add(this.getId());
        
        for (int j = 0; j<idsBatch.size(); j++) {
          
          if (j>0) {
            query.append(" or ");
          } 
          query.append(" gm.subject_id = ? ");
          
          args.add(idsBatch.get(j));
          
        }
        
        query.append(" ) and gdfa.data_field_config_id in ("+ GrouperClientUtils.appendQuestions(GrouperUtil.length(fieldConfigs)) + ")");
        
        for (String configId: fieldConfigs) {
          args.add(configId);
        }
        
        Set<Subject> subjects = search(query.toString(), args, false, false, null, null, dataFieldCache);
        
        for (Subject subject : SubjectApiUtils.nonNull(subjects)) {
          results.put(subject.getId(), subject);
        }
      }
    }
    return results;
  }
  
  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubjectsByIdentifiers(java.util.Collection)
   */
  @Override
  public Map<String, Subject> getSubjectsByIdentifiers(Collection<String> identifiers) {

    if (identifiers == null) {
      return null;
    }
    
    DataFieldCache dataFieldCache = dataFieldCache();
    GrouperSession currentSession = GrouperSession.staticGrouperSession();
    
    Set<String> identifierDataFieldConfigIds = new HashSet<>(dataFieldCache.identifierDataFieldConfigIds);
    
    Subject subjectForSession = currentSession.getSubject();
    
    //TODO return no subjects if not allowed to see?
    Set<String> fieldConfigs = retrieveDataFieldConfigsSubjectCanAccess(subjectForSession, dataFieldCache);
    identifierDataFieldConfigIds.retainAll(fieldConfigs);
    
    if (GrouperUtil.length(identifierDataFieldConfigIds) == 0) {
      return getSubjectsByIds(identifiers);
    }
    
    GrouperUtil.assertion(GrouperUtil.length(fieldConfigs) > 0, "No data field config ids!!");
    
    Map<String, Subject> results = new HashMap<String, Subject>();
    
    if (identifiers.size() > 0) {
      
      int batchSize = 800/identifierDataFieldConfigIds.size();
      int numberOfBatches = SubjectApiUtils.batchNumberOfBatches(identifiers, batchSize);
      
      List<String> identifiersList = new ArrayList<String>(identifiers);
      
      Map<String, String> overallSubjectIdentifierToSubjectId = new HashMap<String, String>();
      
      for (int i=0;i<numberOfBatches; i++) {

        List<String> identifierBatch = SubjectApiUtils.batchList(identifiersList, batchSize, i);        
        
        //retrieve all the member ids that match the identifiers
        StringBuilder subjectIdsQuery = new StringBuilder("select gdfa.subject_id,  gdfa.data_field_config_id, gdfa.value_text from grouper_data_field_assign_v gdfa where "
            + " gdfa.subject_source_id = ? and ( ");
        
        GcDbAccess gcDbAccess = new GcDbAccess();
        
        gcDbAccess.addBindVar(this.getId());
        
        Map<String, String> subjectIdToSubjectIdentifier = new HashMap<String, String>();
        boolean first = true;
        for (int j = 0; j<identifierBatch.size(); j++) {
          
          for (String dataFieldConfigId: identifierDataFieldConfigIds) {
            
            if (!first) {
              subjectIdsQuery.append(" or ");
            } 
            first = false;
            subjectIdsQuery.append(" (gdfa.data_field_config_id = ? and gdfa.value_text = ?) ");
            gcDbAccess.addBindVar(dataFieldConfigId);
            gcDbAccess.addBindVar(identifierBatch.get(j));
          }
        }
        
        subjectIdsQuery.append(" ) ");
        
        List<Object[]> subjectIdsResult = gcDbAccess.sql(subjectIdsQuery.toString()).selectList(Object[].class);
        
        //loop through the member ids result and populate the map of member id to subject identifier
        for (Object[] row : subjectIdsResult) {
          String subjectId = GrouperUtil.stringValue(row[0]);
          String dataFieldConfigId = GrouperUtil.stringValue(row[1]);
          String subjectIdentifierValue = GrouperUtil.stringValue(row[2]);
         
          // make sure overall two memberids do not have the same subject identifier value
          if (overallSubjectIdentifierToSubjectId.containsKey(subjectIdentifierValue)) {
            String existingMemberId = overallSubjectIdentifierToSubjectId
                .get(subjectIdentifierValue);
            if (!StringUtils.equals(existingMemberId, subjectId)) {
              throw new SubjectNotUniqueException(
                  "Two member ids have the same subject identifier value: "
                      + subjectIdentifierValue + ", " + existingMemberId + ", "
                      + subjectId);
            }
          }
          
          subjectIdToSubjectIdentifier.put(subjectId, subjectIdentifierValue);
          overallSubjectIdentifierToSubjectId.put(subjectIdentifierValue, subjectId);
        }
        
        // if there are no subject ids, then return empty map
        if (GrouperUtil.length(subjectIdToSubjectIdentifier) == 0) {
          return results;
        }

        List<String> args = new ArrayList<String>();
       
        //TODO make this a query and not a view
        StringBuilder query = new StringBuilder("select gm.subject_id, gdfa.data_field_config_id, gdfa.value_text from grouper_members gm, grouper_data_field_assign_v gdfa where "
            + "gm.id = gdfa.member_id and gdfa.subject_source_id = ? and ( ");
        
        args.add(this.getId());
        
        // make a list of subject ids
        List<String> subjectIds = new ArrayList<String>(subjectIdToSubjectIdentifier.keySet());
        for (int j = 0; j<subjectIds.size(); j++) {
          
          if (j>0) {
            query.append(" or ");
          } 
          query.append(" gm.subject_id = ? ");
          
          args.add(subjectIds.get(j));
          
        }
        
        query.append(" ) and gdfa.data_field_config_id in ("+ GrouperClientUtils.appendQuestions(GrouperUtil.length(fieldConfigs)) + ")");
        
        for (String configId: fieldConfigs) {
          args.add(configId);
        }
        
        Set<Subject> subjects = search(query.toString(), args, false, false, null, null, dataFieldCache);
        
        for (Subject subject : SubjectApiUtils.nonNull(subjects)) {
          String subjectId = subject.getId();
          String subjectIdentifier = subjectIdToSubjectIdentifier.get(subjectId);
          results.put(subjectIdentifier, subject);
        }
      }
    }
    return results;
  }
  
  @Override
  public Subject getSubject(String subjectId) throws SubjectNotFoundException, SubjectNotUniqueException {
    
    Map<String, Subject> subjectMap = getSubjectsByIds(SubjectApiUtils.toSet(subjectId));
    
    if (SubjectApiUtils.length(subjectMap) > 1) {
      throw new RuntimeException("Why are there more than one result??? " + subjectId + ", " + SubjectApiUtils.length(subjectMap) + " in source: " + this.getId());
    }
    
    Subject subject = null;
    
    if (SubjectApiUtils.length(subjectMap) == 1) {
      subject = subjectMap.values().iterator().next();
    }
    
    if (subject == null) {
      throw new SubjectNotFoundException("Subject not found by id: " + subjectId + " in source: " + this.getId());
    }
    return subject;
  }

  /**
   * Perform a search for subjects
   * 
   * @param query is query to run, prepared statement args should be question marks
   * @param args are the prepared statement args
   * @param expectSingle true if expecting one answer
   * @param exceptionIfNull 
   * @param firstPageOnly if we should only get first page
   * @param tooManyResults flag to return for too many results
   * @param identifiersForIdentifierToMap optional, if we want the resultIdentifierMap back, then pass in the identifiers
   * @param resultIdentifierToSubject optional, if we want the resultIdentifierMap back, then pass in the map,
   * and this will populate it
   * @return subjects or empty set if none or null if expect one and no results and not exception on null
   * @throws SubjectNotFoundException if expecting one and not found
   * @throws SubjectNotUniqueException
   * @throws InvalidQueryRuntimeException 
   */
  private Set<Subject> search(String query, List<String> args, boolean expectSingle, 
      boolean exceptionIfNull,
      Collection<String> identifiersForIdentifierToMap, Map<String, Subject> resultIdentifierToSubject, DataFieldCache dataFieldCache)
      throws SubjectNotFoundException, SubjectNotUniqueException,
      InvalidQueryRuntimeException {

    if (resultIdentifierToSubject != null) {
      if (SubjectApiUtils.length(identifiersForIdentifierToMap) == 0) {
        throw new RuntimeException("Why is there no identifiersForIdentifierToMap???");
      }
    }

    Set<Subject> results = new LinkedHashSet<Subject>();

    try {
      
      GcDbAccess gcDbAccess = new GcDbAccess().sql(query);
      
      for (String arg: GrouperUtil.nonNull(args)) {
        gcDbAccess.addBindVar(arg);
      }
      
      List<Object[]> dbRows = gcDbAccess.selectList(Object[].class);
      if (GrouperUtil.length(dbRows) == 0) {
       
        if (exceptionIfNull) {
          throw new SubjectNotFoundException("Subject not found: " + query + ", "
              + StringUtils.join(args.iterator(), ",")  + " in source: " + this.getId());
        }
        
        return null;
        
      }
      
      Map<String, Map<String, Set<String>>> subjectIdToDataFieldAttributes = new HashMap<String, Map<String, Set<String>>>();
      
      for (Object[] dbRow: dbRows) {
        
        String subjectId = GrouperUtil.stringValue(dbRow[0]);
        String dataFieldConfigId = GrouperUtil.stringValue(dbRow[1]);
        String valueText = GrouperUtil.stringValue(dbRow[2]);
        
        Map<String, Set<String>> subjectAttributes = subjectIdToDataFieldAttributes.get(subjectId);
        if (subjectAttributes == null) {
          subjectAttributes = new HashMap<String, Set<String>>();
          subjectIdToDataFieldAttributes.put(subjectId, subjectAttributes);
        }
        
        Set<String> valueSet = subjectAttributes.get(dataFieldConfigId);
        if (valueSet == null) {
          valueSet = new HashSet<String>();
          subjectAttributes.put(dataFieldConfigId, valueSet);
        }
        
        valueSet.add(valueText);
        
      }
      
      SubjectImpl subjectResult = null;
      for (String subjectId: subjectIdToDataFieldAttributes.keySet()) {
        Map<String, Set<String>> dataFieldConfigIdToSetOfValues = subjectIdToDataFieldAttributes.get(subjectId); // data field attributes
        
        Map<String, Object> sourceAttributesToValues = new CaseInsensitiveMap();

        for (String dataFieldConfigId: dataFieldConfigIdToSetOfValues.keySet()) {   
          
          Set<String> dataFieldAttributeValues = dataFieldConfigIdToSetOfValues.get(dataFieldConfigId);
          if (dataFieldCache.dataFieldMultivaluedConfigIds.contains(dataFieldConfigId)) {
            //multivalued
            Set<String> values = new HashSet<String>();
            
            if (dataFieldCache.dataFieldFormatToLowerCaseConfigIds.contains(dataFieldConfigId)) {
              for (String singleValue: dataFieldAttributeValues) {
                singleValue = singleValue == null ? null: singleValue.toLowerCase();
                values.add(singleValue);
              }
            } else {
              values.addAll(dataFieldAttributeValues);
            }
            
            sourceAttributesToValues.put(dataFieldConfigId.toLowerCase(), values);
          } else {
            //single value
            String singleValue = GrouperUtil.length(dataFieldAttributeValues) == 0 ? null : dataFieldAttributeValues.iterator().next();
            
            if (dataFieldCache.dataFieldFormatToLowerCaseConfigIds.contains(dataFieldConfigId) && singleValue != null) {
              singleValue = singleValue.toLowerCase();
            }
            
            sourceAttributesToValues.put(dataFieldConfigId.toLowerCase(), singleValue);
          }
        }
        
        subjectResult = (SubjectImpl) createSubject(sourceAttributesToValues, subjectId);
        // loop through the privacy priority map and set the subject attribute for each key in the map to be the highest priority value that's not blank
        for (String attributeName : dataFieldCache.attributeNameToListOfPrioritizedPrivacyAttributeNames.keySet()) {
          List<String> privacyAttributeNames = dataFieldCache.attributeNameToListOfPrioritizedPrivacyAttributeNames.get(attributeName);
          Set<String> values = null;
          for (String privacyAttributeName : privacyAttributeNames) {
            if (privacyAttributeName == null) {
              continue;
            }
            Set<String> attributeValues = subjectResult.getAttributeValues(privacyAttributeName.toLowerCase());
            if (GrouperUtil.length(attributeValues) > 0 && StringUtils.isNotBlank(attributeValues.iterator().next())) {
              values = attributeValues;
              break;
            }
          }
          
          subjectResult.internalAssignAttribute(attributeName, values);
        }
        results.add(subjectResult);
        //TODO add privacy
      }
      
      if (subjectResult == null && exceptionIfNull) {
        throw new SubjectNotFoundException("Subject not found: " + query + ", "
            + StringUtils.join(args.iterator(), ",")  + " in source: " + this.getId());
      }
      

    } finally {
      if (log.isDebugEnabled()) {
        log.debug("Query returned " + results.size() + ", " + query + ", " + SubjectUtils.toStringForLog(args));
      }
    }

    if (expectSingle) {
      if (results.size() > 1) {
        throw new SubjectNotUniqueException("Multiple subjects exist: " + query + ", "
            + StringUtils.join(args.iterator(), ",") + " in source: " + this.getId());
      }
      if (results.size() == 0) {
        if (exceptionIfNull) {
          throw new SubjectNotFoundException("Subject not found: " + query + ", "
              + StringUtils.join(args.iterator(), ",")  + " in source: " + this.getId());
        }
        results = null;
      }
    }

    return results;
  }
  
  @Override
  public Subject getSubjectByIdentifier(String identifier) throws SubjectNotFoundException, SubjectNotUniqueException {
    
    Map<String, Subject> subjectMap = getSubjectsByIdentifiers(SubjectApiUtils.toSet(identifier));
    
    if (SubjectApiUtils.length(subjectMap) > 1) {
      throw new RuntimeException("Why are there more than one result??? " + identifier + ", " + SubjectApiUtils.length(subjectMap));
    }

    Subject subject = null;

    if (SubjectApiUtils.length(subjectMap) == 1) {
      subject = subjectMap.values().iterator().next();
    }

    if (subject == null) {
      throw new SubjectNotFoundException("Subject not found by identifier: " + identifier + " in source: " + this.getId());
    }
    return subject;
    
  }

  @Override
  public Set<Subject> search(String searchValue) {
    
    //make result
    if (StringUtils.isBlank(searchValue)) {
      return new HashSet<Subject>();
    }
    
    Set<Subject> subjects = new HashSet<Subject>();
    
    DataFieldCache dataFieldCache = dataFieldCache();
    GrouperSession currentSession = GrouperSession.staticGrouperSession();
    
    Subject subjectForSession = currentSession.getSubject();
    
    //TODO return no subjects if not allowed to see?
    Set<String> fieldConfigs = retrieveDataFieldConfigsSubjectCanAccess(subjectForSession, dataFieldCache);
    
    GrouperUtil.assertion(GrouperUtil.length(fieldConfigs) > 0, "No data field config ids!!");
    
    //lets split by any whitespace space
    String[] terms = searchValue.split("\\s+");
        
    //retrieve all the member ids that match the identifiers
    StringBuilder subjectIdsQuery = new StringBuilder("select gm.subject_id from grouper_members gm where "
        + " gm.subject_source = ? and ");
    
    GcDbAccess gcDbAccess = new GcDbAccess();
    
    gcDbAccess.addBindVar(this.getId());
    
    for (int i = 0; i < terms.length; i++) {
      subjectIdsQuery.append(" gm.search_string0 like ?");
      gcDbAccess.addBindVar("%" + terms[i].toLowerCase() + "%");
      if (i != terms.length - 1) {
        subjectIdsQuery.append(" and ");
      }
    }
    
    List<String> subjectIds = gcDbAccess.sql(subjectIdsQuery.toString()).selectList(String.class);
    if (GrouperUtil.length(subjectIds) == 0) {
     return subjects; 
    }
        
    List<String> args = new ArrayList<String>();
       
    //TODO make this a query and not a view
    StringBuilder query = new StringBuilder("select gm.subject_id, gdfa.data_field_config_id, gdfa.value_text from grouper_members gm, grouper_data_field_assign_v gdfa where "
        + "gm.id = gdfa.member_id and gdfa.subject_source_id = ? and ( ");
    
    args.add(this.getId());
    
    // make a list of subject ids
    for (int j = 0; j<subjectIds.size(); j++) {
      
      if (j>0) {
        query.append(" or ");
      } 
      query.append(" gm.subject_id = ? ");
      
      args.add(subjectIds.get(j));
      
    }
    
    query.append(" ) and gdfa.data_field_config_id in ("+ GrouperClientUtils.appendQuestions(GrouperUtil.length(fieldConfigs)) + ")");
    
    for (String configId: fieldConfigs) {
      args.add(configId);
    }
        
    subjects = search(query.toString(), args, false, false, null, null, dataFieldCache);
    return subjects;
  }

  
  /** if there is a limit to the number of results */
  private Integer maxResults;

  private Integer maxPage;
  
  private boolean errorOnMaxResults;

  private static class DataFieldCache {
    
    private GrouperDataEngine dataEngine;
    
    private GrouperConfig grouperConfig;
    
    private Set<GrouperDataFieldConfig> allDataFieldConfigIds = new LinkedHashSet<GrouperDataFieldConfig>();
    
    private Map<String, List<String>> dataFieldConfigIdToPrioritizedAttributeNames = new HashMap<String, List<String>>();
    
    private Set<String> dataFieldMultivaluedConfigIds = new HashSet<String>(); 
    
    private Set<String> dataFieldFormatToLowerCaseConfigIds = new HashSet<String>(); 
    
    private Set<String> identifierDataFieldConfigIds = new LinkedHashSet<String>();
    
    private Map<MultiKey, Set<String>> sourceIdSubjectIdToDataFieldConfigIds = new HashMap<MultiKey, Set<String>>();
    
    private Map<String, List<String>> attributeNameToListOfPrioritizedPrivacyAttributeNames = new HashMap<String, List<String>>();
    
    private Set<GrouperDataFieldConfig> retrieveAllDataFieldConfigIds(Source source) {
      
      Map<String, GrouperDataFieldConfig> fieldConfigByConfigId = dataEngine.getFieldConfigByConfigId();
      
      String extraAttributesFromSource = "subjectApi.source."+source.getConfigId()+".extraAttributesFromSource";
      String extraAttributes = SubjectConfig.retrieveConfig().propertyValueString(extraAttributesFromSource);
      Set<String> dataFieldConfigIds = null;
      if (StringUtils.isBlank(extraAttributes)) {
        dataFieldConfigIds = new HashSet<String>();
      } else {      
        dataFieldConfigIds = GrouperUtil.splitTrimToSet(extraAttributes, ",") ;
      }

      this.identifierDataFieldConfigIds.clear();
      
      String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + source.getConfigId() + ".numberOfAttributes");
      if (StringUtils.isNotBlank(numberOfAttributes)) {
        
        int numberOfAttrs = Integer.parseInt(numberOfAttributes);
        for (int i=0; i<numberOfAttrs; i++) {
          
          String dataFieldConfigIdSourceAttribute = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + source.getConfigId() + ".attribute."+i+".sourceAttribute");
          String dataFieldConfigIdSubjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + source.getConfigId() + ".attribute."+i+".name");
          String dataFieldConfigIdTranslationType = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + source.getConfigId() + ".attribute."+i+".translationType");
          boolean isMultivalued = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + source.getConfigId() + ".attribute."+i+".multivaluedDataFieldAttribute", false);
          boolean formatToLowerCase = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + source.getConfigId() + ".attribute."+i+".formatToLowerCase", false);
          
          if (isMultivalued) {
            this.dataFieldMultivaluedConfigIds.add(dataFieldConfigIdSourceAttribute);
          }
          if (formatToLowerCase) {
            this.dataFieldFormatToLowerCaseConfigIds.add(dataFieldConfigIdSourceAttribute);
          }
          
          //if translation type is dataFieldPrivacyTarget then add a key to the privacy attribute names map
          if (StringUtils.equals(dataFieldConfigIdTranslationType, "dataFieldPrivacyTarget")) {
            this.attributeNameToListOfPrioritizedPrivacyAttributeNames
                .put(dataFieldConfigIdSubjectAttributeName, new ArrayList<String>());
          }
          
          boolean privacyDataFieldSource = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + source.getConfigId() + ".attribute."+i+".privacyDataFieldSource", false);
          
          if (privacyDataFieldSource) {
            //privacyAttributeName
            String privacyAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + source.getConfigId() + ".attribute."+i+".privacyAttributeName");
            // int privacyPriority
            int privacyPriority = SubjectConfig.retrieveConfig().propertyValueInt("subjectApi.source." + source.getConfigId() + ".attribute."+i+".privacyPriority");
            // if privacyDataFieldSource then add the attribute name to the list of privacy attributes in the values of the map in the index of the privacy level
            if (privacyDataFieldSource && StringUtils.isNotBlank(privacyAttributeName)) {
              List<String> privacyAttributeNames = this.attributeNameToListOfPrioritizedPrivacyAttributeNames.get(privacyAttributeName);
              if (privacyAttributeNames == null) {
                privacyAttributeNames = new ArrayList<String>();
                this.attributeNameToListOfPrioritizedPrivacyAttributeNames
                    .put(privacyAttributeName, privacyAttributeNames);
              }
              while (privacyAttributeNames.size() <= privacyPriority - 1) {
                privacyAttributeNames.add(null);
              }
              privacyAttributeNames.set(privacyPriority - 1, dataFieldConfigIdSubjectAttributeName);
            }
          }
          
          String dataFieldConfigId = null;
          if (StringUtils.equals(dataFieldConfigIdTranslationType, "sourceAttributeSameAsSubjectAttribute")) {
            dataFieldConfigId = dataFieldConfigIdSubjectAttributeName;
          } else if (StringUtils.equals(dataFieldConfigIdTranslationType, "sourceAttribute")) {
            dataFieldConfigId = dataFieldConfigIdSourceAttribute;
          } else {
            continue;
          }
          
          dataFieldConfigIds.add(dataFieldConfigId);
          
          boolean isSubjectIdentifier = GrouperUtil.booleanValue(SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + source.getConfigId() + ".attribute."+i+".subjectIdentifier"), false);
          if (isSubjectIdentifier) {
            this.identifierDataFieldConfigIds.add(dataFieldConfigId);
          }
          
        }
      }
      
      Set<GrouperDataFieldConfig> dataFieldConfigs = new HashSet<GrouperDataFieldConfig>();
      for (String fieldConfigId: dataFieldConfigIds) {
        if (fieldConfigByConfigId.containsKey(fieldConfigId)) {
          GrouperDataFieldConfig grouperDataFieldConfig = fieldConfigByConfigId.get(fieldConfigId);
          dataFieldConfigs.add(grouperDataFieldConfig);
        } else {
          throw new RuntimeException("Found invalid config id: '"+fieldConfigId+"', Source id: '"+source.getId()+"'");
        }
      }
      
      return dataFieldConfigs;
    }
    
  }
  
  private ExpirableCache<Boolean, DataFieldCache> dataFieldCacheExpirableCache = new ExpirableCache<Boolean, GrouperDataFieldSourceAdapter.DataFieldCache>(2);
  
  private DataFieldCache dataFieldCache() {
    DataFieldCache dataFieldCache  = dataFieldCacheExpirableCache.get(Boolean.TRUE);
    if (dataFieldCache == null) {
      
      synchronized (dataFieldCacheExpirableCache) {
        dataFieldCache = dataFieldCacheExpirableCache.get(Boolean.TRUE);
        if (dataFieldCache == null) {
          dataFieldCache = new DataFieldCache();
          
          dataFieldCache.dataEngine = new GrouperDataEngine();
          dataFieldCache.grouperConfig = GrouperConfig.retrieveConfig();
          dataFieldCache.dataEngine.loadFieldsAndRows(dataFieldCache.grouperConfig);
          dataFieldCache.allDataFieldConfigIds = dataFieldCache.retrieveAllDataFieldConfigIds(this);
          dataFieldCacheExpirableCache.put(Boolean.TRUE, dataFieldCache);
        }
      }
    }
    
    return dataFieldCache;
  }
  
  @Override
  public void init() throws SourceUnavailableException {
    
    SubjectConfig.clearCache();
    SubjectConfig subjectConfig = SubjectConfig.retrieveConfig();
    
    try {
      Properties props = initParams();
      //this might not exist if it is Grouper source and no driver...
      
      this.nameAttributeName = props.getProperty("Name_AttributeType");
      if (this.nameAttributeName == null) {
        throw new SourceUnavailableException("Name_AttributeType not defined, source: "
            + this.getId());
      }
      this.descriptionAttributeName = props.getProperty("Description_AttributeType");
      if (this.descriptionAttributeName == null) {
        throw new SourceUnavailableException(
            "Description_AttributeType not defined, source: " + this.getId());
      }
      
      {
        String maxResultsString = props.getProperty("maxResults");
        if (!StringUtils.isBlank(maxResultsString)) {
          try {
            this.maxResults = Integer.parseInt(maxResultsString);
          } catch (NumberFormatException nfe) {
            throw new SourceUnavailableException("Cant parse maxResults: " + maxResultsString, nfe);
          }
        }
      }
      
      {
        String errorOnMaxResultsString = props.getProperty("errorOnMaxResults");
        if (!StringUtils.isBlank(errorOnMaxResultsString)) {
          this.errorOnMaxResults = SubjectUtils.booleanValue(errorOnMaxResultsString, true);
        }
      }
      
      {
        String maxPageString = props.getProperty("maxPageSize");
        if (!StringUtils.isBlank(maxPageString)) {
          try {
            this.maxPage = Integer.parseInt(maxPageString);
          } catch (NumberFormatException nfe) {
            throw new SourceUnavailableException("Cant parse maxPage: " + maxPageString, nfe);
          }
        }
      }
      
      {
        LinkedHashMap<Integer, String> temp = new LinkedHashMap<Integer, String>();
        
        String searchAttributeCountKey = "subjectApi.source."+this.getConfigId()+".searchAttributeCount";
        int searchAttributeCount = subjectConfig.propertyValueInt(searchAttributeCountKey, 0);
        
        for (int i = 0; i < searchAttributeCount; i++) {

          String searchAttributeName = subjectConfig.propertyValueString("subjectApi.source."+this.getConfigId()+".searchAttribute."+i+".attributeName");
          temp.put(i, searchAttributeName);
        }
        this.searchAttributes = temp;
      }
      
      {
        LinkedHashMap<Integer, String> temp = new LinkedHashMap<Integer, String>();
        
        String sortAttributeCountKey = "subjectApi.source."+this.getConfigId()+".sortAttributeCount";
        int sortAttributeCount = subjectConfig.propertyValueInt(sortAttributeCountKey, 0);
        
        for (int i = 0; i < sortAttributeCount; i++) {
          
          String sortAttributeName = subjectConfig.propertyValueString("subjectApi.source."+this.getConfigId()+".sortAttribute."+i+".attributeName");
          temp.put(i, sortAttributeName);
        }
        this.sortAttributes = temp;
      }

      {
        String typesAttributeName = "subjectApi.source."+this.getConfigId()+".types";
        String type = subjectConfig.propertyValueString(typesAttributeName);
        if (StringUtils.isBlank(type)) {
          throw new SourceUnavailableException(
              typesAttributeName + " is required in source: " + this.getId());
        }
        addSubjectType(type);
      }
      
      dataFieldCache();

    } catch (Exception ex) {
      throw new SourceUnavailableException(
          "Unable to init subject.properties Datafield source, source: " + this.getId(), ex);
    }
    
//    GrouperDataEngine dataEngine = new GrouperDataEngine();
//    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
//    dataEngine.loadFieldsAndRows(grouperConfig);
//    
//    Map<String, GrouperDataFieldConfig> fieldConfigByConfigId = dataEngine.getFieldConfigByConfigId();
//    
//    String extraAttributesFromSource = "subjectApi.source."+this.getConfigId()+".extraAttributesFromSource";
//    String extraAttributes = SubjectConfig.retrieveConfig().propertyValueString(extraAttributesFromSource);
//    Set<String> dataFieldConfigIds = GrouperUtil.splitTrimToSet(extraAttributes, ",");
//    
//    String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
//    if (StringUtils.isNotBlank(numberOfAttributes)) {
//      
//      int numberOfAttrs = Integer.parseInt(numberOfAttributes);
//      for (int i=0; i<numberOfAttrs; i++) {
//        
//        String sourceAttribute = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".sourceAttribute");
//        if (StringUtils.isBlank(sourceAttribute)) {
//          continue;
//        }
//        
//        dataFieldConfigIds.add(sourceAttribute);
//        
//      }
//    }
//    
//    Set<GrouperDataFieldConfig> dataFieldConfigs = new HashSet<GrouperDataFieldConfig>();
//    for (String fieldConfigId: dataFieldConfigIds) {
//      if (fieldConfigByConfigId.containsKey(fieldConfigId)) {
//        GrouperDataFieldConfig grouperDataFieldConfig = fieldConfigByConfigId.get(fieldConfigId);
//        dataFieldConfigs.add(grouperDataFieldConfig);
//      }
//    }
    
//    dataFieldConfigIdToPrioritizedAttributeNames.put("name", );
//    dataFieldConfigIdToPrioritizedAttributeNames.put("description", );
    
  }
  

}
