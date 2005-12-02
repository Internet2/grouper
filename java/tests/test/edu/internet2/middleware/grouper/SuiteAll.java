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

import  junit.framework.*;

public class SuiteAll extends TestCase {

  public SuiteAll(String name) {
    super(name);
  }

  static public Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTestSuite(TestBugsOpen.class);
    suite.addTestSuite(TestField.class);
    suite.addTestSuite(TestGrFiFindByName.class);
    suite.addTestSuite(TestGrFiFindByUuid.class);
    suite.addTestSuite(TestGroup.class);
    suite.addTestSuite(TestGroupAddMember.class);
    suite.addTestSuite(TestGroupAddMemberGroup.class);
    suite.addTestSuite(TestGroupDelete.class);
    suite.addTestSuite(TestGroupDeleteMember.class);
    suite.addTestSuite(TestGroupDeleteMemberGroup.class);
    suite.addTestSuite(TestGroupToMember.class);
    suite.addTestSuite(TestGQComplementFilter.class);
    suite.addTestSuite(TestGQGroupAnyAttribute.class);
    suite.addTestSuite(TestGQGroupAttribute.class);
    suite.addTestSuite(TestGQGroupCreatedAfter.class);
    suite.addTestSuite(TestGQGroupCreatedBefore.class);
    suite.addTestSuite(TestGQGroupName.class);
    suite.addTestSuite(TestGQIntersectionFilter.class);
    suite.addTestSuite(TestGQNull.class);
    suite.addTestSuite(TestGQStemCreatedAfter.class);
    suite.addTestSuite(TestGQStemCreatedBefore.class);
    suite.addTestSuite(TestGQStemName.class);
    suite.addTestSuite(TestGQUnionFilter.class);
    suite.addTestSuite(TestGrouperAccessADMIN.class);
    suite.addTestSuite(TestGrouperAccessOPTIN.class);
    suite.addTestSuite(TestGrouperAccessOPTOUT.class);
    suite.addTestSuite(TestGrouperAccessREAD.class);
    suite.addTestSuite(TestGrouperAccessUPDATE.class);
    suite.addTestSuite(TestGrouperAccessVIEW.class);
    suite.addTestSuite(TestGrouperNamingCREATE.class);
    suite.addTestSuite(TestGrouperNamingSTEM.class);
    suite.addTestSuite(TestGrouperSession.class);
    suite.addTestSuite(TestGrouperSourceAdapter.class);
    suite.addTestSuite(TestInternalSourceAdapter.class);
    suite.addTestSuite(TestMemberToGroup.class);
    suite.addTestSuite(TestMeFiFindBySubject.class);
    suite.addTestSuite(TestPrivGroupSTEM.class);
    suite.addTestSuite(TestPrivCREATE.class);
    suite.addTestSuite(TestPrivSTEM.class);
    suite.addTestSuite(TestStem.class);
    suite.addTestSuite(TestStemAddChildGroup.class);
    suite.addTestSuite(TestStemAddChildStem.class);
    suite.addTestSuite(TestStFiFindRootStem.class);
    suite.addTestSuite(TestSuFiInSoAdFindById.class);
    suite.addTestSuite(TestSuFiInSoAdFindByIdentifier.class);
    suite.addTestSuite(TestSuFiGrSoAdFindById.class);
    suite.addTestSuite(TestSuFiGrSoAdFindByIdentifier.class);
    suite.addTestSuite(TestSubjectFinderInternal.class);

    return suite;
  }

}

