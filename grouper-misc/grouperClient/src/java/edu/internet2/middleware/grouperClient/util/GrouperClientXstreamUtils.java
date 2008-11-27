/*
 * @author mchyzer
 * $Id: GrouperClientXstreamUtils.java,v 1.1 2008-11-27 14:25:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.util;

import java.util.Map;

import edu.internet2.middleware.grouperClient.examples.PersonXstreamExample;
import edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.XStream;
import edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.io.xml.DomDriver;
import edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.mapper.MapperWrapper;
import edu.internet2.middleware.grouperClient.ext.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClient.ws.WsRestClassLookup;


/**
 *
 */
public class GrouperClientXstreamUtils {

  /**
   * logger
   */
  static Log log = GrouperClientUtils.retrieveLog(PersonXstreamExample.class);

  /**
   * get xstream with all client aliases intact
   * @return xstream
   */
  public static XStream retrieveXstream() {
    return retrieveXstream(WsRestClassLookup.getAliasClassMap());
  }
  
  /**
   * 
   * @param aliasClassMap for xstream
   * @return xstream, configured to use
   */
  public static XStream retrieveXstream(Map<String, Class<?>> aliasClassMap) {
    XStream xStream = new XStream(new DomDriver()) {

      /**
       * 
       * @see edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.XStream#wrapMapper(edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.mapper.MapperWrapper)
       */
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MapperWrapper(next) {

          /**
           * 
           * @see edu.internet2.middleware.grouperClient.ext.com.thoughtworks.xstream.mapper.MapperWrapper#shouldSerializeMember(java.lang.Class, java.lang.String)
           */
          @Override
          public boolean shouldSerializeMember(Class definedIn, String fieldName) {
            boolean definedInNotObject = definedIn != Object.class;
            if (definedInNotObject) {
              return super.shouldSerializeMember(definedIn, fieldName);
            }

            log.info("Cant find field: " + fieldName);
            return false;
          }

        };
      }

    };
    //dont try to get fancy
    xStream.setMode(XStream.NO_REFERENCES);

    for (String key : GrouperClientUtils.nonNull(aliasClassMap).keySet()) {
      xStream.alias(key, aliasClassMap.get(key));
    }

    xStream.autodetectAnnotations(true);
    return xStream;
  }
  
}
