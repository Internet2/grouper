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

/**
 * @author  blair christensen.
 * @version $Id: Test_I_API_Stem_internal_addChildGroup.java,v 1.1 2007-05-21 18:43:56 blair Exp $
 * @since   1.2.0
 */
public class Test_I_API_Stem_internal_addChildGroup extends GrouperTest {

  // PRIVATE INSTANCE VARIABLES //
  private Stem            parent;
  private GrouperSession  s;


  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {
      // TODO 20070521 this *really* cries out for an object mother     
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperRuntimeException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
  }

  public void tearDown() {
    try {
      s.stop();
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperRuntimeException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
    super.tearDown();
  }


  // TESTS //

  /**
   * Verify that <i>Group</i> is assigned UUID if null UUID argument.
   * @since   1.2.0
   */
  public void test_internal_addChildGroup_assignUuidIfNullUuidArgument() {
    try {
      Group             g = parent.internal_addChildGroup("g", "g", null);
      GrouperValidator  v = NotNullOrEmptyValidator.validate( g.getUuid() );
      assertTrue( "assigned uuid when null uuid arg", v.isValid() );
    }
    catch (GroupAddException eGA) {
      fail( eGA.getMessage() );
    }
    catch (InsufficientPrivilegeException eIP) {
      fail( eIP.getMessage() );
    }
  }
    
  /**
   * Verify that <i>Group</i> is assigned UUID if blank UUID argument.
   * @since   1.2.0
   */
  public void test_internal_addChildGroup_assignUuidIfBlankUuidArgument() {
    try {
      Group             g = parent.internal_addChildGroup( "g", "g", GrouperConfig.EMPTY_STRING );
      GrouperValidator  v = NotNullOrEmptyValidator.validate( g.getUuid() );
      assertTrue( "assigned uuid when blank uuid arg", v.isValid() );
    }
    catch (GroupAddException eGA) {
      fail( eGA.getMessage() );
    }
    catch (InsufficientPrivilegeException eIP) {
      fail( eIP.getMessage() );
    }
  }
    
} 

