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
 * @version $Id: TestStem11.java,v 1.6 2007-01-08 16:43:56 blair Exp $
 * @since   1.0.1
 */
public class TestStem11 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestStem11.class);

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

  // BUGFIX:GCODE:10
  public void testGetPrivsStemmersAndCreatorsAsNonRoot() {
    LOG.info("testGetPrivsStemmersAndCreatorsAsNonRoot");
    try {
      R               r     = R.populateRegistry(0, 0, 1);
      Subject         subjA = r.getSubject("a");
      GrouperSession  s     = GrouperSession.start(subjA);

      r.ns.internal_setSession(s);

      T.amount("privs before grant"   , 0, r.ns.getPrivs(subjA).size());
      T.amount("stemmers before grant", 1, r.ns.getStemmers().size()  );
      T.amount("creators before grant", 0, r.ns.getCreators().size()  );

      r.ns.internal_setSession(r.rs);
      r.ns.grantPriv(subjA, NamingPrivilege.STEM);
      r.ns.internal_setSession(s);

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

