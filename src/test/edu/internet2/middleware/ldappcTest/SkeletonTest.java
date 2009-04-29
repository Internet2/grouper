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

package edu.internet2.middleware.ldappcTest;

/**
 * SkeletonTest.java Template used to create other tests
 */

import junit.framework.TestCase;

/**
 * A convenience class for starting a set of test cases. It can also be used as a template
 * for other test classes.
 */
public class SkeletonTest extends BaseLdappcTestCase {

  /**
   * Class constructor
   * 
   * @param name
   *          Name of the test case.
   */
  public SkeletonTest(String name) {
    super(name);
  }

  /**
   * Set up the fixture.
   */
  protected void setUp() {
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
    junit.textui.TestRunner.run(SkeletonTest.class);
  }

  /**
   * A sanity test -- must always be okay or something is drastically wrong.
   */
  public void testAssert() {
    assertTrue(true);
  }
}
