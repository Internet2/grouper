/*
 * @author mchyzer $Id: WsLiteResponseHandler.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * writes the body of a grouper lite response
 */
public interface WsLiteResponseHandler {

  /** write the response of the lite web service inside the top and bottom 
   * @param writer is the writer to continue the response
   * @throws XMLStreamException if there is a problem with the writer
   */
  public void writeResponse(XMLStreamWriter writer) throws XMLStreamException;
}
