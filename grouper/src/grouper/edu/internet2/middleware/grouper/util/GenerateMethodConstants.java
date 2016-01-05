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
/*
 * @author mchyzer
 * $Id: GenerateMethodConstants.java,v 1.6 2008-10-17 12:06:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.hooks.AttributeDefHooks;


/**
 *
 */
public class GenerateMethodConstants {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    generateConstants(AttributeDefHooks.class);
  }
  /**
   * <pre>
   * generate the Java for the constant fields
   * to ignore a field, annotate with GrouperIngoreFieldConstant
   * </pre>
   * @param theClass 
   *
   */
  private static void generateConstants(Class theClass) {
    Set<String> methodNamesSet = GrouperUtil.methodNames(theClass,theClass, true, false);

    //sort
    List<String> methodNames = new ArrayList<String>(methodNamesSet);
    Collections.sort(methodNames);
    StringBuilder result = new StringBuilder();
    System.out.println("  //*****  START GENERATED WITH GenerateMethodConstants.java *****//\n");
    for (String methodName: methodNames) {
      String caps = "METHOD_" + GrouperUtil.oracleStandardNameFromJava(methodName);
      
      result.append("  /** constant for method name for: " + methodName + " */\n");
      result.append("  public static final String " + caps + " = \"" + methodName + "\";\n\n");
    }
    System.out.print(result.toString());
    System.out.println("  //*****  END GENERATED WITH GenerateMethodConstants.java *****//");
  }

}
