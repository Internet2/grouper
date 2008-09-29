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
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test {@link SubjectFinder.findAll()} with {@link InternalSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSuFiInSoAdSearch.java,v 1.7 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestSuFiInSoAdSearch extends TestCase {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestSuFiInSoAdSearch.class);


  public TestSuFiInSoAdSearch(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // Tests

  public void testSearchBadSearch() {
    LOG.info("testSearchBadSearch");
    Set subjs = SubjectFinder.findAll("i do not exist");
    Assert.assertTrue("subjs == 0", subjs.size() == 0);
  } // public void testSearchBadSearch()

  public void testSearchGoodAllId() {
    LOG.info("testSearchGoodAllId");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJA.getId());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodAllId()

  public void testSearchGoodAllIdentifier() {
    LOG.info("testSearchGoodAllIdentifier");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJA.getName());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodAllIdentifier()

  public void testSearchGoodRootId() {
    LOG.info("testSearchGoodRootId");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJR.getId());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodRootId()

  public void testSearchGoodRootIdentifier() {
    LOG.info("testSearchGoodRootIdentifier");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJR.getName());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodRootIdentifier()

}

