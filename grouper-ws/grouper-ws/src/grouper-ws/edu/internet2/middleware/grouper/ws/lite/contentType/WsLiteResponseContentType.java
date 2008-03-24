/*
 * @author mchyzer $Id: WsLiteResponseContentType.java,v 1.1 2008-03-24 20:19:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite.contentType;

import java.io.Writer;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.CompactWriter;

import edu.internet2.middleware.grouper.ws.lite.GrouperLiteInvalidRequest;
import edu.internet2.middleware.grouper.ws.lite.WsLiteClassLookup;

/**
 * possible content types by grouper ws lite
 */
public enum WsLiteResponseContentType {

  /** default xhtml content type */
  xhtml {

    /**
     * write a string representation to an outputstream
     * @param object to write to output
     * @param writer to write to (e.g. back to http client)
     * @param warnings is where warnings should be written to
     */
    @Override
    public void writeString(Object object, Writer writer) {
      WsXhtmlOutputConverter wsXhtmlOutputConverter = new WsXhtmlOutputConverter(true,
          null);
      wsXhtmlOutputConverter.writeBean(object, writer);
    }
    /**
     * parse a string to an object
     * @param input
     * @return the object
     */
    @Override
    public Object parseString(String input) {
      return WsLiteRequestContentType.xhtml.parseString(input, new StringBuilder());
    }

  },
  /** xml content type */
  xml {

    /**
     * write a string representation to an outputstream
     * @param object to write to output
     * @param writer to write to (e.g. back to http client)
     * @param warnings is where warnings should be written to
     */
    @Override
    public void writeString(Object object, Writer writer) {
      XStream xstream = xstream(false);
      //dont indent
      xstream.marshal(object, new CompactWriter(writer));
    }

    /**
     * parse a string to an object
     * @param input
     * @return the object
     */
    @Override
    public Object parseString(String input) {
      return WsLiteRequestContentType.xml.parseString(input, new StringBuilder());
    }

  },
  /** json content type */
  json {

    /**
     * write a string representation to an outputstream
     * @param object to write to output
     * @param writer to write to (e.g. back to http client)
     * @param warnings is where warnings should be written to
     */
    @Override
    public void writeString(Object object, Writer writer) {
      XStream xstream = xstream(true);
      xstream.toXML(object, writer);
    }

    /**
     * parse a string to an object
     * @param input
     * @return the object
     */
    @Override
    public Object parseString(String input) {
      return WsLiteRequestContentType.json.parseString(input, new StringBuilder());
    }

  };

  /**
   * write a string representation to an outputstream
   * @param object to write to output
   * @param writer to write to (e.g. back to http client)
   * @param warnings is where warnings should be written to
   */
  public abstract void writeString(Object object, Writer writer);

  /**
   * parse a string to an object
   * @param input
   * @return the object
   */
  public abstract Object parseString(String input);

  /**
   * setup an xstream object for input/output
   * @param isJson driver for json 
   * @return the xstream object
   */
  public static XStream xstream(boolean isJson) {
    //note new JsonHierarchicalStreamDriver() doesnt work
    XStream xstream = isJson ? new XStream(new JettisonMappedXmlDriver()) : new XStream();
    //dont try to get fancy
    xstream.setMode(XStream.NO_REFERENCES);
    Map<String, Class<?>> aliasClassMap = WsLiteClassLookup.getAliasClassMap();
    for (String key : aliasClassMap.keySet()) {
      xstream.alias(key, aliasClassMap.get(key));
    }
    return xstream;
  }

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception on not found
   * @return the enum or null or exception if not found
   * @throws GrouperLiteInvalidRequest if problem
   */
  public static WsLiteResponseContentType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperLiteInvalidRequest {
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (WsLiteResponseContentType wsLiteResponseContentType : WsLiteResponseContentType
        .values()) {
      if (StringUtils.equalsIgnoreCase(string, wsLiteResponseContentType.name())) {
        return wsLiteResponseContentType;
      }
    }
    StringBuilder error = new StringBuilder(
        "Cant find wsLiteResponseContentType from string: '").append(string);
    error.append("', expecting one of: ");
    for (WsLiteResponseContentType wsLiteResponseContentType : WsLiteResponseContentType
        .values()) {
      error.append(wsLiteResponseContentType.name()).append(", ");
    }
    throw new GrouperLiteInvalidRequest(error.toString());
  }

}
