package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.helper.GrouperTest;

/**
 *
 */
public class SqlProvisioningGroupTableStartWithTest extends GrouperTest {
  
  /**
   * 
   */
  public void testPopulateProvisionerConfigurationValuesFromStartWith() {
    SqlProvisioningGroupTableStartWith sqlProvisioningGroupTableStartWith = new SqlProvisioningGroupTableStartWith();
    
    Map<String, String> startWithSuffixToValue = new HashMap<>();
    
    startWithSuffixToValue.put("columnNames", "abc,def,ghi");
    
    Map<String, Object> provisionerSuffixToValue = new HashMap<>();
    
    sqlProvisioningGroupTableStartWith.populateProvisionerConfigurationValuesFromStartWith(startWithSuffixToValue, provisionerSuffixToValue);
    
    assertEquals(3, provisionerSuffixToValue.get("numberOfGroupAttributes"));
    assertEquals("abc", provisionerSuffixToValue.get("targetGroupAttribute.0.name"));
    assertEquals("def", provisionerSuffixToValue.get("targetGroupAttribute.1.name"));
    assertEquals("ghi", provisionerSuffixToValue.get("targetGroupAttribute.2.name"));
    assertEquals(true, provisionerSuffixToValue.get("operateOnGrouperGroups"));
    assertEquals("groupAttributes", provisionerSuffixToValue.get("provisioningType"));
  }

}
