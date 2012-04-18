/*******************************************************************************
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.poc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.Expression;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.ExpressionFactory;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.JexlContext;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl.JexlHelper;


/**
 *
 */
public class ScriptingPoc {

  /**
   * @param args
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    //jexlExample();
    //jexlSubstituteScript();
    //jexlGrouperClientSubstitute();
  }

  /**
   * 
   */
  public static void jexlGrouperClientSubstitute() {
    Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
    
    List l = new ArrayList();
    l.add("Hello from location 0");
    l.add(new Integer(2));
    substituteMap.put("array", l);

    substituteMap.put("foo", new Foo());
    substituteMap.put("number", new Integer(10));

    String stringToParse = "Whatever: ${array[0].length()} ${foo.foo} ${number}";

    long start = System.currentTimeMillis();
    for (int i=0;i<100;i++) {
      
      String result = GrouperClientUtils.substituteExpressionLanguage(stringToParse, substituteMap);
  
      System.out.println(result);
      
      System.out.println("Elapsed: " + (System.currentTimeMillis() - start) + "ms");
    }    

  }
  
  /**
   * 
   * @throws Exception
   */
  public static void jexlSubstituteScript() throws Exception {
    JexlContext jc = JexlHelper.createContext();
    List l = new ArrayList();
    l.add("Hello from location 0");
    l.add(new Integer(2));
    jc.getVars().put("array", l);

    jc.getVars().put("foo", new Foo());
    jc.getVars().put("number", new Integer(10));

    String stringToParse = "Whatever: ${array[0].length()} ${foo.foo} ${number}";

    long start = System.currentTimeMillis();
    for (int i=0;i<100;i++) {
    
      int index = 0;
      
      // matching ${ exp }   (non-greedy)
      Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
      Matcher matcher = pattern.matcher(stringToParse);
      
      StringBuilder result = new StringBuilder();
      
      while(matcher.find()) {
        result.append(stringToParse.substring(index, matcher.start()));
        String script = stringToParse.substring(matcher.start(), matcher.end());
  
        //System.out.println("Script: " + script);
        
        String script2 = matcher.group(1);
        
        //System.out.println("Script2: " + script2);
        
        Expression e = ExpressionFactory.createExpression(script2);
        
        Object o = e.evaluate(jc);
        //System.out.println("evaluate: " + o);
  
        result.append(o);
        
        index = matcher.end();
      }
      
      result.append(stringToParse.substring(index, stringToParse.length()));
  
      System.out.println(result.toString());
      
      System.out.println("Elapsed: " + (System.currentTimeMillis() - start) + "ms");
    }    

  }
   
  /**
   * 
   * @throws Exception
   */
  public static void jexlExample() throws Exception {
    /*
     *  First make a jexlContext and put stuff in it
     */
    JexlContext jc = JexlHelper.createContext();

    List l = new ArrayList();
    l.add("Hello from location 0");
    l.add(new Integer(2));
    jc.getVars().put("array", l);

    Expression e = ExpressionFactory.createExpression("array[1]");
    Object o = e.evaluate(jc);
    System.out.println("Object @ location 1 = " + o);

    e = ExpressionFactory.createExpression("array[0].length()");
    o = e.evaluate(jc);

    System.out.println("The length of the string at location 0 is : " + o);
    
    //e = ExpressionFactory.createExpression("Whatever: ${array[0].length()}");
    
    //o = e.evaluate(jc);
    //System.out.println("evaluate: " + o);

    /*
     *  First make a jexlContext and put stuff in it
     */
    jc = JexlHelper.createContext();

    jc.getVars().put("foo", new Foo());
    jc.getVars().put("number", new Integer(10));

    /*
     *  access a method w/o args
     */
    e = ExpressionFactory.createExpression("foo.getFoo()");
    o = e.evaluate(jc);
    System.out.println("value returned by the method getFoo() is : " + o);

    e = ExpressionFactory.createExpression("foo.foo");
    o = e.evaluate(jc);
    System.out.println("value returned by the method foo.foo is : " + o);

    /*
     *  access a method w/ args
     */
    e = ExpressionFactory.createExpression("foo.convert(1)");
    o = e.evaluate(jc);
    System.out.println("value of " + e.getExpression() + " is : " + o);

    e = ExpressionFactory.createExpression("foo.convert(1+7)");
    o = e.evaluate(jc);
    System.out.println("value of " + e.getExpression() + " is : " + o);

    e = ExpressionFactory.createExpression("foo.convert(1+number)");
    o = e.evaluate(jc);
    System.out.println("value of " + e.getExpression() + " is : " + o);

    /*
     * access a property
     */
    e = ExpressionFactory.createExpression("foo.bar");
    o = e.evaluate(jc);
    System.out.println("value returned for the property 'bar' is : " + o);


  }
  
  /**
   * Helper example class.
   */
  public static class Foo {
      /**
       * Gets foo.
       * @return a string.
       */
      public String getFoo() {
          return "This is from getFoo()";
      }

      /**
       * Gets an arbitrary property.
       * @param arg property name.
       * @return arg prefixed with 'This is the property '.
       */
      public String get(String arg) {
          return "This is the property " + arg;
      }

      /**
       * Gets a string from the argument.
       * @param i a long.
       * @return The argument prefixed with 'The value is : '
       */
      public String convert(long i) {
          return "The value is : " + i;
      }
  }


}
