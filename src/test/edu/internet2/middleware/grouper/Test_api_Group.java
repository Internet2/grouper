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

import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;


/**
 * Test {@link Group}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_Group.java,v 1.4 2008-07-21 04:43:57 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_api_Group extends GrouperTest {


  private Group           top_group;
  private GrouperSession  s;
  private Stem            child, root, top;



  public void setUp() {
    super.setUp();
    try {
      this.s            = GrouperSession.start( SubjectFinder.findRootSubject() );
      this.root         = StemFinder.findRootStem(this.s);
      this.top          = this.root.addChildStem("top", "top");
      this.top_group    = this.top.addChildGroup("top group", "top group");
      this.child        = this.top.addChildStem("child", "child");
      this.child.addChildGroup("child group", "child group");
    }
    catch (Exception e) {
      throw new GrouperRuntimeException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }



  /**
   * @since   1.2.1
   */
  public void test_revokePriv_Privilege_namingPrivilege() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException
  {
    try {
      this.top_group.revokePriv(NamingPrivilege.STEM);
      fail("failed to throw expected SchemaException");
    }
    catch (SchemaException eExpected) {
      assertTrue(true);
    }
  }



  /**
   * @since   1.2.1
   */
  public void test_revokePriv_SubjectAndPrivilege_namingPrivilege() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException
  {
    try {
      this.top_group.revokePriv( SubjectFinder.findAllSubject(), NamingPrivilege.STEM );
      fail("failed to throw expected SchemaException");
    }
    catch (SchemaException eExpected) {
      assertTrue(true);
    }
  }

}

