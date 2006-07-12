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
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestBug0.java,v 1.2 2006-07-12 23:50:24 blair Exp $
 * @since   1.0
 */
public class TestBug0 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestBug0.class);

  public TestBug0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // Gary has an XML load file that throws an error when deleting memberships
  // from gC when the load file is reapplied.  I can't yet replicate it -
  // outside of the **full** XML file - at this point.  Bah.
  public void testMysteryError0() {
    LOG.info("testMysteryError0");
    try {
      R       r     = R.populateRegistry(1, 4, 2);
      Group   gA    = r.getGroup("a", "a");   
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Group   gD    = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");

      gA.addMember(subjA);
      gA.addMember(subjB);

      gB.addMember(     gC.toSubject() );
      gB.addMember(     gD.toSubject() );

      // (ns) [i2]      qsuob
      // (ns)           qsuob:faculties
      // (ns) [i2:a]    qsuob:faculties:artf
      // (g)  [i2:a:a]  qsuob:faculties:artf:staff
      // (g)  [i2:a:b]  qsuob:all
      // (m)  [i2:a:a]  + [subjA]   qsuob:faculties:artf:staff + iawi 
      // (m)  [i2:a:a]  + [subjB]   qsuob:faculties:artf:staff + iata 
      gA.deleteMember(subjA);
      gA.deleteMember(subjB);
      gA.addMember(subjA);
      gA.addMember(subjB);

      // (m)  [i2:a:b]  + [i2:a:c]  qsuob:all + qsuob:all_students
      // (m)  [i2:a:b]  + [i2:a:d]  qsuob:all + qsuob:all_academic_staff
      gB.deleteMember(  gC.toSubject() );
      gB.deleteMember(  gD.toSubject() );
      gB.addMember(     gC.toSubject() );
      gB.addMember(     gD.toSubject() );

      // TODO Do these need to be done in different session contexts?
      //      Or different objects?

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testMysteryError0()

} // public class TestBug0

