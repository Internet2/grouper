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

import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestMember0.java,v 1.6 2008-10-20 14:41:20 mchyzer Exp $
 */
public class TestMember0 extends TestCase {

  /**
   * 
   */
  private static final Log LOG = GrouperUtil.getLog(TestMember0.class);

  /**
   * 
   * @param name
   */
  public TestMember0(String name) {
    super(name);
  }

  /**
   * 
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  /**
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown () {
    LOG.debug("tearDown");
  }

  /**
   * 
   */
  public void testFailCanCreateWhenNull() {
    LOG.info("testFailCanCreateWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canCreate(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  /**
   * 
   * @throws Exception
   */
  public void testFindBySubjectId() throws Exception {
    GrouperDAOFactory.getFactory().getMember().findBySubject("GrouperSystem");
    GrouperDAOFactory.getFactory().getMember().findBySubject("GrouperSystem", "g:isa");
    try {
      GrouperDAOFactory.getFactory().getMember().findBySubject("sflkjlksjflksjdlksd");
      fail("Shouldnt find this");
    } catch (MemberNotFoundException snfe) {
      //good
    }
  }
  
}

