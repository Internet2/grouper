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
 * @author mchyzer
 * $Id: StandardApiClientXstreamUtilstils.java,v 1.3 2008-12-02 05:16:38 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiClient.util;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.authzStandardApiClient.ws.AsacRestClassLookup;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.XStream;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.io.xml.DomDriver;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.mapper.MapperWrapper;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.logging.Log;


/**
 *
 */
public class StandardApiClientXstreamUtils {

  /**
   * logger
   */
  static Log log = StandardApiClientUtils.retrieveLog(StandardApiClientXstreamUtils.class);

  /**
   * get xstream with all client aliases intact
   * @return xstream
   */
  public static XStream retrieveXstream() {
    return retrieveXstream(AsacRestClassLookup.getAliasClassMap());
  }
  
  /**
   * 
   * @param aliasClassMap for xstream
   * @return xstream, configured to use
   */
  public static XStream retrieveXstream(Map<String, Class<?>> aliasClassMap) {
    boolean ignoreExtraneousFields = true;
    
    XStream xStream = null;
    
    if (ignoreExtraneousFields) {
    
      xStream = new XStream(new DomDriver()) {

        /**
         * 
         * @see edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.XStream#wrapMapper(edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.mapper.MapperWrapper)
         */
        @Override
        protected MapperWrapper wrapMapper(MapperWrapper next) {
          return new MapperWrapper(next) {
  
            /**
             * 
             * @see edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.mapper.MapperWrapper#shouldSerializeMember(java.lang.Class, java.lang.String)
             */
            @Override
            public boolean shouldSerializeMember(Class definedIn, String fieldName) {
              boolean definedInNotObject = definedIn != Object.class;
              if (definedInNotObject) {
                return super.shouldSerializeMember(definedIn, fieldName);
              }
  
              log.debug("Cant find field: " + fieldName);
              return false;
            }
  
          };
        }
      };
    } else {
      xStream = new XStream(new DomDriver());
    }
    //dont try to get fancy
    xStream.setMode(XStream.NO_REFERENCES);

    for (String key : StandardApiClientUtils.nonNull(aliasClassMap).keySet()) {
      xStream.alias(key, aliasClassMap.get(key));
    }

    xStream.autodetectAnnotations(true);

    //see if omitting fields
    String fieldsToOmit = StandardApiClientConfig.retrieveConfig().propertyValueString("grouper.webService.omitXmlProperties");
    if (!StandardApiClientUtils.isBlank(fieldsToOmit)) {
      List<String> fieldsToOmitList = StandardApiClientUtils.splitTrimToList(fieldsToOmit, ",");
      for (String fieldToOmit: fieldsToOmitList) {
        if (!StandardApiClientUtils.isBlank(fieldToOmit)) {
          try {
            int dotIndex = fieldToOmit.lastIndexOf('.');
            String className = fieldToOmit.substring(0, dotIndex);
            String propertyName = fieldToOmit.substring(dotIndex+1, fieldToOmit.length());
            Class<?> theClass = StandardApiClientUtils.forName(className);
            xStream.omitField(theClass, propertyName);
          } catch (Exception e) {
            throw new RuntimeException("Problem with grouper.webService.omitXmlProperties: " + fieldsToOmit + ", " + e.getMessage(), e);
          }
        }
      }
    }
    
    return xStream;
  }
  
}
