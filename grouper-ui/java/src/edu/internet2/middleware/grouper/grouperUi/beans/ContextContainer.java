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
/**
 * @author mchyzer
 * $Id: ContextContainer.java,v 1.2 2009-10-11 22:04:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletContext;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * container for global things in the context attribute
 */
public class ContextContainer {
  
  /** singleton */
  private static ContextContainer instance = new ContextContainer();
  
  /**
   * return the isntance
   * @return the instance
   */
  public static ContextContainer instance() {
    return instance;
  }

  /**
   * store to session scope
   */
  public void storeToContext() {
    ServletContext servletContext = GrouperUiFilter.retrieveHttpServlet().getServletContext();
    if (servletContext.getAttribute("contextContainer") != instance) {
      servletContext.setAttribute("contextContainer", instance);
    }
  }
  
  /** cache of sources */
  private static GrouperCache<Boolean, List<Source>> sourcesCache = new GrouperCache<Boolean, List<Source>>(
      ContextContainer.class.getName() + ".sources", 100, false, 120, 120, false); 
  
  /**
   * available sourceIds for the upload form
   * @return the source ids
   */
  public List<Source> getSources() {
    try {
      List<Source> sources = sourcesCache.get(Boolean.TRUE);
      if (sources == null) {
        sources = new ArrayList<Source>(SourceManager.getInstance().getSources());
        
        //lets sort them by id
        Collections.sort(sources, new Comparator() {
  
          public int compare(Object o1, Object o2) {
            return ((Source)o1).getId().compareTo(((Source)o2).getId());
          }
        });
        
        sourcesCache.put(Boolean.TRUE, sources);
      }
      
      return sources;
    } catch (Exception e) {
      //TODO take out this try/catch when upgrading to grouper 1.5 and the new source api
      throw new RuntimeException(e);
    }
  }


}
