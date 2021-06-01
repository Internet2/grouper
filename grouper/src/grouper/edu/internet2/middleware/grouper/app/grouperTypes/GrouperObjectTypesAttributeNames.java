package edu.internet2.middleware.grouper.app.grouperTypes;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
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
  
  
  private static AttributeDefName attributeDefNameBase;

  private static AttributeDef attributeDefBase;
  
  private static AttributeDefName attributeDefNameDataOwner;
  private static AttributeDefName attributeDefNameMemberDescription;
  private static AttributeDefName attributeDefNameDirectAssignment;
  private static AttributeDefName attributeDefNameTypeName;
  private static AttributeDefName attributeDefNameServiceName;
  private static AttributeDefName attributeDefNameOwnerStemId;
  
  
  /**
   * marker attribute def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameBase() {
    
    if (attributeDefNameBase != null) {
      return attributeDefNameBase;
    }
    
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
    
    attributeDefNameBase = attributeDefName;
    return attributeDefName;
  }
  
  
  /**
   * attribute value def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDef retrieveAttributeDefBaseDef() {
    
    if (attributeDefBase != null) {
      return attributeDefBase;
    }
    
    AttributeDef attributeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefFinder.findByName(GrouperObjectTypesSettings.objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DEF, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDef == null) {
      throw new RuntimeException("Why cant grouperObjectTypeDef attribute def be found?");
    }
    attributeDefBase = attributeDef;
    return attributeDef;
  }
  
  /**
   * data owner attribute def name assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDataOwner() {
    
    if (attributeDefNameDataOwner != null) {
      return attributeDefNameDataOwner;
    }
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefNameFinder.findByName(GrouperObjectTypesSettings.objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DATA_OWNER, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant grouperObjectTypeDataOwner attribute def name be found?");
    }
    
    attributeDefNameDataOwner = attributeDefName;
    return attributeDefName;
  }
  
  /**
   * member description attribute def name assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameMemberDescription() {
    
    if (attributeDefNameMemberDescription != null) {
      return attributeDefNameMemberDescription;
    }
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefNameFinder.findByName(GrouperObjectTypesSettings.objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_MEMBERS_DESCRIPTION, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant grouperObjectTypeMembersDescription attribute def name be found?");
    }
    
    attributeDefNameMemberDescription = attributeDefName;
    return attributeDefName;
  }
  
  /**
   * direct assignment attribute def name assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDirectAssignment() {
    
    if (attributeDefNameDirectAssignment != null) {
      return attributeDefNameDirectAssignment;
    }
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefNameFinder.findByName(GrouperObjectTypesSettings.objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant grouperObjectTypeDirectAssignment attribute def name be found?");
    }
    
    attributeDefNameDirectAssignment = attributeDefName;
    return attributeDefName;
  }
  
  /**
   * type name attribute def name assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameTypeName() {
    
    if (attributeDefNameTypeName != null) {
      return attributeDefNameTypeName;
    }
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefNameFinder.findByName(GrouperObjectTypesSettings.objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant grouperObjectTypeName attribute def name be found?");
    }
    
    attributeDefNameTypeName = attributeDefName;
    return attributeDefName;
  }
  
  /**
   * service name attribute def name assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameServiceName() {
    
    if (attributeDefNameServiceName != null) {
      return attributeDefNameServiceName;
    }
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefNameFinder.findByName(GrouperObjectTypesSettings.objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_SERVICE_NAME, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant grouperObjectTypeServiceName attribute def name be found?");
    }
    
    attributeDefNameServiceName = attributeDefName;
    return attributeDefName;
  }
  
  /**
   * owner stem id attribute def name assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameOwnerStemId() {
    
    if (attributeDefNameOwnerStemId != null) {
      return attributeDefNameOwnerStemId;
    }
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefNameFinder.findByName(GrouperObjectTypesSettings.objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_OWNER_STEM_ID, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant grouperObjectTypeOwnerStemId attribute def name be found?");
    }
    
    attributeDefNameOwnerStemId = attributeDefName;
    return attributeDefName;
  }
    
  public static void clearCache() {
    
    attributeDefBase = null;
    attributeDefNameBase = null;
    attributeDefNameDataOwner = null;
    attributeDefNameMemberDescription = null;
    attributeDefNameDirectAssignment = null;
    attributeDefNameTypeName = null;
    attributeDefNameServiceName = null;
    attributeDefNameOwnerStemId = null;
    
  }

}
