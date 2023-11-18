/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    ruleCheck.setCheckOwnerId("a group");
    ruleCheck.setCheckType("flattenedMembershipRemove");
    
    ruleDefinition.setCheck(ruleCheck);
    
    ruleDefinition.setIfCondition(new RuleIfCondition("if condition", null, null, null, null, null, null));
    ruleDefinition.setThen(new RuleThen("then part", null, null, null, null));
    
    String json = GrouperUtil.jsonConvertTo(ruleDefinition);
    json = GrouperUtil.indent(json, true);
    
    System.out.println(json);
    
    Map<String, Class<?>> conversionMap = new HashMap<String, Class<?>>();
    conversionMap.put(RuleDefinition.class.getSimpleName(), RuleDefinition.class);
    
    RuleDefinition ruleDefinition2 = (RuleDefinition)GrouperUtil.jsonConvertFrom(conversionMap, json);
    
    assertEquals(ruleDefinition.getThen().toString(), ruleDefinition2.getThen().toString());
    
  }
}
