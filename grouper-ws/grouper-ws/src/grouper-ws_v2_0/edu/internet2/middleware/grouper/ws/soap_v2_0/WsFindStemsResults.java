package edu.internet2.middleware.grouper.ws.soap_v2_0;


/**
 * returned from the stem find query, if none found, return none
 * 
 * @author mchyzer
 * 
 */
public class WsFindStemsResults {

  /**
   * has 0 to many stems that match the query by example
   */
  private WsStem[] stemResults;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * has 0 to many stems that match the query by example
   * 
   * @return the stemResults
   */
  public WsStem[] getStemResults() {
    return this.stemResults;
  }

  /**
   * has 0 to many stems that match the query by example
   * 
   * @param stemResults1
   *            the stemResults to set
   */
  public void setStemResults(WsStem[] stemResults1) {
    this.stemResults = stemResults1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
