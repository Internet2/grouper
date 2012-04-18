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
package edu.internet2.middleware.grouper.misc;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;


/**
 * Implement this (usually in an anonymous inner class) to get a 
 * reference to the grouper session object
 * @version $Id: GrouperSessionHandler.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 * @author mchyzer
 */

public interface GrouperSessionHandler {

  /**
   * This method will be called with the grouper session object to do 
   * what you wish.  
   * @param grouperSession is the grouper session, note, this grouperSession 
   * will be the same as passed in
   * @return the return value to be passed to return value of callback method
   * @throws GrouperSessionException 
   */
  public Object callback(GrouperSession grouperSession) throws GrouperSessionException;
}
