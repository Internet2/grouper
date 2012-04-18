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
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.util;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Class for testing the LdapUtil class.
 */
public class LdapUtilTest extends TestCase {

  /**
   * Constructor
   */
  public LdapUtilTest(String name) {
    super(name);
  }

  /**
   * Tear down the fixture.
   */
  protected void tearDown() {
  }

  /**
   * The main method for running the test.
   */
  public static void main(String args[]) {
    TestRunner.run(LdapUtilTest.class);
  }

  /**
   * A test the makeLdapNameSafe method with no character to escape
   */
  public void testMakeLdapNameSafeNoEscaping() {

    String ldapName = "noCharactersToEsape";
    String safeLdapName = ldapName;
    assertEquals(LdapUtil.makeLdapNameSafe(ldapName), safeLdapName);
  }

  /**
   * A test the makeLdapNameSafe method with leading spaces and #
   */
  public void testMakeLdapNameSafeLeadingSpaces() {

    String ldapName = " # # ##  leadingToEscape";
    String safeLdapName = "\\ \\#\\ \\#\\ \\#\\#\\ \\ leadingToEscape";
    assertEquals(LdapUtil.makeLdapNameSafe(ldapName), safeLdapName);
  }

  /**
   * A test the makeLdapNameSafe method with trailing spaces to escape
   */
  public void testMakeLdapNameSafeTrailingSpaces() {

    String ldapName = "trailingSpaces     ";
    String safeLdapName = "trailingSpaces\\ \\ \\ \\ \\ ";
    assertEquals(LdapUtil.makeLdapNameSafe(ldapName), safeLdapName);
  }

  /**
   * A test the makeLdapNameSafe method with special characters
   */
  public void testMakeLdapNameSafeSpecialCharacters() {

    String ldapName = "s,p+e\"c\\i<a>l;";
    String safeLdapName = "s\\,p\\+e\\\"c\\\\i\\<a\\>l\\;";
    assertEquals(LdapUtil.makeLdapNameSafe(ldapName), safeLdapName);
  }

  /**
   * A test the makeLdapNameSafe method with leading, trailing and special characters
   */
  public void testMakeLdapNameSafeAll() {

    String ldapName = "##  ,+\"   \\<>;    ";
    String safeLdapName = "\\#\\#\\ \\ \\,\\+\\\"   \\\\\\<\\>\\;\\ \\ \\ \\ ";
    assertEquals(LdapUtil.makeLdapNameSafe(ldapName), safeLdapName);
  }

  /**
   * A test the makeLdapNameSafe method with null
   */
  public void testMakeLdapNameSafeNull() {

    String ldapName = null;
    String safeLdapName = null;
    assertTrue(LdapUtil.makeLdapNameSafe(ldapName) == safeLdapName);
  }

  /**
   * A test the makeLdapNameSafe method with empty string
   */
  public void testMakeLdapNameSafeEmpty() {

    String ldapName = "";
    String safeLdapName = ldapName;
    assertEquals(LdapUtil.makeLdapNameSafe(ldapName), safeLdapName);
  }

  /**
   * A test the makeLdapNameSafe method with all spaces
   */
  public void testMakeLdapNameSafeAllSpaces() {

    String ldapName = "            ";
    String safeLdapName = "\\ \\ \\ \\ \\ \\ \\ \\ \\ \\ \\ \\ ";
    assertEquals(LdapUtil.makeLdapNameSafe(ldapName), safeLdapName);
  }

  /**
   * A test of the makeLdapFilterValueSafe method with nothing to escape
   */
  public void testMakeLdapFilterValueSafeNoEscape() {

    String ldapFilterValue = "nothing To Escape";
    String safeLdapFilterValue = ldapFilterValue;
    assertEquals(LdapUtil.makeLdapFilterValueSafe(ldapFilterValue), safeLdapFilterValue);
  }

  /**
   * A test of the makeLdapFilterValueSafe method with '*','(',')' and '\u0000' to escape
   */
  public void testMakeLdapFilterValueSafeEscapeAll() {

    String ldapFilterValue = "(*nothing \\To Escape\u0000)";
    String safeLdapFilterValue = "\\28\\2anothing \\5cTo Escape\\00\\29";
    assertEquals(LdapUtil.makeLdapFilterValueSafe(ldapFilterValue), safeLdapFilterValue);
  }

  /**
   * Test forward slashes
   */
  public void testForwardSlash() {

    String ldapName = "group/C";
    String safeLdapName = "group\\/C";

    assertEquals(safeLdapName, LdapUtil.escapeForwardSlash(ldapName));
    assertEquals(safeLdapName, LdapUtil.escapeForwardSlash(safeLdapName));

    assertEquals(ldapName, LdapUtil.unescapeForwardSlash(safeLdapName));
    assertEquals(ldapName, LdapUtil.unescapeForwardSlash(ldapName));
  }
}
