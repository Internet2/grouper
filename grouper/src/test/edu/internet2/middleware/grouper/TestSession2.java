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

package edu.internet2.middleware.grouper;


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
 * @version $Id: TestSession2.java,v 1.1.2.1 2006-04-11 16:45:49 blair Exp $
 */
public class TestSession2 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestSession2.class);


  // Private Class Variables
  private Source sa;

  public TestSession2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGetStartTime() {
    LOG.info("testGetStartTime");
    GrouperSession s = SessionHelper.getRootSession();
    Assert.assertNotNull("start time !null", s.getStartTime());
    long  start = s.getStartTime().getTime();
    long  epoch = new Date(0).getTime();
    Assert.assertFalse(
      "start[" + start + "] != epoch[" + epoch + "]",
      start == epoch
    );
  } // public void testGetStartTime()

}

