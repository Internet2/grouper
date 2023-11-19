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
 * $Id: GcFindGroups.java,v 1.5 2009-12-19 21:38:27 mchyzer Exp $
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
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestFindAttributeDefNamesRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.morphString.Crypto;


/**
 * class to run find attributeDefNames
 */
public class GcFindAttributeDefNames {


  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   */
  private String wsEndpoint;

  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   * @param theWsEndpoint
   * @return this for chaining
   */
  public GcFindAttributeDefNames assignWsEndpoint(String theWsEndpoint) {
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
  public GcFindAttributeDefNames assignWsUser(String theWsUser) {
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
  public GcFindAttributeDefNames assignWsPass(String theWsPass) {
    this.wsPass = theWsPass;
    return this;
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcFindAttributeDefNames assignWsPassEncrypted(String theWsPassEncrypted) {
    String encryptKey = GrouperClientUtils.encryptKey();
    return this.assignWsPass(new Crypto(encryptKey).decrypt(theWsPassEncrypted));
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcFindAttributeDefNames assignWsPassFile(File theFile) {
    return this.assignWsPass(GrouperClientUtils.readFileIntoString(theFile));
  }

  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcFindAttributeDefNames assignWsPassFileEncrypted(File theFile) {
    return this.assignWsPassEncrypted(GrouperClientUtils.readFileIntoString(theFile));
  }

  /**
   * serviceRole to filter attributes that a user has a certain role
   */
  private String serviceRole;
  
  /**
   * serviceRole to filter attributes that a user has a certain role
   * @param theServiceRole
   * @return this for chaining
   */
  public GcFindAttributeDefNames assignServiceRole(String theServiceRole) {
    this.serviceRole = theServiceRole;
    return this;
  }
  
  /**
   * subject if looking for privileges or service role
   */
  private WsSubjectLookup subjectLookup;

  /**
   * subject lookup if looking for privileges or service role
   * @param theSubjectLookup
   * @return this for chaining
   */
  public GcFindAttributeDefNames assignSubjectLookup(WsSubjectLookup theSubjectLookup) {
    this.subjectLookup = theSubjectLookup;
    return this;
  }
  
  /**
   * if there is one wsAttributeDefNameLookup, and this is specified, then find 
   * the attribute def names which are related to the lookup by this relation, e.g. IMPLIED_BY_THIS, 
   * IMPLIED_BY_THIS_IMMEDIATE, THAT_IMPLY_THIS, THAT_IMPLY_THIS_IMMEDIATE
   */
  private String inheritanceSetRelation;

  /**
   * if there is one wsAttributeDefNameLookup, and this is specified, then find 
   * the attribute def names which are related to the lookup by this relation, e.g. IMPLIED_BY_THIS, 
   * IMPLIED_BY_THIS_IMMEDIATE, THAT_IMPLY_THIS, THAT_IMPLY_THIS_IMMEDIATE
   * @param theInheritanceRelation
   * @return inheritance relation
   */
  public GcFindAttributeDefNames assignInheritanceSetRelation(String theInheritanceRelation) {
    this.inheritanceSetRelation = theInheritanceRelation;
    return this;
  }

  /**
   * type of attribute definition, e.g. attr, domain, limit, perm, type
   */
  private String attributeDefType;
  
  /**
   * type of attribute definition, e.g. attr, domain, limit, perm, type
   * @param theAttributeDefType
   * @return this for chaining
   */
  public GcFindAttributeDefNames assignAttributeDefType(String theAttributeDefType) {
    this.attributeDefType = theAttributeDefType;
    return this;
  }
  
  /**
   * where can the attribute definition be assigned, e.g. any_mem, any_mem_asgn, attr_def, 
   * attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn
   */
  private String attributeAssignType;
  
  /**
   * where can the attribute definition be assigned, e.g. any_mem, any_mem_asgn, attr_def, 
   * attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn
   * @param theAttributeAssignType
   * @return this for chaining
   */
  public GcFindAttributeDefNames assignAttributeAssignType(String theAttributeAssignType) {
    this.attributeAssignType = theAttributeAssignType;
    return this;
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
  public GcFindAttributeDefNames assignScope(String theScope) {
    this.scope = theScope;
    return this;
  }
  
  /**
   * T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   */
  private Boolean splitScope;
                       
  /**
   * T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   * @param theSplitScope
   * @return the split scope
   */
  public GcFindAttributeDefNames assignSplitScope(Boolean theSplitScope) {
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
  public GcFindAttributeDefNames assignClientVersion(String theClientVersion) {
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
  public GcFindAttributeDefNames addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcFindAttributeDefNames addParam(WsParam wsParam) {
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
  public GcFindAttributeDefNames assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.isBlank(this.scope) && GrouperClientUtils.length(this.attributeDefNameUuids) == 0 
        && GrouperClientUtils.length(this.attributeDefNameNames) == 0 && GrouperClientUtils.length(this.attributeDefNameIdIndexes) == 0) {
      throw new RuntimeException("Need to pass in a scope, or attributeDefNameNames or attributeDefNameUuids or attributeDefNameIdIndexes: " + this);
    }
  }
  
  /** attributeDefName names to query */
  private Set<String> attributeDefNameNames = new LinkedHashSet<String>();

  /** attributeDefName names to query */
  private Set<String> attributeDefNameUuids = new LinkedHashSet<String>();

  /** attributeDefName id indexes to query */
  private Set<Long> attributeDefNameIdIndexes = new LinkedHashSet<Long>();

  /** attributeDef names to query */
  private String nameOfAttributeDef;

  /** attributeDef names to query */
  private String uuidOfAttributeDef;

  /** attributeDef id indexes to query */
  private Long idIndexOfAttributeDef;

  /** true or null for ascending, false for descending.  If you pass true or false, must pass a sort string */
  private Boolean ascending;

  /**
   * assign if ascending if sorting
   * @param isAscending
   * @return this for paging
   */
  public GcFindAttributeDefNames assignAscending(Boolean isAscending) {
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
  public GcFindAttributeDefNames assignPageNumber(Integer thePageNumber) {
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
  public GcFindAttributeDefNames assignPageSize(Integer thePageSize) {
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
  public GcFindAttributeDefNames assignSortString(String theSortString) {
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
  public GcFindAttributeDefNames assignPageIsCursor(Boolean pageIsCursor) {
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
  public GcFindAttributeDefNames assignPageLastCursorField(String pageLastCursorField) {
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
  public GcFindAttributeDefNames assignPageLastCursorFieldType(String pageLastCursorFieldType) {
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
  public GcFindAttributeDefNames assignPageCursorFieldIncludesLastRetrieved(Boolean pageCursorFieldIncludesLastRetrieved) {
    this.pageCursorFieldIncludesLastRetrieved = pageCursorFieldIncludesLastRetrieved;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsFindAttributeDefNamesResults execute() {
    this.validate();

    WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestFindAttributeDefNamesRequest findAttributeDefNames = new WsRestFindAttributeDefNamesRequest();

      findAttributeDefNames.setActAsSubjectLookup(this.actAsSubject);

      //add params if there are any
      if (this.params.size() > 0) {
        findAttributeDefNames.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      List<WsAttributeDefNameLookup> attributeDefNameLookups = new ArrayList<WsAttributeDefNameLookup>();
      //add names and/or uuids
      for (String attributeDefNameName : this.attributeDefNameNames) {
        attributeDefNameLookups.add(new WsAttributeDefNameLookup(attributeDefNameName, null));
      }
      for (String attributeDefNameUuid : this.attributeDefNameUuids) {
        attributeDefNameLookups.add(new WsAttributeDefNameLookup(null, attributeDefNameUuid));
      }
      for (Long attributeDefNameIdIndex : this.attributeDefNameIdIndexes) {
        attributeDefNameLookups.add(new WsAttributeDefNameLookup(null, null, attributeDefNameIdIndex.toString()));
      }
      findAttributeDefNames.setWsAttributeDefNameLookups(GrouperClientUtils.toArray(attributeDefNameLookups, WsAttributeDefNameLookup.class));

      if (!GrouperClientUtils.isBlank(this.nameOfAttributeDef) || !GrouperClientUtils.isBlank(this.uuidOfAttributeDef)
          || !GrouperClientUtils.isBlank(this.idIndexOfAttributeDef)) {
        WsAttributeDefLookup attributeDefLookup = null;
        attributeDefLookup = new WsAttributeDefLookup(this.nameOfAttributeDef, this.uuidOfAttributeDef, 
            this.idIndexOfAttributeDef == null ? null : this.idIndexOfAttributeDef.toString());
        findAttributeDefNames.setWsAttributeDefLookup(attributeDefLookup);
      }

      if (!GrouperClientUtils.isBlank(this.attributeAssignType)) {
        findAttributeDefNames.setAttributeAssignType(this.attributeAssignType);
      }
      
      if (!GrouperClientUtils.isBlank(this.attributeDefType)) {
        findAttributeDefNames.setAttributeDefType(this.attributeDefType);
      }
      
      if (!GrouperClientUtils.isBlank(this.inheritanceSetRelation)) {
        findAttributeDefNames.setWsInheritanceSetRelation(this.inheritanceSetRelation);
      }
      
      if (!GrouperClientUtils.isBlank(this.sortString)) {
        findAttributeDefNames.setSortString(this.sortString);
      }
      
      if (this.ascending != null) {
        findAttributeDefNames.setAscending(this.ascending ? "T" : "F");
      }
      
      if (this.pageNumber != null) {
        findAttributeDefNames.setPageNumber(Integer.toString(this.pageNumber));
      }

      if (this.pageSize != null) {
        findAttributeDefNames.setPageSize(Integer.toString(this.pageSize));
      }

      if (!GrouperClientUtils.isBlank(this.scope)) {
        findAttributeDefNames.setScope(this.scope);
      }
      if (this.splitScope != null) {
        findAttributeDefNames.setSplitScope(this.splitScope ? "T" : "F");
      }
      
      if (this.serviceRole != null) {
        findAttributeDefNames.setServiceRole(this.serviceRole);
      }
      
      if (this.subjectLookup != null) {
        findAttributeDefNames.setSubjectLookup(this.subjectLookup);
      }
      
      if (this.pageIsCursor != null) {
        findAttributeDefNames.setPageIsCursor(this.pageIsCursor ? "T": "F");
      }
      
      if (this.pageCursorFieldIncludesLastRetrieved != null) {
        findAttributeDefNames.setPageCursorFieldIncludesLastRetrieved(this.pageCursorFieldIncludesLastRetrieved ? "T": "F");
      }
      
      findAttributeDefNames.setPageLastCursorField(this.pageLastCursorField);
      findAttributeDefNames.setPageLastCursorFieldType(this.pageLastCursorFieldType);
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      grouperClientWs.assignWsUser(this.wsUser);
      grouperClientWs.assignWsPass(this.wsPass);
      grouperClientWs.assignWsEndpoint(this.wsEndpoint);
      
      //kick off the web service
      wsFindAttributeDefNamesResults = (WsFindAttributeDefNamesResults)
        grouperClientWs.executeService("attributeDefNames", findAttributeDefNames, "findAttributeDefNames", this.clientVersion, true);
      
      String resultMessage = wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsFindAttributeDefNamesResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsFindAttributeDefNamesResults;
    
  }

  /**
   * set the AttributeDefName name
   * @param theAttributeDefNameName
   * @return this for chaining
   */
  public GcFindAttributeDefNames addAttributeDefNameName(String theAttributeDefNameName) {
    this.attributeDefNameNames.add(theAttributeDefNameName);
    return this;
  }

  /**
   * set the AttributeDefName uuid
   * @param theAttributeDefNameUuid
   * @return this for chaining
   */
  public GcFindAttributeDefNames addAttributeDefNameUuid(String theAttributeDefNameUuid) {
    this.attributeDefNameUuids.add(theAttributeDefNameUuid);
    return this;
  }

  /**
   * set the AttributeDefName id index
   * @param theAttributeDefNameIdIndex
   * @return this for chaining
   */
  public GcFindAttributeDefNames addAttributeDefNameIdIndex(Long theAttributeDefNameIdIndex) {
    this.attributeDefNameIdIndexes.add(theAttributeDefNameIdIndex);
    return this;
  }

  /**
   * set the AttributeDef name
   * @param theNameOfAttributeDef
   * @return this for chaining
   */
  public GcFindAttributeDefNames assignNameOfAttributeDef(String theNameOfAttributeDef) {
    this.nameOfAttributeDef = theNameOfAttributeDef;
    return this;
  }

  /**
   * set the AttributeDef uuid
   * @param theUuidOfAttributeDef
   * @return this for chaining
   */
  public GcFindAttributeDefNames assignUuidOfAttributeDef(String theUuidOfAttributeDef) {
    this.uuidOfAttributeDef = theUuidOfAttributeDef;
    return this;
  }


  /**
   * set the AttributeDef id index
   * @param theIdIndexOfAttributeDef
   * @return this for chaining
   */
  public GcFindAttributeDefNames assignIdIndexOfAttributeDef(Long theIdIndexOfAttributeDef) {
    this.idIndexOfAttributeDef = theIdIndexOfAttributeDef;
    return this;
  }

}
