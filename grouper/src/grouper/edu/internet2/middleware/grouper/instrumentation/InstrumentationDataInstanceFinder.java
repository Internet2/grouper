/**
 * Copyright 2017 Internet2
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
 */
package edu.internet2.middleware.grouper.instrumentation;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class InstrumentationDataInstanceFinder {
  
  /**
   * @param id
   * @param includeCounts
   * @param exceptionIfNotFound
   * @return instance
   */
  public static InstrumentationDataInstance findById(String id, boolean includeCounts, boolean exceptionIfNotFound) {
    
    String instrumentationDataRootStemName = InstrumentationDataUtils.grouperInstrumentationDataStemName();
    String groupName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_GROUP;    

    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_FOLDER + ":" + id, exceptionIfNotFound);
    
    if (attributeDefName == null) {
      return null;
    }
    
    AttributeAssign assignInstance = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, true, exceptionIfNotFound);
    
    if (assignInstance == null) {
      return null;
    }
    
    return internal_getInstrumentationDataInstance(assignInstance, includeCounts);
  }
  
  /**
   * @param includeCounts
   * @return all instrumentation instances
   */
  public static List<InstrumentationDataInstance> findAll(boolean includeCounts) {
    List<InstrumentationDataInstance> instances = new ArrayList<InstrumentationDataInstance>();
    
    String instrumentationDataRootStemName = InstrumentationDataUtils.grouperInstrumentationDataStemName();
    String groupName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_GROUP;
    String instancesDefName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_DEF;
    

    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
    
    Set<AttributeAssign> assignInstances = group.getAttributeDelegate().retrieveAssignmentsByAttributeDef(instancesDefName);
    for (AttributeAssign attrInstance : assignInstances) {
      instances.add(internal_getInstrumentationDataInstance(attrInstance, includeCounts));
    }
    
    return instances;
  }
  
  private static InstrumentationDataInstance internal_getInstrumentationDataInstance(AttributeAssign attrInstance, boolean includeCounts) {
    String instrumentationDataRootStemName = InstrumentationDataUtils.grouperInstrumentationDataStemName();

    String engineNameName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_ENGINE_NAME_ATTR;
    String lastUpdateName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_LAST_UPDATE_ATTR;
    String serverLabelName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_SERVER_LABEL_ATTR;
   
    String countsName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_COUNTS_ATTR;
    AttributeDefName countsAttrDefName = AttributeDefNameFinder.findByName(countsName, true);

    String uuid = attrInstance.getAttributeDefName().getExtension();
    String engineName = attrInstance.getAttributeValueDelegate().retrieveValueString(engineNameName);
    String lastUpdate = attrInstance.getAttributeValueDelegate().retrieveValueString(lastUpdateName);
    String serverLabel = attrInstance.getAttributeValueDelegate().retrieveValueString(serverLabelName);
    
    InstrumentationDataInstance instance = new InstrumentationDataInstance();
    instance.setUuid(uuid);
    instance.setEngineName(engineName);
    instance.setServerLabel(serverLabel);
    
    if (!StringUtils.isEmpty(lastUpdate)) {
      instance.setLastUpdate(new Date(Long.parseLong(lastUpdate)));
    }
    
    if (includeCounts) {
      List<InstrumentationDataInstanceCounts> countsList = new ArrayList<InstrumentationDataInstanceCounts>();
      
      AttributeAssign countsAssign = attrInstance.getAttributeDelegate().retrieveAssignment("assign", countsAttrDefName, false, false);
      
      if (countsAssign != null) {
        Set<AttributeAssignValue> attributeAssignValues = countsAssign.getValueDelegate().getAttributeAssignValues();
        
        for (AttributeAssignValue attributeAssignValue : attributeAssignValues) {
          String json = attributeAssignValue.getValueString();
          
          Map<String, Object> data = GrouperUtil.jsonConvertFrom(json, LinkedHashMap.class);
          
          InstrumentationDataInstanceCounts counts = new InstrumentationDataInstanceCounts();
          counts.setCreatedOn(attributeAssignValue.getCreatedOn());
          Long startTimeLong = (Long)data.remove("startTime");
          counts.setStartTime(new Date(startTimeLong));
          Object durationObject = data.remove("duration");
          if (durationObject instanceof Long) {
            counts.setDuration((Long)durationObject);
          } else if (durationObject instanceof Integer) {
            counts.setDuration(new Long((Integer)durationObject));
          }
          
          Map<String, Long> actualCounts = new LinkedHashMap<String, Long>();
          for (String key : data.keySet()) {
            actualCounts.put(key, Long.parseLong("" + data.get(key)));
          }
          
          counts.setCounts(actualCounts);
          countsList.add(counts);
        }
      }
      
      instance.setCounts(countsList);
    }
    
    return instance;
  }
}
