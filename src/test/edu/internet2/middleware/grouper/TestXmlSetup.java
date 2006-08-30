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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlSetup.java,v 1.2 2006-08-30 18:35:38 blair Exp $
 */
public class TestXmlSetup extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestXmlSetup.class);

  public TestXmlSetup(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testXmlSetup() {
    LOG.info("testXmlSetup");
    try {
      R       r     = R.populateRegistry(2, 2, 2);
      Stem    nsA   = r.getStem("a");
      Stem    nsB   = r.getStem("b");
      Group   gAA   = r.getGroup("a", "a");
      Group   gAB   = r.getGroup("a", "b");
      Group   gBA   = r.getGroup("b", "a");
      Group   gBB   = r.getGroup("b", "b");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      gAA.addMember(  subjA                                 );
      gAB.addMember(  subjB                                 );
      gBA.addMember(  gAA.toSubject()                       );
      gBB.addCompositeMember( CompositeType.UNION, gAA, gAB );
      r.rs.stop();
      Assert.assertTrue("setup registry for export-and-import", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testXmlSetup()

}

