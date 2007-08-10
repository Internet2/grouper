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

package edu.internet2.middleware.grouper;
import  java.util.Set;


/** 
 * Query filter that retrieves child groups.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ChildGroupFilter.java,v 1.2 2007-08-10 13:19:14 blair Exp $
 * @since   @HEAD@
 */
public class ChildGroupFilter extends BaseQueryFilter {


  private Stem    ns;
  
  
  /**
   * @param   ns      Retrieves all child groups beneath <i>stem</i>.
   * @throws  IllegalArgumentException if <i>ns</i> is null.
   * @since   @HEAD@
   */
  public ChildGroupFilter(Stem ns) 
    throws  IllegalArgumentException
  {
    if (ns == null) { // TODO 20070802 ParameterHelper
      throw new IllegalArgumentException("null Stem");
    }
    this.ns = ns;
  } 


  /**
   * @see     BaseQueryFilter#getResults(GrouperSession)
   * @since   @HEAD
   */
  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    GrouperSession.validate(s);
    GrouperSession orig = ns.getSession();
    this.ns.setSession(s);
    Set results = ns.getChildGroups(Stem.Scope.SUB);
    this.ns.setSession(orig);
    return results;
  } 

}

