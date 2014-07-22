/**
 * Copyright 2012 Internet2
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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.poc;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class JexlExample {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("attributeName", "staff");
    
    String value = GrouperUtil.substituteExpressionLanguage("${attributeName == 'staff'}", variableMap, true, true, true);
    
    System.out.println("True? " + value);
    
    value = GrouperUtil.substituteExpressionLanguage("${attributeName == 'staff' || $attributeName == 'student'}", variableMap, true, true, true);
    
    System.out.println("True? " + value);
    
    value = GrouperUtil.substituteExpressionLanguage("${attributeName.toLowerCase().startsWith('st')}", variableMap, true, true, true);
    
    System.out.println("True? " + value);
    
    value = GrouperUtil.substituteExpressionLanguage("${attributeName.toLowerCase().startsWith('fa')}", variableMap, true, true, true);
    
    System.out.println("False? " + value);
    
    value = GrouperUtil.substituteExpressionLanguage("${attributeName =~ '^fa.*$' }", variableMap, true, true, true);
    
    System.out.println("regex0 False? " + value);
    
    value = GrouperUtil.substituteExpressionLanguage("${attributeName =~ '^st.*$' }", variableMap, true, true, true);
    
    System.out.println("regex1 True? " + value);
    
    value = GrouperUtil.substituteExpressionLanguage("${attributeName !~ '^fa.*$' }", variableMap, true, true, true);
    
    System.out.println("regex2 True? " + value);
    
    value = GrouperUtil.substituteExpressionLanguage("${attributeName !~ '^st.*$' }", variableMap, true, true, true);
    
    System.out.println("regex3 False? " + value);
    
    
    
  }
  
}
 