/*
 * @author mchyzer
 * $Id: ResultMetadataHolder.java,v 1.1 2008-12-08 02:55:48 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import edu.internet2.middleware.grouper.ws.soap_v2_0.WsResultMeta;


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
