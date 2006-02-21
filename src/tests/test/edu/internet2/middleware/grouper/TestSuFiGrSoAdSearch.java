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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link SubjectFinder.findAll()} with {@link GrouperSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSuFiGrSoAdSearch.java,v 1.4 2006-02-21 17:11:33 blair Exp $
 */
public class TestSuFiGrSoAdSearch extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestSuFiGrSoAdSearch.class);


  public TestSuFiGrSoAdSearch(String name) {
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


  // Tests

  public void testSearchBad() {
    LOG.info("testSearchBad");
    Set subjs = SubjectFinder.findAll("i do not exist");
    Assert.assertTrue("subjs == 0", subjs.size() == 0);
  } // public void testSearchBad()

  public void testSearchGood() {
    LOG.info("testSearchGood");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    Group           dc    = StemHelper.addChildGroup(com, "dc", "devclue");
    Set             subjs = SubjectFinder.findAll("educational");
    Assert.assertTrue("subjs == 2", subjs.size() == 2);
  } // public void testSearchGood()

}

