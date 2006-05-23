/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test {@link SubjectFinder.findById()} with {@link InternalSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSuFiInSoAdFindById.java,v 1.2 2006-05-23 19:10:23 blair Exp $
 */
public class TestSuFiInSoAdFindById extends TestCase {

  public TestSuFiInSoAdFindById(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testFindByIdBadId() {
    SubjectTestHelper.getSubjectByBadId(Helper.BAD_SUBJ_ID);
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdBadId()

  public void testFindByIdGoodIdBadType() {
    SubjectTestHelper.getSubjectByBadIdType(SubjectTestHelper.SUBJ_ROOT, "person");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdGoodIdBadType()

  public void testFindByIdGoodIdAll() {
    Subject subj = SubjectTestHelper.getSubjectById(SubjectTestHelper.SUBJ_ALL);
    Assert.assertTrue("found all subject", true);
  } // public void testFindByIdGoodIdAll()

  public void testFindByIdGoodIdGoodTypeAll() {
    Subject subj = SubjectTestHelper.getSubjectByIdType(
      SubjectTestHelper.SUBJ_ALL, "application"
    );
    Assert.assertTrue("found all subject", true);
  } // public void testFindByIdGoodIdGoodTypeAll()

  public void testFindByIdGoodIdRoot() {
    Subject subj = SubjectTestHelper.getSubjectById(SubjectTestHelper.SUBJ_ROOT);
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdGoodIdRoot()

  public void testFindByIdGoodIdGoodTypeRoot() {
    Subject subj = SubjectTestHelper.getSubjectByIdType(
      SubjectTestHelper.SUBJ_ROOT, "application"
    );
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdGoodIdGoodTypeRoot()

}

