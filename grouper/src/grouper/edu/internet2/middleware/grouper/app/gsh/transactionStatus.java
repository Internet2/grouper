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
package edu.internet2.middleware.grouper.app.gsh;
import java.util.Set;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;

/**
 * Transasction activities
 * 
 * @author  chris hyzer
 * @version $Id: transactionStatus.java,v 1.1 2008-09-22 15:06:40 mchyzer Exp $
 * @since   0.0.1
 */
public class transactionStatus {

  /**
   * Transaction status, return the number of open transactions
   * 
   * @param   interpreter           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @return  instructions for use
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static int invoke(Interpreter interpreter, CallStack stack) 
    throws  GrouperShellException {
    GrouperShell.setOurCommand(interpreter, true);
    Set<HibernateSession> hibernateSessions = HibernateSession._internal_staticSessions();
    int i = 0;
    StringBuilder result = new StringBuilder();
    for (HibernateSession hibernateSession : hibernateSessions) {
      if (i != 0) {
        result.append("\n");
      }
      result.append(i).append(": ").append(hibernateSession.getGrouperTransactionType())
        .append(", readonly? ").append(hibernateSession.isReadonly()).append( ", tx active? ")
        .append(hibernateSession.isTransactionActive());
      i++;
    }
    if (hibernateSessions.size() > 0) {
      result.append(" <current>");
    } else {
      result.append("No current trasactions in scope");
    }
    interpreter.println(result.toString());
    return hibernateSessions.size();
  }

}

