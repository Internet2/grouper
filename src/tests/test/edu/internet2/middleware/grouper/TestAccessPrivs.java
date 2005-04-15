/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * $Id: TestAccessPrivs.java,v 1.37 2005-04-15 05:29:32 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;


public class TestAccessPrivs extends TestCase {

  public TestAccessPrivs(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
  }

  protected void tearDown () {
    // Nothing -- Yet
  }


  /*
   * TESTS
   */
  
  public void testHas0() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );

    // Assert current privs
    List privs = s.access().has(s, ns0);
    Assert.assertTrue("privs == 6 " + privs.size(), privs.size() == 6);
    // Because we are connected as root, everything will return true
    Assert.assertTrue(
      "has ADMIN",  s.access().has(s, ns0, Grouper.PRIV_ADMIN)
    );
    Assert.assertTrue(
      "has OPTIN",  s.access().has(s, ns0, Grouper.PRIV_OPTIN)
    );
    Assert.assertTrue(
      "has OPTOUT",  s.access().has(s, ns0, Grouper.PRIV_OPTOUT)
    );
    Assert.assertTrue(
      "has READ",  s.access().has(s, ns0, Grouper.PRIV_READ)
    );
    Assert.assertTrue(
      "has UPDATE", s.access().has(s, ns0, Grouper.PRIV_UPDATE)
    );
    Assert.assertTrue(
      "has VIEW",  s.access().has(s, ns0, Grouper.PRIV_VIEW)
    );

    // We're done
    s.stop();
  }

  public void testHas1() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create g
    GrouperGroup g0  = GrouperGroup.create(
                         s, Constants.g0s, Constants.g0e
                       );

    // Assert current privs
    List privs = s.access().has(s, g0);
    Assert.assertTrue("privs == 6", privs.size() == 6);
    // Because we are connected as root, everything will return true
    Assert.assertTrue(
      "has ADMIN",  s.access().has(s, g0, Grouper.PRIV_ADMIN)
    );
    Assert.assertTrue(
      "has OPTIN",  s.access().has(s, g0, Grouper.PRIV_OPTIN)
    );
    Assert.assertTrue(
      "has OPTOUT",  s.access().has(s, g0, Grouper.PRIV_OPTOUT)
    );
    Assert.assertTrue(
      "has READ",  s.access().has(s, g0, Grouper.PRIV_READ)
    );
    Assert.assertTrue(
      "has UPDATE", s.access().has(s, g0, Grouper.PRIV_UPDATE)
    );
    Assert.assertTrue(
      "has VIEW",  s.access().has(s, g0, Grouper.PRIV_VIEW)
    );

    // We're done
    s.stop();
  }

}

