/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

package edu.internet2.middleware.grouper.validator;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: Test_Integration_ImmediateMembershipValidator_validate.java,v 1.6 2009-12-07 07:31:08 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_Integration_ImmediateMembershipValidator_validate extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = GrouperUtil.getLog(Test_Integration_ImmediateMembershipValidator_validate.class);

  /**
   * 
   */
  public Test_Integration_ImmediateMembershipValidator_validate() {
    super();
    // TODO Auto-generated constructor stub
  }


  /**
   * @param name
   */
  public Test_Integration_ImmediateMembershipValidator_validate(String name) {
    super(name);
    // TODO Auto-generated constructor stub
  }


  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(new Test_Integration_ImmediateMembershipValidator_validate(
        "testValidate_InvalidMemberInCircularCheck"));
  }


  // TESTS //  

  public void testValidate_InvalidGroupInCircularCheck() {
    try {
      LOG.info("testValidate_InvalidGroupInCircularCheck");
      R     r = R.getContext("grouper");
      Group g = r.getGroup("i2mi:grouper", "grouper-dev");

      Membership _ms = new Membership();
      _ms.setType(MembershipType.IMMEDIATE.getTypeString());
      _ms.setDepth(0);
      _ms.setFieldId(FieldFinder.findFieldId("members", 
          FieldType.LIST.getType(), true));
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

      Membership _ms = new Membership();
      _ms.setType(MembershipType.IMMEDIATE.getTypeString());
      _ms.setDepth(0);
      _ms.setFieldId(FieldFinder.findFieldId("members", 
          FieldType.LIST.getType(), true));
      _ms.setOwnerGroupId( g.getUuid() );
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

      Membership _ms = new Membership();
      _ms.setType(MembershipType.IMMEDIATE.getTypeString());
      _ms.setDepth(0);
      _ms.setFieldId(FieldFinder.findFieldId("members", 
          FieldType.LIST.getType(), true));
      _ms.setOwnerGroupId( g.getUuid() );
      _ms.setMemberUuid( g.toMember().getUuid() );
      GrouperValidator v = ImmediateMembershipValidator.validate(_ms);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", ImmediateMembershipValidator.INVALID_CIRCULAR, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_Circular()

  public void testValidate_InvalidMembership() {
    try {
      LOG.info("testValidate_Circular");
      R       r = R.getContext("grouper");
      Group   g = r.getGroup("i2mi:grouper", "grouper-dev");
      Member  m = r.getGroup("i2mi:grouper", "grouper-users").toMember();

      Membership _ms = new Membership();
      _ms.setType(MembershipType.IMMEDIATE.getTypeString());
      _ms.setDepth(0);
      _ms.setFieldId(FieldFinder.findFieldId("members", 
          FieldType.LIST.getType(), true));
      _ms.setOwnerGroupId( g.getUuid() );
      _ms.setMemberUuid( m.getUuid() );
      GrouperValidator v = ImmediateMembershipValidator.validate(_ms);
      assertTrue( "v is invalid", v.isInvalid() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  }
}

