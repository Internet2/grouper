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
import  edu.internet2.middleware.grouper.internal.dto.GrouperDTO;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;

/** 
 * Base Grouper API class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperAPI.java,v 1.3 2007-04-17 14:17:29 blair Exp $
 */
abstract class GrouperAPI {

  // PROTECTED INSTANCE VARIABLES //
  protected GrouperDTO      dto;
  protected GrouperSession  s;


  // CONSTRUCTORS //

  // @since   1.2.0
  protected GrouperAPI() {
    super();
  } // protected GrouperAPI() 


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set setSession(GrouperSession s, Iterator it) {
    Set         objects = new LinkedHashSet();
    GrouperAPI  api;
    while (it.hasNext()) {
      api = (GrouperAPI) it.next();
      api.setSession(s);
      objects.add(api);
    }
    return objects;
  } // protected static Set setSession(s, it)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  // FIXME 20070416 visibility
  public GrouperDTO getDTO() 
    throws  IllegalStateException
  {
    if (this.dto == null) {
      throw new IllegalStateException( "null dto in class " + this.getClass().getName() );
    }
    return this.dto;
  } 

  // @since   1.2.0
  protected GrouperSession getSession() 
    throws  IllegalStateException
  {
    if (this.s == null) {
      throw new IllegalStateException( "null session in class " + this.getClass().getName() );
    }
    return this.s;
  } // protected GrouperSession getSession()

  // @since   1.2.0
  // FIXME 20070416 !!!
  public void setDTO(GrouperDTO dto) {
    this.dto = dto;
  } 

  // @since   1.2.0
  protected void setSession(GrouperSession s) {
    if (s == null) {
      throw new IllegalStateException( "null session in class " + this.getClass().getName() );
    }
    this.s = s;
  } // protected void setSession(s)

} // abstract class GrouperAPI

