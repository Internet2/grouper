/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.GrouperSession;

/** 
 * Basic <code>GrouperSession</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: GrouperSessionDAO.java,v 1.7 2008-07-28 20:12:27 mchyzer Exp $
 * @since   1.2.0
 */
public interface GrouperSessionDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  void create(GrouperSession _s)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void delete(GrouperSession _s)
    throws  GrouperDAOException;

} 

