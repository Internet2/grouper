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
import java.util.Date;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: Test_Unit_API_CompositeValidator_validate.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_Unit_API_CompositeValidator_validate extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = GrouperUtil.getLog(Test_Unit_API_CompositeValidator_validate.class);


  // TESTS //  

  public void testValidate_NullComposite() {
    try {
      LOG.info("testValidate_NullComposite");
      CompositeValidator v = CompositeValidator.validate(null);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", NotNullValidator.INVALID, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_NullComposite()
    
  public void testValidate_InvalidCreateTime() {
    try {
      LOG.info("testValidate_InvalidCreateTime");
      Composite        _c  = new Composite();
      _c.setCreateTime(-1);
      CompositeValidator  v   = CompositeValidator.validate(_c);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", CompositeValidator.INVALID_CREATETIME, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidCreateTime()

  public void testValidate_InvalidCreatorUuid() {
    try {
      LOG.info("testValidate_InvalidCreatorUuid");
      Composite        _c  = new Composite();
      _c.setCreateTime( new Date().getTime() );
      _c.setCreatorUuid(null);
      CompositeValidator  v   = CompositeValidator.validate(_c);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", CompositeValidator.INVALID_CREATORUUID, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidCreatorUuid()

  public void testValidate_InvalidUuid() {
    try {
      LOG.info("testValidate_InvalidUuid");
      Composite        _c  = new Composite();
      _c.setCreateTime( new Date().getTime() );
      _c.setCreatorUuid("creatorUuid");
      _c.setUuid(null);
      CompositeValidator  v   = CompositeValidator.validate(_c);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", CompositeValidator.INVALID_UUID, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidUuid()

  public void testValidate_InvalidType() {
    try {
      LOG.info("testValidate_InvalidType");
      Composite        _c  = new Composite();
      _c.setCreateTime( new Date().getTime() );
      _c.setCreatorUuid("creatorUuid");
      _c.setUuid("uuid");
      _c.setTypeDb(null);
      CompositeValidator  v   = CompositeValidator.validate(_c);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", CompositeValidator.INVALID_TYPE, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidType()

  public void testValidate_InvalidFactorOwnerUuid() {
    try {
      LOG.info("testValidate_InvalidFactorOwnerUuid");
      Composite        _c  = new Composite();
      _c.setCreateTime( new Date().getTime() );
      _c.setCreatorUuid("creatorUuid");
      _c.setUuid("uuid");
      _c.setTypeDb("type");
      _c.setFactorOwnerUuid(null);
      CompositeValidator  v   = CompositeValidator.validate(_c);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", CompositeValidator.INVALID_FACTOROWNERUUID, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidFactorOwnerUuid()

  public void testValidate_InvalidLeftFactorUuid() {
    try {
      LOG.info("testValidate_InvalidLeftFactorUuid");
      Composite        _c  = new Composite();
      _c.setCreateTime( new Date().getTime() );
      _c.setCreatorUuid("creatorUuid");
      _c.setUuid("uuid");
      _c.setTypeDb("type");
      _c.setFactorOwnerUuid("factorOwnerUuid");
      _c.setLeftFactorUuid(null);
      CompositeValidator  v   = CompositeValidator.validate(_c);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", CompositeValidator.INVALID_LEFTFACTORUUID, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidLeftFactorUuid()

  public void testValidate_InvalidRightFactorUuid() {
    try {
      LOG.info("testValidate_InvalidRightFactorUuid");
      Composite        _c  = new Composite();
      _c.setCreateTime( new Date().getTime() );
      _c.setCreatorUuid("creatorUuid");
      _c.setUuid("uuid");
      _c.setTypeDb("type");
      _c.setFactorOwnerUuid("factorOwnerUuid");
      _c.setLeftFactorUuid("leftFactorUuid");
      _c.setRightFactorUuid(null);
      CompositeValidator  v   = CompositeValidator.validate(_c);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", CompositeValidator.INVALID_RIGHTFACTORUUID, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidRightFactorUuid()

  public void testValidate_InvalidLeftAndRightCycle() {
    try {
      LOG.info("testValidate_InvalidLeftAndRightCycle");
      Composite        _c  = new Composite();
      _c.setCreateTime( new Date().getTime() );
      _c.setCreatorUuid("creatorUuid");
      _c.setUuid("uuid");
      _c.setTypeDb("type");
      _c.setFactorOwnerUuid("factorOwnerUuid");
      _c.setLeftFactorUuid("leftFactorUuid");
      _c.setRightFactorUuid("leftFactorUuid");
      CompositeValidator  v   = CompositeValidator.validate(_c);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", CompositeValidator.INVALID_LEFTRIGHTCYCLE, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidLeftAndRightCycle()

  public void testValidate_InvalidOwnerAndLeftCycle() {
    try {
      LOG.info("testValidate_InvalidOwnerAndLeftCycle");
      Composite        _c  = new Composite();
      _c.setCreateTime( new Date().getTime() );
      _c.setCreatorUuid("creatorUuid");
      _c.setUuid("uuid");
      _c.setTypeDb("type");
      _c.setFactorOwnerUuid("factorOwnerUuid");
      _c.setLeftFactorUuid("factorOwnerUuid");
      _c.setRightFactorUuid("rightFactorUuid");
      CompositeValidator  v   = CompositeValidator.validate(_c);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", CompositeValidator.INVALID_OWNERLEFTCYCLE, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidOwnerAndLeftCycle()

  public void testValidate_InvalidOwnerAndRightCycle() {
    try {
      LOG.info("testValidate_InvalidOwnerAndRightCycle");
      Composite        _c  = new Composite();
      _c.setCreateTime( new Date().getTime() );
      _c.setCreatorUuid("creatorUuid");
      _c.setUuid("uuid");
      _c.setTypeDb("type");
      _c.setFactorOwnerUuid("factorOwnerUuid");
      _c.setLeftFactorUuid("leftFactorUuid");
      _c.setRightFactorUuid("factorOwnerUuid");
      CompositeValidator  v   = CompositeValidator.validate(_c);
      assertTrue( "v is invalid", v.isInvalid() );
      assertEquals( "v error msg", CompositeValidator.INVALID_OWNERRIGHTCYCLE, v.getErrorMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testValidate_InvalidOwnerAndRightCycle()

} // public class Test_Unit_API_CompositeValidator_validate extends GrouperTest

