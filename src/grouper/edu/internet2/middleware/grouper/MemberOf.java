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
import  edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import  java.util.Set;

/**
 * <b>THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b> 
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberOf.java,v 1.58 2007-04-19 15:39:50 blair Exp $
 * @since   1.2.0
 */
interface MemberOf {

  // TODO 20070419 just pass in DTOs?
  
  /**
   * @since   1.2.0
   * @throws IllegalStateException
   */
  public void addComposite(GrouperSession s, Group g, Composite c)
    throws  IllegalStateException;

  /**
   * @since   1.2.0
   * @throws IllegalStateException
   */
  public void addImmediate(GrouperSession s, Group g, Field f, MemberDTO _m)
    throws  IllegalStateException;  

  /**
   * @since   1.2.0
   * @throws IllegalStateException
   */
  public void addImmediate(GrouperSession s, Stem ns, Field f, MemberDTO _m)
    throws  IllegalStateException; 

  /**
   * @since   1.2.0
   * @throws IllegalStateException
   */
  public void deleteComposite(GrouperSession s, Group g, Composite c)
    throws  IllegalStateException;

  /**
   * @since   1.2.0
   * @throws IllegalStateException
   */
  public void deleteImmediate(GrouperSession s, Group g, MembershipDTO _ms, MemberDTO _m)
    throws  IllegalStateException;

  /**
   * @since   1.2.0
   * @throws IllegalStateException
   */
  public void deleteImmediate(GrouperSession s, Stem ns, MembershipDTO _ms, MemberDTO _m)
    throws  IllegalStateException;

  /**
   * @since   1.2.0
   */
  public Set getSaves();
  
  /**
   * @since   1.2.0
   */
  public Set getModifiedGroups();
  
  
  /**
   * @since    1.2.0
   */
  public Set getModifiedStems();
  
  /**
   * @since   1.2.0
   */
  public Set getDeletes();
  
}
