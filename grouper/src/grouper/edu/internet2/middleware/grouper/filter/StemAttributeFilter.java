/**
 * Copyright 2014 Internet2
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
 */
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

package edu.internet2.middleware.grouper.filter;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;


/** 
 * Query by stem attribute (granted they arent real attributes as in another table,
 * but the 4 name columns).
 * <p/>
 * @author mchyzer
 * @version $Id: StemAttributeFilter.java,v 1.3 2009-01-02 06:57:11 mchyzer Exp $
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
   * @see edu.internet2.middleware.grouper.filter.BaseQueryFilter#getResults(edu.internet2.middleware.grouper.GrouperSession)
   */
  public Set getResults(GrouperSession s) throws QueryException {
    try {
      return (Set)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          GrouperSession.validate(grouperSession);
          Set results;
  
          if (ns.isRootStem()) {
            //manually find the attribute and filter
            if (StringUtils.equals(attr, GrouperConfig.ATTRIBUTE_DISPLAY_EXTENSION)) {
              results = removeRootStem(GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayExtension(StemAttributeFilter.this.val)); 
            } else if (StringUtils.equals(attr, GrouperConfig.ATTRIBUTE_DISPLAY_NAME)) {
              results = removeRootStem(GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayName(StemAttributeFilter.this.val)); 
            } else if (StringUtils.equals(attr, GrouperConfig.ATTRIBUTE_EXTENSION)) {
              results = removeRootStem(GrouperDAOFactory.getFactory().getStem().findAllByApproximateExtension(StemAttributeFilter.this.val)); 
            } else if (StringUtils.equals(attr, GrouperConfig.ATTRIBUTE_NAME)) {
              results = removeRootStem(GrouperDAOFactory.getFactory().getStem().findAllByApproximateName(StemAttributeFilter.this.val)); 
            } else {
              throw new GrouperSessionException(new QueryException("Illegal attribute to query stems: '" + attr + "', must be in (" + 
                  GrouperConfig.ATTRIBUTE_DISPLAY_EXTENSION + ", " + GrouperConfig.ATTRIBUTE_DISPLAY_NAME + ", " + GrouperConfig.ATTRIBUTE_EXTENSION
                  + ", " + GrouperConfig.ATTRIBUTE_NAME + ")"));
            }
          } else {
            //manually find the attribute and filter
            if (StringUtils.equals(attr, GrouperConfig.ATTRIBUTE_DISPLAY_EXTENSION)) {
              results = GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayExtension(StemAttributeFilter.this.val, getStringForScope(ns)); 
            } else if (StringUtils.equals(attr, GrouperConfig.ATTRIBUTE_DISPLAY_NAME)) {
              results = GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayName(StemAttributeFilter.this.val, getStringForScope(ns)); 
            } else if (StringUtils.equals(attr, GrouperConfig.ATTRIBUTE_EXTENSION)) {
              results = GrouperDAOFactory.getFactory().getStem().findAllByApproximateExtension(StemAttributeFilter.this.val, getStringForScope(ns)); 
            } else if (StringUtils.equals(attr, GrouperConfig.ATTRIBUTE_NAME)) {
              results = GrouperDAOFactory.getFactory().getStem().findAllByApproximateName(StemAttributeFilter.this.val, getStringForScope(ns)); 
            } else {
              throw new GrouperSessionException(new QueryException("Illegal attribute to query stems: '" + attr + "', must be in (" + 
                  GrouperConfig.ATTRIBUTE_DISPLAY_EXTENSION + ", " + GrouperConfig.ATTRIBUTE_DISPLAY_NAME + ", " + GrouperConfig.ATTRIBUTE_EXTENSION
                  + ", " + GrouperConfig.ATTRIBUTE_NAME + ")"));
            }
          }
  
          return results;
        }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof QueryException) {
        throw (QueryException)gse.getCause();
      }
      throw gse;
    }
  } // public Set getResults(s)

}

