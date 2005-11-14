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
 * Test {@link MemberFinder.findBySubject()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMeFiFindBySubject.java,v 1.3 2005-11-14 17:35:35 blair Exp $
 */
public class TestMeFiFindBySubject extends TestCase {

  public TestMeFiFindBySubject(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

/*
  public void testFindBySubjectBadSession() {
    Helper.getMemberBySubjectBad(
      null, SubjectHelper.getSubjectById(Helper.GOOD_SUBJ_ID)
    );
    Assert.assertTrue("failed to find bad member", true);
  } // public void testFindBySubjectBadSession()
*/

/*
  public void testFindBySubjectBadSubject() {
    Helper.getMemberBySubjectBad(
      SessionHelper.getRootSession(), null
    );
    Assert.assertTrue("failed to find bad member", true);
  } // public void testFindBySubjectBadSubject()
*/

  public void testFindBySubject() {
    GrouperSession  s   = SessionHelper.getRootSession();
    String          id  = "GrouperSystem";
    Member          m   = Helper.getMemberBySubject(
      s, SubjectHelper.getSubjectById(id)
    );
    Assert.assertTrue("found member", true);
    if (s.getMember().equals(m)) {
      Assert.assertTrue("s.getMember().equals(m)", true);
    } 
    else {
      Assert.fail("s.getMember().equals(m)");
    }
  } // public void testFindBySubject()

}

