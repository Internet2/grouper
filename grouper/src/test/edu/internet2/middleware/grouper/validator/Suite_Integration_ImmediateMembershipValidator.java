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
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.validator;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author  blair christensen.
 * @version $Id: Suite_Integration_ImmediateMembershipValidator.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.0
 */
public class Suite_Integration_ImmediateMembershipValidator extends GrouperTest {

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(Test_Integration_ImmediateMembershipValidator_validate.class);
    return suite;
  } // static public Test suite()

} // public class Suite_Integration_ImmediateMembershipValidator extends GrouperTest

