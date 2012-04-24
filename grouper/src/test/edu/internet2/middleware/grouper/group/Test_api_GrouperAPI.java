/*******************************************************************************
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
 ******************************************************************************/
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

package edu.internet2.middleware.grouper.group;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MockGrouperAPI;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;


/**
 * Test {@link GrouperAPI}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_GrouperAPI.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.1
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



  public void test_getSession_nullSession() {
    try {
      GrouperSession.clearGrouperSession();
      GrouperSession.staticGrouperSession();
      fail("failed to throw expected IllegalStateException");
    }
    catch (IllegalStateException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getSession_equalsSetSession() 
    throws  SessionException
  {
    GrouperSession.clearGrouperSession();
    
    GrouperSession s = GrouperSession.start( SubjectFinder.findAllSubject() );
    assertEquals( s, GrouperSession.staticGrouperSession() );  
    s.stop();
    
    assertEquals(null, GrouperSession.staticGrouperSession(false));
    
    //do callback and see
    final GrouperSession SESSION = GrouperSession.start(SubjectFinder.findAllSubject(), false);
    final GrouperSession SESSION2 = GrouperSession.start(SubjectFinder.findAllSubject(), false);
    
    assertFalse(SESSION == SESSION2);
    
    GrouperSession.callbackGrouperSession(SESSION, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        assertTrue (SESSION == grouperSession);
        
        //try to nest with same
        GrouperSession.callbackGrouperSession(SESSION, new GrouperSessionHandler() {

          public Object callback(GrouperSession grouperSession)
              throws GrouperSessionException {
            assertTrue (SESSION == grouperSession);

            //nest with different
            GrouperSession.callbackGrouperSession(SESSION2, new GrouperSessionHandler() {

              public Object callback(GrouperSession grouperSession)
                  throws GrouperSessionException {

                assertTrue (SESSION2 == grouperSession);
                return null;
                
              }
              
            });
            
            assertTrue (SESSION == grouperSession);
            return null;
          }
          
        });
        
        
        assertTrue (SESSION == grouperSession);

        return null;
      }
      
    });
  }



}

