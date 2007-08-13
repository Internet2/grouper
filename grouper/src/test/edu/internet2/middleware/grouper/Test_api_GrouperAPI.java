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
import  edu.internet2.middleware.grouper.internal.dto.GrouperDTO;


/**
 * Test {@link GrouperAPI}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_GrouperAPI.java,v 1.1 2007-08-13 19:39:39 blair Exp $
 * @since   @HEAD@
 */
public class Test_api_GrouperAPI extends GrouperTest {


  private GrouperAPI mockAPI;



  public void setUp() {
    super.setUp();
    this.mockAPI = new MockGrouperAPI();
  }

  public void tearDown() {
    super.tearDown();
  }



  public void test_getDTO_nullDTO() {
    try {
      this.mockAPI.getDTO();
      fail("failed to throw expected IllegalStateException");
    }
    catch (IllegalStateException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  
  public void test_getDTO_equalsSetDTO() {
    GrouperDTO mockDTO = new MockGrouperDTO();
    this.mockAPI.setDTO(mockDTO);
    assertEquals( mockDTO, this.mockAPI.getDTO() );
  }



  public void test_getSession_nullSession() {
    try {
      this.mockAPI.getSession();
      fail("failed to throw expected IllegalStateException");
    }
    catch (IllegalStateException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getSession_equalsSetSession() 
    throws  SessionException
  {
    GrouperSession s = GrouperSession.start( SubjectFinder.findAllSubject() );
    this.mockAPI.setSession(s);
    assertEquals( s, this.mockAPI.getSession() );   
  }



  public void test_setDTO_nullDTO() {
    this.mockAPI.equals( this.mockAPI.setDTO(null) );  
  }



  public void test_setSession_nullSession() {
    this.mockAPI.equals( this.mockAPI.setDTO(null) );  
  }

}

