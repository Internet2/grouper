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

package edu.internet2.middleware.grouper.queryFilter;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.BaseQueryFilter;
import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.GrouperDAOFactory;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.QueryException;
import edu.internet2.middleware.grouper.QueryFilter;
import edu.internet2.middleware.grouper.Stem;


/** 
 * Query by stem attribute (granted they arent real attributes as in another table,
 * but the 4 name columns).
 * <p/>
 * @author mchyzer
 * @version $Id: StemAttributeFilter.java,v 1.1.2.1 2008-06-07 16:11:55 mchyzer Exp $
 */
public class StemAttributeFilter extends BaseQueryFilter {
  
  /** attribute can be name, extension, displayName, displayExtension */
  private String  attr;
  
  /** stem to filter in */
  private Stem    ns;
  
  /** value of attribute to check */
  private String  val;


  /**
   * {@link QueryFilter} that returns groups matching the specified
   * attribute specification.
   * <p>
   * This performs a substring, lowercased query on <i>attribute</i>.
   * </p>
   * @param   attr  Search on this attribute.
   * @param   value Search for this value.
   * @param   ns    Restrict results to within this stem.
   */
  public StemAttributeFilter(String attr, String value, Stem ns) {
    this.attr = attr;
    this.ns   = ns;
    this.val  = value;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.BaseQueryFilter#getResults(edu.internet2.middleware.grouper.GrouperSession)
   */
  public Set getResults(GrouperSession s) throws QueryException {
    GrouperSession.validate(s);
    Set candidates = null;

    //manually find the attribute and filter
    if (StringUtils.equals(attr, GrouperConfig.ATTR_DISPLAY_EXTENSION)) {
      candidates = GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayExtension(this.val); 
    } else if (StringUtils.equals(attr, GrouperConfig.ATTR_DISPLAY_NAME)) {
      candidates = GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayName(this.val); 
    } else if (StringUtils.equals(attr, GrouperConfig.ATTR_EXTENSION)) {
      candidates = GrouperDAOFactory.getFactory().getStem().findAllByApproximateExtension(this.val); 
    } else if (StringUtils.equals(attr, GrouperConfig.ATTR_NAME)) {
      candidates = GrouperDAOFactory.getFactory().getStem().findAllByApproximateName(this.val); 
    } else {
      throw new QueryException("Illegal attribute to query stems: '" + attr + "', must be in (" + 
          GrouperConfig.ATTR_DISPLAY_EXTENSION + ", " + GrouperConfig.ATTR_DISPLAY_NAME + ", " + GrouperConfig.ATTR_EXTENSION
          + ", " + GrouperConfig.ATTR_NAME + ")");
    }

    Set results     = this.filterByScope(this.ns, candidates);
    return results;
  } // public Set getResults(s)

}

