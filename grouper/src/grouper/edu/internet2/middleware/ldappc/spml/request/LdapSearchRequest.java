package edu.internet2.middleware.ldappc.spml.request;

import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.Scope;
import org.openspml.v2.msg.spmlsearch.SearchRequest;

public class LdapSearchRequest {

  private String base;

  private String filter;

  private ReturnData returnData;

  private Scope scope;

  private String targetId;

  public SearchRequest getSearchRequest() {

    Query query = new Query();
    query.setTargetID(targetId);
    query.setScope(scope);

    PSOIdentifier basePsoId = new PSOIdentifier();
    basePsoId.setID(base);
    query.setBasePsoID(basePsoId);

    LdapFilterQueryClause filterQC = new LdapFilterQueryClause();
    filterQC.setFilter(filter);
    query.addQueryClause(filterQC);

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setReturnData(returnData);
    searchRequest.setQuery(query);

    return searchRequest;
  }

  public String getBase() {
    return base;
  }

  public void setBase(String base) {
    this.base = base;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public ReturnData getReturnData() {
    return returnData;
  }

  public void setReturnData(ReturnData returnData) {
    this.returnData = returnData;
  }

  public Scope getScope() {
    return scope;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  public String getTargetId() {
    return targetId;
  }

  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }
}
