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
import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStemFinder.java,v 1.9 2009-03-20 19:56:40 mchyzer Exp $
 */
public class TestStemFinder extends GrouperTest {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestStemFinder.class);


  public TestStemFinder(String name) {
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

  public void testFindRootStem() {
    LOG.info("testFindRootStem");
    StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
  } // public void testFindRootStem()

  public void testFindRootByName() {
    LOG.info("testFindRootByName");
    try {
      GrouperSession  s   = SessionHelper.getRootSession();
      Stem            frs = StemHelper.findRootStem(s);
      Stem            fbn = StemHelper.findByName(s, "");
      Assert.assertTrue("frs == fbn", frs.equals(fbn));
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testFindRootByName()

  // TESTS //  
  
  public void testFindAllByApproximateDisplayExtension_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateDisplayExtension_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("lower", "UPPER");
  
      assertEquals(
        "stems found by displayExtension",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayExtension( child.getDisplayExtension() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateDisplayExtension_whenUpperCaseInRegistry

  // TESTS //  
  
  public void testFindAllByApproximateDisplayName_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateDisplayName_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("lower", "UPPER");
  
      assertEquals(
        "stems found by displayName",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayName( child.getDisplayName() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateDisplayName_whenUpperCaseInRegistry

  // TESTS //  
  
  public void testFindAllByApproximateExtension_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateExtension_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("UPPER", "lower");
  
      assertEquals(
        "stems found by extension",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateExtension( child.getExtension() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateExtension_whenUpperCaseInRegistry

  // TESTS //  
  
  public void testFindAllByApproximateName_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateName_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("UPPER", "lower");
  
      assertEquals(
        "stems found by name",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateName( child.getName() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateName_whenUpperCaseInRegistry

}

