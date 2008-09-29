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
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Commit a transaction
 * <p/>
 * @author  chris hyzer
 * @version $Id: transactionCommit.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class transactionCommit {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(transactionCommit.class);

  /**
   * Commit a transaction
   * <p/>
   * @param   interpreter           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param grouperCommitTypeString to use for starting transaction, must be a 
   * GrouperCommitType enum
   * @return  instructions for use
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, 
      String grouperCommitTypeString) 
    throws  GrouperShellException {
    GrouperShell.setOurCommand(interpreter, true);
    GrouperCommitType grouperCommitType = GrouperCommitType
      .valueOfIgnoreCase(grouperCommitTypeString);
    HibernateSession hibernateSession = HibernateSession._internal_hibernateSession();
    if (hibernateSession == null) {
      String error = "Cant commit a transaction since none in scope";
      interpreter.println(error);
      LOG.error(error);
      throw new GrouperShellException(error);
    }
    hibernateSession.commit(grouperCommitType);
    interpreter.println("Committed transaction index: " + (HibernateSession._internal_staticSessions().size()-1));
    return "";
    
  }

}

