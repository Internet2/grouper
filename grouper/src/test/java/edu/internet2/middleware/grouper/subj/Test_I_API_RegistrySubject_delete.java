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

package edu.internet2.middleware.grouper.subj;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.subject.Subject;

/**
 * Test <code>Group.delete()</code>.
 * @author  blair christensen.
 * @version $Id: Test_I_API_RegistrySubject_delete.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_I_API_RegistrySubject_delete extends GrouperTest {

  /**
   * 
   */
  public Test_I_API_RegistrySubject_delete() {
    super();
  }

  /**
   * @param name
   */
  public Test_I_API_RegistrySubject_delete(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new Test_I_API_RegistrySubject_delete("test_delete_failOnNullSession"));
    //TestRunner.run(Test_I_API_RegistrySubject_delete.class);
  }

  // PRIVATE INSTANCE VARIABLES //
  private RegistrySubject rSubjX;
  private GrouperSession  s;
  private Subject         subjX;


  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      rSubjX  = RegistrySubject.add(s, "subjX", "person", "subjX");
      subjX   = SubjectFinder.findById( rSubjX.getId(), true );
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
  }

  public void tearDown() {
    try {
      s.stop();
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
    super.tearDown();
  }


  // TESTS //

  /**
   * Pass in a null <i>GrouperSession</i>.
   * @since   1.2.0
   */
  public void test_delete_failOnNullSession()
    throws  GrouperException,
            InsufficientPrivilegeException
  {
    try {
      rSubjX.delete(null);
      fail("failed to throw expected IllegalStateException");
    }
    catch (IllegalStateException eExpected) {
      assertTrue("threw expected IllegalStateException", true);
    }
  }

  /**
   * Fail to delete an existing <i>RegistrySubject</i> when not root-like.
   * @since   1.2.0
   */
  public void test_delete_failToDeleteWhenNotRoot() 
    throws  GrouperException,
            SessionException
  {
    GrouperSession nrs = GrouperSession.start(subjX);
    try {
      rSubjX.delete(nrs);
      fail("failed to throw expected InsufficientPrivilegeException");
    }
    catch (InsufficientPrivilegeException eExpected) {
      assertTrue("threw expected InsufficientPrivilegeException", true);
    }
    finally {
      nrs.stop();
    }
  }

  /**
   * Delete an existing <i>RegistrySubject</i>.
   * @since   1.2.0
   */
  public void test_delete_ok() 
    throws  GrouperException,
            InsufficientPrivilegeException
  {
    
    rSubjX.delete(s);
    assertTrue("deleted registry subject", true);
  }

} 

