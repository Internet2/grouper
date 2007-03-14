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
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: Test_Unit_API_EffectiveMembershipValidator_validate.java,v 1.2 2007-03-14 18:20:05 blair Exp $
 * @since   1.2.0
 */
public class Test_Unit_API_EffectiveMembershipValidator_validate extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Test_Unit_API_EffectiveMembershipValidator_validate.class);


  // TESTS //  

  public void testValidate_NullMembershipDTO() {
    try {
      LOG.info("testValidate_NullMembershipDTO");
      EffectiveMembershipValidator v = EffectiveMembershipValidator.validate(null);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", NotNullValidator.INVALID, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_NullMembershipDTO()
   
  public void testValidate_InvalidType() {
    try {
      LOG.info("testValidate_InvalidType");
      MembershipDTO _ms = new MembershipDTO();
      _ms.setType(null);
      EffectiveMembershipValidator v = EffectiveMembershipValidator.validate(_ms);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", EffectiveMembershipValidator.INVALID_TYPE, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidType()

  public void testValidate_InvalidDepth() {
    try {
      LOG.info("testValidate_InvalidDepth");
      MembershipDTO _ms = new MembershipDTO();
      _ms.setType(Membership.EFFECTIVE);
      _ms.setDepth(0);
      EffectiveMembershipValidator v = EffectiveMembershipValidator.validate(_ms);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", EffectiveMembershipValidator.INVALID_DEPTH, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidDepth()

  public void testValidate_InvalidViaUuid() {
    try {
      LOG.info("testValidate_InvalidViaUuid");
      MembershipDTO _ms = new MembershipDTO();
      _ms.setType(Membership.EFFECTIVE);
      _ms.setDepth(1);
      _ms.setViaUuid(null);
      EffectiveMembershipValidator v = EffectiveMembershipValidator.validate(_ms);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", EffectiveMembershipValidator.INVALID_VIAUUID, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidViaUuid()

  public void testValidate_InvalidParentUuid() {
    try {
      LOG.info("testValidate_InvalidParentUuid");
      MembershipDTO _ms = new MembershipDTO();
      _ms.setType(Membership.EFFECTIVE);
      _ms.setDepth(1);
      _ms.setViaUuid("viaUuid");
      _ms.setParentUuid(null);
      EffectiveMembershipValidator v = EffectiveMembershipValidator.validate(_ms);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", EffectiveMembershipValidator.INVALID_PARENTUUID, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidParentUuid()

  public void testValidate_InvalidMembership() {
    try {
      LOG.info("testValidate_InvalidMembership");
      MembershipDTO _ms = new MembershipDTO();
      _ms.setType(Membership.EFFECTIVE);
      _ms.setDepth(1);
      _ms.setViaUuid("viaUuid");
      _ms.setParentUuid("parentUuid");
      EffectiveMembershipValidator v = EffectiveMembershipValidator.validate(_ms);
      assertTrue( "v is invalid", v.isInvalid() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidMembership()

} // public class Test_Unit_API_EffectiveMembershipValidator_validate extends GrouperTest

