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

package edu.internet2.middleware.grouper;

/** 
 * Base {@link GrouperDTO} implementation.
 * <p/>
 * @author  blair christensen.
 * @version $Id: BaseGrouperDTO.java,v 1.2 2007-04-12 15:40:41 blair Exp $
 * @since   1.2.0
 */
abstract class BaseGrouperDTO implements GrouperDTO {

  // CONSTRUCTORS //

  // @since   1.2.0
  protected BaseGrouperDTO() {
    super(); // So extending classes don't need to define this
  } // protected BaseGrouperDTO()


  // PROTECTED ABSTRACT METHODS //
  
  // @since   1.2.0
  protected abstract GrouperDAO getDAO();

} // abstract class BaseGrouperDTO implements GrouperDTO

