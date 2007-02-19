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
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: T.java,v 1.12 2007-02-19 20:43:29 blair Exp $
 * @since   1.0
 */
public class T extends GrouperTest {

  // PROTECTED CLASS CONSTANTS //
  protected static final int DATE_OFFSET = 10;


  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(T.class);

  
  // PROTECTED CLASS METHODS //

  // General //

  // @since   1.0
  protected static void amount(String msg, int exp, int got) { 
    LOG.debug("amount()");
    assertTrue(msg + " - exp[" + exp + "] got[" + got + "]", exp == got);
  } // protected static void amount(msg, exp, got)

  // @since   1.0
  protected static void e(Exception e) {
    new GrouperTest().unexpectedException(e);
  } // protected static void unexpectedException(e)

  // @since   1.1.0
  protected static void ok(String msg) {
    assertTrue("OK: " + msg, true);
  } // prortected static void ok(msg)

  // @since   1.0
  protected static void string(String msg, String exp, String got) { 
    LOG.debug("string()");
    assertTrue(msg + " - exp[" + exp + "] got[" + got + "]", exp.equals(got));
  } // protected static void string(msg, exp, got)


  // Groups //

  // @since 1.0
  protected static void getEffectiveMemberships(Group g, int exp) {
    LOG.debug("getEffectiveMemberships()");
    T.amount(g.getName() + " eff mships", exp, g.getEffectiveMemberships().size());
  } // protected static void getEffectiveMemberships(g, exp)

  // @since 1.0
  protected static void getImmediateMemberships(Group g, int exp) {
    LOG.debug("getImmediateMemberships()");
    T.amount(g.getName() + " imm mships", exp, g.getImmediateMemberships().size());
  } // protected static void getImmediateMemberships(g, exp)

  // @since 1.0
  protected static void getMemberships(Group g, int exp) {
    LOG.debug("getMemberships()");
    T.amount(g.getName() + " mships", exp, g.getMemberships().size());
  } // protected static void getMemberships(g, exp)

  // SubjectFinder //

  // @since   1.0
  protected static void getSource(String id) {
    LOG.debug("getSource()");
    try {
      Source sa = SubjectFinder.getSource(id);
      assertNotNull( "sa not null"           , sa                    );
      assertTrue(    "sa instanceof Source"  , sa instanceof Source  );
      T.string("sa id", sa.getId(), id);
    }
    catch (SourceUnavailableException eSU) {
      fail("unexpected exception: " + eSU.getMessage());
    }
  } // protected static void getSource(id)

} // public class T extends GrouperTest

