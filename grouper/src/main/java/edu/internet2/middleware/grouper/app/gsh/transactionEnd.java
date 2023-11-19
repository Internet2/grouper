/**
 * Copyright 2014 Internet2
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
 */
/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * 
 */

package edu.internet2.middleware.grouper.app.gsh;
import org.apache.commons.logging.Log;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * End a transaction
 * <p/>
 * @author  chris hyzer
 * @version $Id: transactionEnd.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class transactionEnd {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(transactionCommit.class);

  /**
   * End a transaction
   * <p/>
   * @param   interpreter           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @return  instructions for use
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack) 
      throws  GrouperShellException {
    GrouperShell.setOurCommand(interpreter, true);
    return invoke(null);
  }

  /**
   * End a transaction
   * <p/>
   * @param   grouperSession
   * @return  instructions for use
   */
  public static String invoke(GrouperSession grouperSession) {
    int txIndex = HibernateSession._internal_staticSessions().size()-1;
    HibernateSession hibernateSession = HibernateSession._internal_hibernateSession();
    if (hibernateSession == null) {
      String error = "Cant end a transaction since none in scope";
      System.out.println(error);
      LOG.error(error);
      throw new GrouperShellException(error);
    }
    try {
      HibernateSession._internal_hibernateSessionEnd(hibernateSession);
    } catch (Throwable t) {
      HibernateSession._internal_hibernateSessionCatch(hibernateSession, t);
    } finally {
      HibernateSession._internal_hibernateSessionFinally(hibernateSession);
    }
    System.out.println("Ended transaction index: " + txIndex + ", " 
        + HibernateSession._internal_staticSessions().size() + " remaining");
    return "";
  }
}

