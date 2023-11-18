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
 * @author mchyzer
 * $Id: GuiSubject.java,v 1.2 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;


/**
 * subject for gui has all attributes etc, and getter to be accessed from screen
 */
@SuppressWarnings("serial")
public class GuiSource extends GuiObjectBase implements Serializable {

  /**
   * @return option label
   */
  public String getOptionLabel() {
    if (this.source == null) {
      return "";
    }
    return GrouperUtil.xmlEscape(this.source.getId() + " - " + this.source.getName());
  }
  
  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GuiSource)) {
      return false;
    }
    return StringUtils.equals(((GuiSource)other).source.getId(), this.source.getId());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
   return new HashCodeBuilder()
     .append(this.source == null ? null : this.source.getId()).toHashCode();
  }
  /**
   * 
   * @param sources
   * @return subjects
   */
  public static Set<GuiSource> convertFromSources(Set<Source> sources) {
    return convertFromSources(sources, null, -1);
  }


  /**
   * 
   * @param sources
   * @param configMax 
   * @param defaultMax 
   * @return gui subjects
   */
  public static Set<GuiSource> convertFromSources(Set<Source> sources, String configMax, int defaultMax) {

    Set<GuiSource> tempSources = new LinkedHashSet<GuiSource>();
    
    Integer max = null;
    
    if (!StringUtils.isBlank(configMax)) {
      max = GrouperUiConfig.retrieveConfig().propertyValueInt(configMax, defaultMax);
    }
    
    int count = 0;
    for (Source source : GrouperUtil.nonNull(sources)) {
      tempSources.add(new GuiSource(source));
      if (max != null && ++count >= max) {
        break;
      }
    }
    
    return tempSources;
    
  }

  /** source */
  private Source source;

  /**
   * construct with source
   * @param source1
   */
  public GuiSource(Source source1) {
    this.source = source1;
  }
  
  /**
   * cant get grouper object
   */
  @Override
  public GrouperObject getGrouperObject() {
    return null;
  }

}
