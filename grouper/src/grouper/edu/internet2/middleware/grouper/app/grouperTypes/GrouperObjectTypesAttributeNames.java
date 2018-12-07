package edu.internet2.middleware.grouper.app.grouperTypes;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

public class GrouperObjectTypesAttributeNames {
  
  /**
   * main attribute definition assigned to groups, folders
   */
  public static final String GROUPER_OBJECT_TYPE_DEF = "grouperObjectTypeDef";
  
  /**
   * main attribute name assigned to grouperObjectTypeDef
   */
  public static final String GROUPER_OBJECT_TYPE_ATTRIBUTE_NAME = "grouperObjectTypeMarker";
  
  /**
   * attribute definition for name value pairs assigned to assignment on groups or folders
   */
  public static final String GROUPER_OBJECT_TYPE_VALUE_DEF = "grouperObjectTypeValueDef";
  
  /**
   * ref, basis, policy,etc, bundle, org, test, service
   */
  public static final String GROUPER_OBJECT_TYPE_NAME = "grouperObjectTypeName";
  
  /**
   * e.g. Registrar's office owns this data
   */
  public static final String GROUPER_OBJECT_TYPE_DATA_OWNER = "grouperObjectTypeDataOwner";
  
  /**
   * human readable description
   */
  public static final String GROUPER_OBJECT_TYPE_MEMBERS_DESCRIPTION = "grouperObjectTypeMembersDescription";
  
  /**
   * if this is directly assigned or inherited
   */
  public static final String GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT = "grouperObjectTypeDirectAssignment";
  
  /**
   * name of the service that this app falls under
   */
  public static final String GROUPER_OBJECT_TYPE_SERVICE_NAME = "grouperObjectTypeServiceName";
  
  /**
   * if this is not a direct assignment, then this is the stem id where it is inherited from
   */
  public static final String GROUPER_OBJECT_TYPE_OWNER_STEM_ID = "grouperObjectTypeOwnerStemId";
  
  
  /**
   * marker attribute def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameBase() {
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefNameFinder.findByName(GrouperObjectTypesSettings.objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_ATTRIBUTE_NAME, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant grouperObjectTypeMarker attribute def name be found?");
    }
    
    return attributeDefName;
  }
  
  
  

}
