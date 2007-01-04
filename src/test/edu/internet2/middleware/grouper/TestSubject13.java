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
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestSubject13.java,v 1.2 2007-01-04 17:17:46 blair Exp $
 * @since   1.1.0
 */
public class TestSubject13 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestSubject13.class);

  public TestSubject13(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFindGrouperAllByIdentifierAndType() {
    LOG.info("testFindGrouperAllByIdentifierAndType");
    try {
      Subject subj = SubjectFinder.findByIdentifier(GrouperConfig.ALL, GrouperConfig.IST);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue("subj instanceof Subject", subj instanceof Subject);
      T.string("subj id"      , GrouperConfig.ALL       , subj.getId()              );
      T.string("subj type"    , GrouperConfig.IST       , subj.getType().getName()  );
      T.string("subj source"  , InternalSourceAdapter.ID, subj.getSource().getId()  );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindGrouperAllByIdentifierAndType()

} // public class TestSubject13

