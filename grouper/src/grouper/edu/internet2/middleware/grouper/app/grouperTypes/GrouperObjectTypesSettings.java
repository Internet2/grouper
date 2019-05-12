package edu.internet2.middleware.grouper.app.grouperTypes;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.BASIS;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.BUNDLE;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.ORG;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.POLICY;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.REF;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperObjectTypesSettings {
  
  public static final String REF = "ref";
  public static final String BASIS = "basis";
  public static final String POLICY = "policy";
  public static final String ETC = "etc";
  public static final String BUNDLE = "bundle";
  public static final String ORG = "org";
  public static final String TEST = "test";
  public static final String SERVICE = "service";
  public static final String APP = "app";
  public static final String READ_ONLY = "readOnly";
  public static final String GROUPER_SECURITY = "grouperSecurity";

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
    return Collections.unmodifiableList(Arrays.asList(REF, BASIS, POLICY, ETC, BUNDLE, ORG, TEST, SERVICE, APP, READ_ONLY, GROUPER_SECURITY));
  }
  
  /**
   * object types that require data owner and member description 
   * @return
   */
  public static List<String> getDataOwnerMemberDescriptionRequiringObjectTypeNames() {
    return Collections.unmodifiableList(Arrays.asList(REF, BASIS, POLICY, BUNDLE, ORG));
  }

  /**
   * object types that require service
   * @return
   */
  public static List<String> getServiceRequiringObjectTypeNames() {
    return Collections.unmodifiableList(Arrays.asList(APP));
  }
  
}
