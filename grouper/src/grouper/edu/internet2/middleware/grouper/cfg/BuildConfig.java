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

package edu.internet2.middleware.grouper.cfg;

/** 
 * Grouper API build configuration.
 * <p/>
 * @author  blair christensen.
 * @version $Id: BuildConfig.java,v 1.2 2007-08-27 15:46:24 blair Exp $
 * @since   @HEAD@
 */
public class BuildConfig implements Configuration {

  
  private PropertiesConfiguration cfg;



  /**
   * Access Grouper API build configuration.
   * <p/>
   * @since   @HEAD@
   */
  public BuildConfig() {
    this.cfg = new PropertiesConfiguration("/buildGrouper.properties");
  }


  /**
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#getProperty(String)
   * @since   @HEAD@
   */
  public String getProperty(String property) 
    throws  IllegalArgumentException
  {
    return this.cfg.getProperty(property);
  }

  /**
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#setProperty(String, String)
   * @since   @HEAD@
   */
  public String setProperty(String property, String value) {
    return this.cfg.setProperty(property, value);
  }

}

