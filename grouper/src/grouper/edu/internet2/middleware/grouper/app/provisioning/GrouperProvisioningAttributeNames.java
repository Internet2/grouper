package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

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
   * Millis since 1970 that this was last full provisioned
   */
  public static final String PROVISIONING_LAST_FULL_MILLIS_SINCE_1970 = "provisioningLastFullMillisSince1970";
  
  /**
   * Millis since 1970 that this was last incremental provisioned. Even if the incremental did not change the target
   */
  public static final String PROVISIONING_LAST_INCREMENTAL_MILLIS_SINCE_1970 = "provisioningLastIncrementalMillisSince1970";
  
  /**
   * Summary of last full run
   */
  public static final String PROVISIONING_LAST_FULL_SUMMARY = "provisioningLastFullSummary";
  
  /**
   * Summary of last incremental run
   */
  public static final String PROVISIONING_LAST_INCREMENTAL_SUMMARY = "provisioningLastIncrementalSummary";
  
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

}
