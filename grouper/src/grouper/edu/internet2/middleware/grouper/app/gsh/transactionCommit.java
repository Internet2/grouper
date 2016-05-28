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
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Commit a transaction
 * 
 * @author  chris hyzer
 * @version $Id: transactionCommit.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class transactionCommit {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(transactionCommit.class);

  /**
   * Commit a transaction
   * 
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

