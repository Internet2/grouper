/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper.database;

import  java.io.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.cfg.*;


/** 
 * Class representing a database session.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: DbSess.java,v 1.4 2005-03-11 01:17:58 blair Exp $
 */
public class DbSess {

  /*
   * PRIVATE CLASS CONSTANTS
   */
  private static final String CF_HIBERNATE = "Grouper.hbm.xml";


  /*
   * PRIVATE CLASS VARIABLES
   */
  private static  Configuration   cfg;     
  private static  SessionFactory  factory;

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private Session     session;
  private Transaction tx;
  private int         txCnt;


  /*
   * CONSTRUCTORS
   */

  /**
   * Create a Hibernate database session.
   */
  public DbSess() {
    _configuration();
    _sessionFactory();
    try {
      this.session = factory.openSession();
      this.txCnt = 0;
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to start database session: " + e
                );
    }
  }


  /*
   * PUBLIC INSTANCE METHODS 
   */

  /**
   * Stop a Hibernate database session.
   */
  public void stop() {
    try {
      this.session.connection().commit();
      try {
        this.session.close();
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error closing database session: " + e
                  );
      }
    } catch (Exception e) {
      throw new RuntimeException(
                  "Error committing database session: " + e
                );
    }
  }

  public void txStart() {
    if (this.txCnt == 0) {
      try {
        this.tx = this.session.beginTransaction();
      } catch (HibernateException e) {
        throw new RuntimeException("Error starting transaction: " + e);
      }
    }
    this.txCnt++;
  }
  public void txCommit() {
    this.txCnt--;
    if (this.txCnt == 0) {
      try {
        this.tx.commit();
      } catch (HibernateException e) {
        this.txRollback();
        throw new RuntimeException(
                    "Error committing transaction: " + e
                  );
      }
    }
  }
  public void txRollback() {
    try {
      this.tx.rollback();
      this.txCnt = 0;
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Error rolling back transactin: " + e
                );
    }
  }
  
  /*
   * PROTECTED INSTANCE METHODS
   */

  /**
   * Return the Hibernate session object.
   */
  public net.sf.hibernate.Session session() {
    return this.session;
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Load Hibernate mapping configuration
   */
  private static void _configuration() {
    if (cfg == null) {
      InputStream in = Session.class
                         .getResourceAsStream("/" + CF_HIBERNATE);
 
      try {
        cfg = new Configuration()
          .addInputStream(in);
      } catch (MappingException e) {
        throw new RuntimeException(
                    "Bad mapping in " + CF_HIBERNATE + ": " + e
                  );
      }
    }
  }

  /*
   * Create the Hibernate session factory
   */
  private static void _sessionFactory() {
    if (factory == null) {
      try {
        factory = cfg.buildSessionFactory();
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Unable to create Hibernate session factory: " + e
                  );
      }
    } 
  }

}

