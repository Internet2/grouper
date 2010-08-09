/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import java.util.HashMap;
import java.util.Map;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * test rule definitions
 * @author mchyzer
 *
 */
public class RuleDefinitionTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleDefinitionTest("testJson"));
  }
  
  /**
   * 
   * @param name
   */
  public RuleDefinitionTest(String name) {
    super(name);
  }

  /**
   * test json
   */
  public void testJson() {
    RuleDefinition ruleDefinition = new RuleDefinition();
    RuleSubjectActAs ruleSubjectActAs = new RuleSubjectActAs();
    ruleSubjectActAs.setSourceId("source");
    ruleSubjectActAs.setSubjectId("subjectId");
    
    ruleDefinition.setActAs(ruleSubjectActAs);
    
    RuleCheck ruleCheck = new RuleCheck();
    ruleCheck.setOwnerId("a group");
    ruleCheck.setType("flattenedMembershipRemove");
    
    ruleDefinition.setCheck(ruleCheck);
    
    ruleDefinition.setIfCondition("if condition");
    ruleDefinition.setThen("then part");
    
    String json = GrouperUtil.jsonConvertTo(ruleDefinition);
    json = GrouperUtil.indent(json, true);
    
    System.out.println(json);
    
    Map<String, Class<?>> conversionMap = new HashMap<String, Class<?>>();
    conversionMap.put(RuleDefinition.class.getSimpleName(), RuleDefinition.class);
    
    RuleDefinition ruleDefinition2 = (RuleDefinition)GrouperUtil.jsonConvertFrom(conversionMap, json);
    
    assertEquals(ruleDefinition.getThen(), ruleDefinition2.getThen());
    
  }
}
