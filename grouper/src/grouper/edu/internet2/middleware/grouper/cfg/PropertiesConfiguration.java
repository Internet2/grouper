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
import java.util.Properties;
import java.util.Set;

import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Access {@link Configuration} in a <i>Properties</i> file.
 * <p/>
 * @author  blair christensen.
 * @version $Id: PropertiesConfiguration.java,v 1.11 2009-03-15 06:37:24 mchyzer Exp $
 * @since   1.2.1
 */
public class PropertiesConfiguration implements Configuration {

  /** */
  private ConfigurationHelper helper    = new ConfigurationHelper();
  /** */
  private Properties          cfg       = null;
  /** */
  private String              resource  = null;



  /**
   * Access a <i>Properties</i>-based {@link Configuration}.
   * <p/>
   * @param   resource  Resource to use for opening properties file as an <i>InputStream</i>.
   * @since   1.2.1
   */
  public PropertiesConfiguration(String resource) {
    this.resource = resource;
  }


  /**
   * Retrieve a properties file, opening it if necessary.
   * @return properties
   * @since   1.2.1
   */
  public Properties getProperties() {
    if ( this.cfg == null ) {
      try {
        this.cfg = GrouperUtil.propertiesFromResourceName(this.resource, false, true);
      }
      catch (Exception eInitializingError) {
        throw new GrouperException( 
          "error reading configuration: " + eInitializingError.getMessage(), eInitializingError 
        );
      }
    }
    return this.cfg;
  }

  /**
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#getProperty(String)
   * @since   1.2.1
   */
  public String getProperty(String property) 
    throws  IllegalArgumentException
  {
    this.helper.validateParamsNotNull(property);
    String val = this.getProperties().getProperty(property);
    return val == null ? null : val.trim();
  }

  /**
   * Does not persist value to <i>properties</i> file.
   * <p/>
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#setProperty(String, String)
   * @since   1.2.1
   */
  public String setProperty(String property, String value) {
    this.helper.validateParamsNotNull(property, value);
    this.getProperties().setProperty(property, value);
    return this.getProperty(property);
  }

  /**
   * return the set of keys
   * @return the keys
   */
  public Set<String> keySet() {
    return (Set<String>)(Object)this.getProperties().keySet();
  }
  
}

