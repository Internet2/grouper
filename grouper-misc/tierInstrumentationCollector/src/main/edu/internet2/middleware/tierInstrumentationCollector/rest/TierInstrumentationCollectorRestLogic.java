/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.tierInstrumentationCollector.rest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcTransactionCallback;
import edu.internet2.middleware.grouperClient.jdbc.GcTransactionEnd;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.tierInstrumentationCollector.config.TierInstrumentationCollectorConfig;
import edu.internet2.middleware.tierInstrumentationCollector.corebeans.TicResponseBeanBase;
import edu.internet2.middleware.tierInstrumentationCollector.db.TierInstrumentationCollectorAttributeType;
import edu.internet2.middleware.tierInstrumentationCollector.db.TierInstrumentationCollectorEntry;
import edu.internet2.middleware.tierInstrumentationCollector.db.TierInstrumentationCollectorEntryAttribute;
import net.sf.json.JSONObject;

/**
 * logic for rest calls
 * @author mchyzer
 *
 */
public class TierInstrumentationCollectorRestLogic {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(TierInstrumentationCollectorRestLogic.class);

  /**
   * 
   */
  public static TicResponseBeanBase uploadSave(JSONObject uploadJsonObject, Map<String, String> params) {
    if (uploadJsonObject == null) {
      throw new RuntimeException("uploadJsonObject is null");
    }
    
    final TierInstrumentationCollectorEntry entry = new TierInstrumentationCollectorEntry();
    
    entry.setUuid(GrouperClientUtils.uuid());
    entry.setTheTimestamp(new Timestamp(System.currentTimeMillis()));

    //attribute names which are included in the entry and should not be added as foreign key attributes
    Set<String> attributesToSkip = new HashSet<String>();
    
    //required fields
    entry.setComponent(entryFieldValue(uploadJsonObject, "component", true, attributesToSkip));
    entry.setReportFormat(GrouperClientUtils.longValue(entryFieldValue(uploadJsonObject, "reportFormat", true, attributesToSkip)));
    entry.setVersion(entryFieldValue(uploadJsonObject, "version", false, attributesToSkip));
    entry.setEnvironment(entryFieldValue(uploadJsonObject, "environment", false, attributesToSkip));
    entry.setInstitution(entryFieldValue(uploadJsonObject, "institution", false, attributesToSkip));
    
    final List<TierInstrumentationCollectorEntryAttribute> attributes = new ArrayList<TierInstrumentationCollectorEntryAttribute>();
    
    for (Object keyObject : uploadJsonObject.keySet()) {
      String key = (String)keyObject;
      if (attributesToSkip.contains(key)) {
        continue;
      }
      
      //lets see if configured
      String typeString = TierInstrumentationCollectorConfig.retrieveConfig().propertyValueStringRequired("tic.componentName." 
          + entry.getComponent() + ".entryVersion." + entry.getReportFormat() + ".attributeName." + key + ".type");
      
      TierInstrumentationCollectorAttributeType type = TierInstrumentationCollectorAttributeType.valueOfIgnoreCase(typeString, true);
      
      TierInstrumentationCollectorEntryAttribute attribute = new TierInstrumentationCollectorEntryAttribute();
      attributes.add(attribute);
      attribute.setAttributeName(key);
      attribute.setAttributeType(typeString);
      attribute.setUuid(GrouperClientUtils.uuid());
      
      switch(type){
        case boolean_type:
          boolean booleanValue = uploadJsonObject.getBoolean(key);
          attribute.setAttributeValueString(booleanValue ? "T" : "F");
          break;
        case floating_type:
          double doubleValue = uploadJsonObject.getDouble(key);
          attribute.setAttributeValueFloating(doubleValue);
          break;
        case integer_type:
          long intValue = uploadJsonObject.getLong(key);
          attribute.setAttributeValueInteger(intValue);
          break;
        case string_type:
          String stringValue = uploadJsonObject.getString(key);
          attribute.setAttributeValueString(stringValue);
          break;
        case timestamp_type:
          //TODO
          break;
      }
      
    }
    new GcDbAccess().callbackTransaction(new GcTransactionCallback() {

      /**
       * 
       * @see edu.internet2.middleware.grouperClient.jdbc.GcTransactionCallback#callback(edu.internet2.middleware.grouperClient.jdbc.GcDbAccess)
       */
      @Override
      public Object callback(GcDbAccess dbAccess) {
        new GcDbAccess().storeToDatabase(entry);
        for (TierInstrumentationCollectorEntryAttribute attribute : attributes) {
          attribute.setEntryUuid(entry.getUuid());
        }
        new GcDbAccess().storeListToDatabase(attributes);

        return null;
      }
    });
    return new TicResponseBeanBase() {
    };
  }

  /**
   * entry field value
   * @param uploadJsonObject
   * @param fieldName
   * @param required
   * @param attributesToSkip
   * @return value
   */
  private static String entryFieldValue(JSONObject uploadJsonObject, String fieldName, boolean required, Set<String> attributesToSkip) {
    attributesToSkip.add(fieldName);
    if (!required && !uploadJsonObject.containsKey(fieldName)) {
      return null;
    }
    String fieldValue = uploadJsonObject.getString(fieldName);
    if (StringUtils.isBlank(fieldValue)) {
      throw new RuntimeException("fieldValue is null");
    }
    return fieldValue;
  }
  
}
