/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  junit.framework.*;

/**
 * Test {@link GrouperNamingPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperNamingSTEM.java,v 1.1 2005-11-14 18:35:39 blair Exp $
 */
public class TestGrouperNamingSTEM extends TestCase {

  public TestGrouperNamingSTEM(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testAddChildStemAtRoot() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.getRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Assert.assertFalse(
      "root !STEM", edu.hasStem( s.getSubject() )
    );
    Assert.assertFalse(
      "subj0 !STEM", edu.hasStem( SubjectHelper.SUBJ0 )
    );
    Assert.assertFalse(
      "subj1 !STEM", edu.hasStem( SubjectHelper.SUBJ1 )
    );
  } // public void testAddChildStemAtRoot()

}

