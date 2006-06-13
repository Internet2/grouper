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
 * @version $Id: TestMember17.java,v 1.1 2006-06-13 17:40:39 blair Exp $
 */
public class TestMember17 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestMember17.class);

  public TestMember17(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testPassCanReadWhenRoot() {
    LOG.info("testPassCanReadWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can read", m.canRead(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanReadWhenRoot()

}

