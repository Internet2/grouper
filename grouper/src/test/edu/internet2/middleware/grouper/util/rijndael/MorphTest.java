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
 * $Id: MorphTest.java,v 1.5 2009-03-20 19:56:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util.rijndael;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.morphString.Morph;



/**
 * test morphing
 */
public class MorphTest extends GrouperTest {

  /**
   * @param name
   */
  public MorphTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public MorphTest() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(MorphTest.class);
  }

  /**
   * 
   */
  public void testMorph() {
    Morph.testMorphKey = "ert234mN54";
    String morphed = Morph.encrypt("whatever");
    assertFalse(StringUtils.equals(morphed, "whatever"));
    String unmorphed = Morph.decrypt(morphed);
    assertEquals(unmorphed, "whatever");
  }
  
}
