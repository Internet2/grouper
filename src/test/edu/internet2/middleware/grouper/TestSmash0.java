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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestSmash0.java,v 1.2 2006-10-11 13:10:10 blair Exp $
 * @since   1.1.0
 */
public class TestSmash0 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestSmash0.class);

  public TestSmash0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testAdd100Stems100Stems100Stems() {
    LOG.info("testAdd100Stems100Stems100Stems");
    try {
      GrouperSession  s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      Stem            root  = StemFinder.findRootStem(s);
      int             cnt   = 0;
      Stem            top, child;
      for (int i = 0; i < 100; i++) {
        top = root.addChildStem("top " + i + " [" + cnt++ + "]", "top " + i);
        for (int j = 0; j < 100; j ++) {
          child = top.addChildStem("child " + j + " [" + cnt++ + "]", "child " + j);
          for (int k = 0; k < 100; k ++) {
            child.addChildStem("grandchild " + k + " [" + cnt++ + "]", "grandchild " + k);
          }
        }
      }
      s.stop();
      Assert.assertTrue(true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAdd100Stems100Stems100Stems()

} // public class TestSmash0

