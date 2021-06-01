/**
 * Copyright 2016 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
/*
 * @author vsachdeva
 */
package edu.internet2.middleware.grouperClient.api;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestFindAttributeDefsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.morphString.Crypto;

/**
 * class to run find attributeDefs
 */
public class GcFindAttributeDefs {


  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   */
  private String wsEndpoint;

  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   * @param theWsEndpoint
   * @return this for chaining
   */
  public GcFindAttributeDefs assignWsEndpoint(String theWsEndpoint) {
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
  public GcFindAttributeDefs assignWsUser(String theWsUser) {
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
  public GcFindAttributeDefs assignWsPass(String theWsPass) {
    this.wsPass = theWsPass;
    return this;
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcFindAttributeDefs assignWsPassEncrypted(String theWsPassEncrypted) {
    String encryptKey = GrouperClientUtils.encryptKey();
    return this.assignWsPass(new Crypto(encryptKey).decrypt(theWsPassEncrypted));
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcFindAttributeDefs assignWsPassFile(File theFile) {
    return this.assignWsPass(GrouperClientUtils.readFileIntoString(theFile));
  }

  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcFindAttributeDefs assignWsPassFileEncrypted(File theFile) {
    return this.assignWsPassEncrypted(GrouperClientUtils.readFileIntoString(theFile));
  }

  /**
   * search string with % as wildcards will search name, display name, description
   */
  private String scope;

  /**
   * search string with % as wildcards will search name, display name, description
   * @param theScope
   * @return scope
   */
  public GcFindAttributeDefs assignScope(String theScope) {
    this.scope = theScope;
    return this;
  }

  /**
   * T or F, if T will split the scope by whitespace, and find attribute defs with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   */
  private Boolean splitScope;

  /**
   * T or F, if T will split the scope by whitespace, and find attribute defs with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   * @param theSplitScope
   * @return the split scope
   */
  public GcFindAttributeDefs assignSplitScope(Boolean theSplitScope) {
    this.splitScope = theSplitScope;
    return this;
  }

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcFindAttributeDefs assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
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
  public GcFindAttributeDefs addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }

  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcFindAttributeDefs addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }

  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcFindAttributeDefs assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }

  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.isBlank(this.scope)
        && GrouperClientUtils.length(this.attributeDefUuids) == 0
        && GrouperClientUtils.length(this.attributeDefNames) == 0
        && GrouperClientUtils.length(this.attributeDefIdIndexes) == 0) {
      throw new RuntimeException(
          "Need to pass in a scope, or attributeDefNames or attributeDefUuids or attributeDefIdIndexes: "
              + this);
    }
  }

  /** attributeDefName names to query */
  private Set<String> attributeDefNames = new LinkedHashSet<String>();

  /** attributeDefName names to query */
  private Set<String> attributeDefUuids = new LinkedHashSet<String>();

  /** attributeDefName id indexes to query */
  private Set<Long> attributeDefIdIndexes = new LinkedHashSet<Long>();

  /** true or null for ascending, false for descending.  If you pass true or false, must pass a sort string */
  private Boolean ascending;

  /** Privilege to be checked for the logged in user or actAsSubject **/
  private String privilegeName;

  /**
   * assign privilege name
   * @param privilegeName
   * @return this 
   */
  public GcFindAttributeDefs assignPrivilege(String thePrivilegeName) {
    this.privilegeName = thePrivilegeName;
    return this;
  }

  /**
   * assign if ascending if sorting
   * @param isAscending
   * @return this for paging
   */
  public GcFindAttributeDefs assignAscending(Boolean isAscending) {
    this.ascending = isAscending;
    return this;
  }

  /** page number 1 indexed if paging */
  private Integer pageNumber;

  /**
   * assign page number if paging
   * @param thePageNumber
   * @return this for chaining
   */
  public GcFindAttributeDefs assignPageNumber(Integer thePageNumber) {
    this.pageNumber = thePageNumber;
    return this;
  }

  /** page size if paging */
  private Integer pageSize;

  /**
   * assign page size if paging
   * @param thePageSize
   * @return this for chaining
   */
  public GcFindAttributeDefs assignPageSize(Integer thePageSize) {
    this.pageSize = thePageSize;
    return this;
  }

  /** must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;

  /**
   * assign sort string if sorting, e.g. can sort on name, displayName, extension, displayExtension
   * @param theSortString
   * @return the sort string
   */
  public GcFindAttributeDefs assignSortString(String theSortString) {
    this.sortString = theSortString;
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
  public GcFindAttributeDefs assignPageIsCursor(Boolean pageIsCursor) {
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
  public GcFindAttributeDefs assignPageLastCursorField(String pageLastCursorField) {
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
  public GcFindAttributeDefs assignPageLastCursorFieldType(String pageLastCursorFieldType) {
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
  public GcFindAttributeDefs assignPageCursorFieldIncludesLastRetrieved(Boolean pageCursorFieldIncludesLastRetrieved) {
    this.pageCursorFieldIncludesLastRetrieved = pageCursorFieldIncludesLastRetrieved;
    return this;
  }

  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsFindAttributeDefsResults execute() {
    this.validate();

    WsFindAttributeDefsResults wsFindAttributeDefsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestFindAttributeDefsRequest findAttributeDefs = new WsRestFindAttributeDefsRequest();

      findAttributeDefs.setActAsSubjectLookup(this.actAsSubject);

      //add params if there are any
      if (this.params.size() > 0) {
        findAttributeDefs
            .setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }

      List<WsAttributeDefLookup> attributeDefLookups = new ArrayList<WsAttributeDefLookup>();
      //add names and/or uuids
      for (String attributeDefeName : this.attributeDefNames) {
        attributeDefLookups.add(new WsAttributeDefLookup(attributeDefeName, null));
      }
      for (String attributeDefUuid : this.attributeDefUuids) {
        attributeDefLookups.add(new WsAttributeDefLookup(null, attributeDefUuid));
      }
      for (Long attributeDefIdIndex : this.attributeDefIdIndexes) {
        attributeDefLookups
            .add(new WsAttributeDefLookup(null, null, attributeDefIdIndex.toString()));
      }
      findAttributeDefs.setWsAttributeDefLookups(
          GrouperClientUtils.toArray(attributeDefLookups, WsAttributeDefLookup.class));

      if (!GrouperClientUtils.isBlank(this.sortString)) {
        findAttributeDefs.setSortString(this.sortString);
      }

      if (this.ascending != null) {
        findAttributeDefs.setAscending(this.ascending ? "T" : "F");
      }

      if (this.pageNumber != null) {
        findAttributeDefs.setPageNumber(Integer.toString(this.pageNumber));
      }

      if (this.pageSize != null) {
        findAttributeDefs.setPageSize(Integer.toString(this.pageSize));
      }

      if (!GrouperClientUtils.isBlank(this.scope)) {
        findAttributeDefs.setScope(this.scope);
      }
      if (this.splitScope != null) {
        findAttributeDefs.setSplitScope(this.splitScope ? "T" : "F");
      }

      if (this.privilegeName != null) {
        findAttributeDefs.setPrivilegeName(privilegeName);
      }
      
      if (this.pageIsCursor != null) {
        findAttributeDefs.setPageIsCursor(this.pageIsCursor ? "T": "F");
      }
      
      if (this.pageCursorFieldIncludesLastRetrieved != null) {
        findAttributeDefs.setPageCursorFieldIncludesLastRetrieved(this.pageCursorFieldIncludesLastRetrieved ? "T": "F");
      }
      
      findAttributeDefs.setPageLastCursorField(this.pageLastCursorField);
      findAttributeDefs.setPageLastCursorFieldType(this.pageLastCursorFieldType);

      GrouperClientWs grouperClientWs = new GrouperClientWs();

      grouperClientWs.assignWsUser(this.wsUser);
      grouperClientWs.assignWsPass(this.wsPass);
      grouperClientWs.assignWsEndpoint(this.wsEndpoint);
      
      //kick off the web service
      wsFindAttributeDefsResults = (WsFindAttributeDefsResults) grouperClientWs
          .executeService("attributeDefs", findAttributeDefs, "findAttributeDefs",
              this.clientVersion, true);

      String resultMessage = wsFindAttributeDefsResults.getResultMetadata()
          .getResultMessage();
      grouperClientWs.handleFailure(wsFindAttributeDefsResults, null, resultMessage);

    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsFindAttributeDefsResults;

  }

  /**
   * set the AttributeDef name
   * @param theAttributeDefName
   * @return this for chaining
   */
  public GcFindAttributeDefs addAttributeDefName(String theAttributeDefName) {
    this.attributeDefNames.add(theAttributeDefName);
    return this;
  }

  /**
   * set the AttributeDef uuid
   * @param theAttributeDefUuid
   * @return this for chaining
   */
  public GcFindAttributeDefs addAttributeDefUuid(String theAttributeDefUuid) {
    this.attributeDefUuids.add(theAttributeDefUuid);
    return this;
  }

  /**
   * set the AttributeDef id index
   * @param theAttributeDefIdIndex
   * @return this for chaining
   */
  public GcFindAttributeDefs addAttributeDefIdIndex(Long theAttributeDefIdIndex) {
    this.attributeDefIdIndexes.add(theAttributeDefIdIndex);
    return this;
  }

}
