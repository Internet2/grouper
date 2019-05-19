package edu.internet2.middleware.grouper.app.grouperTypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperObjectTypesSettings {
  
  public static final String REF = "ref";
  public static final String BASIS = "basis";
  public static final String POLICY = "policy";
  public static final String ETC = "etc";
  public static final String ORG = "org";
  public static final String TEST = "test";
  public static final String SERVICE = "service";
  public static final String APP = "app";
  public static final String READ_ONLY = "readOnly";
  public static final String GROUPER_SECURITY = "grouperSecurity";
  
  // bundle is only being used in auto assign types to suggest folder by assigned type ref 
  // even though the folder extension matched "bundle" 
  public static final String BUNDLE = "bundle";

  /**
   * if object types are enabled
   * @return if object types are enabled
   */
  public static boolean objectTypesEnabled() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("objectTypes.enable", true);
  }
  
  /**
   * 
   * @return the stem name with no last colon
   */
  public static String objectTypesStemName() {
    return GrouperUtil.stripEnd(GrouperConfig.retrieveConfig().propertyValueString("objectTypes.systemFolder", 
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":objectTypes"), ":");
  }
  
  /**
   * all the object types
   * @return
   */
  public static List<String> getObjectTypeNames() {
    return Collections.unmodifiableList(Arrays.asList(BASIS, REF, POLICY, ETC, GROUPER_SECURITY, ORG, APP, SERVICE, READ_ONLY, TEST));
  }
  
  /**
   * object types that require data owner and member description 
   * @return
   */
  public static List<String> getDataOwnerMemberDescriptionRequiringObjectTypeNames() {
    return Collections.unmodifiableList(Arrays.asList(REF, BASIS, POLICY, ORG));
  }

  /**
   * object types that require service
   * @return
   */
  public static List<String> getServiceRequiringObjectTypeNames() {
    return Collections.unmodifiableList(Arrays.asList(APP));
  }
  
  /**
   * map showing which folder extension should go with with type 
   * @return map of folder extension to object type
   */
  public static Map<String, String> getFolderExtensionToTypeSuggestion() {
    
    Map<String, String> extensionToType = new HashMap<String, String>();
    
    for (String objectType: getObjectTypeNames()) {
      extensionToType.put(objectType, objectType);
    }
    // for folders that end with bundle we want to suggest that ref be assigned to them
    extensionToType.put(BUNDLE, REF);
    
    return extensionToType;
  }
  
}
