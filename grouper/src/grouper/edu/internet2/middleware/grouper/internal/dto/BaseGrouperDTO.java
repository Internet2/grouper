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

package edu.internet2.middleware.grouper.internal.dto;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAO;

/** 
 * Base {@link GrouperDTO} implementation.
 * <p/>
 * @author  blair christensen.
 * @version $Id: BaseGrouperDTO.java,v 1.1 2007-04-17 14:17:29 blair Exp $
 * @since   1.2.0
 */
public abstract class BaseGrouperDTO implements GrouperDTO {
  // FIXME 20070416 visibility - and methods

  // CONSTRUCTORS //

  // @since   1.2.0
  public BaseGrouperDTO() {
    super(); // So extending classes don't need to define this
  } 


  // PUBLIC ABSTRACT METHODS //
  
  // @since   1.2.0
  public abstract GrouperDAO getDAO();

} 

