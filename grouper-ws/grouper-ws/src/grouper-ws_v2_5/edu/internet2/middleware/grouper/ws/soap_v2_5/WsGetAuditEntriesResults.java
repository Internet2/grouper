package edu.internet2.middleware.grouper.ws.soap_v2_5;

/**
 * 
 * @author vsachdeva
 *
 */
public class WsGetAuditEntriesResults {
  
  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();
  
  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();
  
  /**
   * 
   */
  private WsAuditEntry[] wsAuditEntries;
  
  
  /**
   * @return metadata about the result
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * metadata about the result
   * @param resultMetadata1
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * @return metadata about the result
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * 
   * @param responseMetadata1
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  /**
   * get audit entries
   * @return audit entries
   */
  public WsAuditEntry[] getWsAuditEntries() {
    return this.wsAuditEntries;
  }

  /**
   * set audit entries
   * @param wsAuditEntries1
   */
  public void setWsAuditEntries(WsAuditEntry[] wsAuditEntries1) {
    this.wsAuditEntries = wsAuditEntries1;
  }

}
