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
import  edu.internet2.middleware.subject.Subject;
import  java.util.Date;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: Test_Integration_ImmediateMembershipValidator_validate.java,v 1.1 2007-03-09 20:36:28 blair Exp $
 * @since   1.2.0
 */
public class Test_Integration_ImmediateMembershipValidator_validate extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Test_Integration_ImmediateMembershipValidator_validate.class);


  // TESTS //  

  public void testValidate_InvalidGroupInCircularCheck() {
    try {
      LOG.info("testValidate_InvalidGroupInCircularCheck");
      R     r = R.getContext("grouper");
      Group g = r.getGroup("i2mi:grouper", "grouper-dev");

      MembershipDTO _ms = new MembershipDTO();
      _ms.setType(Membership.IMMEDIATE);
      _ms.setDepth(0);
      _ms.setViaUuid(null);
      _ms.setParentUuid(null);
      _ms.setListName("members");
      _ms.setOwnerUuid(null);
      _ms.setMemberUuid( g.toMember().getUuid() );
      try {
        ImmediateMembershipValidator.validate(_ms);
        fail("failed to throw IllegalStateException on unknown group in circular check");
      }
      catch (IllegalStateException eExpected) {
        assertTrue("threw expected IllegalStateException on unknown group in circular check", true);
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidGroupInCircularCheck()

  public void testValidate_InvalidMemberInCircularCheck() {
    try {
      LOG.info("testValidate_InvalidGroupInCircularCheck");
      R     r = R.getContext("grouper");
      Group g = r.getGroup("i2mi:grouper", "grouper-dev");

      MembershipDTO _ms = new MembershipDTO();
      _ms.setType(Membership.IMMEDIATE);
      _ms.setDepth(0);
      _ms.setViaUuid(null);
      _ms.setParentUuid(null);
      _ms.setListName("members");
      _ms.setOwnerUuid( g.getUuid() );
      _ms.setMemberUuid(null);
      try {
        ImmediateMembershipValidator.validate(_ms);
        fail("failed to throw IllegalStateException on unknown member in circular check");
      }
      catch (IllegalStateException eExpected) {
        assertTrue("threw expected IllegalStateException on unknown member in circular check", true);
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidMemberInCircularCheck()

  public void testValidate_Circular() {
    try {
      LOG.info("testValidate_Circular");
      R     r = R.getContext("grouper");
      Group g = r.getGroup("i2mi:grouper", "grouper-dev");

      MembershipDTO _ms = new MembershipDTO();
      _ms.setType(Membership.IMMEDIATE);
      _ms.setDepth(0);
      _ms.setViaUuid(null);
      _ms.setParentUuid(null);
      _ms.setListName("members");
      _ms.setOwnerUuid( g.getUuid() );
      _ms.setMemberUuid( g.toMember().getUuid() );
      ImmediateMembershipValidator v = ImmediateMembershipValidator.validate(_ms);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", ImmediateMembershipValidator.INVALID_CIRCULAR, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_Circular()

  public void testValidate_MembershipAlreadyExists() {
    try {
      LOG.info("testValidate_Circular");
      R       r     = R.getContext("grouper");
      Group   g     = r.getGroup("i2mi:grouper", "grouper-dev");
      Member  m     = r.getGroup("i2mi:grouper", "grouper-users").toMember();
      Subject subj  = r.getGroup("i2mi:grouper", "grouper-users").toSubject();
      g.addMember(subj);

      MembershipDTO _ms = new MembershipDTO();
      _ms.setType(Membership.IMMEDIATE);
      _ms.setDepth(0);
      _ms.setViaUuid(null);
      _ms.setParentUuid(null);
      _ms.setListName("members");
      _ms.setOwnerUuid( g.getUuid() );
      _ms.setMemberUuid( m.getUuid() );
      ImmediateMembershipValidator v = ImmediateMembershipValidator.validate(_ms);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", ImmediateMembershipValidator.INVALID_EXISTS, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_MembershipAlreadyExists()

  public void testValidate_InvalidMembership() {
    try {
      LOG.info("testValidate_Circular");
      R       r = R.getContext("grouper");
      Group   g = r.getGroup("i2mi:grouper", "grouper-dev");
      Member  m = r.getGroup("i2mi:grouper", "grouper-users").toMember();

      MembershipDTO _ms = new MembershipDTO();
      _ms.setType(Membership.IMMEDIATE);
      _ms.setDepth(0);
      _ms.setViaUuid(null);
      _ms.setParentUuid(null);
      _ms.setListName("members");
      _ms.setOwnerUuid( g.getUuid() );
      _ms.setMemberUuid( m.getUuid() );
      ImmediateMembershipValidator v = ImmediateMembershipValidator.validate(_ms);
      assertTrue( "v is invalid", v.isInvalid() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidMembership()

} // public class Test_Integration_ImmediateMembershipValidator_validate extends GrouperTest

