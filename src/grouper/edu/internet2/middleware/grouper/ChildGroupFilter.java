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
 * @version $Id: ChildGroupFilter.java,v 1.5 2008-06-24 06:07:03 mchyzer Exp $
 * @since   1.2.1
 */
public class ChildGroupFilter extends BaseQueryFilter {


  private Stem    ns;
  
  
  /**
   * @param   ns      Retrieves all child groups beneath <i>stem</i>.
   * @throws  IllegalArgumentException if <i>ns</i> is null.
   * @since   1.2.1
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
   * @since   1.2.1
   */
  public Set<Group> getResults(GrouperSession s) 
    throws QueryException
  {
    return (Set<Group>)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        GrouperSession.validate(grouperSession);
        Set<Group> results = ns.getChildGroups(Stem.Scope.SUB);
        return results;
      }
      
    });
  } 

}

