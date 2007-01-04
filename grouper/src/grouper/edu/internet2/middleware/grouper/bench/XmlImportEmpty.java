/*
  Copyright (C) 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2006-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.bench;
import  edu.internet2.middleware.grouper.*;      
import  java.util.Properties;
import  java.io.*;

/**
 * Benchmark importing an effectively empty XML file.
 * @author  blair christensen.
 * @version $Id: XmlImportEmpty.java,v 1.2 2007-01-04 17:17:45 blair Exp $
 * @since   1.1.0
 */
public class XmlImportEmpty extends BaseGrouperBenchmark {

  // PRIVATE INSTANCE VARIABLES
  XmlImporter importer;
  String      xml;


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new XmlImportEmpty();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected XmlImportEmpty() {
    super();
  } // protected XmlImportEmpty()

  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.1.0
   */
  public void init() 
    throws GrouperRuntimeException 
  {
    try {
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlExporter     exporter  = new XmlExporter( s, new Properties() );
      Writer          w         = new StringWriter();
      exporter.export(w);
      this.xml                  = w.toString();
      this.importer             = new XmlImporter( s, new Properties() );
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e.getMessage());
    }
  } // public void init()

  /**
   * @since 1.1.0
   */
  public void run() 
    throws GrouperRuntimeException 
  {
    try {
      this.importer.load( XmlReader.getDocumentFromString(this.xml) );
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e);
    }
  } // public void run()

} // public class XmlImportEmpty

