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
import  java.lang.reflect.*;
import  java.util.*;

/**
 * Grouper Utility Class.
 * @author  blair christensen.
 * @version $Id: U.java,v 1.3 2006-08-17 19:25:16 blair Exp $
 * @since   1.0
 */
class U {

  // PRIVATE CLASS CONSTANTS //
  private static final String Q_CLOSE = "'";
  private static final String Q_OPEN  = "'";


  // PROTECTED CLASS METHODS //
  // @since 1.0
  protected static String q(String input) {
    return Q_OPEN + input + Q_CLOSE;
  } // protected static String q(input)

  // @since   1.1.0
  protected static Object realizeInterface(String name) 
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
  } // protected static Object realizeInterface(name)

  // @since 1.0.1
  protected static Set setMembershipSessions(GrouperSession s, List l) {
    // TODO Do I need to validate the session?
    Membership  ms;
    Set         mships  = new LinkedHashSet();
    Iterator    iter    = l.iterator();
    while (iter.hasNext()) {
      ms = (Membership) iter.next();
      ms.setSession(s);
      mships.add(ms);
    }
    return mships;
  } // protected static Set setMembershipSessions(s, l)

} // class U

