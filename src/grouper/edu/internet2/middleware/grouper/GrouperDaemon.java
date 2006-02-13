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


import  java.io.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.logging.*;


/** 
 * Persistent Grouper Process.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperDaemon.java,v 1.1 2006-02-13 21:10:58 blair Exp $    
 */
public class GrouperDaemon {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(GrouperDaemon.class);

  // Private Instance Variables
  private boolean stop  = false;


  // Public Class Methods
  public static void main(String[] args) {
    GrouperDaemon d = new GrouperDaemon();
    if (args.length > 0) {
      if      (args[0].equals("list"))        {
        d.getQueue();
      }
      else if (args[0].equals("list-active")) {
        d.getQueueActive();
      }
      else if (args[0].equals("list-failed")) {
        d.getQueueFailed();
      }
      else if (args[0].equals("start"))       {
        d.start();
      }
      else if (args[0].equals("stop"))        {
        d.stop();
      }
      else                                    {
        System.err.println(d.getUsage());
        System.exit(1);
      }
    } // if (args.length > 0) ...
    else {
      System.err.println(d.getUsage());
      System.exit(0);
    }
  } // public static void main(args)


  // Protected Instance Methods
  protected boolean isStopped() {
    return this.stop;
  } // protected boolean isStopped()

  protected void stopDaemon() {
    System.err.println("GD.STOPDAEMON!");
    this.stop = true;
    DaemonHsqldbThread.stopServer();
  } // protected void stopDaemon()


  // Private Instance Methods
  private void getQueue() {
    Set         results = TxQueueFinder.findAll();
    System.err.println("RESULTS ALL: " + results);
  } // private void getQueue()

  private void getQueueActive() {
    Set         results = TxQueueFinder.findByStatus("wait");
    System.err.println("RESULTS ACTIVE: " + results);
  } // private void getQueueActive()

  private void getQueueFailed() {
    Set         results = TxQueueFinder.findByStatus("fail");
    System.err.println("RESULTS FAILED: " + results);
  } // private void getQueueFailed()
 
  private String getUsage() {
    return this.getClass().getName() + " (list|list-active|list-failed|start|stop)";
  } // private String getUsage()
 
  private void start() {
    // Create the threads.  Right now this is an extremely simplistic
    // model but at some point I'll probably move to pools, etc.
    DaemonShutdownThread    shutdown  = new DaemonShutdownThread(this);
    DaemonTxRunnerThread    txRunner  = new DaemonTxRunnerThread(this);

    // Register a shutdown hook that currently does absolutely nothing
    Runtime rt = Runtime.getRuntime();
    rt.addShutdownHook(shutdown);

    // Conditionally create-and-start HSQLDB server thread
    this.startHsqldb();

    // And then start the appropriate threads
    txRunner.start();
  } // private void start()

  private void startHsqldb() {
    String      cf      = "hibernate.properties";
    Properties  props   = GrouperConfig.getInstance().getProperties(cf);
    String      driver  = props.getProperty("hibernate.connection.driver_class");
    if (driver.equals("org.hsqldb.jdbcDriver")) {
      DaemonHsqldbThread    hsqldb  = new DaemonHsqldbThread(this);
      hsqldb.start();
    }
  } // private void startHsqldb()

  private void stop() {
    try {
      Session hs    = HibernateHelper.getSession();
      TxQueue stop  = new TxQueueStop();
      HibernateHelper.save(stop);
      hs.close();
    }
    catch (HibernateException eH) {
      String msg = eH.getMessage();
      LOG.fatal(msg);
      throw new RuntimeException(msg);
    }
  } // private void stop();
  
}    

