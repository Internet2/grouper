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

package edu.internet2.middleware.grouper.internal.dao.hib3;
import  edu.internet2.middleware.grouper.cfg.Configuration;
import  edu.internet2.middleware.grouper.cfg.PropertiesConfiguration;

/** 
 * Hibernate DAO configuration.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Hib3DaoConfig.java,v 1.3 2009-02-01 22:38:48 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3DaoConfig implements Configuration {

  /**
   * 
   */
  private PropertiesConfiguration cfg;



  /**
   * Access Hibernate configuration.
   * <p/>
   * @since   @HEAD@
   */
  public Hib3DaoConfig() {
    this.cfg = new PropertiesConfiguration("/grouper.hibernate.properties");
  }


  /**
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#getProperty(String)
   * @since   @HEAD@
   */
  public String getProperty(String property) 
    throws  IllegalArgumentException
  {
    String val = this.cfg.getProperty(property);
    return val == null ? null : val.trim();
  }

  /**
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#setProperty(String, String)
   * @since   @HEAD@
   */
  public String setProperty(String property, String value) {
    return this.cfg.setProperty(property, value);
  }

}

