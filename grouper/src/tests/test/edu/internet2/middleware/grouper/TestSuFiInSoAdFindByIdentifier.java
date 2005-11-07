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
import  java.util.*;
import  junit.framework.*;

/**
 * Test {@link SubjectFinder.findByIdentifier()} with {@link InternalSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSuFiInSoAdFindByIdentifier.java,v 1.1.2.1 2005-11-07 17:46:06 blair Exp $
 */
public class TestSuFiInSoAdFindByIdentifier extends TestCase {

  public TestSuFiInSoAdFindByIdentifier(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testFindByIdentifierBadId() {
    SubjectHelper.getSubjectByBadId(Helper.BAD_SUBJ_ID);
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdentifierBadId()

  public void testFindByIdentifierGoodIdBadType() {
    SubjectHelper.getSubjectByBadIdType(Helper.GOOD_SUBJ_ID, "person");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdentifierGoodIdBadType()

  public void testFindByIdentifierGoodId() {
    Subject subj = SubjectHelper.getSubjectByIdentifier(Helper.GOOD_SUBJ_ID);
    Assert.assertTrue("found subject", true);
  } // public void testFindByIdentifierGoodId()

  public void testFindByIdentifierGoodIdGoodType() {
    Subject subj = SubjectHelper.getSubjectByIdentifierType(
      Helper.GOOD_SUBJ_ID, "application"
    );
    Assert.assertTrue("found subject", true);
  } // public void testFindByIdentifierGoodIdGoodType()

}

