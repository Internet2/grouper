/*
 * @author mchyzer
 * $Id: GrouperLoaderResultset.java,v 1.9 2009-10-18 16:30:51 mchyzer Exp $
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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.ldap.GrouperLoaderLdapServer;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.ldap.LdapHandler;
import edu.internet2.middleware.grouper.ldap.LdapHandlerBean;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;


/**
 * encapsulate a resultset into this resultset to be case-insensitive and
 * column-order insensitive
 */
public class GrouperLoaderResultset {
  
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
  public static final String SUBJECT_SOURCE_ID_COL = "SUBJECT_SOURCE_ID";

  /**
   * get a resultset on another resultset and a group name
   * @param parentResultSet
   * @param groupName
   */
  public GrouperLoaderResultset(GrouperLoaderResultset parentResultSet, String groupName) {
    this.columnNames = parentResultSet.columnNames == null ? null : new ArrayList<String>(parentResultSet.columnNames);
    this.columnTypes = parentResultSet.columnTypes == null ? null : new ArrayList<Integer>(parentResultSet.columnTypes);
    for (int i=0;i<parentResultSet.data.size();i++) {
      
      if (StringUtils.equals(groupName, (String)parentResultSet.getCell(i, GROUP_NAME_COL, true))) {
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
    for (int i=0;i<this.data.size();i++) {
      groupNames.add((String)this.getCell(i, GROUP_NAME_COL, true));
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
  public GrouperLoaderResultset(GrouperLoaderDb grouperLoaderDb, String query, String jobName, Hib3GrouperLoaderLog hib3GrouperLoaderLog) {

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
        for (int i=0;i<columnCount;i++) {
          this.columnNames.add(resultSetMetaData.getColumnLabel(i+1));
          this.columnTypes.add(resultSetMetaData.getColumnType(i+1));
        }
        
        while(resultSet.next()) {
          Row row = new Row();
          Object[] rowData = new Object[columnCount];
          row.setRowData(rowData);
          this.data.add(row);
          //at this point, assume everything is either a string or a timestamp
          for (int i=0;i<columnCount;i++) {
            if (this.columnTypes.get(i) == Types.TIMESTAMP) {
              rowData[i] = resultSet.getTimestamp(i+1);
            } else {
              rowData[i] = resultSet.getString(i+1);
            }
          }
          
        }
      } finally {
        //this is important so no one sneaks some delete statement in there
        GrouperUtil.rollbackQuietly(connection);
      }
    } catch (SQLException se) {
      throw new RuntimeException("Problem with query: " + query + ",  on db: " + grouperLoaderDb, se);
    } finally {
      GrouperUtil.closeQuietly(resultSet);
      GrouperUtil.closeQuietly(statement);
      GrouperUtil.closeQuietly(connection);
    }
    this.convertToSubjectIdIfNeeded(jobName, hib3GrouperLoaderLog, null, true);

  }
  
  /**
   * get subject id col
   * @param subjectIdType
   * @return
   */
  private String subjectIdCol(String subjectIdType) {
    String subjectIdCol = "SUBJECT_ID";

    if(!StringUtils.isBlank(subjectIdType)) {
     
      if (StringUtils.equalsIgnoreCase(subjectIdType, "SUBJECT_ID") || StringUtils.equalsIgnoreCase(subjectIdType, "subjectId")) {
        subjectIdCol = "SUBJECT_ID";
      } else if (StringUtils.equalsIgnoreCase(subjectIdType, "SUBJECT_IDENTIFIER") || StringUtils.equalsIgnoreCase(subjectIdType, "subjectIdentifier")) {
        subjectIdCol = "SUBJECT_IDENTIFIER";
      } else if (StringUtils.equalsIgnoreCase(subjectIdType, "SUBJECT_ID_OR_IDENTIFIER") || StringUtils.equalsIgnoreCase(subjectIdType, "subjectIdOrIdentifier")) {
        subjectIdCol = "SUBJECT_ID_OR_IDENTIFIER";
      } else {
        throw new RuntimeException("Not expecting subjectIdType: '" + subjectIdType + "', should be subjectId, subjectIdentifier, or subjectIdOrIdentifier");
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
   * @param errorUnresolvable 
   */
  public GrouperLoaderResultset(String ldapServerId, String filter, 
      String searchDn, String subjectAttribute, String sourceId, 
      String subjectIdType, String ldapSearchScope, String jobName, 
      Hib3GrouperLoaderLog hib3GrouperLoaderLog, String ldapSubjectExpression, boolean errorUnresolvable) {
    
    //run the query
    LdapSearchScope ldapSearchScopeEnum = LdapSearchScope.valueOfIgnoreCase(ldapSearchScope, false);
    
    boolean hasSourceId = !StringUtils.isBlank(sourceId);
    
    String subjectIdCol = subjectIdCol(subjectIdType);
    
    this.columnNames.add(subjectIdCol);
    this.columnTypes.add(Types.VARCHAR);
    
    if (hasSourceId) {
      
      this.columnNames.add("SUBJECT_SOURCE_ID");
      this.columnTypes.add(Types.VARCHAR);
      
    }
    
    List<String> results = LdapSession.list(String.class, ldapServerId, searchDn, ldapSearchScopeEnum, filter, subjectAttribute);

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
    this.convertToSubjectIdIfNeeded(jobName, hib3GrouperLoaderLog, ldapSubjectExpression, errorUnresolvable);
  }
  
  /**
   * get a resultset based on an ldap server and filter
   * @param ldapServerId server id in grouper-loader.properties
   * @param filter ldap filter query
   * @param searchDn place in ldap where search starts from
   * @param subjectAttributeName attribute where the subjectId, or subjectIdentifier, or subjectIdOrIdentifier is
   * @param sourceId if all subjects come from one source, put the sourceId here
   * @param subjectIdType the type of the subjectId, either: subjectId, subjectIdentifier, or subjectIdOrIdentifier
   * @param ldapSearchScope either OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
   * @param jobName for logging if problem
   * @param hib3GrouperLoaderLog 
   * @param groupsLikeString this is the sql string to identify groups in registry like the loader groups to delete orphans
   * @param subjectExpression 
   * @param errorUnresolvable 
   * @param extraAttributes 
   * @param groupNameExpression 
   */
  public GrouperLoaderResultset(final String ldapServerId, final String filter, 
      final String searchDn, final String subjectAttributeName, final String sourceId, 
      final String subjectIdType, final String ldapSearchScope, final String jobName, final Hib3GrouperLoaderLog hib3GrouperLoaderLog,
      final String groupsLikeString, final String subjectExpression, final boolean errorUnresolvable, final String extraAttributes,
      final String groupNameExpression) {
    
    //run the query
    final LdapSearchScope ldapSearchScopeEnum = LdapSearchScope.valueOfIgnoreCase(ldapSearchScope, false);
    
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
      throw new RuntimeException("Why is group name blank??? " + hib3GrouperLoaderLog.getJobName());
    }

    final String groupParentFolderName = GrouperUtil.parentStemNameFromName(groupName);
    
    Map<String, List<String>> resultMap = null;
    try {
      
      resultMap = (Map<String, List<String>>)LdapSession.callbackLdapSession(ldapServerId, new LdapHandler() {
        
        public Object callback(LdapHandlerBean ldapHandlerBean) throws NamingException {
  
          Ldap ldap = ldapHandlerBean.getLdap();
          
          Iterator<SearchResult> searchResultIterator = null;
          
          List<String> attributesList = new ArrayList<String>();
          attributesList.add(subjectAttributeName);
          String[] extraAttributeArray = null;
          
          if (!StringUtils.isBlank(extraAttributes)) {
            extraAttributeArray = GrouperUtil.splitTrim(extraAttributes, ",");
            for (String attribute : extraAttributeArray) {
              attributesList.add(attribute);
            }
            
          }
          
          SearchFilter searchFilterObject = new SearchFilter(filter);
          String[] attributeArray = GrouperUtil.toArray(attributesList, String.class);
          
          SearchControls searchControls = ldap.getLdapConfig().getSearchControls(attributeArray);
          
          if (ldapSearchScopeEnum != null) {
            searchControls.setSearchScope(ldapSearchScopeEnum.getSeachControlsConstant());
          }
          
          if (StringUtils.isBlank(searchDn)) {
            searchResultIterator = ldap.search(
                searchFilterObject, searchControls);
          } else {
            searchResultIterator = ldap.search(searchDn,
                searchFilterObject, searchControls);
          }
          
          Map<String, List<String>> result = new HashMap<String, List<String>>();
          int subObjectCount = 0;
          while (searchResultIterator.hasNext()) {

            SearchResult searchResult = searchResultIterator.next();
            
            List<String> valueResults = new ArrayList<String>();
            String nameInNamespace = searchResult.getName();
            //for some reason this returns: cn=test:testGroup,dc=upenn,dc=edu
            // instead of cn=test:testGroup,ou=groups,dc=upenn,dc=edu
            if (nameInNamespace != null && !StringUtils.isBlank(searchDn)) {
              GrouperLoaderLdapServer grouperLoaderLdapServer = GrouperLoaderConfig.retrieveLdapProfile(ldapServerId);
              String baseDn = grouperLoaderLdapServer.getBaseDn();
              if (!StringUtils.isBlank(baseDn) && nameInNamespace.endsWith("," + baseDn)) {
                
                //sub one to get the comma out of there
                nameInNamespace = nameInNamespace.substring(0, nameInNamespace.length() - (baseDn.length()+1));
                nameInNamespace += "," + searchDn + "," + baseDn;
              }
            }
            String loaderGroupName = null;
            //TODO allow null group name expression
            if (!StringUtils.isBlank(groupNameExpression)) {
              Map<String, Object> envVars = new HashMap<String, Object>();

              Map<String, Object> groupAttributes = new HashMap<String, Object>();
              groupAttributes.put("dn", nameInNamespace);
              if (!StringUtils.isBlank(extraAttributes)) {
                for (String groupAttributeName : extraAttributeArray) {
                  Attribute groupAttribute = searchResult.getAttributes().get(groupAttributeName);
                  
                  if (groupAttribute != null) {
                    
                    if (groupAttribute.size() > 1) {
                      throw new RuntimeException("Grouper LDAP loader only supports single valued group attributes at this point: " + groupAttributeName);
                    }
                    Object attributeValue = groupAttribute.get(0);
                    attributeValue = GrouperUtil.typeCast(attributeValue, String.class);
                    groupAttributes.put(groupAttributeName, attributeValue);
                    
                  }
                }
              }
              envVars.put("groupAttributes", groupAttributes);
              String elGroupName = LoaderLdapUtils.substituteEl(groupNameExpression, envVars);
              loaderGroupName = groupParentFolderName + ":" + elGroupName;
            }

            result.put(loaderGroupName, valueResults);
            
            Attribute subjectAttribute = searchResult.getAttributes().get(subjectAttributeName);
            
            if (subjectAttribute != null) {
              for (int i=0;i<subjectAttribute.size();i++) {
                
                Object attributeValue = subjectAttribute.get(i);
                attributeValue = GrouperUtil.typeCast(attributeValue, String.class);
                if (attributeValue != null) {
                  subObjectCount++;
                  valueResults.add((String)attributeValue);
                }
              }
            }
          }
  
          if (LOG.isDebugEnabled()) {
            LOG.debug("Found " + result.size() + " results, (" + subObjectCount + " sub-results) for serverId: " + ldapServerId + ", searchDn: " + searchDn
              + ", filter: '" + filter + "', returning subject attribute: " 
              + subjectAttributeName + ", some results: " + GrouperUtil.toStringForLog(result, 100) );
          }
          
          return result;
        }
      });
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Error querying ldap server id: " + ldapServerId + ", searchDn: " + searchDn
          + ", filter: '" + filter + "', returning subject attribute: " + subjectAttributeName);
      throw re;
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
    this.convertToSubjectIdIfNeeded(jobName, hib3GrouperLoaderLog, subjectExpression, errorUnresolvable);
  }

  /**
   * if there is no subject id col, then make one and resolve the subjects
   * @param jobName for logging
   * @param hib3GrouperLoaderLog 
   * @param ldapSubjectExpression 
   * @param errorUnresolvable 
   */
  private void convertToSubjectIdIfNeeded(String jobName, 
      Hib3GrouperLoaderLog hib3GrouperLoaderLog, String ldapSubjectExpression, boolean errorUnresolvable) {
    
    int subjectIdColIndex = this.columnIndex("SUBJECT_ID", false);
    subjectIdColIndex = subjectIdColIndex == -1 ? this.columnIndex("SUBJECT_IDENTIFIER", false) : subjectIdColIndex;
    subjectIdColIndex = subjectIdColIndex == -1 ? this.columnIndex("SUBJECT_ID_OR_IDENTIFIER", false) : subjectIdColIndex;

    if (!StringUtils.isBlank(ldapSubjectExpression) && subjectIdColIndex > -1) {
      
      Map<String, Object> envVars = new HashMap<String, Object>();
      for (Row row : GrouperUtil.nonNull(this.data)) {
        String subjectId = (String)row.getRowData()[subjectIdColIndex];
        
        envVars.clear();
        envVars.put("subjectId", subjectId);
        
        
        String newSubjectId = LoaderLdapUtils.substituteEl(ldapSubjectExpression, envVars);
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Converted subject id from '" + subjectId + "' to '" + newSubjectId + "' based on subjectExpression: '" + ldapSubjectExpression + "'");
        }
        
        row.getRowData()[subjectIdColIndex] = newSubjectId;
      }
      
    }
      
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
          if (errorUnresolvable) {
            //subject error
            hib3GrouperLoaderLog.appendJobMessage(row.getSubjectError());
            hib3GrouperLoaderLog.addUnresolvableSubjectCount(1);
          }
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
     * @param hib3GrouperLoaderLog
     * @return the subject
     */
    public Subject getSubject(String jobName) {
      if (this.subject != null || this.subjectError != null) {
        return this.subject;
      }
      //if it is null, and null, then it must not have been retrieved,
      //so get it
      String subjectId = (String)this.getCell(
          GrouperLoaderResultset.SUBJECT_ID_COL, false);
      String subjectIdentifier = (String)this.getCell(
          GrouperLoaderResultset.SUBJECT_IDENTIFIER_COL, false);
      String subjectIdOrIdentifier = (String)this.getCell(
          GrouperLoaderResultset.SUBJECT_ID_OR_IDENTIFIER_COL, false);
      
      String subjectSourceId = (String)this.getCell(
          GrouperLoaderResultset.SUBJECT_SOURCE_ID_COL, false);

      String defaultSubjectSourceId = GrouperLoaderConfig.getPropertyString(
          GrouperLoaderConfig.DEFAULT_SUBJECT_SOURCE_ID);
      
      String subjectIdForLog = null;
      String subjectColForLog = null;
      
      //maybe get the sourceId from config file
      subjectSourceId = StringUtils.defaultString(subjectSourceId, defaultSubjectSourceId);
      try {
        
        if (!StringUtils.isBlank(subjectId)) {
          subjectIdForLog = subjectId;
          subjectColForLog = "subjectId";
          if (!StringUtils.isBlank(subjectSourceId)) {
            this.subject = SubjectFinder.getSource(subjectSourceId).getSubject(subjectId, true);
            //CH 20091013: we need the loader to be based on subjectId to eliminate lookups...
            //this.subject = SubjectFinder.getSource(subjectSourceId).getSubject(subjectId, false);
            //if (this.subject == null) {
            //  this.subject = SubjectFinder.getSource(subjectSourceId).getSubjectByIdentifier(subjectId, true);
            //}
          } else {
            this.subject = SubjectFinder.findById(subjectId, true);
            //this.subject = SubjectFinder.findByIdOrIdentifier(subjectId, true);
          }
        } else if (!StringUtils.isBlank(subjectIdentifier)) {
          subjectIdForLog = subjectIdentifier;
          subjectColForLog = "subjectIdentifier";
          if (!StringUtils.isBlank(subjectSourceId)) {
            this.subject = SubjectFinder.findByIdentifierAndSource(subjectIdentifier, subjectSourceId, true);
          } else {
            this.subject = SubjectFinder.findByIdentifier(subjectIdentifier, true);
          }
          
        } else if (!StringUtils.isBlank(subjectIdOrIdentifier)) {
          subjectIdForLog = subjectIdOrIdentifier;
          subjectColForLog = "subjectIdOrIdentifier";
          if (!StringUtils.isBlank(subjectSourceId)) {
            this.subject = SubjectFinder.findByIdOrIdentifierAndSource(subjectIdOrIdentifier, subjectSourceId, true);
          } else {
            this.subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, true);
          }
          
        } else {
          throw new RuntimeException("Loader job needs to have SUBJECT_ID, SUBJECT_IDENTIFIER, or SUBJECT_ID_OR_IDENTIFIER! " 
              + jobName + ", " + GrouperUtil.toStringForLog(GrouperLoaderResultset.this.getColumnNames()));
        }
      } catch (Exception e) {
        this.subjectError = "Problem with " + subjectColForLog + ": " 
            + subjectIdForLog + ", subjectSourceId: " + subjectSourceId + ", in jobName: " + jobName;
        LOG.error(this.subjectError, e); 
        if (e instanceof SubjectNotFoundException
            || e instanceof SubjectNotUniqueException
            || e instanceof SourceUnavailableException) {
          //swallow these...
        } else {
          //rethrow these
          if (e instanceof RuntimeException) {
            throw (RuntimeException)e;
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
    for(String columnName : this.columnNames) {
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
    StringBuilder error = new StringBuilder("Cant find column: '" + columnName + "' in columns: ");
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
   * @return row if found, else null
   */
  public Row find(String subjectId, String subjectSourceId) {
    int subjectIndex = this.columnIndex(SUBJECT_ID_COL);

    //might not have subject source id
    boolean hasSubjectSourceIdCol = this.hasColumnName(SUBJECT_SOURCE_ID_COL);
    int subjectSourceIdIndex = hasSubjectSourceIdCol ? this.columnIndex(SUBJECT_SOURCE_ID_COL) : -1;

    Iterator<Row> iterator = this.data.iterator();
    while(iterator.hasNext()) {
      Row row = iterator.next();
      Object[] rowData = row.getRowData();
      if (StringUtils.equals((String)rowData[subjectIndex], subjectId)) {
        if (hasSubjectSourceIdCol) {
          if (!StringUtils.equals((String)rowData[subjectSourceIdIndex], subjectSourceId)) {
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

}
