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

import java.util.HashMap;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineIdentifier;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class InstrumentationDataUtils {
    
  /** instances def extension */
  public static final String INSTRUMENTATION_DATA_INSTANCES_DEF = "instrumentationDataInstancesDef";
  
  /** counts def extension */
  public static final String INSTRUMENTATION_DATA_INSTANCE_COUNTS_DEF = "instrumentationDataInstanceCountsDef";
  
  /** details def extension */
  public static final String INSTRUMENTATION_DATA_INSTANCE_DETAILS_DEF = "instrumentationDataInstanceDetailsDef";
  
  /** counts attr extension */
  public static final String INSTRUMENTATION_DATA_INSTANCE_COUNTS_ATTR = "instrumentationDataInstanceCounts";
  
  /** last update attr extension */
  public static final String INSTRUMENTATION_DATA_INSTANCE_LAST_UPDATE_ATTR = "instrumentationDataInstanceLastUpdate";
  
  /** engine name attr extension */
  public static final String INSTRUMENTATION_DATA_INSTANCE_ENGINE_NAME_ATTR = "instrumentationDataInstanceEngineName";
  
  /** server label attr extension */
  public static final String INSTRUMENTATION_DATA_INSTANCE_SERVER_LABEL_ATTR = "instrumentationDataInstanceServerLabel";
  
  /** instances folder extension */
  public static final String INSTRUMENTATION_DATA_INSTANCES_FOLDER = "instrumentationDataInstances";
  
  /** instrumentation data instances group extension */
  public static final String INSTRUMENTATION_DATA_INSTANCES_GROUP = "instrumentationDataInstancesGroup";
  
  /** collectors def extension */
  public static final String INSTRUMENTATION_DATA_COLLECTORS_DEF = "instrumentationDataCollectorsDef";
  
  /** details def extension */
  public static final String INSTRUMENTATION_DATA_COLLECTOR_DETAILS_DEF = "instrumentationDataCollectorDetailsDef";
  
  /** last update attr extension */
  public static final String INSTRUMENTATION_DATA_COLLECTOR_LAST_UPDATE_ATTR = "instrumentationDataCollectorLastUpdate";
  
  /** uuid attr extension */
  public static final String INSTRUMENTATION_DATA_COLLECTOR_UUID_ATTR = "instrumentationDataCollectorUuid";
  
  /** collectors folder extension */
  public static final String INSTRUMENTATION_DATA_COLLECTORS_FOLDER = "instrumentationDataCollectors";
  
  /** instrumentation data collector group extension */
  public static final String INSTRUMENTATION_DATA_COLLECTORS_GROUP = "instrumentationDataCollectorsGroup";
  
  private static boolean collectingStats = false;
  
  private static String instrumentationDataStemName = null;
  
  /**
   * @return the collectingStats
   */
  public static boolean isCollectingStats() {
    return collectingStats;
  }

  
  /**
   * @param theCollectingStats the theCollectingStats to set
   */
  public static void setCollectingStats(boolean theCollectingStats) {
    collectingStats = theCollectingStats;
  }
  
  /**
   * stem name for instrumentation data attributes
   * @return stem name
   */
  public static String grouperInstrumentationDataStemName() {
    if (instrumentationDataStemName == null) {
      instrumentationDataStemName = GrouperCheckConfig.attributeRootStemName() + ":instrumentationData";
    }
    return instrumentationDataStemName;
  }
  
  /**
   * return parent attribute assignment for the given instance
   * @param grouperEngineIdentifier 
   * @param uuid 
   * @return attribute assignment
   */
  public static AttributeAssign grouperInstrumentationInstanceParentAttributeAssignment(GrouperEngineIdentifier grouperEngineIdentifier, String uuid) {    

    String instanceDefNameName = grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_FOLDER + ":" + uuid;
    
    AttributeDefName instanceDefName = AttributeDefNameFinder.findByName(instanceDefNameName, false);
    
    if (instanceDefName == null) {
      Stem instrumentationDataInstancesFolder = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_FOLDER, true);
      String instancesDefName = grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_DEF;
      AttributeDef instancesDef = AttributeDefFinder.findByName(instancesDefName, true);
      instanceDefName = instrumentationDataInstancesFolder.addChildAttributeDefName(instancesDef, uuid, uuid);
    }
        
    // make sure instance is assigned to group
    String groupName = grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_GROUP;
    Group group = GrouperDAOFactory.getFactory().getGroup().findByNameSecure(groupName, true, null, GrouperUtil.toSet(TypeOfGroup.group));
    AttributeAssignResult result = group.getAttributeDelegate().assignAttribute(instanceDefName);
    AttributeAssign parentAssignment = result.getAttributeAssign();
    
    // make sure engine name is assigned
    parentAssignment.getAttributeValueDelegate().assignValue(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_ENGINE_NAME_ATTR, grouperEngineIdentifier.getGrouperEngine());
    
    // make sure server label is assigned
    String serverLabel = GrouperUtil.substituteExpressionLanguage(GrouperConfig.retrieveConfig().propertyValueString("instrumentation.serverLabel.el", "${grouperUtil.hostname()}"), new HashMap<String, Object>(), true, true, true);
    parentAssignment.getAttributeValueDelegate().assignValue(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_SERVER_LABEL_ATTR, serverLabel);
    
    return parentAssignment;
  }
  
  /**
   * return parent attribute assignment for the given collector
   * @param jobName
   * @return attribute assignment
   */
  public static AttributeAssign grouperInstrumentationCollectorParentAttributeAssignment(String jobName) {    

    String collectorDefNameName = grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTORS_FOLDER + ":" + jobName;
    
    AttributeDefName collectorDefName = AttributeDefNameFinder.findByName(collectorDefNameName, false);
    
    if (collectorDefName == null) {
      Stem instrumentationDataCollectorsFolder = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTORS_FOLDER, true);
      String collectorsDefName = grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTORS_DEF;
      AttributeDef collectorsDef = AttributeDefFinder.findByName(collectorsDefName, true);
      collectorDefName = instrumentationDataCollectorsFolder.addChildAttributeDefName(collectorsDef, jobName, jobName);
    }
        
    // make sure collector is assigned to group
    String groupName = grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTORS_GROUP;
    Group group = GrouperDAOFactory.getFactory().getGroup().findByNameSecure(groupName, true, null, GrouperUtil.toSet(TypeOfGroup.group));
    AttributeAssignResult result = group.getAttributeDelegate().assignAttribute(collectorDefName);
    AttributeAssign parentAssignment = result.getAttributeAssign();
    
    // make sure uuid is assigned
    if (parentAssignment.getAttributeValueDelegate().retrieveValueString(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_UUID_ATTR) == null) {
      parentAssignment.getAttributeValueDelegate().assignValue(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_UUID_ATTR, GrouperUuid.getUuid());
    }
    
    return parentAssignment;
  }
}
