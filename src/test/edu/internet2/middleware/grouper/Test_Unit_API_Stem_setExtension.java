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
 * @version $Id: Test_Unit_API_Stem_setExtension.java,v 1.1 2007-02-19 20:43:29 blair Exp $
 * @since   1.2.0
 */
public class Test_Unit_API_Stem_setExtension extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Test_Unit_API_Stem_setExtension.class);


  // TESTS //  

  public void testSetExtension_NullValue() {
    try {
      LOG.info("testSetExtension_NullValue");
      Stem ns = new Stem();
      ns.setDTO( new StemDTO() );
      ns.setExtension(null);
      fail("should have thrown StemModifyException");
    }
    catch (StemModifyException eNSM) {
      assertTrue(true);
      assertEquals( E.ATTR_NULL, eNSM.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_NullValue()
    
  public void testSetExtension_EmptyValue() {
    try {
      LOG.info("testSetExtension_EmptyValue");
      Stem ns = new Stem();
      ns.setDTO( new StemDTO() );
      ns.setExtension(GrouperConfig.EMPTY_STRING);
      fail("should have thrown StemModifyException");
    }
    catch (StemModifyException eNSM) {
      assertTrue(true);
      assertEquals( E.ATTR_NULL, eNSM.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_EmptyValue()
    
  public void testSetExtension_ValueContainsColon() {
    try {
      LOG.info("testSetExtension_ValueContainsColon");
      Stem ns = new Stem();
      ns.setDTO( new StemDTO() );
      ns.setExtension("co:on");
      fail("should have thrown StemModifyException");
    }
    catch (StemModifyException eNSM) {
      assertTrue(true);
      assertEquals( E.ATTR_COLON, eNSM.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_ValueContainsColon()
    
  public void testSetExtension_WhitespaceOnlyValue() {
    try {
      LOG.info("testSetExtension_WhitespaceOnlyValue");
      Stem ns = new Stem();
      ns.setDTO( new StemDTO() );
      ns.setExtension(" ");
      fail("should have thrown StemModifyException");
    }
    catch (StemModifyException eNSM) {
      assertTrue(true);
      assertEquals( E.ATTR_NULL, eNSM.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_WhitespaceOnlyValue()
    
} // public class Test_Unit_API_Stem_setExtension extends GrouperTest

