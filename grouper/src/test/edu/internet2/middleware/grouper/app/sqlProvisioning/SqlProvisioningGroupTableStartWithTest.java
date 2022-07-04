package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 *
 */
public class SqlProvisioningGroupTableStartWithTest extends GrouperTest {
  
  
  public SqlProvisioningGroupTableStartWithTest() {
    super();
  }

  public SqlProvisioningGroupTableStartWithTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new SqlProvisioningGroupTableStartWithTest("testPopulateProvisionerConfigurationValuesFromStartWith"));
  }
  
  /**
   * 
   */
  public void testPopulateProvisionerConfigurationValuesFromStartWith() {
    SqlProvisioningStartWith sqlProvisioningGroupTableStartWith = new SqlProvisioningStartWith();
    
    Map<String, String> startWithSuffixToValue = new HashMap<>();
    
    startWithSuffixToValue.put("hasGroupTable", "true");
    startWithSuffixToValue.put("groupTableColumnNames", "abc,def,ghi");
    startWithSuffixToValue.put("groupTableIdColumn", "id");
    
    
    Map<String, Object> provisionerSuffixToValue = new HashMap<>();
    
    sqlProvisioningGroupTableStartWith.populateProvisionerConfigurationValuesFromStartWith(startWithSuffixToValue, provisionerSuffixToValue);
    
    assertEquals(4, provisionerSuffixToValue.get("numberOfGroupAttributes"));
    assertEquals("id", provisionerSuffixToValue.get("targetGroupAttribute.0.name"));
    assertEquals("abc", provisionerSuffixToValue.get("targetGroupAttribute.1.name"));
    assertEquals("def", provisionerSuffixToValue.get("targetGroupAttribute.2.name"));
    assertEquals("ghi", provisionerSuffixToValue.get("targetGroupAttribute.3.name"));
    assertEquals("true", provisionerSuffixToValue.get("operateOnGrouperGroups"));
  }

}
