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

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestSubject12.java,v 1.4 2008-09-29 03:38:27 mchyzer Exp $
 * @since   1.1.0
 */
public class TestSubject12 extends TestCase {

  private static final Log LOG = GrouperUtil.getLog(TestSubject12.class);

  public TestSubject12(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFindGrouperAllByIdentifier() {
    LOG.info("testFindGrouperAllByIdentifier");
    try {
      Subject subj = SubjectFinder.findByIdentifier(GrouperConfig.ALL);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue("subj instanceof Subject", subj instanceof Subject);
      T.string("subj id"      , GrouperConfig.ALL       , subj.getId()              );
      T.string("subj type"    , GrouperConfig.IST       , subj.getType().getName()  );
      T.string("subj source"  , InternalSourceAdapter.ID, subj.getSource().getId()  );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindGrouperAllByIdentifier()

} // public class TestSubject12

