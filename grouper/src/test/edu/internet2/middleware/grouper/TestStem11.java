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
import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestStem11.java,v 1.10 2008-09-29 03:38:27 mchyzer Exp $
 * @since   1.0.1
 */
public class TestStem11 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestStem11.class);

  public TestStem11(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    //TestRunner.run(new TestStem11("testDelMembersWithADMIN"));
    TestRunner.run(TestStem11.class);
  }

  // BUGFIX:GCODE:10
  public void testGetPrivsStemmersAndCreatorsAsNonRoot() {
    LOG.info("testGetPrivsStemmersAndCreatorsAsNonRoot");
    try {
      final R r = R.populateRegistry(0, 0, 1);
      final Subject subjA = r.getSubject("a");
      GrouperSession s = GrouperSession.start(subjA);


      T.amount("privs before grant"   , 0, r.ns.getPrivs(subjA).size());
      T.amount("stemmers before grant", 1, r.ns.getStemmers().size()  );
      T.amount("creators before grant", 0, r.ns.getCreators().size()  );

      GrouperSession.callbackGrouperSession(r.rs, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            r.ns.grantPriv(subjA, NamingPrivilege.STEM);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          return null;
        }
        
      });

      T.amount("privs after grant"    , 1, r.ns.getPrivs(subjA).size());
      T.amount("stemmers after grant" , 2, r.ns.getStemmers().size()  );
      T.amount("creators after grant" , 0, r.ns.getCreators().size()  );

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetPrivsStemmersAndCreatorsAsNonRoot()

}

