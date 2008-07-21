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
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import  junit.framework.*;

/**
 * Test {@link SubjectFinder.findByIdentifier()} with {@link InternalSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSuFiInSoAdFindByIdfr.java,v 1.10 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestSuFiInSoAdFindByIdfr extends TestCase {

  public TestSuFiInSoAdFindByIdfr(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testFindByIdentifierBadId() {
    SubjectTestHelper.getSubjectByBadId("i do not exist");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdentifierBadId()

  public void testFindByIdentifierGoodIdBadType() {
    SubjectTestHelper.getSubjectByBadIdType(SubjectTestHelper.SUBJ_ROOT, "person");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdentifierGoodIdBadType()

  public void testFindByIdentifierGoodIdRoot() {
    SubjectTestHelper.getSubjectByIdentifier(SubjectTestHelper.SUBJ_ROOT);
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdentifierGoodIdRoot()

  public void testFindByIdentifierGoodIdGoodTypeRoot() {
    SubjectTestHelper.getSubjectByIdentifierType(
      SubjectTestHelper.SUBJ_ROOT, "application"
    );
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdentifierGoodIdGoodTypeRoot()

}

