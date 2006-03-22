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
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * @author  blair christensen.
 * @version $Id: TestUnionFactor2.java,v 1.1 2006-03-22 18:43:23 blair Exp $
 */
public class TestUnionFactor2 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestUnionFactor2.class);

  public TestUnionFactor2(String name) {
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

  public void testAddSimpleUnionFactor() {
    LOG.info("testAddSimpleUnionFactor");
    try {
      R r = R.createOneStemAndFourGroups();
      try {
        Assert.assertTrue("i2 members==0",  r.i2.getMembers().size() == 0);
        Assert.assertTrue("ub members==0",  r.ub.getMembers().size() == 0);
        Assert.assertTrue("uc members==0",  r.uc.getMembers().size() == 0);

        r.i2.addFactor( new UnionFactor(r.ub, r.uc) );
        Assert.assertTrue(  "added union factor", true);

        Assert.assertFalse( "i2 !isFactor"      , r.i2.isFactor());
        Assert.assertTrue(  "ub isFactor"       , r.ub.isFactor());
        Assert.assertTrue(  "uc isFactor"       , r.uc.isFactor());

        Assert.assertTrue(  "i2 hasFactor"      , r.i2.hasFactor());
        Assert.assertFalse( "ub !hasFactor"     , r.ub.hasFactor());
        Assert.assertFalse( "uc !hasFactor"     , r.uc.hasFactor());

        Assert.assertTrue("i2 members==0",  r.i2.getMembers().size() == 0);
        Assert.assertTrue("ub members==0",  r.ub.getMembers().size() == 0);
        Assert.assertTrue("uc members==0",  r.uc.getMembers().size() == 0);
      }
      catch (FactorAddException eFA) {
        Assert.fail(eFA.getMessage());
      } 
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testAddSimpleUnionFactor()

}

