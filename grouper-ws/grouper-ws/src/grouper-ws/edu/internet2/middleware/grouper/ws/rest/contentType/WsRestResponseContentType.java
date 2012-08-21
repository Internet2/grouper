/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer $Id: WsRestResponseContentType.java,v 1.8 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;

import java.io.Writer;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubject;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.WsRestClassLookup;
import edu.internet2.middleware.grouper.ws.rest.json.JsonConverter;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;

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
      JsonConverter jsonConverter = WsRestRequestContentType.jsonConverter();
      try {
        jsonConverter.convertToJson(object, writer);
      } catch (RuntimeException re) {
        LOG.error("Error converting json object with converter: " 
            + GrouperUtil.className(jsonConverter) + ", " + GrouperUtil.className(object));
        throw new RuntimeException("Error converting json object with converter: " + GrouperUtil.className(jsonConverter)
            + ", " + GrouperUtil.className(object), re);
      }
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

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsRestResponseContentType.class);

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
    XStream xstream = null;
    
    boolean ignoreExtraneousFields = GrouperWsConfig.retrieveConfig().propertyValueBoolean("ws.ignoreExtraneousXmlFieldsRest", false);
    
    if (ignoreExtraneousFields) {
      xstream = new XStream(isJson ? new JettisonMappedXmlDriver() : new XppDriver()) {

        /**
         * 
         * @see edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStream#wrapMapper(edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.MapperWrapper)
         */
        @Override
        protected MapperWrapper wrapMapper(MapperWrapper next) {
          return new MapperWrapper(next) {
  
            /**
             * 
             * @see edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.MapperWrapper#shouldSerializeMember(java.lang.Class, java.lang.String)
             */
            @SuppressWarnings("unchecked")
            @Override
            public boolean shouldSerializeMember(Class definedIn, String fieldName) {
              boolean definedInNotObject = definedIn != Object.class;
              if (definedInNotObject) {
                return super.shouldSerializeMember(definedIn, fieldName);
              }

              LOG.info("Cant find field: " + fieldName);
              return false;
            }

          };
        }
      };

    } else {
      xstream = isJson ? new XStream(new JettisonMappedXmlDriver()) : new XStream();
    }

    //see if omitting fields
    String fieldsToOmit = GrouperWsConfig.retrieveConfig().propertyValueString("ws.omitXmlPropertiesRest");
    if (!GrouperUtil.isBlank(fieldsToOmit)) {
      String[] fieldsToOmitList = GrouperUtil.splitTrim(fieldsToOmit, ",");
      for (String fieldToOmit: fieldsToOmitList) {
        if (!GrouperUtil.isBlank(fieldToOmit)) {
          try {
            int dotIndex = fieldToOmit.lastIndexOf('.');
            String className = fieldToOmit.substring(0, dotIndex);
            String propertyName = fieldToOmit.substring(dotIndex+1, fieldToOmit.length());
            Class<?> theClass = GrouperUtil.forName(className);
            xstream.omitField(theClass, propertyName);
          } catch (Exception e) {
            throw new RuntimeException("Problem with ws.omitXmlPropertiesRest: " + fieldsToOmit + ", " + e.getMessage(), e);
          }
        }
      }
    }
    GrouperVersion clientVersion = GrouperWsVersionUtils.retrieveCurrentClientVersion();
    if (clientVersion != null && clientVersion.lessThanArg(GrouperVersion.valueOfIgnoreCase("v1_4_000"))) {
      xstream.omitField(WsSubject.class, "identifierLookup");
    }
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
