/**
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
 */
/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * enum of possible transaction types
 * 
 * @author mchyzer
 */
public enum GcTransactionType {
  
  /** use the current transaction if one exists, if not, create a new readonly tx.
   * Note, the enclosing transaction could be readonly or readwrite, and no error
   * will be thrown.  However, no matter what, this code cannot commit or rollback... */
  READONLY_OR_USE_EXISTING,
  
  /** even if in a current tx, do not use transactions */
  NONE,
  
  /** even if in the middle of a transaction, create a new readonly autonomous nested transaction.  Code
   * in this state cannot commit or rollback.
   */
  READONLY_NEW,
  
  /**
   * use the current transaction if one exists.  If there is a current transaction, it 
   * MUST be read/write or there will be an exception.  If there isnt a transaction in 
   * scope, then create a new read/write one.  If you do not commit at the end, and there
   * is a normal return (no exception), then the transaction will be committed if new, 
   * and not if reusing an existing one.  If there is an exception, and the tx is new, it will
   * be rolledback.  If there is an exception and the tx is reused, the tx will not be touched,
   * and the exception will propagate.
   */
  READ_WRITE_OR_USE_EXISTING,

  /**
   * even if in the middle of a transaction, create a new read/write autonomous nested transaction.
   * If this block is exited normally it will always commit.  If exception is thrown, it will 
   * always rollback.
   */
  READ_WRITE_NEW;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static GcTransactionType valueOfIgnoreCase(String string) {
    return GrouperClientUtils.enumValueOfIgnoreCase(GcTransactionType.class,string, false );
  }

}
