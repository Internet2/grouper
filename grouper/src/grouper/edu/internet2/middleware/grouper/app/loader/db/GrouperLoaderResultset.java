/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer $Id: GrouperLoaderResultset.java,v 1.9 2009-10-18 16:30:51 mchyzer Exp
 * $
 */
package edu.internet2.middleware.grouper.app.loader.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperShell;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapElUtils;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * encapsulate a resultset into this resultset to be case-insensitive and
 * column-order insensitive
 */
public class GrouperLoaderResultset {

  /**
   * if we should bulk lookup subjects to add/remove
   */
  public void bulkLookupSubjects() {

    //do not bulk retrieve subjects if not configured to do that
    if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.bulkLookupSubjects", true)) {
      return;
    }

    //dont do this twice if has already done on the outer list
    if (this.hasBulkLookupedSubjects) {
      return;
    }
    
    this.hasBulkLookupedSubjects = true;
    
    //lets assume all rows are the same
    if (GrouperUtil.length(this.data) == 0) {
      return;
    }

    //if we are looking by subject id or identifier and dont have the source
    Set<String> subjectIdsOrIdentifiers = new HashSet<String>();

    //if we have the source and are looking by source and id or identifier
    Map<String, Set<String>> sourceToSubjectIdsOrIdentifiers = new LinkedHashMap<String, Set<String>>();

    String subjectIdCol = null;
    String sourceIdCol = null;

