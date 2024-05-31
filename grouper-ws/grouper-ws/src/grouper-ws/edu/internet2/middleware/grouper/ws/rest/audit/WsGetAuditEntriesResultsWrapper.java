package edu.internet2.middleware.grouper.ws.rest.audit;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAuditEntriesResults;
import io.swagger.annotations.ApiModelProperty;

public class WsGetAuditEntriesResultsWrapper {
  WsGetAuditEntriesResults WsGetAuditEntriesResults = null;

  @ApiModelProperty(name = "WsGetAuditEntriesResults", value = "Identifies the response of a get audit entries request")
  public WsGetAuditEntriesResults getWsGetAuditEntriesResults() {
    return WsGetAuditEntriesResults;
  }

  
  public void setWsGetAuditEntriesResults(WsGetAuditEntriesResults wsGetAuditEntriesResults) {
    WsGetAuditEntriesResults = wsGetAuditEntriesResults;
  }
  

}
