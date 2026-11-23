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
import edu.internet2.middleware.grouper.member.SearchStringEnum;
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
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.InvalidQueryRuntimeException;
import edu.internet2.middleware.subject.provider.SubjectImpl;

public class GrouperDataFieldSourceAdapter extends BaseSourceAdapter {

  /** logger */
  private static Log log = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(GrouperDataFieldSourceAdapter.class);

  /**
   * for testing only, if true then getSubjectsByIdentifiers will throw a RuntimeException
   * to simulate a database timeout
   */
  public static boolean testingThrowExceptionOnGetSubjectsByIdentifiers = false;
  
  @Override
  public boolean isEditable() {
    return true;
  }
  
  @Override
  public Map<Integer, String> getSortAttributes() {
    return this.sortAttributes;
  }

  /**
   * prefix of the internal subject attributes (index appended) that hold a configured search
   * attribute computed only from data fields everyone can read (public or authenticated privacy
   * realm).  For each search string index that is open to every user (no
   * security.member.search.stringN.wheelOnly / allowOnlyGroup), getSearchAttributes() returns this
   * name, so the search string Grouper stores in grouper_members.search_stringN never contains
   * values of realm-restricted fields.  The provider sync resolves subjects as root, which can read
   * every realm; without this, any user could find subjects by the values of fields they cannot
   * read.  An index the site restricted keeps the configured attribute (full values): the site
   * limits who can search it.
   */
  public static final String PUBLIC_SEARCH_ATTRIBUTE_PREFIX = "grouperDataFieldPublicSearchAttribute";

  /**
   * the search attributes Grouper stores in grouper_members.search_stringN: for each configured
   * subjectApi.source.&lt;id&gt;.searchAttribute.N, the internal attribute PUBLIC_SEARCH_ATTRIBUTE_PREFIX + N,
   * or the configured attribute if search string N is restricted
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSearchAttributes()
   */
  @Override
  public Map<Integer, String> getSearchAttributes() {
    Map<Integer, String> result = new LinkedHashMap<Integer, String>();
    if (this.searchAttributes != null) {
      for (Integer index : this.searchAttributes.keySet()) {
        result.put(index, isSearchStringRestricted(index) ? this.searchAttributes.get(index) : PUBLIC_SEARCH_ATTRIBUTE_PREFIX + index);
      }
    }
    return result;
  }

  /**
   * the configured internal attributes plus the public search attributes, so screens that hide
   * internal attributes do not list them
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getInternalAttributes()
   */
  @Override
  public Set<String> getInternalAttributes() {
    Set<String> result = new HashSet<String>();
    Set<String> configured = super.getInternalAttributes();
    if (configured != null) {
      result.addAll(configured);
    }
    for (String searchAttributeName : this.getSearchAttributes().values()) {
      if (searchAttributeName != null && searchAttributeName.startsWith(PUBLIC_SEARCH_ATTRIBUTE_PREFIX)) {
        result.add(searchAttributeName);
      }
    }
    return result;
  }

