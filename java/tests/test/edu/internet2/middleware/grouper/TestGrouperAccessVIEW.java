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
 * Test {@link GrouperAccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperAccessVIEW.java,v 1.2 2005-11-15 18:23:28 blair Exp $
 */
public class TestGrouperAccessVIEW extends TestCase {

  // Private Class Variables
  Stem            edu;
  Group           i2;
  Stem            root;
  GrouperSession  s;


  public TestGrouperAccessVIEW(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.getRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testDefaultPrivs() {
    PrivHelper.getPrivs(
      s, i2, s.getSubject(),       0, true,  true, true, true, true, true
    );
    PrivHelper.getPrivs(
      s, i2, SubjectHelper.SUBJ0,  0, false, false, false, false, false, false
    );
    PrivHelper.getPrivs(
      s, i2, SubjectHelper.SUBJ1,  0, false, false, false, false, false, false
    );
  } // public void testDefaultPrivs()

}

