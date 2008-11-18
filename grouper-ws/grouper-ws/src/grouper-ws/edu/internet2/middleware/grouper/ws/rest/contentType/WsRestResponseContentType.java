/*
 * @author mchyzer $Id: WsRestResponseContentType.java,v 1.5 2008-11-18 10:05:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;

import java.io.Writer;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.CompactWriter;

import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.WsRestClassLookup;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * possible content types by grouper ws rest
 */
public enum WsRestResponseContentType {

  /** default xhtml content type */
  xhtml {

    /**
     * get the content type
     * @return the http content type
     */
    @Override
    public String getContentType() {
      return WsRestRequestContentType.xhtml.getContentType();
    }

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
      return WsRestRequestContentType.xhtml.parseString(input, new StringBuilder());
    }

  },
  /** xml content type */
  xml {

    /**
     * get the content type
     * @return the http content type
     */
    @Override
    public String getContentType() {
      return WsRestRequestContentType.xml.getContentType();
    }

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
      return WsRestRequestContentType.xml.parseString(input, new StringBuilder());
    }

  },
  /** json content type */
  json {

    /**
     * get the content type
     * @return the http content type
     */
    @Override
    public String getContentType() {
      return WsRestRequestContentType.json.getContentType();
    }

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
      return WsRestRequestContentType.json.parseString(input, new StringBuilder());
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
   * get the content type
   * @return the http content type
   */
  public abstract String getContentType();

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
    xstream.autodetectAnnotations(true);
    Map<String, Class<?>> aliasClassMap = WsRestClassLookup.getAliasClassMap();
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
   * @throws GrouperRestInvalidRequest if problem
   */
  public static WsRestResponseContentType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    return GrouperServiceUtils.enumValueOfIgnoreCase(WsRestResponseContentType.class, 
        string, exceptionOnNotFound);
  }

}
