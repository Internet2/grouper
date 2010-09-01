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

package edu.internet2.middleware.grouper.membership;
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestMemberOf1.java,v 1.2 2009-08-12 12:44:45 shilen Exp $
 * @since   1.0
 */
public class TestMemberOf1 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestMemberOf1.class);

  public TestMemberOf1(String name) {
    super(name);
  }

  public void testForwardMemberOfDeletion() {
    LOG.info("testForwardMemberOfDeletion");
    try {
      R       r         = R.populateRegistry(0, 0, 10);
      Stem    ns        = r.ns;                                   // qsuob
      Stem    nsA       = ns.addChildStem("a", "a");              // qsuob:faculties
      Stem    nsA_A     = nsA.addChildStem("artf", "artf");       // qsuob:faculties:artf
      nsA.addChildStem("mvsf", "mvsf");       // qsuob:faculties:mvsf
      nsA.addChildStem("scif", "scif");       // qsuob:faculties:scif
      nsA.addChildStem("engf", "engf");       // qsuob:faculties:engf
      Group   gA_A      = nsA_A.addChildGroup("artf", "artf");    // qsuob:faculties:artf:staff
      Group   gA_B      = nsA_A.addChildGroup("mvsf", "mvsf");    // qsuob:faculties:mvsf:staff
      Group   gA_C      = nsA_A.addChildGroup("scif", "scif");    // qsuob:faculties:scif:staff
      Group   gA_D      = nsA_A.addChildGroup("engf", "engf");    // qsuob:faculties:engf:staff
      Group   gALL      = ns.addChildGroup("all", "all");         // qsuob:all
      Group   gAAS      = ns.addChildGroup("aas", "aas");         // qsuob:all_academic_staff
      Subject iawi      = r.getSubject("a");
      Subject iata      = r.getSubject("b");
      Subject kewi      = r.getSubject("c");
      Subject keta      = r.getSubject("d");
      Subject mawi      = r.getSubject("e");
      Subject mata      = r.getSubject("f");
      Subject fiwi      = r.getSubject("g");
      Subject fita      = r.getSubject("h");
      Subject jowi      = r.getSubject("i");
      Subject jota      = r.getSubject("j");

      gA_A.addMember(iawi);
      //  iawi  -> gA_A

      gA_A.addMember(iata);
      //  iawi  -> gA_A
      //  iata  -> gA_A

      gA_B.addMember(kewi);
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B

      gA_B.addMember(keta);
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B

      gA_B.addMember(mawi);
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B

      gA_B.addMember(mata);
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B

      gA_C.addMember(fiwi);
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C

      gA_C.addMember(fita);
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C
      //  fita  -> gA_C

      gA_D.addMember(jowi);
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C
      //  fita  -> gA_C
      //  jowi  -> gA_D

      gA_D.addMember(jota);
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C
      //  fita  -> gA_C
      //  jowi  -> gA_D
      //  jota  -> gA_D

      gALL.addMember( gAAS.toSubject() );
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C
      //  fita  -> gA_C
      //  jowi  -> gA_D
      //  jota  -> gA_D
      //  gAAS  -> gALL

      gAAS.addMember( gA_A.toSubject() );
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C
      //  fita  -> gA_C
      //  jowi  -> gA_D
      //  jota  -> gA_D
      //  gAAS  -> gALL
      //  gA_A  -> gAAS -> gALL
      //  iawi  -> gA_A -> gAAS -> gALL
      //  iata  -> gA_A -> gAAS -> gALL
      //  gA_A  -> gAAS
      //  iawi  -> gA_A -> gAAS
      //  iata  -> gA_A -> gAAS

      gAAS.addMember( gA_D.toSubject() );
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C
      //  fita  -> gA_C
      //  jowi  -> gA_D
      //  jota  -> gA_D
      //  gAAS  -> gALL
      //  gA_A  -> gAAS -> gALL
      //  iawi  -> gA_A -> gAAS -> gALL
      //  iata  -> gA_A -> gAAS -> gALL
      //  gA_D  -> gAAS -> gALL
      //  jowi  -> gA_D -> gAAS -> gALL
      //  jota  -> gA_D -> gAAS -> gALL
      //  gA_A  -> gAAS
      //  iawi  -> gA_A -> gAAS
      //  iata  -> gA_A -> gAAS
      //  gA_D  -> gAAS
      //  jowi  -> gA_D -> gAAS
      //  jota  -> gA_D -> gAAS

      gAAS.addMember( gA_C.toSubject() );
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C
      //  fita  -> gA_C
      //  jowi  -> gA_D
      //  jota  -> gA_D
      //  gAAS  -> gALL
      //  gA_A  -> gAAS -> gALL
      //  iawi  -> gA_A -> gAAS -> gALL
      //  iata  -> gA_A -> gAAS -> gALL
      //  gA_C  -> gAAS -> gALL
      //  fiwi  -> gA_C -> gAAS -> gALL
      //  fita  -> gA_C -> gAAS -> gALL
      //  gA_D  -> gAAS -> gALL
      //  jowi  -> gA_D -> gAAS -> gALL
      //  jota  -> gA_D -> gAAS -> gALL
      //  gA_A  -> gAAS
      //  iawi  -> gA_A -> gAAS
      //  iata  -> gA_A -> gAAS
      //  gA_C  -> gAAS
      //  fiwi  -> gA_C -> gAAS
      //  fita  -> gA_C -> gAAS
      //  gA_D  -> gAAS
      //  jowi  -> gA_D -> gAAS
      //  jota  -> gA_D -> gAAS

      gAAS.addMember( gA_B.toSubject() );
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C
      //  fita  -> gA_C
      //  jowi  -> gA_D
      //  jota  -> gA_D
      //  gAAS  -> gALL
      //  gA_A  -> gAAS -> gALL
      //  iawi  -> gA_A -> gAAS -> gALL
      //  iata  -> gA_A -> gAAS -> gALL
      //  gA_B  -> gAAS -> gALL
      //  kewi  -> gA_B -> gAAS -> gALL
      //  keta  -> gA_B -> gAAS -> gALL
      //  mawi  -> gA_B -> gAAS -> gALL
      //  mata  -> gA_B -> gAAS -> gALL
      //  gA_C  -> gAAS -> gALL
      //  fiwi  -> gA_C -> gAAS -> gALL
      //  fita  -> gA_C -> gAAS -> gALL
      //  gA_D  -> gAAS -> gALL
      //  jowi  -> gA_D -> gAAS -> gALL
      //  jota  -> gA_D -> gAAS -> gALL
      //  gA_A  -> gAAS
      //  iawi  -> gA_A -> gAAS
      //  iata  -> gA_A -> gAAS
      //  gA_B  -> gAAS
      //  kewi  -> gA_B -> gAAS
      //  keta  -> gA_B -> gAAS
      //  mawi  -> gA_B -> gAAS
      //  mata  -> gA_B -> gAAS
      //  gA_C  -> gAAS
      //  fiwi  -> gA_C -> gAAS
      //  fita  -> gA_C -> gAAS
      //  gA_D  -> gAAS
      //  jowi  -> gA_D -> gAAS
      //  jota  -> gA_D -> gAAS

      // Setup now complete
      T.getMemberships(gA_A, 2);
      T.getMemberships(gA_B, 4);
      T.getMemberships(gA_C, 2);
      T.getMemberships(gA_D, 2);
      T.getMemberships(gALL, 15);
      T.getMemberships(gAAS, 14);

      // Now try to break things
      
      Iterator iter = gA_A.getImmediateMembers().iterator();
      while (iter.hasNext()) {
        Member m = (Member) iter.next();
        gA_A.deleteMember( m.getSubject() );
      }
      T.getMemberships(gA_A, 0);
      T.getMemberships(gA_B, 4);
      T.getMemberships(gA_C, 2);
      T.getMemberships(gA_D, 2);
      T.getMemberships(gALL, 13);
      T.getMemberships(gAAS, 12);
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C
      //  fita  -> gA_C
      //  jowi  -> gA_D
      //  jota  -> gA_D
      //  gAAS  -> gALL
      //  gA_A  -> gAAS -> gALL
      //  gA_B  -> gAAS -> gALL
      //  kewi  -> gA_B -> gAAS -> gALL
      //  keta  -> gA_B -> gAAS -> gALL
      //  mawi  -> gA_B -> gAAS -> gALL
      //  mata  -> gA_B -> gAAS -> gALL
      //  gA_C  -> gAAS -> gALL
      //  fiwi  -> gA_C -> gAAS -> gALL
      //  fita  -> gA_C -> gAAS -> gALL
      //  gA_D  -> gAAS -> gALL
      //  jowi  -> gA_D -> gAAS -> gALL
      //  jota  -> gA_D -> gAAS -> gALL
      //  gA_A  -> gAAS
      //  gA_B  -> gAAS
      //  kewi  -> gA_B -> gAAS
      //  keta  -> gA_B -> gAAS
      //  mawi  -> gA_B -> gAAS
      //  mata  -> gA_B -> gAAS
      //  gA_C  -> gAAS
      //  fiwi  -> gA_C -> gAAS
      //  fita  -> gA_C -> gAAS
      //  gA_D  -> gAAS
      //  jowi  -> gA_D -> gAAS
      //  jota  -> gA_D -> gAAS

      gA_A.addMember(iawi);
      T.getMemberships(gA_A, 1);
      T.getMemberships(gA_B, 4);
      T.getMemberships(gA_C, 2);
      T.getMemberships(gA_D, 2);
      T.getMemberships(gALL, 14);
      T.getMemberships(gAAS, 13);
      //  iawi  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C
      //  fita  -> gA_C
      //  jowi  -> gA_D
      //  jota  -> gA_D
      //  gAAS  -> gALL
      //  gA_A  -> gAAS -> gALL
      //  iawi  -> gA_A -> gAAS -> gALL
      //  gA_B  -> gAAS -> gALL
      //  kewi  -> gA_B -> gAAS -> gALL
      //  keta  -> gA_B -> gAAS -> gALL
      //  mawi  -> gA_B -> gAAS -> gALL
      //  mata  -> gA_B -> gAAS -> gALL
      //  gA_C  -> gAAS -> gALL
      //  fiwi  -> gA_C -> gAAS -> gALL
      //  fita  -> gA_C -> gAAS -> gALL
      //  gA_D  -> gAAS -> gALL
      //  jowi  -> gA_D -> gAAS -> gALL
      //  jota  -> gA_D -> gAAS -> gALL
      //  gA_A  -> gAAS
      //  iawi  -> gA_A -> gAAS
      //  gA_B  -> gAAS
      //  kewi  -> gA_B -> gAAS
      //  keta  -> gA_B -> gAAS
      //  mawi  -> gA_B -> gAAS
      //  mata  -> gA_B -> gAAS
      //  gA_C  -> gAAS
      //  fiwi  -> gA_C -> gAAS
      //  fita  -> gA_C -> gAAS
      //  gA_D  -> gAAS
      //  jowi  -> gA_D -> gAAS
      //  jota  -> gA_D -> gAAS

      gA_A.addMember(iata);
      T.getMemberships(gA_A, 2);
      T.getMemberships(gA_B, 4);
      T.getMemberships(gA_C, 2);
      T.getMemberships(gA_D, 2);
      T.getMemberships(gALL, 15);
      T.getMemberships(gAAS, 14);
      //  iawi  -> gA_A
      //  iata  -> gA_A
      //  kewi  -> gA_B
      //  keta  -> gA_B
      //  mawi  -> gA_B
      //  mata  -> gA_B
      //  fiwi  -> gA_C
      //  fita  -> gA_C
      //  jowi  -> gA_D
      //  jota  -> gA_D
      //  gAAS  -> gALL
      //  gA_A  -> gAAS -> gALL
      //  iawi  -> gA_A -> gAAS -> gALL
      //  iata  -> gA_A -> gAAS -> gALL
      //  gA_B  -> gAAS -> gALL
      //  kewi  -> gA_B -> gAAS -> gALL
      //  keta  -> gA_B -> gAAS -> gALL
      //  mawi  -> gA_B -> gAAS -> gALL
      //  mata  -> gA_B -> gAAS -> gALL
      //  gA_C  -> gAAS -> gALL
      //  fiwi  -> gA_C -> gAAS -> gALL
      //  fita  -> gA_C -> gAAS -> gALL
      //  gA_D  -> gAAS -> gALL
      //  jowi  -> gA_D -> gAAS -> gALL
      //  jota  -> gA_D -> gAAS -> gALL
      //  gA_A  -> gAAS
      //  iawi  -> gA_A -> gAAS
      //  iata  -> gA_A -> gAAS
      //  gA_B  -> gAAS
      //  kewi  -> gA_B -> gAAS
      //  keta  -> gA_B -> gAAS
      //  mawi  -> gA_B -> gAAS
      //  mata  -> gA_B -> gAAS
      //  gA_C  -> gAAS
      //  fiwi  -> gA_C -> gAAS
      //  fita  -> gA_C -> gAAS
      //  gA_D  -> gAAS
      //  jowi  -> gA_D -> gAAS
      //  jota  -> gA_D -> gAAS

      try {
        Membership ms = MembershipFinder.findImmediateMembership(
          r.rs, gALL, gAAS.toSubject(), Group.getDefaultList(), true
        );
        Assert.assertNotNull(ms);
        gALL.deleteMember( gAAS.toSubject() );
        Assert.assertTrue("finally, a hibernate exception wasn't thrown", true);
        T.getMemberships(gA_A, 2);
        T.getMemberships(gA_B, 4);
        T.getMemberships(gA_C, 2);
        T.getMemberships(gA_D, 2);
        T.getMemberships(gALL, 0);
        T.getMemberships(gAAS, 14);
      }
      catch (GrouperException eGRT) {
        Assert.fail("GrouperRuntimeException thrown: " + eGRT.getMessage());
      }

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testForwardMemberOfDeletion()

} // public class TestMemberOf1