    String defaultSubjectSourceId = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        GrouperLoaderConfig.DEFAULT_SUBJECT_SOURCE_ID);
    
    for (Row row : this.data) {
      
      if (subjectIdCol == null) {
        
        String subjectId = (String) row.getCell(
            GrouperLoaderResultset.SUBJECT_ID_COL, false);
        
        if (!StringUtils.isBlank(subjectId)) {
          subjectIdCol = GrouperLoaderResultset.SUBJECT_ID_COL;
        } else {
          String subjectIdentifier = (String) row.getCell(
              GrouperLoaderResultset.SUBJECT_IDENTIFIER_COL, false);
          if (!StringUtils.isBlank(subjectIdentifier)) {
            subjectIdCol = GrouperLoaderResultset.SUBJECT_IDENTIFIER_COL;
          } else {
            String subjectIdOrIdentifier = (String) row.getCell(
                GrouperLoaderResultset.SUBJECT_ID_OR_IDENTIFIER_COL, false);
            if (!StringUtils.isBlank(subjectIdOrIdentifier)) {
              subjectIdCol = GrouperLoaderResultset.SUBJECT_ID_OR_IDENTIFIER_COL;
            } else {
              //wtf?  dont do anthing, cant find subject id col, let the normal error message prevail
              return;
            }
          }
        }
        //see if there is a source id col
        String subjectSourceId = (String) row.getCell(
            GrouperLoaderResultset.SUBJECT_SOURCE_ID_COL, false);
        if (!StringUtils.isBlank(subjectSourceId)) {
          sourceIdCol = GrouperLoaderResultset.SUBJECT_SOURCE_ID_COL;
        }
      }
      String sourceId = null;
      String subjectIdOrIdentifer = null;
      if (!StringUtils.isBlank(sourceIdCol)) {
        sourceId = (String) row.getCell(
            GrouperLoaderResultset.SUBJECT_SOURCE_ID_COL, false);
      }
      //default this to what is in the config file
      sourceId = StringUtils.defaultString(sourceId, defaultSubjectSourceId);
      subjectIdOrIdentifer = (String) row.getCell(subjectIdCol, false);

      if (!StringUtils.isBlank(sourceId)) {
        Set<String> subjectIdsOrIdentifiersForSource = sourceToSubjectIdsOrIdentifiers.get(sourceId);
        //lazy load for source
        if (subjectIdsOrIdentifiersForSource == null) {
          subjectIdsOrIdentifiersForSource = new HashSet<String>();
          sourceToSubjectIdsOrIdentifiers.put(sourceId, subjectIdsOrIdentifiersForSource);
        }
        subjectIdsOrIdentifiersForSource.add(subjectIdOrIdentifer);
      } else {
        //no source, just keep track of identifier
        subjectIdsOrIdentifiers.add(subjectIdOrIdentifer);
      }

    }
    
    Map<String, Map<String, Subject>> sourceToSubjectIdOrIdentifierToSubject = new HashMap<String, Map<String, Subject>>();
    
    //lets resolve the subjects
    //see if there are any where we know the source
    if (GrouperUtil.length(sourceToSubjectIdsOrIdentifiers) > 0) {
      
      Map<String, Subject> localSubjectIdOrIdentifierToSubject = null;
      
      for (String sourceId : sourceToSubjectIdsOrIdentifiers.keySet()) {
        Set<String> subjectIdsOrIdentifiersForSource = sourceToSubjectIdsOrIdentifiers.get(sourceId);
        //what are they?
        if (StringUtils.equals(subjectIdCol, GrouperLoaderResultset.SUBJECT_ID_COL)) {

          localSubjectIdOrIdentifierToSubject = SubjectFinder.findByIds(subjectIdsOrIdentifiersForSource, sourceId);
        
        } else if (StringUtils.equals(subjectIdCol, GrouperLoaderResultset.SUBJECT_IDENTIFIER_COL)) {
        
          localSubjectIdOrIdentifierToSubject = SubjectFinder.findByIdentifiers(subjectIdsOrIdentifiersForSource, sourceId);
          
        } else if (StringUtils.equals(subjectIdCol, GrouperLoaderResultset.SUBJECT_ID_OR_IDENTIFIER_COL)) {
          
          localSubjectIdOrIdentifierToSubject = SubjectFinder.findByIdsOrIdentifiers(subjectIdsOrIdentifiersForSource, sourceId);
          
        } else {
          throw new RuntimeException("Not expecting subjectIdCol: " + subjectIdCol);
        }
        
        //keep track in map
        sourceToSubjectIdOrIdentifierToSubject.put(sourceId, localSubjectIdOrIdentifierToSubject);
        
      }
    }

    Map<String, Subject> subjectIdOrIdentifierToSubject = null;

    if (GrouperUtil.length(subjectIdsOrIdentifiers) > 0) {
      //what are they?
      if (StringUtils.equals(subjectIdCol, GrouperLoaderResultset.SUBJECT_ID_COL)) {
        subjectIdOrIdentifierToSubject = SubjectFinder.findByIds(subjectIdsOrIdentifiers);
      } else if (StringUtils.equals(subjectIdCol, GrouperLoaderResultset.SUBJECT_IDENTIFIER_COL)) {
        subjectIdOrIdentifierToSubject = SubjectFinder.findByIdentifiers(subjectIdsOrIdentifiers);
      } else if (StringUtils.equals(subjectIdCol, GrouperLoaderResultset.SUBJECT_ID_OR_IDENTIFIER_COL)) {
        subjectIdOrIdentifierToSubject = SubjectFinder.findByIdsOrIdentifiers(subjectIdsOrIdentifiers);
      } else {
        throw new RuntimeException("Not expecting subjectIdCol: " + subjectIdCol);
      }
    }
    
    for (Row row : this.data) {

      String subjectIdOrIdentifier = (String)row.getCell(subjectIdCol, false);
      Subject subject = null;
      
      //get the subject from the source id map
      if (!StringUtils.isBlank(sourceIdCol) || !StringUtils.isBlank(defaultSubjectSourceId)) {
        String sourceId = StringUtils.defaultString((String)row.getCell(sourceIdCol, false), defaultSubjectSourceId);
        if (!StringUtils.isBlank(sourceId)) {
          Map<String, Subject> localSubjectIdOrIdentifierToSubject = sourceToSubjectIdOrIdentifierToSubject.get(sourceId);
          if (localSubjectIdOrIdentifierToSubject != null) {
            subject = localSubjectIdOrIdentifierToSubject.get(subjectIdOrIdentifier);
          }
        }
      }
      if (subject == null && subjectIdOrIdentifierToSubject != null) {
        //get the subject from the subjectId map
        subject = subjectIdOrIdentifierToSubject.get(subjectIdOrIdentifier);
      }

      //if we found it, set it
      if (subject != null) {
        row.subject = subject;
      }
      
    }

    

  }
  
  /**
   * 
   */
  public static final String SUBJECT_ID_COL = "SUBJECT_ID";

  /**
   * 
   */
  public static final String SUBJECT_IDENTIFIER_COL = "SUBJECT_IDENTIFIER";

  /**
   * 
   */
  public static final String SUBJECT_ID_OR_IDENTIFIER_COL = "SUBJECT_ID_OR_IDENTIFIER";

  /**
   * 
   */
  public static final String ACTION_NAME_COL = "ACTION_NAME";

  /**
   * 
   */
  public static final String GROUP_NAME_COL = "GROUP_NAME";

  /**
   * 
   */
  public static final String GROUP_DISPLAY_NAME_COL = "GROUP_DISPLAY_NAME";

  /**
   * 
   */
  public static final String GROUP_DESCRIPTION_COL = "GROUP_DESCRIPTION";

  /**
   * 
   */
  public static final String GROUP_VIEWERS_COL = "VIEWERS";

  /**
   * 
   */
  public static final String GROUP_READERS_COL = "READERS";

  /**
   * 
   */
  public static final String GROUP_UPDATERS_COL = "UPDATERS";

  /**
   * 
   */
  public static final String GROUP_ADMINS_COL = "ADMINS";

  /**
   * 
   */
  public static final String ATTR_NAME_COL = "ATTR_NAME";

  /**
   * 
   */
  public static final String ATTR_DISPLAY_NAME_COL = "ATTR_DISPLAY_NAME";

  /**
   * 
   */
  public static final String ATTR_DESCRIPTION_COL = "ATTR_DESCRIPTION";

  /**
   * 
   */
  public static final String IF_HAS_ATTR_NAME_COL = "IF_HAS_ATTR_NAME";

  /**
   * 
   */
  public static final String THEN_HAS_ATTR_NAME_COL = "THEN_HAS_ATTR_NAME";

  /**
   * 
   */
  public static final String IF_HAS_ACTION_NAME_COL = "IF_HAS_ACTION_NAME";

  /**
   * 
   */
  public static final String THEN_HAS_ACTION_NAME_COL = "THEN_HAS_ACTION_NAME";

  /**
   * 
   */
  public static final String GROUP_OPTINS_COL = "OPTINS";

  /**
   * 
   */
  public static final String GROUP_OPTOUTS_COL = "OPTOUTS";

  /**
   * 
   */
  public static final String GROUP_ATTR_READERS_COL = "GROUP_ATTR_READERS";
  
  /**
   * 
   */
  public static final String GROUP_ATTR_UPDATERS_COL = "GROUP_ATTR_UPDATERS";
  
  /**
   * 
   */
  public static final String SUBJECT_SOURCE_ID_COL = "SUBJECT_SOURCE_ID";

  /**
   * get a resultset on another resultset and a group name
   * @param parentResultSet
   * @param groupName
   */
  public GrouperLoaderResultset(GrouperLoaderResultset parentResultSet, String groupName) {
    
    //if bulk lookuped the parent, then use that for the child
    this.hasBulkLookupedSubjects = parentResultSet.hasBulkLookupedSubjects;

    this.columnNames = parentResultSet.columnNames == null ? null
        : new ArrayList<String>(parentResultSet.columnNames);
    this.columnTypes = parentResultSet.columnTypes == null ? null
        : new ArrayList<Integer>(parentResultSet.columnTypes);
    for (int i = 0; i < parentResultSet.data.size(); i++) {

      if (StringUtils.equals(groupName, (String) parentResultSet.getCell(i,
          GROUP_NAME_COL, true))) {
        //dont clone the row, just add the row there
        this.data.add(parentResultSet.data.get(i));
      }

    }
  }

  /**
   * get a set of group names
   * @return the set of names, never null
   */
  public Set<String> groupNames() {
    Set<String> groupNames = new LinkedHashSet<String>();
    for (int i = 0; i < this.data.size(); i++) {
      groupNames.add((String) this.getCell(i, GROUP_NAME_COL, true));
    }
    return groupNames;
  }

  /**
   * get a resultset based on a db and query
   * @param grouperLoaderDb
   * @param query
   * @param jobName 
   * @param hib3GrouperLoaderLog 
   */
  public GrouperLoaderResultset(GrouperLoaderDb grouperLoaderDb, String query,
      String jobName, Hib3GrouperLoaderLog hib3GrouperLoaderLog) {

    //small security check (not failsafe, but better than nothing)
    if (!query.toLowerCase().trim().startsWith("select")) {
      throw new RuntimeException("Invalid query, must start with select: " + query);
    }
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      connection = grouperLoaderDb.connection();
      try {
        // create and execute a SELECT
        statement = connection.createStatement();
        resultSet = statement.executeQuery(query);

        //lets get some column info and stuff
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        int columnCount = resultSetMetaData.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
          this.columnNames.add(resultSetMetaData.getColumnLabel(i + 1));
          this.columnTypes.add(resultSetMetaData.getColumnType(i + 1));
        }

        while (resultSet.next()) {
          Row row = new Row();
          Object[] rowData = new Object[columnCount];
          row.setRowData(rowData);
          this.data.add(row);
          //at this point, assume everything is either a string or a timestamp
          for (int i = 0; i < columnCount; i++) {
            if (this.columnTypes.get(i) == Types.TIMESTAMP) {
              rowData[i] = resultSet.getTimestamp(i + 1);
            } else {
              rowData[i] = resultSet.getString(i + 1);
            }
          }

        }
      } finally {
        //this is important so no one sneaks some delete statement in there
        GrouperUtil.rollbackQuietly(connection);
      }
    } catch (SQLException se) {
      throw new RuntimeException("Problem with query: " + query + ",  on db: "
          + grouperLoaderDb, se);
    } finally {
      GrouperUtil.closeQuietly(resultSet);
      GrouperUtil.closeQuietly(statement);
      GrouperUtil.closeQuietly(connection);
    }
    this.convertToSubjectIdIfNeeded(jobName, hib3GrouperLoaderLog, null);

  }

  /**
   * get subject id col
   * @param subjectIdType
   * @return the subjectId col
   */
  private String subjectIdCol(String subjectIdType) {
    String subjectIdCol = "SUBJECT_ID";

    if (!StringUtils.isBlank(subjectIdType)) {

      if (StringUtils.equalsIgnoreCase(subjectIdType, "SUBJECT_ID")
          || StringUtils.equalsIgnoreCase(subjectIdType, "subjectId")) {
        subjectIdCol = "SUBJECT_ID";
      } else if (StringUtils.equalsIgnoreCase(subjectIdType, "SUBJECT_IDENTIFIER")
          || StringUtils.equalsIgnoreCase(subjectIdType, "subjectIdentifier")) {
        subjectIdCol = "SUBJECT_IDENTIFIER";
      } else if (StringUtils.equalsIgnoreCase(subjectIdType, "SUBJECT_ID_OR_IDENTIFIER")
          || StringUtils.equalsIgnoreCase(subjectIdType, "subjectIdOrIdentifier")) {
        subjectIdCol = "SUBJECT_ID_OR_IDENTIFIER";
      } else {
        throw new RuntimeException("Not expecting subjectIdType: '" + subjectIdType
            + "', should be subjectId, subjectIdentifier, or subjectIdOrIdentifier");
      }
    }
    return subjectIdCol;
  }

  /**
   * get a resultset based on an ldap server and filter
   * @param ldapServerId server id in grouper-loader.properties
   * @param filter ldap filter query
   * @param searchDn place in ldap where search starts from
   * @param subjectAttribute attribute where the subjectId, or subjectIdentifier, or subjectIdOrIdentifier is
   * @param sourceId if all subjects come from one source, put the sourceId here
   * @param subjectIdType the type of the subjectId, either: subjectId, subjectIdentifier, or subjectIdOrIdentifier
   * @param ldapSearchScope either OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
   * @param jobName for logging if problem
   * @param hib3GrouperLoaderLog 
   * @param ldapSubjectExpression
   */
  public GrouperLoaderResultset(String ldapServerId, String filter,
      String searchDn, String subjectAttribute, String sourceId,
      String subjectIdType, String ldapSearchScope, String jobName,
      Hib3GrouperLoaderLog hib3GrouperLoaderLog, String ldapSubjectExpression) {

    //run the query
    LdapSearchScope ldapSearchScopeEnum = LdapSearchScope.valueOfIgnoreCase(
        ldapSearchScope, false);

    boolean hasSourceId = !StringUtils.isBlank(sourceId);

    String subjectIdCol = subjectIdCol(subjectIdType);

    this.columnNames.add(subjectIdCol);
    this.columnTypes.add(Types.VARCHAR);

    if (hasSourceId) {

      this.columnNames.add("SUBJECT_SOURCE_ID");
      this.columnTypes.add(Types.VARCHAR);

    }

    List<String> results = LdapSessionUtils.ldapSession().list(String.class, ldapServerId, searchDn,
        ldapSearchScopeEnum, filter, subjectAttribute);

    for (String result : results) {

      Row row = new Row();
      Object[] rowData = new Object[hasSourceId ? 2 : 1];
      row.setRowData(rowData);
      this.data.add(row);
      rowData[0] = result;
      if (hasSourceId) {
        rowData[1] = sourceId;
      }
    }
    this.convertToSubjectIdIfNeeded(jobName, hib3GrouperLoaderLog, ldapSubjectExpression);
  }

  /**
   * 
   */
  public GrouperLoaderResultset() {
    
  }
  
  /**
   * get a resultset based on an ldap server and filter for ldap list of groups
   * @param ldapServerId server id in grouper-loader.properties
   * @param filter ldap filter query
   * @param searchDn place in ldap where search starts from
   * @param subjectAttributeName attribute where the subjectId, or subjectIdentifier, or subjectIdOrIdentifier is
   * @param sourceId if all subjects come from one source, put the sourceId here
   * @param subjectIdType the type of the subjectId, either: subjectId, subjectIdentifier, or subjectIdOrIdentifier
   * @param ldapSearchScope either OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
   * @param jobName for logging if problem
   * @param hib3GrouperLoaderLog 
   * @param subjectExpression 
   * @param extraAttributes 
   * @param groupNameExpression 
   * @param groupDisplayNameExpression 
   * @param groupDescriptionExpression 
   * @param groupNameToDisplayName map to translate group name to display name
   * @param groupNameToDescription map to translate group name to description
   * @param groupNames keep track of which group names there are
   */
  public void initForLdapListOfGroups(final String ldapServerId, final String filter,
      final String searchDn, final String subjectAttributeName, final String sourceId,
      final String subjectIdType, final String ldapSearchScope, final String jobName,
      final Hib3GrouperLoaderLog hib3GrouperLoaderLog,
      final String subjectExpression,
      final String extraAttributes,
      final String groupNameExpression, final String groupDisplayNameExpression, 
      final String groupDescriptionExpression,  
      final Map<String, String> groupNameToDisplayName,
      final Map<String, String> groupNameToDescription, final Set<String> groupNames) {

    //run the query
    final LdapSearchScope ldapSearchScopeEnum = LdapSearchScope.valueOfIgnoreCase(
        ldapSearchScope, false);

    boolean hasSourceId = !StringUtils.isBlank(sourceId);

    String subjectIdCol = subjectIdCol(subjectIdType);

    this.columnNames.add("GROUP_NAME");
    this.columnTypes.add(Types.VARCHAR);

    this.columnNames.add(subjectIdCol);
    this.columnTypes.add(Types.VARCHAR);

    if (hasSourceId) {

      this.columnNames.add("SUBJECT_SOURCE_ID");
      this.columnTypes.add(Types.VARCHAR);

    }

    final String groupName = hib3GrouperLoaderLog.getGroupNameFromJobName();

    if (StringUtils.isBlank(groupName)) {
      throw new RuntimeException("Why is group name blank??? "
          + hib3GrouperLoaderLog.getJobName());
    }

    boolean requireTopStemAsStemFromConfigGroup = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(
        "loader.ldap.requireTopStemAsStemFromConfigGroup", true);
    
    String groupParentFolderNameTemp = requireTopStemAsStemFromConfigGroup ? (GrouperUtil.parentStemNameFromName(groupName) + ":") : "";
    if (!StringUtils.isBlank(groupParentFolderNameTemp) && !groupParentFolderNameTemp.endsWith(":")) {
      groupParentFolderNameTemp += ":";
    }
    final String groupParentFolderName = groupParentFolderNameTemp;

    Map<String, List<String>> resultMap = new HashMap<String, List<String>>();

    try {

      List<String> attributesList = new ArrayList<String>();
      attributesList.add(subjectAttributeName);
      String[] extraAttributeArray = null;

      if (!StringUtils.isBlank(extraAttributes)) {
        extraAttributeArray = GrouperUtil.splitTrim(extraAttributes, ",");
        for (String attribute : extraAttributeArray) {
          attributesList.add(attribute);
        }

      }

      String[] attributeArray = GrouperUtil.toArray(attributesList, String.class);

      List<LdapEntry> searchResults = LdapSessionUtils.ldapSession().list(ldapServerId, searchDn, ldapSearchScopeEnum, filter, attributeArray, null);

      int subObjectCount = 0;
      for (LdapEntry searchResult : searchResults) {

        List<String> valueResults = new ArrayList<String>();
        String nameInNamespace = searchResult.getDn();
        
        String baseDn = GrouperLoaderConfig.parseLdapBaseDnFromUrlConfig(ldapServerId);

        String defaultFolder = defaultLdapFolder();

        String loaderGroupName = defaultFolder + LoaderLdapElUtils.convertDnToSubPath(nameInNamespace, 
            baseDn, searchDn);

        String loaderGroupDisplayName = null;
        String loaderGroupDescription = null;

        if (!StringUtils.isBlank(groupNameExpression)
            || !StringUtils.isBlank(groupDisplayNameExpression)
            || !StringUtils.isBlank(groupDescriptionExpression)) {

          Map<String, Object> envVars = new HashMap<String, Object>();

          Map<String, Object> groupAttributes = new HashMap<String, Object>();
          groupAttributes.put("dn", nameInNamespace);
          if (!StringUtils.isBlank(extraAttributes)) {
            for (String groupAttributeName : extraAttributeArray) {
              LdapAttribute groupAttribute = searchResult.getAttribute(
                  groupAttributeName);

              if (groupAttribute != null) {

                if (groupAttribute.getStringValues().size() > 1) {
                  throw new RuntimeException(
                      "Grouper LDAP loader only supports single valued group attributes at this point: "
                      + groupAttributeName);
                }
                String attributeValue = groupAttribute.getStringValues().iterator().next();
                groupAttributes.put(groupAttributeName, attributeValue);

              }
            }
          }
          envVars.put("groupAttributes", groupAttributes);
          if (!StringUtils.isBlank(groupNameExpression)) {
            String elGroupName = LoaderLdapUtils.substituteEl(groupNameExpression,
                envVars);
            loaderGroupName = groupParentFolderName + elGroupName;
          }
          if (!StringUtils.isBlank(groupDisplayNameExpression)) {
            String elGroupDisplayName = LoaderLdapUtils.substituteEl(groupDisplayNameExpression,
                envVars);
            loaderGroupDisplayName = groupParentFolderName +  elGroupDisplayName;
          }
          if (!StringUtils.isBlank(groupDescriptionExpression)) {
            String elGroupDescription = LoaderLdapUtils.substituteEl(groupDescriptionExpression,
                envVars);
            loaderGroupDescription = elGroupDescription;
          }
        }

        groupNames.add(loaderGroupName);

        resultMap.put(loaderGroupName, valueResults);

        if (!StringUtils.isBlank(loaderGroupDisplayName)) {
          groupNameToDisplayName.put(loaderGroupName, loaderGroupDisplayName);
        }

        if (!StringUtils.isBlank(loaderGroupDescription)) {
          groupNameToDescription.put(loaderGroupName, loaderGroupDescription);
        }

        LdapAttribute subjectAttribute = searchResult.getAttribute(
            subjectAttributeName);

        if (subjectAttribute != null) {
          for (String attributeValue : subjectAttribute.getStringValues()) {

            if (attributeValue != null) {
              subObjectCount++;
              valueResults.add((String) attributeValue);
            }
          }
        }
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("Found " + resultMap.size() + " results, (" + subObjectCount
            + " sub-results) for serverId: " + ldapServerId + ", searchDn: "
            + searchDn
            + ", filter: '" + filter + "', returning subject attribute: "
            + subjectAttributeName + ", some results: "
            + GrouperUtil.toStringForLog(resultMap, 100));
      }
    } catch (Exception re) {
      GrouperUtil.injectInException(re, "Error querying ldap server id: " + ldapServerId
          + ", searchDn: " + searchDn
          + ", filter: '" + filter + "', returning subject attribute: "
          + subjectAttributeName);
      throw new RuntimeException(re);
    }

    for (String currentGroupName : resultMap.keySet()) {
      List<String> results = resultMap.get(currentGroupName);
      for (String result : results) {
        Row row = new Row();
        Object[] rowData = new Object[hasSourceId ? 3 : 2];
        row.setRowData(rowData);
        this.data.add(row);
        rowData[0] = currentGroupName;
        rowData[1] = result;
        if (hasSourceId) {
          rowData[2] = sourceId;
        }
      }
    }
    this.convertToSubjectIdIfNeeded(jobName, hib3GrouperLoaderLog, subjectExpression);
  }

  /**
   * get a resultset based on an ldap server and filter for ldap list of groups
   * @param ldapServerId server id in grouper-loader.properties
   * @param filter ldap filter query
   * @param searchDn place in ldap where search starts from
   * @param subjectAttributeName attribute where the subjectId, or subjectIdentifier, or subjectIdOrIdentifier is
   * @param groupAttributeName attribute (e.g. affiliation) of subject which holds link to group
   * @param sourceId if all subjects come from one source, put the sourceId here
   * @param subjectIdType the type of the subjectId, either: subjectId, subjectIdentifier, or subjectIdOrIdentifier
   * @param ldapSearchScope either OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
   * @param jobName for logging if problem
   * @param hib3GrouperLoaderLog 
   * @param subjectExpression 
   * @param extraAttributes 
   * @param groupNameExpression 
   * @param groupDisplayNameExpression 
   * @param groupDescriptionExpression 
   * @param groupNameToDisplayName map to translate group name to display name
   * @param groupNameToDescription map to translate group name to description
   * @param ldapAttributeFilterExpression if specified, this will filter the attributes that are turned into 
   * groups, should return true or false
   */
  public void initForLdapGroupsFromAttributes(final String ldapServerId, final String filter,
      final String searchDn, final String subjectAttributeName,
      final String groupAttributeName, final String sourceId,
      final String subjectIdType, final String ldapSearchScope, final String jobName,
      final Hib3GrouperLoaderLog hib3GrouperLoaderLog,
      final String subjectExpression,
      final String extraAttributes,
      final String groupNameExpression, 
      final String groupDisplayNameExpression, 
      final String groupDescriptionExpression,  
      final Map<String, String> groupNameToDisplayName,
      final Map<String, String> groupNameToDescription, final String ldapAttributeFilterExpression) {

    //run the query
    final LdapSearchScope ldapSearchScopeEnum = LdapSearchScope.valueOfIgnoreCase(
        ldapSearchScope, false);

    boolean hasSourceId = !StringUtils.isBlank(sourceId);

    String subjectIdCol = subjectIdCol(subjectIdType);

    this.columnNames.add("GROUP_NAME");
    this.columnTypes.add(Types.VARCHAR);

    this.columnNames.add(subjectIdCol);
    this.columnTypes.add(Types.VARCHAR);

    if (hasSourceId) {

      this.columnNames.add("SUBJECT_SOURCE_ID");
      this.columnTypes.add(Types.VARCHAR);

    }

    final String overallGroupName = hib3GrouperLoaderLog.getGroupNameFromJobName();

    if (StringUtils.isBlank(overallGroupName)) {
      throw new RuntimeException("Why is group name blank??? "
          + hib3GrouperLoaderLog.getJobName());
    }

    boolean requireTopStemAsStemFromConfigGroup = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(
        "loader.ldap.requireTopStemAsStemFromConfigGroup", true);

    String groupParentFolderNameTemp = requireTopStemAsStemFromConfigGroup ? (GrouperUtil.parentStemNameFromName(overallGroupName) + ":") : "";
    if (!StringUtils.isBlank(groupParentFolderNameTemp) && !groupParentFolderNameTemp.endsWith(":")) {
      groupParentFolderNameTemp += ":";
    }
    final String groupParentFolderName = groupParentFolderNameTemp;
    
    Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
    try {
      
      List<String> attributesList = new ArrayList<String>();

      //there can be subject attribute
      if (!StringUtils.isBlank(subjectAttributeName)) {
        attributesList.add(subjectAttributeName);
      }
      //there must be a group attribute points to group
      // and multiple attributes may be present and separated
      // by a comma
      String[] groupAttributeNameArray = null;
      groupAttributeNameArray = GrouperUtil.splitTrim(groupAttributeName, ",");
      for (String attribute: groupAttributeNameArray) {
        attributesList.add(attribute);
      }

      String[] extraAttributeArray = null;

      if (!StringUtils.isBlank(extraAttributes)) {
        extraAttributeArray = GrouperUtil.splitTrim(extraAttributes, ",");
        for (String attribute : extraAttributeArray) {
          attributesList.add(attribute);
        }

      }

      String[] attributeArray = GrouperUtil.toArray(attributesList, String.class);

      List<LdapEntry> searchResults = LdapSessionUtils.ldapSession().list(ldapServerId, searchDn, ldapSearchScopeEnum, filter, attributeArray, null);

      Map<String, String> attributeNameToGroupNameMap = new HashMap<String, String>();

      int subObjectCount = 0;
      int subObjectValidCount = 0;

      //if filtering attributes by a jexl, then this is the cached result true or false, for if it is a valid attribute
      Map<String, Boolean> validAttributes = new HashMap<String, Boolean>();

      for (LdapEntry searchResult : searchResults) {

        String subjectNameInNamespace = searchResult.getDn();
        String subjectId = null;

        if (!StringUtils.isBlank(subjectAttributeName)) {
          LdapAttribute subjectAttributeObject = searchResult.getAttribute(subjectAttributeName);
          if (subjectAttributeObject == null) {
            throw new RuntimeException("Cant find attribute " + subjectAttributeName + " in LDAP record.  Maybe you have "
                + "bad data in your LDAP or need to add to your filter a restriction that this attribute exists: '" 
                + subjectNameInNamespace + "'");
          }
          subjectId = (String) subjectAttributeObject.getStringValues().iterator().next();
        }

        if (!StringUtils.isBlank(subjectExpression)) {
          Map<String, Object> envVars = new HashMap<String, Object>();

          Map<String, Object> subjectAttributes = new HashMap<String, Object>();
          subjectAttributes.put("dn", subjectNameInNamespace);

          if (!StringUtils.isBlank(subjectId)) {
            subjectAttributes.put("subjectId", subjectId);
          }

          if (!StringUtils.isBlank(extraAttributes)) {
            for (String currSubjectAttributeName : extraAttributeArray) {
              LdapAttribute subjectAttribute = searchResult.getAttribute(
                  currSubjectAttributeName);

              if (subjectAttribute != null) {

                if (subjectAttribute.getStringValues().size() > 1) {
                  throw new RuntimeException(
                      "Grouper LDAP loader only supports single valued subject attributes at this point: "
                      + currSubjectAttributeName);
                }
                subjectAttributes.put(currSubjectAttributeName, subjectAttribute.getStringValues().iterator().next());

              }
            }
          }
          envVars.put("subjectAttributes", subjectAttributes);
          subjectId = LoaderLdapUtils.substituteEl(subjectExpression,
              envVars);
        }

        if (StringUtils.isBlank(groupAttributeName)) {
          throw new RuntimeException("LDAP_GROUPS_FROM_ATTRIBUTES loader type requires group attribute name");
        }

        // loop over attribute names that indicate group membership
        for (String attribute: groupAttributeNameArray) {

          LdapAttribute groupAttribute = searchResult.getAttribute(attribute);

          if (groupAttribute != null) {
            for (String attributeValue : groupAttribute.getStringValues()) {

              if (attributeValue != null) {
                subObjectCount++;

                //lets see if we know the groupName
                String groupName = attributeNameToGroupNameMap.get(attributeValue);
                if (StringUtils.isBlank(groupName)) {

                  //lets see if valid attribute, see if a filter expression is set
                  if (!StringUtils.isBlank(ldapAttributeFilterExpression)) {
                    //see if we have already calculated it
                    if (!validAttributes.containsKey(attributeValue)) {

                      Map<String, Object> variableMap = new HashMap<String, Object>();
                      variableMap.put("attributeValue", attributeValue);

                      //lets run the filter on the attribute name
                      String attributeResultBooleanString = GrouperUtil.substituteExpressionLanguage(
                          ldapAttributeFilterExpression, variableMap, true, false, false);

                      boolean attributeResultBoolean = false;
                      try {
                        attributeResultBoolean = GrouperUtil.booleanValue(attributeResultBooleanString);
                      } catch (RuntimeException re) {
                        throw new RuntimeException("Error parsing boolean: '" + attributeResultBooleanString 
                            + "', expecting true or false, from expression: " + ldapAttributeFilterExpression );
                      }

                      if (LOG.isDebugEnabled()) {
                        LOG.debug("Attribute '" + attributeValue + "' is allowed to be used based on expression? " 
                            + attributeResultBoolean + ", '" + ldapAttributeFilterExpression + "', note the attributeValue is" +
                        " in a variable called attributeValue");
                      }

                      validAttributes.put((String)attributeValue, attributeResultBoolean);

                    }

                    //lets see if filtering
                    if (!validAttributes.get(attributeValue)) {
                      continue;
                    }
                  }

                  subObjectValidCount++;

                  String defaultFolder = defaultLdapFolder();

                  groupName = defaultFolder + attributeValue;


                  String loaderGroupDisplayName = null;
                  String loaderGroupDescription = null;

                  if (!StringUtils.isBlank(groupNameExpression)
                      || !StringUtils.isBlank(groupDisplayNameExpression)
                      || !StringUtils.isBlank(groupDescriptionExpression)) {

                    //calculate it

                    Map<String, Object> envVars = new HashMap<String, Object>();

                    envVars.put("groupAttribute", attributeValue);

                    if (!StringUtils.isBlank(groupNameExpression)) {
                      groupName = LoaderLdapUtils.substituteEl(groupNameExpression,
                          envVars);
                    }
                    if (!StringUtils.isBlank(groupDisplayNameExpression)) {
                      String elGroupDisplayName = LoaderLdapUtils.substituteEl(groupDisplayNameExpression,
                          envVars);
                      loaderGroupDisplayName = groupParentFolderName + elGroupDisplayName;
                    }
                    if (!StringUtils.isBlank(groupDescriptionExpression)) {
                      String elGroupDescription = LoaderLdapUtils.substituteEl(groupDescriptionExpression,
                          envVars);
                      loaderGroupDescription = elGroupDescription;
                    }
                  }

                  groupName = groupParentFolderName + groupName;

                  if (!StringUtils.isBlank(loaderGroupDisplayName)) {
                    groupNameToDisplayName.put(groupName, loaderGroupDisplayName);
                  }

                  if (!StringUtils.isBlank(loaderGroupDescription)) {
                    groupNameToDescription.put(groupName, loaderGroupDescription);
                  }

                  //cache this
                  attributeNameToGroupNameMap.put((String)attributeValue, groupName);

                  //init the subject list
                  if (!resultMap.containsKey(groupName)) {
                    resultMap.put(groupName, new ArrayList<String>());
                  }
                }
                //get the "row" for the group
                List<String> valueResults = resultMap.get(groupName);
                //add the subject
                valueResults.add((String) subjectId);
              }
            }
          }

        } // end of looping over attributes indicating group membership
      } // end of looping over search results


      if (LOG.isDebugEnabled()) {
        LOG.debug("Found " + resultMap.size() + " results, (" + subObjectCount
            + " sub-results, " + subObjectValidCount + " valid-sub-results) for serverId: " + ldapServerId + ", searchDn: "
            + searchDn
            + ", filter: '" + filter + "', returning subject attribute: "
            + subjectAttributeName + ", some results: "
            + GrouperUtil.toStringForLog(resultMap, 100));
      }
    } catch (Exception re) {
      GrouperUtil.injectInException(re, "Error querying ldap server id: " + ldapServerId
          + ", searchDn: " + searchDn
          + ", filter: '" + filter + "', returning subject attribute: "
          + subjectAttributeName);
      throw new RuntimeException(re);
    }

    for (String currentGroupName : resultMap.keySet()) {
      List<String> results = resultMap.get(currentGroupName);
      for (String result : results) {
        Row row = new Row();
        Object[] rowData = new Object[hasSourceId ? 3 : 2];
        row.setRowData(rowData);
        this.data.add(row);
        rowData[0] = currentGroupName;
        rowData[1] = result;
        if (hasSourceId) {
          rowData[2] = sourceId;
        }
      }
    }
    this.convertToSubjectIdIfNeeded(jobName, hib3GrouperLoaderLog, null);
  }

  /**
   * if there is no subject id col, then make one and resolve the subjects
   * @param jobName for logging
   * @param hib3GrouperLoaderLog 
   * @param ldapSubjectExpression 
   */
  private void convertToSubjectIdIfNeeded(String jobName,
      Hib3GrouperLoaderLog hib3GrouperLoaderLog, String ldapSubjectExpression) {

    int subjectIdColIndex = this.columnIndex("SUBJECT_ID", false);
    subjectIdColIndex = subjectIdColIndex == -1 ? this.columnIndex("SUBJECT_IDENTIFIER",
        false) : subjectIdColIndex;
    subjectIdColIndex = subjectIdColIndex == -1 ? this.columnIndex(
        "SUBJECT_ID_OR_IDENTIFIER", false) : subjectIdColIndex;

    if (!StringUtils.isBlank(ldapSubjectExpression) && subjectIdColIndex > -1) {

      Map<String, Object> envVars = new HashMap<String, Object>();
      for (Row row : GrouperUtil.nonNull(this.data)) {
        String subjectId = (String) row.getRowData()[subjectIdColIndex];

        envVars.clear();
        envVars.put("subjectId", subjectId);

        String newSubjectId = LoaderLdapUtils
            .substituteEl(ldapSubjectExpression, envVars);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Converted subject id from '" + subjectId + "' to '" + newSubjectId
              + "' based on subjectExpression: '" + ldapSubjectExpression + "'");
        }

        row.getRowData()[subjectIdColIndex] = newSubjectId;
      }

    }
    
    /*
    // we've got it, we done!
    if (this.hasColumnName("SUBJECT_ID")) {
      return;
    }

    int subjectSourceIdIndex = this.columnIndex("SUBJECT_SOURCE_ID", false);
    boolean addedSourceCol = subjectSourceIdIndex == -1;

    //there is no subject id, dont try to find one
    if (subjectIdColIndex == -1) {
      return;
    }

    if (this.data != null) {
      //lets resolve each row and replace the subject_identifier or subject_id_or_identifier with subject_id
      Iterator<Row> iterator = this.data.iterator();

      while (iterator.hasNext()) {

        Row row = iterator.next();
        Subject subject = row.getSubject(jobName);
        if (subject == null) {
          //subject error
          hib3GrouperLoaderLog.addUnresolvableSubjectCount(1);
          hib3GrouperLoaderLog.appendJobMessage(row.getSubjectError());
          
          iterator.remove();
          continue;
        }
        Object[] rowData = row.getRowData();
        rowData[subjectIdColIndex] = subject.getId();

        if (addedSourceCol) {
          //add a col for source id since we just resolved it...
          Object[] newRowData = new Object[rowData.length + 1];
          System.arraycopy(rowData, 0, newRowData, 0, rowData.length);
          newRowData[rowData.length] = subject.getSourceId();
          row.setRowData(newRowData);
        }

      }
    }

    //lets change the column names
    if (addedSourceCol) {
      this.columnNames.add("SUBJECT_SOURCE_ID");
      this.columnTypes.add(Types.VARCHAR);
    }
    this.columnNames.set(subjectIdColIndex, "SUBJECT_ID");
    //i assume it is varchar already, if not, it should be
    this.columnTypes.set(subjectIdColIndex, Types.VARCHAR);
*/
  }

  /** column names (toUpper) */
  private List<String> columnNames = new ArrayList<String>();

  /** column types (from java.sql.Types) */
  private List<Integer> columnTypes = new ArrayList<Integer>();

  /** array of arrays of data for the grid of the resultset
   * the number of cols will equal the number of column names
   * user arrays since lightweight
   */
  private List<Row> data = new ArrayList<Row>();

  /**
   * if has bulk lookuped subjects already
   */
  private boolean hasBulkLookupedSubjects = false;
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderResultset.class);

  /**
   * simple struct for subjects
   */
  public class Row {

    /** row data from db */
    private Object[] rowData;

    /** the subject for this row */
    private Subject subject;

    /** the error for this row */
    private String subjectError;

    /**
     * @return the rowData
     */
    public Object[] getRowData() {
      return this.rowData;
    }

    /**
     * @param rowData1 the rowData to set
     */
    public void setRowData(Object[] rowData1) {
      this.rowData = rowData1;
    }

    /**
     * @param jobName for logging
     * @return the subject
     */
    public Subject getSubject(String jobName) {
      if (this.subject != null || this.subjectError != null) {
        return this.subject;
      }
      //if it is null, and null, then it must not have been retrieved,
      //so get it
      String subjectId = (String) this.getCell(
          GrouperLoaderResultset.SUBJECT_ID_COL, false);
      String subjectIdentifier = (String) this.getCell(
          GrouperLoaderResultset.SUBJECT_IDENTIFIER_COL, false);
      String subjectIdOrIdentifier = (String) this.getCell(
          GrouperLoaderResultset.SUBJECT_ID_OR_IDENTIFIER_COL, false);

      String subjectSourceId = (String) this.getCell(
          GrouperLoaderResultset.SUBJECT_SOURCE_ID_COL, false);

      String defaultSubjectSourceId = GrouperLoaderConfig.retrieveConfig().propertyValueString(
          GrouperLoaderConfig.DEFAULT_SUBJECT_SOURCE_ID);

      String subjectIdForLog = null;
      String subjectColForLog = null;

      //maybe get the sourceId from config file
      subjectSourceId = StringUtils
          .defaultString(subjectSourceId, defaultSubjectSourceId);
      try {

        if (!StringUtils.isBlank(subjectId)) {
          subjectIdForLog = subjectId;
          subjectColForLog = "subjectId";
          if (!StringUtils.isBlank(subjectSourceId)) {
            this.subject = SubjectFinder.getSource(subjectSourceId).getSubject(subjectId,
                false);
            //CH 20091013: we need the loader to be based on subjectId to eliminate lookups...
            //this.subject = SubjectFinder.getSource(subjectSourceId).getSubject(subjectId, false);
            //if (this.subject == null) {
            //  this.subject = SubjectFinder.getSource(subjectSourceId).getSubjectByIdentifier(subjectId, true);
            //}
          } else {
            this.subject = SubjectFinder.findById(subjectId, false);
            //this.subject = SubjectFinder.findByIdOrIdentifier(subjectId, true);
          }
        } else if (!StringUtils.isBlank(subjectIdentifier)) {
          subjectIdForLog = subjectIdentifier;
          subjectColForLog = "subjectIdentifier";
          if (!StringUtils.isBlank(subjectSourceId)) {
            this.subject = SubjectFinder.findByIdentifierAndSource(subjectIdentifier,
                subjectSourceId, false);
          } else {
            this.subject = SubjectFinder.findByIdentifier(subjectIdentifier, false);
          }

        } else if (!StringUtils.isBlank(subjectIdOrIdentifier)) {
          subjectIdForLog = subjectIdOrIdentifier;
          subjectColForLog = "subjectIdOrIdentifier";
          if (!StringUtils.isBlank(subjectSourceId)) {
            this.subject = SubjectFinder.findByIdOrIdentifierAndSource(
                subjectIdOrIdentifier, subjectSourceId, false);
          } else {
            this.subject = SubjectFinder
                .findByIdOrIdentifier(subjectIdOrIdentifier, false);
          }

        } else {
          throw new RuntimeException(
              "Loader job needs to have SUBJECT_ID, SUBJECT_IDENTIFIER, or SUBJECT_ID_OR_IDENTIFIER! "
                  + jobName
                  + ", "
                  + GrouperUtil.toStringForLog(GrouperLoaderResultset.this
                      .getColumnNames()));
        }
        
        if (this.subject == null) {
          String subjectWarning = "Subject is unresolvable '" + subjectIdForLog + "' col: " + subjectColForLog + ", jobName: " + jobName;
          LOG.warn(subjectWarning);
          this.subjectError = subjectWarning;
          
          if (GrouperShell.runFromGsh) {
            System.out.println(subjectWarning);
          }
        }
        
      } catch (Exception e) {
        this.subjectError = "Problem with " + subjectColForLog + ": "
            + subjectIdForLog + ", subjectSourceId: " + subjectSourceId
            + ", in jobName: " + jobName;
        LOG.error(this.subjectError, e);
        
        if (GrouperShell.runFromGsh) {
          System.out.println(this.subjectError);
        }
        
        if (e instanceof SubjectNotFoundException
            || e instanceof SubjectNotUniqueException
            || e instanceof SourceUnavailableException) {
          //swallow these...
        } else {
          //rethrow these
          if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
          }
          //this shouldnt really be possible
          throw new RuntimeException(e);
        }

      }
      return this.subject;
    }

    /**
     * get a cell in the data structure
     * @param columnName
     * @param exceptionOnColNotFound
     * @return the cell or null if col not found and not throwing exception if col not found
     */
    public Object getCell(String columnName, boolean exceptionOnColNotFound) {

      if (GrouperLoaderResultset.this.hasColumnName(columnName)) {
        int columnIndex = GrouperLoaderResultset.this.columnIndex(columnName);
        return this.rowData[columnIndex];
      }
      if (exceptionOnColNotFound) {
        throw new RuntimeException("Column not found: " + columnName);
      }
      return null;
    }

    /**
     * @return the error
     */
    public String getSubjectError() {
      return this.subjectError;
    }

  }

  /**
   * find a column index in the resultset
   * @param columnNameInput
   * @return the column index
   */
  public int columnIndex(String columnNameInput) {
    return columnIndex(columnNameInput, true);
  }

  /**
   * find a column index in the resultset
   * @param columnNameInput
   * @param throwErrorIfNotFound if should throw error if not found
   * @return the column index or -1 if not found or exception
   */
  public int columnIndex(String columnNameInput, boolean throwErrorIfNotFound) {
    int i = 0;
    for (String columnName : this.columnNames) {
      if (StringUtils.equalsIgnoreCase(columnName, columnNameInput)) {
        return i;
      }
      i++;
    }
    if (throwErrorIfNotFound) {
      throw new RuntimeException("Cant find column: " + columnNameInput);
    }
    return -1;
  }

  /**
   * find a certain row
   * @param i
   * @return the row
   */
  public Row retrieveRow(int i) {
    return this.data.get(i);
  }

  /**
   * return the number of rows
   * @return the number of rows
   */
  public int numberOfRows() {
    return this.data == null ? 0 : this.data.size();
  }

  /**
   * return the column names
   * @return the column names
   */
  public List<String> getColumnNames() {
    return this.columnNames;
  }

  /**
   * get a cell in the data structure
   * @param rowIndex
   * @param columnName
   * @param exceptionOnColNotFound
   * @return the cell or null if col not found and not throwing exception if col not found
   */
  public Object getCell(int rowIndex, String columnName, boolean exceptionOnColNotFound) {
    return this.data.get(rowIndex).getCell(columnName, exceptionOnColNotFound);
  }

  /**
   * make sure this column name is here
   * @param columnName
   */
  public void assertColumnName(String columnName) {
    for (String existingColumn : this.columnNames) {
      if (StringUtils.equalsIgnoreCase(columnName, existingColumn)) {
        return;
      }
    }
    StringBuilder error = new StringBuilder("Cant find column: '" + columnName
        + "' in columns: ");
    for (String existingColumn : this.columnNames) {
      error.append(existingColumn).append(", ");
    }
    throw new RuntimeException(error.toString());
  }

  /**
   * make sure this column name is here
   * @param columnName
   * @return true if the column is there
   */
  public boolean hasColumnName(String columnName) {

    for (String existingColumn : this.columnNames) {
      if (StringUtils.equalsIgnoreCase(columnName, existingColumn)) {
        return true;
      }
    }
    return false;
  }

  /**
   * remove by row
   * @param row
   */
  public void remove(Row row) {
    this.data.remove(row);
  }

  /**
   * remove by row index
   * @param i
   */
  public void remove(int i) {
    this.data.remove(i);
  }

  /**
   * find a row and return
   * @param subjectId
   * @param subjectSourceId
   * @param subjectIdentifier0
   * @return row if found, else null
   */
  public Row find(String subjectId, String subjectSourceId, String subjectIdentifier0) {

    //might not have subject id or identifier
    boolean hasSubjectIdOrIdentifierCol = this.hasColumnName(SUBJECT_ID_OR_IDENTIFIER_COL);
    int subjectIdOrIdentifierIndex = hasSubjectIdOrIdentifierCol ? this.columnIndex(SUBJECT_ID_OR_IDENTIFIER_COL) : -1;
    
    //might not have subject identifier
    boolean hasSubjectIdentifierCol = this.hasColumnName(SUBJECT_IDENTIFIER_COL);
    int subjectIdentifierIndex = hasSubjectIdentifierCol ? this.columnIndex(SUBJECT_IDENTIFIER_COL) : -1;
    
    //might not have subject id
    boolean hasSubjectIdCol = this.hasColumnName(SUBJECT_ID_COL);
    int subjectIdIndex = hasSubjectIdCol ? this.columnIndex(SUBJECT_ID_COL) : -1;

    //might not have subject source id
    boolean hasSubjectSourceIdCol = this.hasColumnName(SUBJECT_SOURCE_ID_COL);
    int subjectSourceIdIndex = hasSubjectSourceIdCol ? this.columnIndex(SUBJECT_SOURCE_ID_COL) : -1;

    Iterator<Row> iterator = this.data.iterator();
    while (iterator.hasNext()) {
      Row row = iterator.next();
      Object[] rowData = row.getRowData();
      if ((hasSubjectIdCol && StringUtils.equals((String) rowData[subjectIdIndex], subjectId)) || 
          (hasSubjectIdentifierCol && StringUtils.equals((String) rowData[subjectIdentifierIndex], subjectIdentifier0)) ||
          (hasSubjectIdOrIdentifierCol && (StringUtils.equals((String) rowData[subjectIdOrIdentifierIndex], subjectId) 
              || StringUtils.equals((String) rowData[subjectIdOrIdentifierIndex], subjectIdentifier0)))) {
        if (hasSubjectSourceIdCol) {
          if (!StringUtils.equals((String) rowData[subjectSourceIdIndex], subjectSourceId)) {
            continue;
          }
        }
        //at this point, they are the same
        return row;
        //dont break, since could have multiple
      }
    }
    
    return null;
  }

  /**
   * @return default ldap folder for groups including trailing colon if not blank
   */
  private static String defaultLdapFolder() {
    String defaultFolder = "groups:";
    
    if (GrouperLoaderConfig.retrieveConfig().properties().containsKey("loader.ldap.defaultGroupFolder")) {
      defaultFolder = StringUtils.defaultString(GrouperLoaderConfig.retrieveConfig().propertyValueString("loader.ldap.defaultGroupFolder"));
      if (!StringUtils.isBlank(defaultFolder) && !defaultFolder.endsWith(":")) {
        defaultFolder += ":";
      }
    }
    return defaultFolder;
  }

}
