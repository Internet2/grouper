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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import edu.internet2.middleware.grouper.exception.GrouperException;

/**
 * Generic Grouper DAO exception.
 * @author  blair christensen.
 * @version $Id: GrouperDAOException.java,v 1.5 2009-03-15 06:37:22 mchyzer Exp $
 * @since   1.2.0
 */
public class GrouperDAOException extends GrouperException {
  private static final long serialVersionUID = -7856283917603254749L;
  /**
   * @since   1.2.0
   */
  public GrouperDAOException() { 
    super(); 
  } // public GrouperDAOException()
  /**
   * @since   1.2.0
   */
  public GrouperDAOException(String msg) { 
    super(msg); 
  } // public GrouperDAOException(msg)
  /**
   * @since   1.2.0
   */
  public GrouperDAOException(String msg, Throwable cause) { 
    super(msg, cause); 
  } // public GrouperDAOException(msg, cause)
  /**
   * @since   1.2.0
   */
  public GrouperDAOException(Throwable cause) { 
    super(cause); 
  } // public GrouperDAOException(cause)

} 

