package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

public class GrouperProvisioningAttributeNames {
  
  /**
   * main attribute definition assigned to groups, folders
   */
  public static final String PROVISIONING_DEF = "provisioningDef";
  
  /**
   * main attribute name assigned to provisioningDef
   */
  public static final String PROVISIONING_ATTRIBUTE_NAME = "provisioningMarker";
  
  /**
   * attribute definition for name value pairs assigned to assignment on groups or folders
   */
  public static final String PROVISIONING_VALUE_DEF = "provisioningValueDef";
  
  /**
   * pspngLdap, box, etc
   */
  public static final String PROVISIONING_TARGET = "provisioningTarget";
  
  /**
   * if this is directly assigned or inherited
   */
  public static final String PROVISIONING_DIRECT_ASSIGNMENT = "provisioningDirectAssign";
  
  /**
   * if this is not a direct assignment, then this is the stem id where it is inherited from
   */
  public static final String PROVISIONING_OWNER_STEM_ID = "provisioningOwnerStemId";
  
  /**
   * If folder provisioning applies to only this folder or this folder and subfolders. one|sub
   */
  public static final String PROVISIONING_STEM_SCOPE = "provisioningStemScope";
  
  /**
   * If you should provisioning (default to true)
   */
  public static final String PROVISIONING_DO_PROVISION = "provisioningDoProvision";
  
  /**
   * metadata json
   */
  public static final String PROVISIONING_METADATA_JSON = "provisioningMetadataJson";
  
  
  /**
   * marker attribute def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameBase() {
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefNameFinder.findByName(GrouperProvisioningSettings.provisioningConfigStemName()+":"+PROVISIONING_ATTRIBUTE_NAME, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant provisioningMarker attribute def name be found?");
    }
    
    return attributeDefName;
  }
  
  
  /**
   * attribute value def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDef retrieveAttributeDefBaseDef() {
    
    AttributeDef attributeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefFinder.findByName(GrouperProvisioningSettings.provisioningConfigStemName()+":"+PROVISIONING_DEF, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDef == null) {
      throw new RuntimeException("Why cant provisioningDef attribute def be found?");
    }
    
    return attributeDef;
  }

  /** attribute def name cache */
  private static ExpirableCache<String, AttributeDefName> attributeDefNameCache = new ExpirableCache<String, AttributeDefName>(5);

  /**
   * provisioning marker attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameMarker() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperProvisioningSettings.provisioningConfigStemName() + ":" + PROVISIONING_ATTRIBUTE_NAME);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant provisioning marker attribute def name be found?");
    }
    return attributeDefName;
  
  }

  /**
   * provisioning 'do provision' attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDoProvision() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperProvisioningSettings.provisioningConfigStemName() + ":" + PROVISIONING_DO_PROVISION);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant provisioning 'do provision' attribute def name be found?");
    }
    return attributeDefName;
  
  }

  /**
   * cache this.  note, not sure if its necessary
   */
  private static AttributeDefName retrieveAttributeDefNameFromDbOrCache(final String name) {
    
    AttributeDefName attributeDefName = attributeDefNameCache.get(name);
  
    if (attributeDefName == null) {
      
      attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return AttributeDefNameFinder.findByName(name, false, new QueryOptions().secondLevelCache(false));
          
        }
        
      });
      if (attributeDefName == null) {
        return null;
      }
      attributeDefNameCache.put(name, attributeDefName);
    }
    
    return attributeDefName;
  }


  /**
   * provisioning 'target' attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameTarget() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperProvisioningSettings.provisioningConfigStemName() + ":" + PROVISIONING_TARGET);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant provisioning 'target' attribute def name be found?");
    }
    return attributeDefName;
  
  }

  /**
   * provisioning 'direct assignment' attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDirectAssignment() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperProvisioningSettings.provisioningConfigStemName() + ":" + PROVISIONING_DIRECT_ASSIGNMENT);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant provisioning 'direct assignment' attribute def name be found?");
    }
    return attributeDefName;
  
  }

  /**
   * provisioning 'stem scope' attribute def name: sub or one
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameStemScope() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperProvisioningSettings.provisioningConfigStemName() + ":" + PROVISIONING_STEM_SCOPE);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant provisioning 'stem scope' attribute def name be found?");
    }
    return attributeDefName;
  
  }

}
