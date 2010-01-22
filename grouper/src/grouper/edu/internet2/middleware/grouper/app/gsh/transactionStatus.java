
package edu.internet2.middleware.grouper.app.gsh;
import java.util.Set;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;

/**
 * Transasction activities
 * <p/>
 * @author  chris hyzer
 * @version $Id: transactionStatus.java,v 1.1 2008-09-22 15:06:40 mchyzer Exp $
 * @since   0.0.1
 */
public class transactionStatus {

  /**
   * Transaction status, return the number of open transactions
   * <p/>
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

