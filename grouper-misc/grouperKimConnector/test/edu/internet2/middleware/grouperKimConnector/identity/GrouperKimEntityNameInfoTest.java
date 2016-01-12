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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class GrouperKimEntityNameInfoTest extends TestCase {
  
  /**
   * 
   */
  public GrouperKimEntityNameInfoTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GrouperKimEntityNameInfoTest(String name) {
    super(name);
    
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    //TestRunner.run(GrouperKimEntityNameInfoTest.class);
    TestRunner.run(new GrouperKimEntityNameInfoTest("testGetDefaultNamesForPrincipalIds"));
  }

  /**
   * 
   */
  public void testName() {
    GrouperKimEntityNameInfo grouperKimEntityNameInfo = new GrouperKimEntityNameInfo();
    grouperKimEntityNameInfo.setFormattedName("Michael Chris Hyzer");
    assertEquals("Michael Chris Hyzer", grouperKimEntityNameInfo.getFormattedNameUnmasked());
    assertEquals("Michael Chris Hyzer", grouperKimEntityNameInfo.getFormattedName());
    assertEquals("Michael", grouperKimEntityNameInfo.getFirstName());
    assertEquals("Michael", grouperKimEntityNameInfo.getFirstNameUnmasked());
    assertEquals("Hyzer", grouperKimEntityNameInfo.getLastName());
    assertEquals("Hyzer", grouperKimEntityNameInfo.getLastNameUnmasked());
    assertEquals("Chris", grouperKimEntityNameInfo.getMiddleName());
    assertEquals("Chris", grouperKimEntityNameInfo.getMiddleNameUnmasked());
    
  }
  
  /**
   * 
   */
  public void testFirstName() {
    GrouperKimEntityNameInfo grouperKimEntityNameInfo = new GrouperKimEntityNameInfo();
    grouperKimEntityNameInfo.setFirstName("Michael");
    assertEquals("Michael", grouperKimEntityNameInfo.getFormattedName());
    assertEquals("Michael", grouperKimEntityNameInfo.getFormattedNameUnmasked());
    assertEquals("Michael", grouperKimEntityNameInfo.getFirstName());
    assertEquals("Michael", grouperKimEntityNameInfo.getFirstNameUnmasked());
    assertEquals("", grouperKimEntityNameInfo.getLastName());
    assertEquals("", grouperKimEntityNameInfo.getLastNameUnmasked());
    assertEquals("", grouperKimEntityNameInfo.getMiddleName());
    assertEquals("", grouperKimEntityNameInfo.getMiddleNameUnmasked());
    
  }
  
  /**
   * 
   */
  public void testFirstLastName() {
    GrouperKimEntityNameInfo grouperKimEntityNameInfo = new GrouperKimEntityNameInfo();
    grouperKimEntityNameInfo.setFirstName("Michael");
    grouperKimEntityNameInfo.setLastName("Hyzer");
    assertEquals("Michael Hyzer", grouperKimEntityNameInfo.getFormattedName());
    assertEquals("Michael Hyzer", grouperKimEntityNameInfo.getFormattedNameUnmasked());
    assertEquals("Michael", grouperKimEntityNameInfo.getFirstName());
    assertEquals("Michael", grouperKimEntityNameInfo.getFirstNameUnmasked());
    assertEquals("Hyzer", grouperKimEntityNameInfo.getLastName());
    assertEquals("Hyzer", grouperKimEntityNameInfo.getLastNameUnmasked());
    assertEquals("", grouperKimEntityNameInfo.getMiddleName());
    assertEquals("", grouperKimEntityNameInfo.getMiddleNameUnmasked());
    
  }
  
  /**
   * 
   */
  public void testLastName() {
    GrouperKimEntityNameInfo grouperKimEntityNameInfo = new GrouperKimEntityNameInfo();
    grouperKimEntityNameInfo.setLastName("Hyzer");
    assertEquals("Hyzer", grouperKimEntityNameInfo.getFormattedNameUnmasked());
    assertEquals("Hyzer", grouperKimEntityNameInfo.getFormattedName());
    assertEquals("", grouperKimEntityNameInfo.getFirstName());
    assertEquals("", grouperKimEntityNameInfo.getFirstNameUnmasked());
    assertEquals("Hyzer", grouperKimEntityNameInfo.getLastName());
    assertEquals("Hyzer", grouperKimEntityNameInfo.getLastNameUnmasked());
    assertEquals("", grouperKimEntityNameInfo.getMiddleName());
    assertEquals("", grouperKimEntityNameInfo.getMiddleNameUnmasked());
    
  }
  
  /**
   * 
   */
  public void testFirstAndName() {
    GrouperKimEntityNameInfo grouperKimEntityNameInfo = new GrouperKimEntityNameInfo();
    grouperKimEntityNameInfo.setFirstName("Michael");
    grouperKimEntityNameInfo.setFormattedName("Michael Hyzer");
    assertEquals("Michael Hyzer", grouperKimEntityNameInfo.getFormattedNameUnmasked());
    assertEquals("Michael Hyzer", grouperKimEntityNameInfo.getFormattedName());
    assertEquals("Michael", grouperKimEntityNameInfo.getFirstName());
    assertEquals("Michael", grouperKimEntityNameInfo.getFirstNameUnmasked());
    assertEquals("", grouperKimEntityNameInfo.getLastName());
    assertEquals("", grouperKimEntityNameInfo.getLastNameUnmasked());
    assertEquals("", grouperKimEntityNameInfo.getMiddleName());
    assertEquals("", grouperKimEntityNameInfo.getMiddleNameUnmasked());
    
  }
  
  /**
   * 
   */
  public void testLastAndName() {
    GrouperKimEntityNameInfo grouperKimEntityNameInfo = new GrouperKimEntityNameInfo();
    grouperKimEntityNameInfo.setLastName("Hyzer");
    grouperKimEntityNameInfo.setFormattedName("Michael Hyzer");
    assertEquals("Michael Hyzer", grouperKimEntityNameInfo.getFormattedNameUnmasked());
    assertEquals("Michael Hyzer", grouperKimEntityNameInfo.getFormattedName());
    assertEquals("", grouperKimEntityNameInfo.getFirstName());
    assertEquals("", grouperKimEntityNameInfo.getFirstNameUnmasked());
    assertEquals("Hyzer", grouperKimEntityNameInfo.getLastName());
    assertEquals("Hyzer", grouperKimEntityNameInfo.getLastNameUnmasked());
    assertEquals("", grouperKimEntityNameInfo.getMiddleName());
    assertEquals("", grouperKimEntityNameInfo.getMiddleNameUnmasked());
    
  }
}
