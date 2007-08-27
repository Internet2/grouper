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
import  edu.internet2.middleware.grouper.GrouperException;
import  edu.internet2.middleware.grouper.GrouperRuntimeException;
import  java.io.IOException;
import  java.io.InputStream;
import  java.util.Properties;

/** 
 * Access {@link Configuration} in a <i>Properties</i> file.
 * <p/>
 * @author  blair christensen.
 * @version $Id: PropertiesConfiguration.java,v 1.3 2007-08-27 15:46:24 blair Exp $
 * @since   @HEAD@
 */
public class PropertiesConfiguration implements Configuration {


  private ConfigurationHelper helper    = new ConfigurationHelper();
  private Properties          cfg       = null;
  private String              resource  = null;



  /**
   * Access a <i>Properties</i>-based {@link Configuration}.
   * <p/>
   * @param   resource  Resource to use for opening properties file as an <i>InputStream</i>.
   * @since   @HEAD@
   */
  public PropertiesConfiguration(String resource) {
    this.resource = resource;
  }


  /**
   * Retrieve a properties file, opening it if necessary.
   * @since   @HEAD@
   */
  private Properties getProperties() {
    if ( this.cfg == null ) {
      try {
        this.cfg = this.readProperties( new Properties() );
      }
      catch (GrouperException eInitializingError) {
        throw new GrouperRuntimeException( 
          "error reading configuration: " + eInitializingError.getMessage(), eInitializingError 
        );
      }
    }
    return this.cfg;
  }

  /**
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#getProperty(String)
   * @since   @HEAD@
   */
  public String getProperty(String property) 
    throws  IllegalArgumentException
  {
    this.helper.validateParamsNotNull(property);
    return this.getProperties().getProperty(property);
  }

  /**
   * Load properties from file.
   * @since   @HEAD@
   */
  private Properties readProperties(Properties props) 
    throws  GrouperException
  {
    try {
      InputStream in = this.getClass().getResourceAsStream(this.resource);
      if (in == null) {
        throw new GrouperException("null input stream");
      }
      props.load(in);
      return props;
    }
    catch (IOException eIO) {
      throw new GrouperException( eIO.getMessage(), eIO );
    }
  }

  /**
   * Does not persist value to <i>properties</i> file.
   * <p/>
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#setProperty(String, String)
   * @since   @HEAD@
   */
  public String setProperty(String property, String value) {
    this.helper.validateParamsNotNull(property, value);
    this.getProperties().setProperty(property, value);
    return this.getProperty(property);
  }

}

