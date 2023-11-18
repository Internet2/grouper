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
 * @author mchyzer
 * $Id: GcGetMembers.java,v 1.5 2009-12-07 07:33:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsResponseMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetMembersRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.morphString.Crypto;


/**
 * class to run a get members web service call
 */
public class GcGetMembers {

  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   */
  private String wsEndpoint;

  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   * @param theWsEndpoint
   * @return this for chaining
   */
  public GcGetMembers assignWsEndpoint(String theWsEndpoint) {
    this.wsEndpoint = theWsEndpoint;
    return this;
  }
  
  /**
   * ws user
   */
  private String wsUser;

  /**
   * ws user
   * @param theWsUser
   * @return this for chaining
   */
  public GcGetMembers assignWsUser(String theWsUser) {
    this.wsUser = theWsUser;
    return this;
  }
  
  /**
   * ws pass
   */
  private String wsPass;

  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcGetMembers assignWsPass(String theWsPass) {
    this.wsPass = theWsPass;
    return this;
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcGetMembers assignWsPassEncrypted(String theWsPassEncrypted) {
    String encryptKey = GrouperClientUtils.encryptKey();
    return this.assignWsPass(new Crypto(encryptKey).decrypt(theWsPassEncrypted));
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcGetMembers assignWsPassFile(File theFile) {
    return this.assignWsPass(GrouperClientUtils.readFileIntoString(theFile));
  }

  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcGetMembers assignWsPassFileEncrypted(File theFile) {
    return this.assignWsPassEncrypted(GrouperClientUtils.readFileIntoString(theFile));
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GcGetMembers.class);

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GcGetMembers gcGetMembers = new GcGetMembers().addGroupName("penn:isc:nandt:apps:pennbox:user")
      .addSubjectAttributeName("PENNNAME").assignAutopage(true);
    gcGetMembers.assignPageSize(10000);
    WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();
    System.out.println("Found " + GrouperClientUtils.length(wsGetMembersResults.getResults()[0].getWsSubjects()));
    for (WsSubject wsSubject : wsGetMembersResults.getResults()[0].getWsSubjects()) {
      System.out.println(wsSubject.getAttributeValue(0));
    }
    System.out.println("Found " + GrouperClientUtils.length(wsGetMembersResults.getResults()[0].getWsSubjects()));

    //printPagingReport();
  }
  
  /**
   * if should page through results so it doesnt timeout.  Note, if pageSize is not set, will page 10k records
   */
  private Boolean autopage;

  /**
   * if should page through results so it doesnt timeout.  Note, if pageSize is not set, will page 10k records
   * @param theAutopage
   * @return this for chaining
   */
  public GcGetMembers assignAutopage(Boolean theAutopage) {
    this.autopage = theAutopage;
    return this;
  }
  
  /**
   * The overlap of the autopage.  Note, should at least be 1, defaults to 5% of the pageSize.
   */
  private Integer autopageOverlap;
  
  /**
   * The overlap of the autopage.  Note, should at least be 1, defaults to 5% of the pageSize.
   * @param theAutopageOverlap
   * @return this for chaining
   */
  public GcGetMembers assignAutopageOverlap(Integer theAutopageOverlap) {
    this.autopageOverlap = theAutopageOverlap;
    return this;
  }
  
  /**
   * page size if paging
   */
  private Integer pageSize;
      
  /**
   * page number 1 indexed if paging
   */
  private Integer pageNumber;
  
  /**
   * sortString must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, name, description, sortString0, sortString1, sortString2, sortString3, sortString4
   */
  private String sortString;
  
  /**
   * ascending T or null for ascending, F for descending.  
   */
  private Boolean ascending;
  

  /**
   * page size if paging
   * @param pageSize1
   */
  public void assignPageSize(Integer pageSize1) {
    this.pageSize = pageSize1;
  }



  /**
   * page number 1 indexed if paging
   * @param pageNumber1
   */
  public void assignPageNumber(Integer pageNumber1) {
    this.pageNumber = pageNumber1;
  }


  /**
   * sortString must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, name, description, sortString0, sortString1, sortString2, sortString3, sortString4
   * @param sortString1
   */
  public void assignSortString(String sortString1) {
    this.sortString = sortString1;
  }

  /**
   * ascending T or null for ascending, F for descending.  
   * @param ascending1
   */
  public void assignAscending(Boolean ascending1) {
    this.ascending = ascending1;
  }


  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGetMembers assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** group names to query */
  private Set<String> groupNames = new LinkedHashSet<String>();
  
  /** group uuids to query */
  private Set<String> groupUuids = new LinkedHashSet<String>();
  
  /**
   * set the group name
   * @param theGroupName
   * @return this for chaining
   */
  public GcGetMembers addGroupName(String theGroupName) {
    this.groupNames.add(theGroupName);
    return this;
  }
  
  /** group id indexes to query */
  private Set<Long> groupIdIndexes = new LinkedHashSet<Long>();
  
  /**
   * set the group id index
   * @param theGroupIdIndex
   * @return this for chaining
   */
  public GcGetMembers addGroupIdIndex(Long theGroupIdIndex) {
    this.groupIdIndexes.add(theGroupIdIndex);
    return this;
  }
  
  /**
   * set the group uuid
   * @param theGroupUuid
   * @return this for chaining
   */
  public GcGetMembers addGroupUuid(String theGroupUuid) {
    this.groupUuids.add(theGroupUuid);
    return this;
  }
  
  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcGetMembers addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGetMembers addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** member filter */
  private WsMemberFilter memberFilter;
  
  /**
   * assign the member filter
   * @param theMemberFilter
   * @return this for chaining
   */
  public GcGetMembers assignMemberFilter(WsMemberFilter theMemberFilter) {
    this.memberFilter = theMemberFilter;
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcGetMembers assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.groupNames) == 0
        && GrouperClientUtils.length(this.groupUuids) == 0
        && GrouperClientUtils.length(this.groupIdIndexes) == 0) {
      throw new RuntimeException("Group name or uuid or id index is required: " + this);
    }
    
