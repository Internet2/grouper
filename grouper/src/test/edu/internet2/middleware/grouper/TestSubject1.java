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

import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestSubject1.java,v 1.1.2.1 2006-04-13 16:32:36 blair Exp $
 */
public class TestSubject1 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestSubject1.class);

  public TestSubject1(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGetSourcesWithType() {
    LOG.info("testGetSourcesWithType");
    Set applications  = SubjectFinder.getSources("application");
    Set groups        = SubjectFinder.getSources("group");
    Set people        = SubjectFinder.getSources("person");
    T.amount("application sources", 1, applications.size());  
    T.amount("group sources"      , 1, groups.size()      );  
    T.amount("person sources"     , 1, people.size()      );  
  } // public void testGetSourcesWithType()

}

