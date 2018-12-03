package edu.internet2.middleware.grouper.app.grouperTypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperObjectTypesSettings {

  
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
    return Collections.unmodifiableList(Arrays.asList("ref", "basis", "policy", "etc", "bundle", "org", "test", "service", "app", "readOnly", "grouperSecurity"));
  }

  
}
