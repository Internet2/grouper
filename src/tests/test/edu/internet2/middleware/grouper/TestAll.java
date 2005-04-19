/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;


public class TestAll extends TestCase {

  public TestAll(String name) {
    super(name);
  }

  static public Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTestSuite(TestInstantiate.class);
    suite.addTestSuite(TestConfigAndSchema.class);
    suite.addTestSuite(TestFieldCasing.class);
    suite.addTestSuite(TestSubjects.class);
    suite.addTestSuite(TestSessions.class);
    suite.addTestSuite(TestMembers.class);
    suite.addTestSuite(TestStemsAdd.class);
    suite.addTestSuite(TestStemsLoad.class);
    suite.addTestSuite(TestStemsChildren.class);
    suite.addTestSuite(TestStemsDelete.class);
    suite.addTestSuite(TestStemsAttrs.class);
    suite.addTestSuite(TestStemsAttrsAdd.class);
    suite.addTestSuite(TestStemsAttrsRep.class);
    suite.addTestSuite(TestStemsAttrsDel.class);
    suite.addTestSuite(TestStemsAttrsNoMod.class);
    suite.addTestSuite(TestStemsAsGroups.class);
    suite.addTestSuite(TestStemsMoF.class);
    // TODO TestStemsMoFAdd
    // TODO TestStemsMoFDel
    suite.addTestSuite(TestGroupsAdd.class);
    suite.addTestSuite(TestGroupsLoad.class);
    suite.addTestSuite(TestGroupsDelete.class);
    suite.addTestSuite(TestGroupsAttrs.class);
    suite.addTestSuite(TestGroupsAttrsAdd.class);
    suite.addTestSuite(TestGroupsAttrsRep.class);
    suite.addTestSuite(TestGroupsAttrsDel.class);
    suite.addTestSuite(TestGroupsAttrsNoMod.class);
    suite.addTestSuite(TestGroupsMoF.class);
    suite.addTestSuite(TestGroupsMoFAdd0.class);
    suite.addTestSuite(TestGroupsMoFChain0.class);
    suite.addTestSuite(TestGroupsMoFAdd1.class);
    suite.addTestSuite(TestGroupsMoFChain1.class);
    suite.addTestSuite(TestGroupsMoFAdd2.class);
    suite.addTestSuite(TestGroupsMoFAdd2NotMems.class);
    suite.addTestSuite(TestGroupsMoFAdd2ReChain.class);
    suite.addTestSuite(TestGroupsMoFAdd3.class);
    suite.addTestSuite(TestGroupsMoFAdd3Reverse.class);
    suite.addTestSuite(TestGroupsMoFAdd4.class);
    suite.addTestSuite(TestGroupsMoFAdd5.class);
    suite.addTestSuite(TestGroupsMoFAdd6.class);
    suite.addTestSuite(TestGroupsMoFAdd7.class);
    suite.addTestSuite(TestGroupsMoFAdd8.class);
    suite.addTestSuite(TestGroupsMoFAdd9.class);
    suite.addTestSuite(TestGroupsMoFAdd10.class);
    suite.addTestSuite(TestGroupsMoFAdd10Reverse.class);
    suite.addTestSuite(TestGroupsMoFDel0.class);
    suite.addTestSuite(TestGroupsMoFDel1.class);
    // TODO TestMixedMoF
    // TODO TestMixedMoFAdd
    // TODO TestMixedMoFDel
    suite.addTestSuite(TestGroupsHasMember.class);
    suite.addTestSuite(TestMembersIsMember.class);
    // TODO Flesh out
    suite.addTestSuite(TestNamingPrivs.class);
    suite.addTestSuite(TestNamingGrantMoF0.class);
    suite.addTestSuite(TestNamingGrantMoF1.class);
    suite.addTestSuite(TestNamingGrantMoF2.class);
    // TODO TestNamingPrivsRevoke
    // TODO Flesh out
    suite.addTestSuite(TestAccessPrivs.class);
    suite.addTestSuite(TestAccessGrantMoF0.class);
    suite.addTestSuite(TestAccessGrantMoF1.class);
    suite.addTestSuite(TestAccessGrantMoF2.class);
    // TODO TestAccessPrivsRevoke
    // TODO Flesh out
    suite.addTestSuite(TestQueries.class);
    // Bugs
    suite.addTestSuite(TestBug348.class);
    suite.addTestSuite(TestBug349.class);

    return suite;
  }

}

