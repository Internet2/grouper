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

package test.edu.internet2.middleware.grouper;

import  junit.framework.*;

public class SuiteAll extends TestCase {

  public SuiteAll(String name) {
    super(name);
  }

  static public Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTestSuite(TestBugsClosed.class);
    suite.addTestSuite(TestBugsOpen.class);

    suite.addTestSuite(TestAccessPrivilege.class);
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
    suite.addTestSuite(TestGrouperConfig.class);
    suite.addTestSuite(TestGroupTypes.class);
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
    // TODO Are the _TestGrouperAccess*_ and _TestGrouperNaming*_
    //      classes still relevant now that I have the _TestPriv*_ 
    //      classes?
    // suite.addTestSuite(TestGrouperAccessADMIN.class);
    // suite.addTestSuite(TestGrouperAccessOPTIN.class);
    // suite.addTestSuite(TestGrouperAccessOPTOUT.class);
    // suite.addTestSuite(TestGrouperAccessREAD.class);
    // suite.addTestSuite(TestGrouperAccessUPDATE.class);
    // suite.addTestSuite(TestGrouperAccessVIEW.class);
    // suite.addTestSuite(TestGrouperNamingCREATE.class);
    // suite.addTestSuite(TestGrouperNamingSTEM.class);
    suite.addTestSuite(TestGrouperSession.class);
    suite.addTestSuite(TestGrouperSourceAdapter.class);
    suite.addTestSuite(TestGrouperSubject.class);
    suite.addTestSuite(TestInternalSourceAdapter.class);
    suite.addTestSuite(TestMember.class);
    suite.addTestSuite(TestMemberFinder.class);
    suite.addTestSuite(TestMemberToGroup.class);
    suite.addTestSuite(TestMemberOf.class);
    // TODO suite.addTestSuite(TestMemberOfAccessPrivs.class);
    // TODO suite.addTestSuite(TestMemberOfNamingPrivs.class);
    suite.addTestSuite(TestMembership.class);
    suite.addTestSuite(TestNamingPrivilege.class);
    suite.addTestSuite(TestPrivADMIN.class);    // TODO group-with-priv checks
    suite.addTestSuite(TestPrivCREATE.class);  
    suite.addTestSuite(TestPrivOPTIN.class);    // TODO group-with-priv checks
    suite.addTestSuite(TestPrivOPTOUT.class);   // TODO group-with-priv checks
    suite.addTestSuite(TestPrivREAD.class);     // TODO group-with-priv checks
    suite.addTestSuite(TestPrivSTEM.class);     // TODO group-with-priv checks
    suite.addTestSuite(TestPrivVIEW.class);     // TODO group-with-priv checks
    suite.addTestSuite(TestPrivUPDATE.class);   // TODO group-with-priv checks
    suite.addTestSuite(TestStem.class);
    suite.addTestSuite(TestStemAddChildGroup.class);
    suite.addTestSuite(TestStemAddChildStem.class);
    suite.addTestSuite(TestStemFinder.class);
    suite.addTestSuite(TestSuFiInSoAdFindById.class);
    suite.addTestSuite(TestSuFiInSoAdFindByIdfr.class);
    suite.addTestSuite(TestSuFiInSoAdSearch.class);
    suite.addTestSuite(TestSuFiGrSoAdFindById.class);
    suite.addTestSuite(TestSuFiGrSoAdFindByIdfr.class);
    suite.addTestSuite(TestSuFiGrSoAdSearch.class);
    suite.addTestSuite(TestSubjectFinderInternal.class);  // TODO Hrm...
    suite.addTestSuite(TestWrongFieldType.class); 
    suite.addTestSuite(TestTxDaemon.class);
    // MANUAL suite.addTestSuite(TestWheelGroup.class); 

    return suite;
  }

}

