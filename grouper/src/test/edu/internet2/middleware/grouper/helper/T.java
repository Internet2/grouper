/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

package edu.internet2.middleware.grouper.helper;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;

/**
 * @author  blair christensen.
 * @version $Id: T.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.0
 */
public class T extends GrouperTest {

  // public CLASS CONSTANTS //
  public static final int DATE_OFFSET = 10;


  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = GrouperUtil.getLog(T.class);

  
  // public CLASS METHODS //

  // General //

  // @since   1.0
  public static void amount(String msg, int exp, int got) { 
    LOG.debug("amount()");
    assertTrue(msg + " - exp[" + exp + "] got[" + got + "]", exp == got);
  } // public static void amount(msg, exp, got)

  // @since   1.0
  public static void e(Exception e) {
    //CH 20080826 This hides exceptions, just throw
    //new GrouperTest().unexpectedException(e);
    LOG.error("Error in test", e);
    throw new RuntimeException(e);
  } // public static void unexpectedException(e)

  // @since   1.1.0
  public static void ok(String msg) {
    assertTrue("OK: " + msg, true);
  } // prortected static void ok(msg)

  // @since   1.0
  public static void string(String msg, String exp, String got) { 
    LOG.debug("string()");
    assertTrue(msg + " - exp[" + exp + "] got[" + got + "]", exp.equals(got));
  } // public static void string(msg, exp, got)


  // Groups //

  // @since 1.0
  public static void getEffectiveMemberships(Group g, int exp) {
    LOG.debug("getEffectiveMemberships()");
    T.amount(g.getName() + " eff mships", exp, g.getEffectiveMemberships().size());
  } // public static void getEffectiveMemberships(g, exp)

  // @since 1.0
  public static void getImmediateMemberships(Group g, int exp) {
    LOG.debug("getImmediateMemberships()");
    T.amount(g.getName() + " imm mships", exp, g.getImmediateMemberships().size());
  } // public static void getImmediateMemberships(g, exp)

  // @since 1.0
  public static void getMemberships(Group g, int exp) {
    LOG.debug("getMemberships()");
    T.amount(g.getName() + " mships", exp, g.getMemberships().size());
  } // public static void getMemberships(g, exp)

  // SubjectFinder //

  // @since   1.0
  public static void getSource(String id) {
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
  } // public static void getSource(id)

} // public class T extends GrouperTest

