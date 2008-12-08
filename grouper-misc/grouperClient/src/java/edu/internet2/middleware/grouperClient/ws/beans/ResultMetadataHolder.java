/*
 * @author mchyzer
 * $Id: ResultMetadataHolder.java,v 1.1 2008-12-08 02:55:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws.beans;



/**
 * has result metadata
 */
public interface ResultMetadataHolder {
  
  /**
   * get the result metadata
   * @return the result metadata
   */
  public WsResultMeta getResultMetadata();
}
