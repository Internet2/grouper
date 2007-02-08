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
import  java.lang.reflect.*;
import  java.util.*;

/**
 * Grouper Utility Class.
 * @author  blair christensen.
 * @version $Id: U.java,v 1.12 2007-02-08 16:25:25 blair Exp $
 * @since   1.0
 */
class U {

  // PRIVATE CLASS CONSTANTS //
  private static final String Q_CLOSE   = "'";
  private static final String Q_OPEN    = "'";
  private static final String QP_CLOSE  = ") ";
  private static final String QP_OPEN   = "(";


  // PROTECTED CLASS METHODS //
  
  // This isn't the best place for this but until I have a better idea...
  // @since   1.2.0
  protected static String internal_constructName(String stem, String extn) {
    // TODO 20061018 I should perform validation here, no?
    if (stem.equals(Stem.ROOT_EXT)) {
      return extn;
    }
    return stem + Stem.ROOT_INT + extn;
  } // protected static String internal_constructName(stem, extn)
 
  // @since   1.2.0
  protected static String internal_q(boolean input) {
    return U.internal_q( Boolean.toString(input) );
  } // protected static String internal_q(input)
 
  // @since   1.2.0
  protected static String internal_q(String input) {
    return Q_OPEN + input + Q_CLOSE;
  } // protected static String internal_q(input)

  // @since   1.2.0
  protected static String internal_qp(String input) {
    if (Validator.internal_isNotNullOrBlank(input)) {
      return QP_OPEN + input + QP_CLOSE;
    }
    return GrouperConfig.EMPTY_STRING;
  } // protected static String internal_qp(input)

  // @since   1.2.0
  protected static Object internal_realizeInterface(String name) 
    throws  GrouperRuntimeException
  {
    try {
      Class       classType   = Class.forName(name);
      Class[]     paramsClass = new Class[] { };
      Constructor con         = classType.getDeclaredConstructor(paramsClass);
      Object[]    params      = new Object[] { };
      return con.newInstance(params);
    }
    catch (Exception e) {
      String msg = E.CANNOT_REALIZE_INTERFACE + name + ": " + e.getMessage();
      ErrorLog.fatal(PrivilegeResolver.class, msg);
      throw new GrouperRuntimeException(msg, e);
    }
  } // protected static Object internal_realizeInterface(name)

  // @since   1.2.0
  protected static Set internal_setMembershipSessions(GrouperSession s, List l) {
    Membership  ms;
    Set         mships  = new LinkedHashSet();
    Iterator    iter    = l.iterator();
    while (iter.hasNext()) {
      ms = (Membership) iter.next();
      ms.setSession(s);
      mships.add(ms);
    }
    return mships;
  } // protected static Set internal_setMembershipSessions(s, l)

} // class U

