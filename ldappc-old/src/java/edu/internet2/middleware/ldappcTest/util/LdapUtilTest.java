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

package edu.internet2.middleware.ldappcTest.util;

import junit.framework.TestCase;


import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappcTest.DisplayTest;

/**
 * Class for testing the LdapUtil class.
 */
public class LdapUtilTest extends TestCase
{
    /**
     * Constructor
     */
    public LdapUtilTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName()); 
    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown() 
    {
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[]) 
    {
         junit.textui.TestRunner.run(LdapUtilTest.class);
    }
    
    /**
     * A test the makeLdapNameSafe method with no character to escape
     */
    public void testMakeLdapNameSafeNoEscaping() 
    {
        DisplayTest.showRunTitle("testMakeLdapNameSafeNoEscaping", "No character to escape");
    
        String ldapName = "noCharactersToEsape";
        String safeLdapName = ldapName;
        assertEquals( LdapUtil.makeLdapNameSafe(ldapName),safeLdapName);
    }
    
    /**
     * A test the makeLdapNameSafe method with leading spaces and #
     */
    public void testMakeLdapNameSafeLeadingSpaces() 
    {
        DisplayTest.showRunTitle("testMakeLdapNameSafeLeadingSpaces", "Leading spaces and # to escape");
    
        String ldapName = " # # ##  leadingToEscape";
        String safeLdapName = "\\ \\#\\ \\#\\ \\#\\#\\ \\ leadingToEscape";
        assertEquals( LdapUtil.makeLdapNameSafe(ldapName),safeLdapName);
    }
    
    /**
     * A test the makeLdapNameSafe method with trailing spaces to escape
     */
    public void testMakeLdapNameSafeTrailingSpaces() 
    {
        DisplayTest.showRunTitle("testMakeLdapNameSafeTrailingSpaces", "Trailing spaces to escape");
    
        String ldapName = "trailingSpaces     ";
        String safeLdapName = "trailingSpaces\\ \\ \\ \\ \\ ";
        assertEquals( LdapUtil.makeLdapNameSafe(ldapName),safeLdapName);
    }
    
    /**
     * A test the makeLdapNameSafe method with special characters
     */
    public void testMakeLdapNameSafeSpecialCharacters() 
    {
        DisplayTest.showRunTitle("testMakeLdapNameSafeSpecialCharacters", "Special characters to escape");
    
        String ldapName = "s,p+e\"c\\i<a>l;";
        String safeLdapName = "s\\,p\\+e\\\"c\\\\i\\<a\\>l\\;";
        assertEquals( LdapUtil.makeLdapNameSafe(ldapName),safeLdapName);
    }
    
    /**
     * A test the makeLdapNameSafe method with leading, trailing and special characters
     */
    public void testMakeLdapNameSafeAll() 
    {
        DisplayTest.showRunTitle("testMakeLdapNameSafeAll", "Leading spaces, trailing spaces, and special characters to escape");
    
        String ldapName = "##  ,+\"   \\<>;    ";
        String safeLdapName = "\\#\\#\\ \\ \\,\\+\\\"   \\\\\\<\\>\\;\\ \\ \\ \\ ";
        assertEquals( LdapUtil.makeLdapNameSafe(ldapName),safeLdapName);
    }
    
    /**
     * A test the makeLdapNameSafe method with null
     */
    public void testMakeLdapNameSafeNull() 
    {
        DisplayTest.showRunTitle("testMakeLdapNameSafeNull", "Passing in null");
    
        String ldapName = null;
        String safeLdapName = null;
        assertTrue( LdapUtil.makeLdapNameSafe(ldapName)==safeLdapName);
    }
    
    /**
     * A test the makeLdapNameSafe method with empty string
     */
    public void testMakeLdapNameSafeEmpty() 
    {
        DisplayTest.showRunTitle("testMakeLdapNameSafeEmpty", "Passing in an empty string");
    
        String ldapName = "";
        String safeLdapName = ldapName;
        assertEquals( LdapUtil.makeLdapNameSafe(ldapName),safeLdapName);
    }
    
    /**
     * A test the makeLdapNameSafe method with all spaces
     */
    public void testMakeLdapNameSafeAllSpaces() 
    {
        DisplayTest.showRunTitle("testMakeLdapNameSafeAllSpaces", "Passing in an all spaces string");
    
        String ldapName = "            ";
        String safeLdapName = "\\ \\ \\ \\ \\ \\ \\ \\ \\ \\ \\ \\ ";
        assertEquals( LdapUtil.makeLdapNameSafe(ldapName),safeLdapName);
    }
    
    /**
     * A test of the makeLdapFilterValueSafe method with nothing to escape
     */
    public void testMakeLdapFilterValueSafeNoEscape()
    {
        DisplayTest.showRunTitle("testMakeLdapFilterValueSafeNoEscape", "Passing in string with nothing to escape");
        
        String ldapFilterValue = "nothing To Escape";
        String safeLdapFilterValue = ldapFilterValue;
        assertEquals( LdapUtil.makeLdapFilterValueSafe(ldapFilterValue),safeLdapFilterValue);
    }
    
    /**
     * A test of the makeLdapFilterValueSafe method with '*','(',')' and '\u0000' to escape
     */
    public void testMakeLdapFilterValueSafeEscapeAll()
    {
        DisplayTest.showRunTitle("testMakeLdapFilterValueSafeEscapeAll", "Passing in string with all 4 characters to escape");
        
        String ldapFilterValue = "(*nothing \\To Escape\u0000)";
        String safeLdapFilterValue = "\\28\\2anothing \\5cTo Escape\\00\\29";
        assertEquals( LdapUtil.makeLdapFilterValueSafe(ldapFilterValue),safeLdapFilterValue);
    }
}
