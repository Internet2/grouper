/**
 * Copyright 2018 Internet2
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
package edu.internet2.middleware.grouper.app.grouperTypes;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;

@DisallowConcurrentExecution
public class GrouperObjectTypesJob extends OtherJobBase {

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    GrouperSession.startRootSession();
    
    updateMetadataOnDirectStemsChildren();
    updateMetadataOnIndirectGrouperObjects();
    
    return null;
  }
  
  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_grouperObjectTypeDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperObjectTypesJob().run(otherJobInput);
  }
  
  
  protected static List<Stem> updateMetadataOnDirectStemsChildren() {
    
    if (!GrouperObjectTypesSettings.objectTypesEnabled()) {
      return new ArrayList<Stem>();
    }
    
    List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("true").findStems());
    
    
    for (Stem stem: stems) {
      List<GrouperObjectTypesAttributeValue> attributeValues = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValues(stem);
      
      for (GrouperObjectTypesAttributeValue attributeValue: attributeValues) {
        GrouperObjectTypesConfiguration.saveOrUpdateTypeAttributes(attributeValue, stem);        
      }
      
    }
    
    return stems;
    
  }
  
  
  protected static void updateMetadataOnIndirectGrouperObjects() {
    
    if (!GrouperObjectTypesSettings.objectTypesEnabled()) {
      return;
    }
    
    Set<GrouperObject> indirectGrouperObjects = new HashSet<GrouperObject>();
    
    List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("false").findStems());
    
    List<Group> groups = new ArrayList<Group>(new GroupFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("false").findGroups());
    
    indirectGrouperObjects.addAll(stems);
    indirectGrouperObjects.addAll(groups);
    
    for (GrouperObject grouperObject: indirectGrouperObjects) {
     GrouperObjectTypesConfiguration.copyConfigFromParent(grouperObject);
    }
    
  }
  
}
