/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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


import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.logging.*;
import  org.hsqldb.*;


/** 
 * {@link grouperd} thread for running a HSQLB server.
 * <p />
 * @author  blair christensen.
 * @version $Id: DaemonHsqldbThread.java,v 1.2 2006-02-14 17:18:31 blair Exp $    
 */
public class DaemonHsqldbThread extends Thread {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(DaemonHsqldbThread.class);


  // Private Class Variables
  private static  Properties  props   = null;
  private static  Server      server  = null;

  // Private Instance Variables
  private GrouperDaemon gd  = null;


  // Constructors
  protected DaemonHsqldbThread(GrouperDaemon gd) {
    super();
    this.gd = gd;
    this.startServer();
  } // protected GrouperShutdownThread()


  // Public Instance Methods

  public void run( ) {
    while (true) {
      if (this.gd.isStopped()) {
        break; 
      }
      try {
        Thread.sleep( (long) (Math.random() * 1000) );
      } catch (InterruptedException e) {
        // Nothing
      }
    }
  } // public void run()


  // Protected Class Methods
  protected static void stopServer() {
    if (server != null) {
      try {
        server.signalCloseAllServerConnections();
        Thread.sleep( (long) (Math.random() * 1000) ); 
        net.sf.hibernate.Session  hs  = HibernateHelper.getSession();
        Connection conn = hs.connection();
        PreparedStatement shutdown = conn.prepareStatement(
          "SHUTDOWN COMPACT"
        );
        shutdown.executeUpdate();
        hs.close();
        server.stop();
      }
      catch (Exception e) {
        String  msg   = e.getMessage();
        LOG.fatal(msg);
        throw new RuntimeException(msg);
      }
    }
  } // protected static void stopServer();


  // Private Instance Methods
  private void startServer() {
    if (props == null) {
      String  cf    = "hsqldb-server.properties";
              props = GrouperConfig.getInstance().getProperties(cf);
    }
    if (server == null) {
      server  = new Server();    
      String      prop  = "server.silent";
      if (props.getProperty(prop) != null) {
        server.setSilent( Boolean.valueOf(props.getProperty(prop)).booleanValue() );
      }
      prop              = "server.trace";
      if (props.getProperty(prop) != null) {
        server.setTrace( Boolean.valueOf(props.getProperty(prop)).booleanValue() );
      }
      prop              = "server.address";
      if (props.getProperty(prop) != null) {
        server.setAddress(props.getProperty(prop));
      }
      prop              = "server.port";
      if (props.getProperty(prop) != null) {
        server.setPort( Integer.parseInt( props.getProperty(prop)) );
      }
      prop              = "server.dbname.0";
      if (props.getProperty(prop) != null) {
        server.setDatabaseName(0, props.getProperty(prop));
      }
      prop              = "server.database.0";
      if (props.getProperty(prop) != null) {
        server.setDatabasePath(0, props.getProperty(prop));
      }
      server.start();
    }
  } // private void startServer()  
}

