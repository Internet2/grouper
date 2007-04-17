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
import  edu.internet2.middleware.grouper.internal.dto.StemDTO;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: Test_Unit_API_Stem_isRootStem.java,v 1.2 2007-04-17 14:17:30 blair Exp $
 * @since   1.2.0
 */
public class Test_Unit_API_Stem_isRootStem extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Test_Unit_API_Stem_isRootStem.class);


  // TESTS //  

  public void testIsRootStem_DoNotThrowNullPointerException() {
    try {
      LOG.info("testIsRootStem_DoNotThrowNullPointerException");
      Stem ns = new Stem();
      ns.setDTO( new StemDTO() );
      assertFalse( ns.isRootStem() );
    }
    catch (NullPointerException eNP) {
      fail( "threw NullPointerException: " + eNP.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_NullValue()
    
} // public class Test_Unit_API_Stem_isRootStem extends GrouperTest

