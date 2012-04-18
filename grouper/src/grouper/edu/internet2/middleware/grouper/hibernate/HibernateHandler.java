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
 * Created on Jun 21, 2005
 *
 */
package edu.internet2.middleware.grouper.hibernate;

import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

/**
 * Implement this (usually in an anonymous inner class) to get a 
 * reference to the hibernate session object
 * @version $Id: HibernateHandler.java,v 1.3 2009-02-06 16:33:18 mchyzer Exp $
 * @author mchyzer
 */

public interface HibernateHandler {

  /**
   * This method will be called with the hibernate session object to do 
   * what you wish.  Note, HibernateException or RuntimeExceptions can be
   * thrown by this method... others should be handled somehow..
   * @param hibernateHandlerBean holds the hibernate session and other things 
   * will be the same as passed in if it existed, else a new one
   * @return the return value to be passed to return value of callback method
   * @throws GrouperDAOException if there is a problem
   */
  public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException;
}
