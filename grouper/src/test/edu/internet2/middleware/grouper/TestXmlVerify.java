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
 * @version $Id: TestXmlVerify.java,v 1.1 2006-06-27 17:04:45 blair Exp $
 */
public class TestXmlVerify extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestXmlVerify.class);

  public TestXmlVerify(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    // Do not reset - yet - since I'm depending upon data being present in the
    // registry for this to work
    //RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testXmlVerify() {
    LOG.info("testXmlVerify");
    try {
      // TODO Better testing
      GrouperSession  s = GrouperSession.start(
        SubjectFinder.findById(GrouperConfig.ROOT)
      );
      Stem            ns  = StemFinder.findByName(  s, "i2"                 );  
      Stem            nsA = StemFinder.findByName(  s, ns.getName() + ":a"  );
      Stem            nsB = StemFinder.findByName(  s, ns.getName() + ":b"  );
      Group           gAA = GroupFinder.findByName( s, nsA.getName() + ":a" );
      Group           gAB = GroupFinder.findByName( s, nsA.getName() + ":b" );
      Group           gBA = GroupFinder.findByName( s, nsB.getName() + ":a" );
      Group           gBB = GroupFinder.findByName( s, nsB.getName() + ":b" );
      s.stop();
      Assert.assertTrue("verified registry after export-and-import", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testXmlVerify()

}

