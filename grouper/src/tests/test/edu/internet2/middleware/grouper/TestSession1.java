/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

package test.edu.internet2.middleware.grouper;


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link GrouperSession} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSession1.java,v 1.1 2006-03-29 17:19:25 blair Exp $
 */
public class TestSession1 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestSession1.class);


  // Private Class Variables
  private Source sa;

  public TestSession1(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  public void testStartSessionGoodSubject() {
    LOG.info("testStartSessionGoodSubject");
    GrouperSession s = SessionHelper.getSession("GrouperSystem", "application");
  } // public void testStartSessionGoodSubject()

}