    if (this.pointInTimeFrom != null || this.pointInTimeTo != null) {
      if (this.includeGroupDetail != null && this.includeGroupDetail) {
        throw new RuntimeException("Cannot specify includeGroupDetail for point in time queries.");
      }
      
      if (this.memberFilter != null && !this.memberFilter.equals(WsMemberFilter.All)) {
        throw new RuntimeException("Cannot specify a member filter for point in time queries.");
      }
    }
    
    if (this.autopage != null && this.autopage) {
      // autopage
      if (this.pageNumber != null && this.pageNumber != 0) {
        throw new RuntimeException("Dont specify the pageNumber when autopaging");
      }
    }
    
    if (this.autopage != null && this.autopage) {
      if (GrouperClientUtils.length(this.groupNames) +
        GrouperClientUtils.length(this.groupUuids) + 
        GrouperClientUtils.length(this.groupIdIndexes) > 1) {
        throw new RuntimeException("If autopaging, just get members of one group!");
      }
    }
  }
  
  /** field name to add member */
  private String fieldName;
  
  /**
   * assign the field name to the request
   * @param theFieldName
   * @return this for chaining
   */
  public GcGetMembers assignFieldName(String theFieldName) {
    this.fieldName = theFieldName;
    return this;
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /** source ids to limit the results to, or null for all sources */
  private Set<String> sourceIds = null;
  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   */
  private Timestamp pointInTimeFrom;
  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.
   */
  private Timestamp pointInTimeTo;
  
  /**
   * add a source id to filter by (or none for all sources)
   * @param sourceId
   * @return this for chaining
   */
  public GcGetMembers addSourceId(String sourceId) {
    if (this.sourceIds == null) {
      this.sourceIds = new LinkedHashSet<String>();
    }
    this.sourceIds.add(sourceId);
    return this;
  }
  
  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcGetMembers addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcGetMembers assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcGetMembers assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   * @param pointInTimeFrom
   * @return this for chaining
   */
  public GcGetMembers assignPointInTimeFrom(Timestamp pointInTimeFrom) {
    this.pointInTimeFrom = pointInTimeFrom;
    return this;
  }
  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.
   * @param pointInTimeTo
   * @return this for chaining
   */
  public GcGetMembers assignPointInTimeTo(Timestamp pointInTimeTo) {
    this.pointInTimeTo = pointInTimeTo;
    return this;
  }
  
  /**
   * T for when pagination is of cursor type. F or null otherwise
   */
  private Boolean pageIsCursor;
  
  /**
   * T for when pagination is of cursor type. F or null otherwise
   * @param pageIsCursor
   * @return
   */
  public GcGetMembers assignPageIsCursor(Boolean pageIsCursor) {
    this.pageIsCursor = pageIsCursor;
    return this;
  }
  
  /**
   * value of last cursor field
   */
  private String pageLastCursorField;
  
  /**
   * value of last cursor field
   * @param pageLastCursorField
   * @return
   */
  public GcGetMembers assignPageLastCursorField(String pageLastCursorField) {
    this.pageLastCursorField = pageLastCursorField;
    return this;
  }
  
  /**
   * type of last cursor field (string, int, long, date, timestamp)
   */
  private String pageLastCursorFieldType;
  
  /**
   * type of last cursor field (string, int, long, date, timestamp)
   * @param pageLastCursorFieldType
   * @return
   */
  public GcGetMembers assignPageLastCursorFieldType(String pageLastCursorFieldType) {
    this.pageLastCursorFieldType = pageLastCursorFieldType;
    return this;
  }
  
  /**
   * should the last retrieved item be included again in the current result set
   */
  private Boolean pageCursorFieldIncludesLastRetrieved;
  
  /**
   * should the last retrieved item be included again in the current result set
   * @param pageCursorFieldIncludesLastRetrieved
   * @return
   */
  public GcGetMembers assignPageCursorFieldIncludesLastRetrieved(Boolean pageCursorFieldIncludesLastRetrieved) {
    this.pageCursorFieldIncludesLastRetrieved = pageCursorFieldIncludesLastRetrieved;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGetMembersResults execute() {
    this.validate();
    if (this.autopage == null || !this.autopage) {
      return executeHelper();
    }
    
    // autopage
    if (this.pageSize == null || this.pageSize <= 2) {
      this.pageSize = 10000;
    }
    if (this.sortString == null) {
      this.sortString = "m.id";
    }
    if (this.autopageOverlap == null || this.autopageOverlap < 1) {
      this.autopageOverlap = (int)(this.pageSize * 0.05);
      if (this.autopageOverlap < 1) {
        this.autopageOverlap = 1;
      }
    }
    
    long nowNanos = System.nanoTime();
    
    OUTER: for (int outerLoop = 0;outerLoop<5;outerLoop++) {

      WsGetMembersResults wsGetMembersResultsOuter = new WsGetMembersResults();
      WsResponseMeta wsResponseMetaOuter = new WsResponseMeta();
      wsGetMembersResultsOuter.setResponseMetadata(wsResponseMetaOuter);
      
      WsResultMeta wsResultMetaOuter = new WsResultMeta();
      wsGetMembersResultsOuter.setResultMetadata(wsResultMetaOuter);
      
      StringBuilder resultWarnings = new StringBuilder();
      StringBuilder resultMessage = new StringBuilder();

      WsGetMembersResult wsGetMembersResultOuter = null;
      List<WsSubject> wsSubjectListOuter = new ArrayList<WsSubject>();
      
      // sourceId, subjectId, identifierLookup
      Set<MultiKey> sourceIdSubjectIdIdentifier = new HashSet<MultiKey>();
      
      boolean firstRun = true;
      
      int ttl = 10000;
      
      int originalPageSize = this.pageSize;
      int lastIndexRetrieved = -1;
      
      try {
      
        while(true) {
  
          this.calculatePaging(originalPageSize, lastIndexRetrieved);
          
          if (LOG.isDebugEnabled()) {
            final int fromIndex = (this.pageNumber-1) * this.pageSize;
            final int toIndex = (this.pageNumber * this.pageSize) - 1;
            LOG.debug("Retrieving records: " 
                + fromIndex + " - " 
                + toIndex
                + ", pageSize: " + this.pageSize + ", pageNumber: " + this.pageNumber);
          }
          
          WsGetMembersResults wsGetMembersResultsInner = this.executeHelper();
          
          lastIndexRetrieved = (((this.pageNumber-1) * this.pageSize)-1) + GrouperClientUtils.length(wsGetMembersResultsInner.getResults()[0].getWsSubjects()) ;
              
          // see if we have overlap
          boolean hasOverlap = false;
          
          // copy results in
          for (WsGetMembersResult wsGetMembersResult : 
              GrouperClientUtils.nonNull(wsGetMembersResultsInner.getResults(), WsGetMembersResult.class)) {

            if (wsGetMembersResultOuter == null) {
              wsGetMembersResultOuter = wsGetMembersResult;
            }

            for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsGetMembersResult.getWsSubjects(), WsSubject.class)) {
  
              MultiKey multiKey = new MultiKey(
                  wsSubject.getSourceId(), wsSubject.getId(), wsSubject.getIdentifierLookup());
              
              // if we have overlap, skip it
              if (sourceIdSubjectIdIdentifier.contains(multiKey)) {
                hasOverlap = true;
                continue;
              }
              
              // no overlap
              sourceIdSubjectIdIdentifier.add(multiKey);
              
              // keep track of subjects for this group              
              wsSubjectListOuter.add(wsSubject);
            }
            
          }
  
          if (firstRun) {
            wsGetMembersResultsOuter.setSubjectAttributeNames(wsGetMembersResultsInner.getSubjectAttributeNames());
  
            wsResponseMetaOuter.setServerVersion(wsGetMembersResultsInner.getResponseMetadata().getServerVersion());
            
            wsResultMetaOuter.setParams(wsGetMembersResultsInner.getResultMetadata().getParams());
            wsResultMetaOuter.setResultCode(wsGetMembersResultsInner.getResultMetadata().getResultCode());
            wsResultMetaOuter.setResultCode2(wsGetMembersResultsInner.getResultMetadata().getResultCode2());
            wsResultMetaOuter.setSuccess(wsGetMembersResultsInner.getResultMetadata().getSuccess());
            
          } else {
            
            // if not first run and no overlap, thats not good
            if (!hasOverlap) {
              //hmmm, something is wrong, rest a little and let other changes shake out
              GrouperClientUtils.sleep(30000);
              continue OUTER;
            }
            
            //lets adjust paging
            
          }
         
  
          if (GrouperClientUtils.length(wsGetMembersResultsInner.getResponseMetadata().getResultWarnings()) > 0) {
            resultWarnings.append(wsGetMembersResultsInner.getResponseMetadata().getResultWarnings() + "\n");
          }
          
          if (GrouperClientUtils.length(wsGetMembersResultsInner.getResultMetadata().getResultMessage()) > 0) {
            resultMessage.append(wsGetMembersResultsInner.getResultMetadata().getResultMessage() + "\n");
          }
          
          // see if we are done
          if (GrouperClientUtils.length(wsGetMembersResultsInner.getResults()[0].getWsSubjects()) < this.pageSize) {
            
            // set the results at a group level
            wsGetMembersResultsOuter.setResults(new WsGetMembersResult[]{wsGetMembersResultOuter});
              
            if (GrouperClientUtils.length(wsSubjectListOuter) > 0) {
              wsGetMembersResultOuter.setWsSubjects(GrouperClientUtils.toArray(wsSubjectListOuter, WsSubject.class));
            }
            
            wsResponseMetaOuter.setResultWarnings(resultWarnings.toString());
            wsResponseMetaOuter.setMillis(""+((System.nanoTime() - nowNanos) / 10000));
            wsResultMetaOuter.setResultMessage(resultMessage.toString());
            
            return wsGetMembersResultsOuter;
          }
  
          firstRun = false;
          ttl--;
          if (ttl < 0) {
            throw new RuntimeException("TTL is less than 0, started at 100k...  endless loop?  page size too small?");
          }
        }
      } finally {
        this.pageSize = originalPageSize;
        
      }
    }
    throw new RuntimeException("Tried 5 times to get the paged result, but the data changed too frequently!  Error!");
  }
  
  /**
   * test the paging with overlap with this report
   */
  private static void printPagingReport() {
    GcGetMembers gcGetMembers = new GcGetMembers();
    gcGetMembers.pageSize = 1000;
    gcGetMembers.autopageOverlap = 50;
    int lastIndex = -1;
    while (true) {
      gcGetMembers.calculatePaging(1000, lastIndex);
      final int fromIndex = (gcGetMembers.pageNumber-1) * gcGetMembers.pageSize;
      final int toIndex = (gcGetMembers.pageNumber * gcGetMembers.pageSize) - 1;
      System.out.println("Retrieving records: " 
          + fromIndex + " - " 
          + toIndex
          + ", pageSize: " + gcGetMembers.pageSize + ", pageNumber: " + gcGetMembers.pageNumber);
      lastIndex = toIndex;
      if (toIndex > 70000) {
        break;
      }
    }
  }
  
  /**
   * 
   * @param originalPageSize
   * @param lastIndexRetrieved
   */
  private void calculatePaging(int originalPageSize, int lastIndexRetrieved) {
    
    int wasPageSize = this.pageSize == null ? -1 : this.pageSize;
    int wasPageNumber = this.pageNumber == null ? -1 : this.pageNumber;
    this.calculatePagingHelper(originalPageSize, lastIndexRetrieved);
    if (this.pageSize == wasPageSize && this.pageNumber == wasPageNumber) {
      throw new RuntimeException("Paging not working, stuck on page number: originalPageSize: " + originalPageSize
          + ", lastIndexRetrieved: " + lastIndexRetrieved + ", pageNumber: " + this.pageNumber + ", pageSize: " + this.pageSize);
    }
    
  }
  /**
   * 
   * @param originalPageSize
   * @param lastIndexRetrieved
   */
  private void calculatePagingHelper(int originalPageSize, int lastIndexRetrieved) {
    
    int pageSizeAdd = (int)Math.max((originalPageSize*0.1), 50);
    
    if (lastIndexRetrieved == -1) {
      this.pageNumber = 1;
      this.pageSize = (originalPageSize + (3 * pageSizeAdd));
      return;
    }
    int highBound = originalPageSize + (3 * pageSizeAdd);
    int lowBound = originalPageSize - (3 * pageSizeAdd);
    int[] pageSizesToRecords = new int[1+highBound-lowBound];
    int[] pageSizesToRemainders = new int[1+highBound-lowBound];
    
    // get the remainder and the records that will be retrieved 
    for (int i=0;i<pageSizesToRecords.length;i++) {
      int localPageSize = lowBound + i;
      pageSizesToRemainders[i] = lastIndexRetrieved % localPageSize;
      pageSizesToRecords[i] = localPageSize - pageSizesToRemainders[i];
    }
    
    // see if there is one that allows the page overlap in the remainder
    int bestIndex = -1;
    int bestRecords = -1;
    for (int i=0;i<pageSizesToRecords.length;i++) {
      if (pageSizesToRemainders[i] >= this.autopageOverlap) {
        if (pageSizesToRecords[i] > bestRecords) {
          bestIndex = i;
          bestRecords = pageSizesToRecords[i];
        }
      }
    }
    
    if (bestIndex > -1) {
      this.pageSize = bestIndex + lowBound;
      this.pageNumber = (lastIndexRetrieved / this.pageSize) + 1;
      return;
    }
    
    bestIndex = -1;
    int bestRemainder = -1;
    //just get the one with the highest overlap i guess
    for (int i=0;i<pageSizesToRecords.length;i++) {
      if (pageSizesToRemainders[i] >= bestRemainder) {
        bestIndex = i;
        bestRemainder = pageSizesToRemainders[i];
      }
    }
    
    if (bestIndex > -1) {
      this.pageSize = bestIndex + lowBound;
      this.pageNumber = (lastIndexRetrieved / this.pageSize) + 1;
      return;
    }

    throw new RuntimeException("Cant find a pageSize and pageNumber: originalPageSize: " 
        + originalPageSize + ", originalAutoPageOverlap: " +  this.autopageOverlap 
        + ", lastIndexRetrieved: " +  lastIndexRetrieved);
    
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  private WsGetMembersResults executeHelper() {
    WsGetMembersResults wsGetMembersResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestGetMembersRequest getMembers = new WsRestGetMembersRequest();

      getMembers.setActAsSubjectLookup(this.actAsSubject);

      getMembers.setFieldName(this.fieldName);
      
      getMembers.setMemberFilter(this.memberFilter == null ? null : this.memberFilter.name());

      List<WsGroupLookup> groupLookups = new ArrayList<WsGroupLookup>();
      //add names and/or uuids
      for (String groupName : this.groupNames) {
        groupLookups.add(new WsGroupLookup(groupName, null));
      }
      for (String groupUuid : this.groupUuids) {
        groupLookups.add(new WsGroupLookup(null, groupUuid));
      }
      for (Long groupIdIndex : this.groupIdIndexes) {
        groupLookups.add(new WsGroupLookup(null, null, groupIdIndex.toString()));
      }
      getMembers.setWsGroupLookups(GrouperClientUtils.toArray(groupLookups, WsGroupLookup.class));
      
      if (this.includeGroupDetail != null) {
        getMembers.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        getMembers.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      if (this.subjectAttributeNames.size() > 0) {
        getMembers.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      //add params if there are any
      if (this.params.size() > 0) {
        getMembers.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      if (GrouperClientUtils.length(this.sourceIds) > 0) {
        getMembers.setSourceIds(GrouperClientUtils.toArray(this.sourceIds, String.class));
      }
      
      if (this.ascending != null) {
        getMembers.setAscending(this.ascending ? "T" : "F");
      }
      
      getMembers.setSortString(this.sortString);
      
      if (this.pageNumber != null) {
        getMembers.setPageNumber(this.pageNumber.toString());
      }

      if (this.pageSize != null) {
        getMembers.setPageSize(this.pageSize.toString());
      }
      
      getMembers.setPointInTimeFrom(GrouperClientUtils.dateToString(this.pointInTimeFrom));
      getMembers.setPointInTimeTo(GrouperClientUtils.dateToString(this.pointInTimeTo));
      
      if (this.pageIsCursor != null) {
        getMembers.setPageIsCursor(this.pageIsCursor ? "T": "F");
      }
      
      if (this.pageCursorFieldIncludesLastRetrieved != null) {
        getMembers.setPageCursorFieldIncludesLastRetrieved(this.pageCursorFieldIncludesLastRetrieved ? "T": "F");
      }
      
      getMembers.setPageLastCursorField(this.pageLastCursorField);
      getMembers.setPageLastCursorFieldType(this.pageLastCursorFieldType);
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      grouperClientWs.assignWsUser(this.wsUser);
      grouperClientWs.assignWsPass(this.wsPass);
      grouperClientWs.assignWsEndpoint(this.wsEndpoint);
      
      //kick off the web service
      wsGetMembersResults = (WsGetMembersResults)
        grouperClientWs.executeService("groups", getMembers, "getMembers", this.clientVersion, true);
      
      String resultMessage = wsGetMembersResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsGetMembersResults, wsGetMembersResults.getResults(), resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGetMembersResults;
    
  }
}
