/*
 * @author mchyzer $Id: WsRestResponseHandler.java,v 1.1 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * writes the body of a grouper rest response
 */
public interface WsRestResponseHandler {

  /** write the response of the rest web service inside the top and bottom 
   * @param writer is the writer to continue the response
   * @throws XMLStreamException if there is a problem with the writer
   */
  public void writeResponse(XMLStreamWriter writer) throws XMLStreamException;
}
