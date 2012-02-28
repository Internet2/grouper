/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * request bean in body of rest request
 */
public class WsRestFindAttributeDefNamesRequest implements WsRequestBean {

  /**
   * search string with % as wildcards will search name, display name, description
   */
  private String scope;
  
  
  /**
   * search string with % as wildcards will search name, display name, description
   * @return the scope
   */
  public String getScope() {
    return this.scope;
  }

  
  /**
   * search string with % as wildcards will search name, display name, description
   * @param scope1 the scope to set
   */
  public void setScope(String scope1) {
    this.scope = scope1;
  }

  
  /**
   * splitScope T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   * @return the splitScope
   */
  public String getSplitScope() {
    return this.splitScope;
  }

  
  /**
   * splitScope T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   * @param splitScope1 the splitScope to set
   */
  public void setSplitScope(String splitScope1) {
    this.splitScope = splitScope1;
  }

  
  /**
   * find names associated with this attribute definition
   * @return the wsAttributeDefLookup
   */
  public WsAttributeDefLookup getWsAttributeDefLookup() {
    return this.wsAttributeDefLookup;
  }

  
  /**
   * find names associated with this attribute definition
   * @param wsAttributeDefLookup1 the wsAttributeDefLookup to set
   */
  public void setWsAttributeDefLookup(WsAttributeDefLookup wsAttributeDefLookup1) {
    this.wsAttributeDefLookup = wsAttributeDefLookup1;
  }

  
  /**
   * where can the attribute definition be assigned, e.g. any_mem, any_mem_asgn, attr_def, 
   * attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn
   * @return the attributeAssignType
   */
  public String getAttributeAssignType() {
    return this.attributeAssignType;
  }

  
  /**
   * where can the attribute definition be assigned, e.g. any_mem, any_mem_asgn, attr_def, 
   * attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn
   * @param attributeAssignType1 the attributeAssignType to set
   */
  public void setAttributeAssignType(String attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }

  
  /**
   * type of attribute definition, e.g. attr, domain, limit, perm, type
   * @return the attributeDefType
   */
  public String getAttributeDefType() {
    return this.attributeDefType;
  }

  
  /**
   * type of attribute definition, e.g. attr, domain, limit, perm, type
   * @param attributeDefType1 the attributeDefType to set
   */
  public void setAttributeDefType(String attributeDefType1) {
    this.attributeDefType = attributeDefType1;
  }

  
  /**
   * if you want to just pass in a list of uuids and/or names.
   * @return the wsAttributeDefNameLookups
   */
  public WsAttributeDefNameLookup[] getWsAttributeDefNameLookups() {
    return this.wsAttributeDefNameLookups;
  }

  
  /**
   * if you want to just pass in a list of uuids and/or names.
   * @param wsAttributeDefNameLookups1 the wsAttributeDefNameLookups to set
   */
  public void setWsAttributeDefNameLookups(
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups1) {
    this.wsAttributeDefNameLookups = wsAttributeDefNameLookups1;
  }

  
  /**
   * page size if paging on a sort filter or parent
   * @return the pageSize
   */
  public String getPageSize() {
    return this.pageSize;
  }

  
  /**
   * page size if paging on a sort filter or parent
   * @param pageSize1 the pageSize to set
   */
  public void setPageSize(String pageSize1) {
    this.pageSize = pageSize1;
  }

  
  /**
   * page number 1 indexed if paging on a sort filter or parent
   * @return the pageNumber
   */
  public String getPageNumber() {
    return this.pageNumber;
  }

  
  /**
   * page number 1 indexed if paging on a sort filter or parent
   * @param pageNumber1 the pageNumber to set
   */
  public void setPageNumber(String pageNumber1) {
    this.pageNumber = pageNumber1;
  }

  
  /**
   * must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   * @return the sortString
   */
  public String getSortString() {
    return this.sortString;
  }

  
  /**
   * must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   * @param sortString1 the sortString to set
   */
  public void setSortString(String sortString1) {
    this.sortString = sortString1;
  }

  
  /**
   * ascending or null for ascending, F for descending.  
   * @return the ascending
   */
  public String getAscending() {
    return this.ascending;
  }

  
  /**
   * ascending or null for ascending, F for descending.  
   * @param ascending1 the ascending to set
   */
  public void setAscending(String ascending1) {
    this.ascending = ascending1;
  }

  
  /**
   * wsInheritanceSetRelation if there is one wsAttributeDefNameLookup, and this is specified, then find 
   * the attribute def names which are related to the lookup by this relation, e.g. IMPLIED_BY_THIS, 
   * IMPLIED_BY_THIS_IMMEDIATE, THAT_IMPLY_THIS, THAT_IMPLY_THIS_IMMEDIATE
   * @return the wsInheritanceSetRelation
   */
  public String getWsInheritanceSetRelation() {
    return this.wsInheritanceSetRelation;
  }

  
  /**
   * wsInheritanceSetRelation if there is one wsAttributeDefNameLookup, and this is specified, then find 
   * the attribute def names which are related to the lookup by this relation, e.g. IMPLIED_BY_THIS, 
   * IMPLIED_BY_THIS_IMMEDIATE, THAT_IMPLY_THIS, THAT_IMPLY_THIS_IMMEDIATE
   * @param wsInheritanceSetRelation1 the wsInheritanceSetRelation to set
   */
  public void setWsInheritanceSetRelation(String wsInheritanceSetRelation1) {
    this.wsInheritanceSetRelation = wsInheritanceSetRelation1;
  }

  /**
   * splitScope T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   */
  private String splitScope;
  
  /**
   * find names associated with this attribute definition
   */
  private WsAttributeDefLookup wsAttributeDefLookup;
  
  /**
   * where can the attribute definition be assigned, e.g. any_mem, any_mem_asgn, attr_def, 
   * attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn
   */
  private String attributeAssignType;
  
  /**
   * type of attribute definition, e.g. attr, domain, limit, perm, type
   */
  private String attributeDefType;
  
  /**
   * if you want to just pass in a list of uuids and/or names.
   */
  private WsAttributeDefNameLookup[] wsAttributeDefNameLookups;
  
  /**
   * page size if paging on a sort filter or parent
   */
  private String pageSize;
  
  /**
   * page number 1 indexed if paging on a sort filter or parent
   */
  private String pageNumber;
  
  /**
   * must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   */
  private String sortString;
  
  /**
   * ascending or null for ascending, F for descending.  
   */
  private String ascending;
  
  /**
   * wsInheritanceSetRelation if there is one wsAttributeDefNameLookup, and this is specified, then find 
   * the attribute def names which are related to the lookup by this relation, e.g. IMPLIED_BY_THIS, 
   * IMPLIED_BY_THIS_IMMEDIATE, THAT_IMPLY_THIS, THAT_IMPLY_THIS_IMMEDIATE
   */
  private String wsInheritanceSetRelation;
  
  /** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return version
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /** if acting as someone else */
  private WsSubjectLookup actAsSubjectLookup;
  
  /**
   * if acting as someone else
   * @return act as subject
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  /**
   * if acting as someone else
   * @param actAsSubjectLookup1
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  /** optional: reserved for future use */
  private  WsParam[] params;

  
  
  /**
   * optional: reserved for future use
   * @return params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * optional: reserved for future use
   * @param params1
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  


}
