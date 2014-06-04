/*******************************************************************************
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: JdbcConnectionBean.java,v 1.1 2008-09-16 05:12:09 mchyzer Exp $
 */
package edu.internet2.middleware.subject.provider;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * bean that wraps connections
 */
public interface JdbcConnectionBean {

  /**
   * get a connection (dont close this when done, just call "doneWithConnection()"
   * @return the connection
   * @throws SQLException if there is a problem
   */
  public Connection connection() throws SQLException;
  
  /**
   * call this when the connection is done.  This will do any cleanup
   * this is a null-safe method.  
   * @throws SQLException if there is a problem
   */
  public void doneWithConnection() throws SQLException;

  /**
   * call this when the connection is not needed, in the finally block
   * this might return to pool.  In general, this shouldnt throw exceptions
   * since it is done in a finally block, it should only log them
   */
  public void doneWithConnectionFinally();

  /**
   * call this when the connection is done but there was an error, will pass
   * an exception.  This should do whatever and rethrow the exception as runtime
   * @param t 
   */
  public void doneWithConnectionError(Throwable t);
}