  /**
   * if the site limited who can search on grouper_members.search_stringN
   * (security.member.search.stringN.wheelOnly or .allowOnlyGroup, see SearchStringEnum)
   * @param index
   * @return true if restricted
   */
  private static boolean isSearchStringRestricted(int index) {
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("security.member.search.string" + index + ".wheelOnly", false)) {
      return true;
    }
    return !StringUtils.isBlank(GrouperConfig.retrieveConfig().propertyValueString("security.member.search.string" + index + ".allowOnlyGroup"));
  }

  /**
   * data field config ids in a privacy realm that every logged in user can read (public or
   * authenticated); only these feed the stored search strings
   * @param dataFieldCache
   * @return the config ids
   */
  private static Set<String> publicDataFieldConfigIds(DataFieldCache dataFieldCache) {
    Set<String> result = dataFieldCache.publicDataFieldConfigIds;
    if (result != null) {
      return result;
    }
    result = new HashSet<String>();
    Map<String, GrouperPrivacyRealmConfig> privacyRealmConfigByConfigId = dataFieldCache.dataEngine.getPrivacyRealmConfigByConfigId();
    for (GrouperDataFieldConfig dataFieldConfig: dataFieldCache.allDataFieldConfigIds) {
      GrouperPrivacyRealmConfig privacyRealmConfig = privacyRealmConfigByConfigId == null ? null
          : privacyRealmConfigByConfigId.get(dataFieldConfig.getGrouperPrivacyRealmConfigId());
      // a missing realm is not public (fail closed)
      if (privacyRealmConfig != null && (privacyRealmConfig.isPrivacyRealmPublic() || privacyRealmConfig.isPrivacyRealmAuthenticated())) {
        result.add(dataFieldConfig.getConfigId());
      }
    }
    dataFieldCache.publicDataFieldConfigIds = result;
    return result;
  }

  /**
   * set the internal public search attributes on a resolved subject: build a second subject from
   * only the publicly readable data fields of the same rows and copy each configured search
   * attribute's value from it, so the stored search strings are the same whoever resolved the subject
   * @param subjectResult the subject being returned
   * @param subjectId
   * @param dataFieldConfigIdToSetOfValues the data field values read for this subject
   * @param dataFieldCache
   */
  private void assignPublicSearchAttributes(SubjectImpl subjectResult, String subjectId,
      Map<String, Set<String>> dataFieldConfigIdToSetOfValues, DataFieldCache dataFieldCache) {

    if (this.searchAttributes == null || this.searchAttributes.size() == 0) {
      return;
    }

    Set<String> publicDataFieldConfigIds = publicDataFieldConfigIds(dataFieldCache);

    // same conversion as search(), restricted to publicly readable fields
    Map<String, Object> publicSourceAttributesToValues = new CaseInsensitiveMap();
    for (String dataFieldConfigId: dataFieldConfigIdToSetOfValues.keySet()) {
      if (!publicDataFieldConfigIds.contains(dataFieldConfigId)) {
        continue;
      }
      Set<String> dataFieldAttributeValues = dataFieldConfigIdToSetOfValues.get(dataFieldConfigId);
      boolean toLowerCase = dataFieldCache.dataFieldFormatToLowerCaseConfigIds.contains(dataFieldConfigId);
      if (dataFieldCache.dataFieldMultivaluedConfigIds.contains(dataFieldConfigId)) {
        Set<String> values = new HashSet<String>();
        for (String singleValue: GrouperUtil.nonNull(dataFieldAttributeValues)) {
          values.add(toLowerCase && singleValue != null ? singleValue.toLowerCase() : singleValue);
        }
        publicSourceAttributesToValues.put(dataFieldConfigId.toLowerCase(), values);
      } else {
        String singleValue = GrouperUtil.length(dataFieldAttributeValues) == 0 ? null : dataFieldAttributeValues.iterator().next();
        if (toLowerCase && singleValue != null) {
          singleValue = singleValue.toLowerCase();
        }
        publicSourceAttributesToValues.put(dataFieldConfigId.toLowerCase(), singleValue);
      }
    }

    SubjectImpl publicSubject = (SubjectImpl) createSubject(publicSourceAttributesToValues, subjectId);
    publicSubject.setResolvedFromSource(true);
    publicSubject.getTranslationMap().put("subjectid", subjectId);
    // same privacy priority attributes as the real subject, from the public values
    for (String attributeName : dataFieldCache.attributeNameToListOfPrioritizedPrivacyAttributeNames.keySet()) {
      List<String> privacyAttributeNames = dataFieldCache.attributeNameToListOfPrioritizedPrivacyAttributeNames.get(attributeName);
      Set<String> values = null;
      for (String privacyAttributeName : privacyAttributeNames) {
        if (privacyAttributeName == null) {
          continue;
        }
        Set<String> attributeValues = publicSubject.getAttributeValues(privacyAttributeName.toLowerCase());
        if (GrouperUtil.length(attributeValues) > 0 && StringUtils.isNotBlank(attributeValues.iterator().next())) {
          values = attributeValues;
          break;
        }
      }
      publicSubject.internalAssignAttribute(attributeName, values);
    }

    for (Integer index : this.searchAttributes.keySet()) {
      // a restricted search string keeps the configured attribute (see getSearchAttributes)
      if (isSearchStringRestricted(index)) {
        continue;
      }
      String searchAttributeName = this.searchAttributes.get(index);
      Set<String> values = StringUtils.isBlank(searchAttributeName) ? null : publicSubject.getAttributeValues(searchAttributeName, false);
      subjectResult.internalAssignAttribute(PUBLIC_SEARCH_ATTRIBUTE_PREFIX + index,
          values == null ? null : new LinkedHashSet<String>(values));
    }
  }

  /**
   * escape a search term for "like ? escape '!'" so % and _ in the term match literally
   * @param term
   * @return the escaped term
   */
  static String escapeLikeTerm(String term) {
    if (term == null) {
      return null;
    }
    return term.replace("!", "!!").replace("%", "!%").replace("_", "!_");
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
    String message = "subject.properties data field source id: " + this.getId();
    return message;
  }
  
  
  private Set<String> retrieveDataFieldConfigsSubjectCanAccess(Subject subject, DataFieldCache dataFieldCache) {
    
    MultiKey sourceIdSubjectId = new MultiKey(subject.getSourceId(), subject.getId());
    Set<String> dataFieldConfigIds = dataFieldCache.sourceIdSubjectIdToDataFieldConfigIds.get(sourceIdSubjectId);
    
    if (dataFieldConfigIds != null) {
      return dataFieldConfigIds;
    }
    
    Set<String> result = new HashSet<String>();
    
    Map<String, GrouperPrivacyRealmConfig> privacyRealmConfigByConfigId = dataFieldCache.dataEngine.getPrivacyRealmConfigByConfigId();
    
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
    }
    
    dataFieldCache.sourceIdSubjectIdToDataFieldConfigIds.put(sourceIdSubjectId, result);
    
    return result;
  }
  
  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubjectsByIds(java.util.Collection)
   */
  @Override
  public Map<String, Subject> getSubjectsByIds(Collection<String> subjectIds) {

    Map<String, Subject> results = new HashMap<String, Subject>();
    if (GrouperUtil.length(subjectIds) == 0) {
      return results;
    }

    DataFieldCache dataFieldCache = dataFieldCache();
    
    GrouperSession currentSession = GrouperSession.staticGrouperSession();
    
    Subject subjectForSession = currentSession.getSubject();
    
    Set<String> fieldConfigs = retrieveDataFieldConfigsSubjectCanAccess(subjectForSession, dataFieldCache);
    
    if (GrouperUtil.length(fieldConfigs) == 0) {
      return results;
    }
    
    if (subjectIds.size() > 0) {
      
      int batchSize = 800;
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(subjectIds, batchSize, false);
      
      List<String> idsList = new ArrayList<String>(subjectIds);
      
      for (int i=0;i<numberOfBatches;i++) {

        List<String> idsBatch = GrouperUtil.batchList(idsList, batchSize, i);

        List<Object> args = new ArrayList<Object>();

        StringBuilder query = new StringBuilder("""
            SELECT
              gm.subject_id,
              gdf.config_id AS data_field_config_id,
              gd.the_text AS value_text
          FROM
              grouper_members gm
          INNER JOIN
              grouper_data_field_assign gdfa
              ON gdfa.member_internal_id = gm.internal_id
          INNER JOIN
              grouper_data_field gdf
              ON gdfa.data_field_internal_id = gdf.internal_id
          LEFT JOIN
              grouper_dictionary gd
              ON gdfa.value_dictionary_internal_id = gd.internal_id
          WHERE
              gm.subject_source = ? and (
            """);

        args.add(this.getId());
        
        for (int j = 0; j<idsBatch.size(); j++) {
          
          if (j>0) {
            query.append(" or ");
          } 
          query.append(" gm.subject_id = ? ");
          
          args.add(idsBatch.get(j));
          
        }
        
        // Use internal IDs for data field filtering when available
        {
          List<Long> fieldInternalIds = new ArrayList<Long>();
          List<String> fieldConfigsWithoutInternalId = new ArrayList<String>();
          for (String configId : fieldConfigs) {
            Long internalId = dataFieldCache.dataFieldConfigIdToInternalId.get(configId);
            if (internalId != null) {
              fieldInternalIds.add(internalId);
            } else {
              fieldConfigsWithoutInternalId.add(configId);
            }
          }
          if (fieldConfigsWithoutInternalId.isEmpty()) {
            query.append(" ) and gdfa.data_field_internal_id in (" + GrouperClientUtils.appendQuestions(fieldInternalIds.size()) + ")");
            for (Long internalId : fieldInternalIds) {
              args.add(internalId);
            }
          } else {
            query.append(" ) and gdf.config_id in ("+ GrouperClientUtils.appendQuestions(GrouperUtil.length(fieldConfigs)) + ")");
            for (String configId: fieldConfigs) {
              args.add(configId);
            }
          }
        }

        Set<Subject> subjects = search(query.toString(), args, false, false, null, null, dataFieldCache);

        for (Subject subject : GrouperUtil.nonNull(subjects)) {
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

    if (testingThrowExceptionOnGetSubjectsByIdentifiers) {
      throw new RuntimeException("Test simulated database timeout in getSubjectsByIdentifiers");
    }

    Map<String, Subject> results = new HashMap<String, Subject>();

    if (GrouperUtil.length(identifiers) == 0) {
      return results;
    }
    
    DataFieldCache dataFieldCache = dataFieldCache();
    GrouperSession currentSession = GrouperSession.staticGrouperSession();
    
    Set<String> identifierDataFieldConfigIds = new HashSet<>(dataFieldCache.identifierDataFieldConfigIds);
    
    Subject subjectForSession = currentSession.getSubject();
    
    Set<String> fieldConfigs = retrieveDataFieldConfigsSubjectCanAccess(subjectForSession, dataFieldCache);
    identifierDataFieldConfigIds.retainAll(fieldConfigs);
    
    if (GrouperUtil.length(identifierDataFieldConfigIds) == 0) {
      return getSubjectsByIds(identifiers);
    }
    
    if (GrouperUtil.length(fieldConfigs) == 0) {
      return results;
    }
    
    if (identifiers.size() > 0) {
      
      int batchSize = 800/identifierDataFieldConfigIds.size();
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(identifiers, batchSize, false);
      
      List<String> identifiersList = new ArrayList<String>(identifiers);
      
      Map<String, String> overallSubjectIdentifierToSubjectId = new HashMap<String, String>();
      
      for (int i=0;i<numberOfBatches; i++) {

        List<String> identifierBatch = GrouperUtil.batchList(identifiersList, batchSize, i);

        // Resolve identifier text values to dictionary internal_ids
        Map<Long, String> dictInternalIdToText = new HashMap<Long, String>();
        {
          int dictBatchSize = 800;
          int dictNumberOfBatches = GrouperUtil.batchNumberOfBatches(identifierBatch, dictBatchSize, false);
          for (int db = 0; db < dictNumberOfBatches; db++) {
            List<String> dictBatch = GrouperUtil.batchList(identifierBatch, dictBatchSize, db);
            StringBuilder dictQuery = new StringBuilder("SELECT internal_id, the_text FROM grouper_dictionary WHERE the_text IN (");
            dictQuery.append(GrouperClientUtils.appendQuestions(dictBatch.size()));
            dictQuery.append(")");
            GcDbAccess dictAccess = new GcDbAccess().sql(dictQuery.toString());
            for (String text : dictBatch) {
              dictAccess.addBindVar(text);
            }
            List<Object[]> dictRows = dictAccess.selectList(Object[].class);
            for (Object[] row : dictRows) {
              dictInternalIdToText.put(GrouperUtil.longObjectValue(row[0], false), GrouperUtil.stringValue(row[1]));
            }
          }
        }

        // Build reverse lookup: text -> dictionary internal_id
        Map<String, Long> textToDictInternalId = new HashMap<String, Long>();
        for (Map.Entry<Long, String> entry : dictInternalIdToText.entrySet()) {
          textToDictInternalId.put(entry.getValue(), entry.getKey());
        }

        // Build query using resolved integer IDs for point lookups on fld_assgn_field_dict_idx
        StringBuilder subjectIdsQuery = new StringBuilder("""
            SELECT
                gm.subject_id,
                gdf.config_id AS data_field_config_id,
                gd.the_text AS value_text
            FROM
                grouper_data_field_assign gdfa
            INNER JOIN
                grouper_members gm
                ON gdfa.member_internal_id = gm.internal_id
            INNER JOIN
                grouper_data_field gdf
                ON gdfa.data_field_internal_id = gdf.internal_id
            LEFT JOIN
                grouper_dictionary gd
                ON gdfa.value_dictionary_internal_id = gd.internal_id
            WHERE
                gm.subject_source = ?
                AND (
            """);

        GcDbAccess gcDbAccess = new GcDbAccess();

        gcDbAccess.addBindVar(this.getId());

        Map<String, String> subjectIdToSubjectIdentifier = new HashMap<String, String>();
        boolean first = true;
        boolean hasAnyConditions = false;
        for (int j = 0; j<identifierBatch.size(); j++) {

          String identifierText = identifierBatch.get(j);
          Long dictInternalId = textToDictInternalId.get(identifierText);

          // If the identifier doesn't exist in the dictionary, skip it
          if (dictInternalId == null) {
            continue;
          }

          for (String dataFieldConfigId: identifierDataFieldConfigIds) {

            Long fieldInternalId = dataFieldCache.dataFieldConfigIdToInternalId.get(dataFieldConfigId);
            if (fieldInternalId == null) {
              // might not be cached yet if data field was created after cache was built, try to resolve it
              List<Object[]> fieldRows = new GcDbAccess().sql("SELECT internal_id FROM grouper_data_field WHERE config_id = ?").addBindVar(dataFieldConfigId).selectList(Object[].class);
              if (GrouperUtil.length(fieldRows) > 0) {
                fieldInternalId = GrouperUtil.longObjectValue(fieldRows.get(0)[0], false);
                dataFieldCache.dataFieldConfigIdToInternalId.put(dataFieldConfigId, fieldInternalId);
              }
            }
            if (fieldInternalId == null) {
              continue;
            }

            if (!first) {
              subjectIdsQuery.append(" or ");
            }
            first = false;
            hasAnyConditions = true;
            subjectIdsQuery.append(" (gdfa.data_field_internal_id = ? and gdfa.value_dictionary_internal_id = ?) ");
            gcDbAccess.addBindVar(fieldInternalId);
            gcDbAccess.addBindVar(dictInternalId);
          }
        }

        // If no identifiers resolved to dictionary entries, skip this batch
        if (!hasAnyConditions) {
          continue;
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
        
        // if there are no subject ids in this batch, skip to the next batch
        if (GrouperUtil.length(subjectIdToSubjectIdentifier) == 0) {
          continue;
        }

        List<Object> args = new ArrayList<Object>();

        StringBuilder query = new StringBuilder("""
            SELECT
                gm.subject_id,
                gdf.config_id AS data_field_config_id,
                gd.the_text AS value_text
            FROM
                grouper_members gm
            INNER JOIN
                grouper_data_field_assign gdfa
                ON gdfa.member_internal_id = gm.internal_id
            INNER JOIN
                grouper_data_field gdf
                ON gdfa.data_field_internal_id = gdf.internal_id
            LEFT JOIN
                grouper_dictionary gd
                ON gdfa.value_dictionary_internal_id = gd.internal_id
            WHERE
                gm.subject_source = ?
                AND (

            """);

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

        // Use internal IDs for data field filtering when available
        {
          List<Long> fieldInternalIds = new ArrayList<Long>();
          List<String> fieldConfigsWithoutInternalId = new ArrayList<String>();
          for (String configId : fieldConfigs) {
            Long internalId = dataFieldCache.dataFieldConfigIdToInternalId.get(configId);
            if (internalId != null) {
              fieldInternalIds.add(internalId);
            } else {
              fieldConfigsWithoutInternalId.add(configId);
            }
          }
          if (fieldConfigsWithoutInternalId.isEmpty()) {
            query.append(" ) and gdfa.data_field_internal_id in (" + GrouperClientUtils.appendQuestions(fieldInternalIds.size()) + ")");
            for (Long internalId : fieldInternalIds) {
              args.add(internalId);
            }
          } else {
            query.append(" ) and gdf.config_id in ("+ GrouperClientUtils.appendQuestions(GrouperUtil.length(fieldConfigs)) + ")");
            for (String configId: fieldConfigs) {
              args.add(configId);
            }
          }
        }

        Set<Subject> subjects = search(query.toString(), args, false, false, null, null, dataFieldCache);

        for (Subject subject : GrouperUtil.nonNull(subjects)) {
          String subjectId = subject.getId();
          String subjectIdentifier = subjectIdToSubjectIdentifier.get(subjectId);
          results.put(subjectIdentifier, subject);
        }
      }
    }

    // if configured, also try to resolve any remaining identifiers as subject IDs
    if (dataFieldCache.subjectIdResolvedAsIdentifier) {
      Set<String> unresolvedIdentifiers = new HashSet<String>(identifiers);
      unresolvedIdentifiers.removeAll(results.keySet());
      if (unresolvedIdentifiers.size() > 0) {
        Map<String, Subject> idResults = getSubjectsByIds(unresolvedIdentifiers);
        for (Map.Entry<String, Subject> entry : idResults.entrySet()) {
          results.put(entry.getKey(), entry.getValue());
        }
      }
    }

    return results;
  }
  
  @Override
  public Subject getSubject(String subjectId) throws SubjectNotFoundException, SubjectNotUniqueException {
    
    Map<String, Subject> subjectMap = getSubjectsByIds(GrouperUtil.toSet(subjectId));
    
    if (GrouperUtil.length(subjectMap) > 1) {
      throw new RuntimeException("Why are there more than one result??? " + subjectId + ", " + GrouperUtil.length(subjectMap) + " in source: " + this.getId());
    }
    
    Subject subject = null;
    
    if (GrouperUtil.length(subjectMap) == 1) {
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
  private Set<Subject> search(String query, List<Object> args, boolean expectSingle,
      boolean exceptionIfNull,
      Collection<String> identifiersForIdentifierToMap, Map<String, Subject> resultIdentifierToSubject, DataFieldCache dataFieldCache)
      throws SubjectNotFoundException, SubjectNotUniqueException,
      InvalidQueryRuntimeException {

    if (resultIdentifierToSubject != null) {
      if (GrouperUtil.length(identifiersForIdentifierToMap) == 0) {
        throw new RuntimeException("Why is there no identifiersForIdentifierToMap???");
      }
    }

    Set<Subject> results = new LinkedHashSet<Subject>();

    try {
      
      GcDbAccess gcDbAccess = new GcDbAccess().sql(query);
      
      for (Object arg: GrouperUtil.nonNull(args)) {
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
        subjectResult.setResolvedFromSource(true);
        subjectResult.getTranslationMap().put("subjectid", subjectId);
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
        // stored search strings come from these: only publicly readable fields, whoever resolves
        assignPublicSearchAttributes(subjectResult, subjectId, dataFieldConfigIdToSetOfValues, dataFieldCache);
        results.add(subjectResult);
      }
      
      if (subjectResult == null && exceptionIfNull) {
        throw new SubjectNotFoundException("Subject not found: " + query + ", "
            + StringUtils.join(args.iterator(), ",")  + " in source: " + this.getId());
      }
      

    } finally {
      if (log.isDebugEnabled()) {
        log.debug("Query returned " + results.size() + ", " + query + ", " + GrouperUtil.toStringForLog(args));
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
    
    Map<String, Subject> subjectMap = getSubjectsByIdentifiers(GrouperUtil.toSet(identifier));
    
    if (GrouperUtil.length(subjectMap) > 1) {
      throw new RuntimeException("Why are there more than one result??? " + identifier + ", " + GrouperUtil.length(subjectMap));
    }

    Subject subject = null;

    if (GrouperUtil.length(subjectMap) == 1) {
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
    Set<Subject> subjects = new HashSet<Subject>();
    
    if (StringUtils.isBlank(searchValue) || searchValue.trim().length() < 2) {
      return subjects;
    }
    
    
    DataFieldCache dataFieldCache = dataFieldCache();
    GrouperSession currentSession = GrouperSession.staticGrouperSession();
    
    Subject subjectForSession = currentSession.getSubject();
    
    Set<String> fieldConfigs = retrieveDataFieldConfigsSubjectCanAccess(subjectForSession, dataFieldCache);
    
    if (GrouperUtil.length(fieldConfigs) == 0) {
      return subjects;
    }
    
    // find the default search string the user has access to
    SearchStringEnum searchStringEnum = SearchStringEnum.getDefaultSearchString();
    if (searchStringEnum == null) {
      return subjects;
    }

    //lets split by any whitespace space
    String[] terms = searchValue.split("\\s+");

    //retrieve all the member ids that match the identifiers
    StringBuilder subjectIdsQuery = new StringBuilder("select gm.subject_id from grouper_members gm where "
        + " gm.subject_source = ? and ");

    GcDbAccess gcDbAccess = new GcDbAccess();

    gcDbAccess.addBindVar(this.getId());

    for (int i = 0; i < terms.length; i++) {
      // escape % and _ so a term matches literally (no wildcard probing)
      subjectIdsQuery.append(" gm.search_string" + searchStringEnum.getIndex() + " like ? escape '!'");
      gcDbAccess.addBindVar("%" + escapeLikeTerm(terms[i].toLowerCase()) + "%");
      if (i != terms.length - 1) {
        subjectIdsQuery.append(" and ");
      }
    }

    // limit the number of results from the search (min 5, default 100, max 500)
    int maxSearchResults = this.maxResults != null ? this.maxResults : 100;
    maxSearchResults = Math.max(5, Math.min(500, maxSearchResults));

    List<String> subjectIds = gcDbAccess.sql(subjectIdsQuery.toString()).paging(1, maxSearchResults).selectList(String.class);
    if (GrouperUtil.length(subjectIds) == 0) {
     return subjects; 
    }
        
    List<Object> args = new ArrayList<Object>();

    StringBuilder query = new StringBuilder("""
        SELECT gm.subject_id, gdf.config_id AS data_field_config_id, gd.the_text AS value_text
        FROM grouper_members gm,
             grouper_data_field_assign gdfa
        JOIN grouper_data_field gdf
          ON gdfa.data_field_internal_id = gdf.internal_id
        LEFT JOIN grouper_dictionary gd
          ON gdfa.value_dictionary_internal_id = gd.internal_id
        WHERE gdfa.member_internal_id = gm.internal_id
          AND gm.subject_source = ?
          AND (
        """);

    args.add(this.getId());

    // make a list of subject ids
    for (int j = 0; j<subjectIds.size(); j++) {

      if (j>0) {
        query.append(" or ");
      }
      query.append(" gm.subject_id = ? ");

      args.add(subjectIds.get(j));

    }

    // Use internal IDs for data field filtering when available
    {
      List<Long> fieldInternalIds = new ArrayList<Long>();
      List<String> fieldConfigsWithoutInternalId = new ArrayList<String>();
      for (String configId : fieldConfigs) {
        Long internalId = dataFieldCache.dataFieldConfigIdToInternalId.get(configId);
        if (internalId != null) {
          fieldInternalIds.add(internalId);
        } else {
          fieldConfigsWithoutInternalId.add(configId);
        }
      }
      if (fieldConfigsWithoutInternalId.isEmpty()) {
        query.append(" ) and gdfa.data_field_internal_id in (" + GrouperClientUtils.appendQuestions(fieldInternalIds.size()) + ")");
        for (Long internalId : fieldInternalIds) {
          args.add(internalId);
        }
      } else {
        query.append(" ) and gdf.config_id in ("+ GrouperClientUtils.appendQuestions(GrouperUtil.length(fieldConfigs)) + ")");
        for (String configId: fieldConfigs) {
          args.add(configId);
        }
      }
    }

    subjects = search(query.toString(), args, false, false, null, null, dataFieldCache);
    return subjects;
  }

  
  /** if there is a limit to the number of results (min 5, default 100, max 500) */
  Integer maxResults;

  private Integer maxPage;
  
  private boolean errorOnMaxResults;

  private static class DataFieldCache {
    
    private GrouperDataEngine dataEngine;
    
    private GrouperConfig grouperConfig;
    
    private Set<GrouperDataFieldConfig> allDataFieldConfigIds = new LinkedHashSet<GrouperDataFieldConfig>();
    
    private Set<String> dataFieldMultivaluedConfigIds = new HashSet<String>();

    /** data field config ids in a public or authenticated privacy realm, computed on first use */
    private Set<String> publicDataFieldConfigIds = null;

    private Set<String> dataFieldFormatToLowerCaseConfigIds = new HashSet<String>(); 
    
    private Set<String> identifierDataFieldConfigIds = new LinkedHashSet<String>();
    
    private Map<MultiKey, Set<String>> sourceIdSubjectIdToDataFieldConfigIds = new HashMap<MultiKey, Set<String>>();
    
    private Map<String, List<String>> attributeNameToListOfPrioritizedPrivacyAttributeNames = new HashMap<String, List<String>>();

    private boolean subjectIdResolvedAsIdentifier = false;

    /** cache of data field config_id to grouper_data_field.internal_id */
    private Map<String, Long> dataFieldConfigIdToInternalId = new HashMap<String, Long>();

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

      this.subjectIdResolvedAsIdentifier = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + source.getConfigId() + ".subjectIdResolvedAsIdentifier", false);

      Set<GrouperDataFieldConfig> dataFieldConfigs = new HashSet<GrouperDataFieldConfig>();
      for (String fieldConfigId: dataFieldConfigIds) {
        if (fieldConfigByConfigId.containsKey(fieldConfigId)) {
          GrouperDataFieldConfig grouperDataFieldConfig = fieldConfigByConfigId.get(fieldConfigId);
          dataFieldConfigs.add(grouperDataFieldConfig);
        } else {
          throw new RuntimeException("Found invalid config id: '"+fieldConfigId+"', Source id: '"+source.getId()+"'");
        }
      }

      // Pre-resolve data field config_ids to grouper_data_field.internal_id for optimized queries
      if (dataFieldConfigIds.size() > 0) {
        StringBuilder fieldIdQuery = new StringBuilder("SELECT config_id, internal_id FROM grouper_data_field WHERE config_id IN (");
        fieldIdQuery.append(GrouperClientUtils.appendQuestions(dataFieldConfigIds.size()));
        fieldIdQuery.append(")");
        GcDbAccess fieldIdAccess = new GcDbAccess().sql(fieldIdQuery.toString());
        for (String configId : dataFieldConfigIds) {
          fieldIdAccess.addBindVar(configId);
        }
        List<Object[]> fieldIdRows = fieldIdAccess.selectList(Object[].class);
        for (Object[] row : fieldIdRows) {
          this.dataFieldConfigIdToInternalId.put(GrouperUtil.stringValue(row[0]), GrouperUtil.longObjectValue(row[1], false));
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
          this.errorOnMaxResults = GrouperUtil.booleanValue(errorOnMaxResultsString, true);
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
        
  }
  

}
