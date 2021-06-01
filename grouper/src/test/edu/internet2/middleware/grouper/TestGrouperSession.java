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
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import junit.textui.TestRunner;


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
    TestRunner.run(new TestGrouperSession("testStaticSession"));
  }
  
  private GrouperSession  s;
  private String          prop_valid    = "privileges.access.interface";
  private String          prop_invalid  = "invalid property";


  public void tearDown() {
    super.tearDown();
  }

  public void testStaticSession() {
    
    GrouperSession grouperSessionTest = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    assertTrue(SubjectHelper.eq(SubjectTestHelper.SUBJ0, GrouperSession.staticGrouperSession().getSubject()));
    
    GrouperSession grouperSessionSystem = GrouperSession.start(SubjectFinder.findRootSubject());
    
    assertTrue(SubjectHelper.eq(SubjectFinder.findRootSubject(), GrouperSession.staticGrouperSession().getSubject()));

    grouperSessionSystem.stop();
    
    grouperSessionTest.stop();
  }
  
  
  public void test_getAccessClass_notNull() {
    try {
      this.s    = GrouperSession.start(SubjectFinder.findAllSubject() );
      assertNotNull( this.s.getAccessClass() );
    } catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    } finally {
      GrouperSession.stopQuietly(this.s);
    }
  }

  public void test_getAccessClass_defaultAccessAdapter() {
    try {
      this.s    = GrouperSession.start(SubjectFinder.findAllSubject() );
      assertEquals( GrouperAccessAdapter.class.getName(), this.s.getAccessClass() );
    } catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    } finally {
      GrouperSession.stopQuietly(this.s);
    }
  }

  /**
   * @since   1.2.1
   */
  public void test_getAccessResolver_notNull() {
    try {
      this.s    = GrouperSession.start(SubjectFinder.findAllSubject() );
      assertNotNull( this.s.getAccessResolver() );
    } catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    } finally {
      GrouperSession.stopQuietly(this.s);
    }
  }
  /**
   * @since   1.2.1
   */
  public void test_getAccessResolver_equals() {
    try {
      this.s    = GrouperSession.start(SubjectFinder.findAllSubject() );
      assertEquals(
          this.s.getAccessResolver(),
          this.s.getAccessResolver()
        );
    } catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    } finally {
      GrouperSession.stopQuietly(this.s);
    }
  }



  public void test_getNamingClass_notNull() {
    try {
      this.s    = GrouperSession.start(SubjectFinder.findAllSubject() );
      assertNotNull( this.s.getNamingClass() );
    } catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    } finally {
      GrouperSession.stopQuietly(this.s);
    }
  }

  public void test_getNamingClass_defaultAccessAdapter() {
    try {
      this.s    = GrouperSession.start(SubjectFinder.findAllSubject() );
      assertEquals( GrouperNamingAdapter.class.getName(), this.s.getNamingClass() );
    } catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    } finally {
      GrouperSession.stopQuietly(this.s);
    }
  }


  /**
   * @since   1.2.1
   */
  public void test_getNamingResolver_notNull() {
    try {
      this.s    = GrouperSession.start(SubjectFinder.findAllSubject() );
      assertNotNull( this.s.getNamingResolver() );
    } catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    } finally {
      GrouperSession.stopQuietly(this.s);
    }
  }
  /**
   * @since   1.2.1
   */
  public void test_getNamingResolver_equals() {
    try {
      this.s    = GrouperSession.start(SubjectFinder.findAllSubject() );
      assertEquals(
          this.s.getNamingResolver(),
          this.s.getNamingResolver()
        );
    } catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    } finally {
      GrouperSession.stopQuietly(this.s);
    }
  }

}

