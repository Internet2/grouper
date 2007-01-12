/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
  
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
  
      http://www.apache.org/licenses/LICENSE-2.0
  
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package edu.internet2.middleware.ldappcTest;

import edu.internet2.middleware.ldappcTest.TestOptions;

/**
 * Displays or logs JUnit test run titles.
 * Objects calling the showClassName and showRunTitle method should 
 * 1) call the showClassName once per test class in setup,
 * 2) call the showRunTitle(String method, String name)
 * method, 
 * 3) then for additional tests within the same method
 * call showRunTitle(String name)
 * @author Gil Singer
 */
public class DisplayTest
{
    DisplayTest displayTest;

    /**
     * String for inserting 8 spaces.
     */
    private static final String INDENT = "        ";

    /**
     * String for twice the normal spacing.
     */
    private static final String INDENT2 = INDENT + INDENT;

    /**
     * String for line feed.
     */
    private static final String LF = "\n";

    /**
     * Displays or logs the name of a JUnit test case when enable
     * by the value of TestOptions.SHOW_NAMES
     *
     * @param method Name of the method containing the test
     * @param name Title of the test run
     */
    public static void showRunTitle(String method, String name)
    {
        if (TestOptions.SHOW_NAMES)
        {
            String msg = INDENT + "Running: " + method + LF + INDENT2 + name;
            System.out.println(msg);
        }
    }
    
    /**
     * Displays or logs the name of a JUnit test case when enable
     * by the value of TestOptions.SHOW_NAMES
     * For each method calling this, use the two argument version first.
     *
     * @param name Title of the test run
     */
    public static void showRunTitle(String name)
    {
        if (TestOptions.SHOW_NAMES)
        {
            System.out.println(INDENT + name);
        }
    }
    
    /**
     * Displays or logs the name of a JUnit test case when enable
     * by the value of TestOptions.SHOW_NAMES
     * For each test class call this from setup.
     *
     * @param className Name of the class containing the test.
     */
    public static void showRunClass(String className)
    {
        if (TestOptions.SHOW_NAMES)
        {
            System.out.println(LF + "TestClass: " + className);
        }
    }

}

