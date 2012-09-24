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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 * Test {@link GrouperSession}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: TestGrouperSession.java,v 1.9 2009-08-11 20:34:18 mchyzer Exp $
 * @since   1.2.1
 */
public class TestGrouperSession extends GrouperTest {


  /**
   * 
   */
  public TestGrouperSession() {
    super();
  }

  /**
   * @param name
   */
  public TestGrouperSession(String name) {
    super(name);
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGrouperSession("test_getAccessClass_notNull"));
  }
  
  private GrouperSession  s;
  private String          prop_valid    = "privileges.access.interface";
  private String          prop_invalid  = "invalid property";


  public void setUp() {
    super.setUp();
    try {
      this.s    = GrouperSession.start( SubjectFinder.findAllSubject() );
    }
    catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
    this.s.stop();
  }


  public void test_getAccessClass_notNull() {
    assertNotNull( this.s.getAccessClass() );
  }

  public void test_getAccessClass_defaultAccessAdapter() {
    assertEquals( GrouperConfig.retrieveConfig().propertyValueString(GrouperConfig.ACCESS_PRIVILEGE_INTERFACE), this.s.getAccessClass() );
  }

  /**
   * @since   1.2.1
   */
  public void test_getAccessResolver_notNull() {
    assertNotNull( this.s.getAccessResolver() );
  }
  /**
   * @since   1.2.1
   */
  public void test_getAccessResolver_equals() {
    assertEquals(
      this.s.getAccessResolver(),
      this.s.getAccessResolver()
    );
  }



  public void test_getNamingClass_notNull() {
    assertNotNull( this.s.getNamingClass() );
  }

  public void test_getNamingClass_defaultAccessAdapter() {
    assertEquals( GrouperConfig.retrieveConfig().propertyValueString(GrouperConfig.NAMING_PRIVILEGE_INTERFACE), this.s.getNamingClass() );
  }


  /**
   * @since   1.2.1
   */
  public void test_getNamingResolver_notNull() {
    assertNotNull( this.s.getNamingResolver() );
  }
  /**
   * @since   1.2.1
   */
  public void test_getNamingResolver_equals() {
    assertEquals(
      this.s.getNamingResolver(),
      this.s.getNamingResolver()
    );
  }

}

