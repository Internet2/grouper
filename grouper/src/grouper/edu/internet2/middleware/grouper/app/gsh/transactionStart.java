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
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;

/**
 * Start a transaction
 * <p/>
 * @author  chris hyzer
 * @version $Id: transactionStart.java,v 1.1 2008-09-22 15:06:40 mchyzer Exp $
 * @since   0.0.1
 */
public class transactionStart {

  /**
   * Start a transaction
   * <p/>
   * @param   interpreter           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param grouperTransactionTypeString to use for starting transaction, must be a 
   * GrouperTransactionType enum
   * @return  instructions for use
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, 
      String grouperTransactionTypeString) 
    throws  GrouperShellException {
    GrouperShell.setOurCommand(interpreter, true);
    GrouperTransactionType grouperTransactionType = GrouperTransactionType
      .valueOfIgnoreCase(grouperTransactionTypeString);
    HibernateSession._internal_hibernateSession(grouperTransactionType);
    interpreter.println("Started transaction index: " + (HibernateSession._internal_staticSessions().size()-1));
    return "";
  }

}

