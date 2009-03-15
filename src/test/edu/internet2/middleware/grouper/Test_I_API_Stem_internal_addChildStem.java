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

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.NotNullOrEmptyValidator;

/**
 * @author  blair christensen.
 * @version $Id: Test_I_API_Stem_internal_addChildStem.java,v 1.4 2009-03-15 06:37:22 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_I_API_Stem_internal_addChildStem extends GrouperTest {

  // PRIVATE INSTANCE VARIABLES //
  private Stem            parent;
  private GrouperSession  s;


  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {   
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
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
   * Verify that <i>Stem</i> is assigned UUID if null UUID argument.
   * @since   1.2.0
   */
  public void test_internal_addChildStem_assignUuidIfNullUuidArgument() {
    try {
      Stem              ns  = parent.internal_addChildStem("ns", "ns", null);
      GrouperValidator  v   = NotNullOrEmptyValidator.validate( ns.getUuid() );
      assertTrue( "assigned uuid when null uuid arg", v.isValid() );
    }
    catch (InsufficientPrivilegeException eIP) {
      fail( eIP.getMessage() );
    }
    catch (StemAddException eNSA) {
      fail( eNSA.getMessage() );
    }
  }
    
  /**
   * Verify that <i>Stem</i> is assigned UUID if blank UUID argument.
   * @since   1.2.0
   */
  public void test_internal_addChildStem_assignUuidIfBlankUuidArgument() {
    try {
      Stem              ns  = parent.internal_addChildStem( "ns", "ns", GrouperConfig.EMPTY_STRING );
      GrouperValidator  v   = NotNullOrEmptyValidator.validate( ns.getUuid() );
      assertTrue( "assigned uuid when blank uuid arg", v.isValid() );
    }
    catch (InsufficientPrivilegeException eIP) {
      fail( eIP.getMessage() );
    }
    catch (StemAddException eNSA) {
      fail( eNSA.getMessage() );
    }
  }
    
} 

