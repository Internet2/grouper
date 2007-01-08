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
import  org.apache.commons.lang.time.*;

/** 
 * Grouper log helper class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: LogHelper.java,v 1.5 2007-01-08 18:04:06 blair Exp $
 * @since   1.0
 */
class LogHelper {

  // PRIVATE CLASS CONSTANTS //
  private static final String OPEN_B  = "[";
  private static final String OPEN_P  = " (";
  private static final String CLOSE_B = "] ";
  private static final String CLOSE_P = "ms)";


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static String internal_formatClass(Class c) {
    return OPEN_B + c.getName() + CLOSE_B;
  } // protected static String internal_formatClass(c)

  // @since   1.2.0
  protected static String internal_formatMsg(GrouperSession s, String msg) {
    return internal_formatSession(s) + msg;
  } // protected static String internal_formatMsg(s, msg)

  // @since   1.2.0
  protected static String internal_formatSession(GrouperSession s) {
    return internal_formatSession(s.toString());
  } // protected static String internal_formatSession(s)

  // @since   1.2.0
  protected static String internal_formatSession(String s) {
    return OPEN_B + s + CLOSE_B;
  } // protected static String internal_formatSession(s)

  // @since   1.2.0
  protected static String internal_formatStopWatch(StopWatch sw) {
    return OPEN_P + sw.getTime() + CLOSE_P;
  } // protected static String internal_formatStopWatch(sw)

}

