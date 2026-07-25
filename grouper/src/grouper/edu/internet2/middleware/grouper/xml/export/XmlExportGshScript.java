/**
 * Copyright 2018 Internet2
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
 */

package edu.internet2.middleware.grouper.xml.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * new File("c:/temp/script.gsh").delete();
 * grouperSession = GrouperSession.startRootSession();
 * new edu.internet2.middleware.grouper.xml.export.XmlExportGshScript().assignStemName(":").assignFileNameToWriteTo("c:/temp/script.gsh").exportGsh();
 * 
 * or export everything under multiple stems in one script
 * 
 * new edu.internet2.middleware.grouper.xml.export.XmlExportGshScript().addStemName("some:folder").addStemName("another:folder").assignFileNameToWriteTo("c:/temp/script.gsh").exportGsh();
 */
public class XmlExportGshScript {

  /**
   * stems to export, all objects under each of these stems will be exported
   */
  private Set<Stem> stems = new LinkedHashSet<Stem>();
  
  /**
   * names of stems to export which could not be resolved to a stem
   */
  private Set<String> stemNames = new LinkedHashSet<String>();

  /**
   * assign a parent stem, this replaces any stems which were previously assigned or added
   * @param theStemId
   * @return this for chaining
   */
  public XmlExportGshScript assignStemId(String theStemId) {
    this.stems.clear();
    this.stemNames.clear();
    return this.addStemId(theStemId);
  }

  /**
   * add a parent stem, all objects under each added stem will be exported
   * @param theStemId
   * @return this for chaining
   */
  public XmlExportGshScript addStemId(String theStemId) {
    this.stems.add(StemFinder.findByUuid(GrouperSession.staticGrouperSession(), theStemId, true));
    return this;
  }

  /**
   * 
   */
  private Set<String> objectNames = new HashSet<String>();
  
  /**
   * 
   * @param theObjectName
   * @return this for chaining
   */
  public XmlExportGshScript addObjectName(String theObjectName) {
    this.objectNames.add(theObjectName);
    return this;
  }
  
  /**
   * assign a parent stem, this replaces any stems which were previously assigned or added
   * @param theStemName
   * @return this for chaining
   */
  public XmlExportGshScript assignStemName(String theStemName) {
    this.stems.clear();
    this.stemNames.clear();
    return this.addStemName(theStemName);
  }

  /**
   * add a parent stem, all objects under each added stem will be exported
   * @param theStemName
   * @return this for chaining
   */
  public XmlExportGshScript addStemName(String theStemName) {
    Stem theStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), theStemName, false);
    if (theStem != null) {
      this.stems.add(theStem);
    } else {
      this.stemNames.add(theStemName);
    }
    return this;
  }

  /**
   * file to write to, must be a file which doesnt exist
   */
  private File fileToWriteTo;
  
  /**
   * file to write to
   * @param theFileName
   * @return this for chaining
   */
  public XmlExportGshScript assignFileNameToWriteTo(String theFileName) {
    
    if (theFileName == null || !theFileName.endsWith(".gsh")) {
      throw new RuntimeException("File name should end in .gsh: " + theFileName);
    }
    
    this.fileToWriteTo = new File(theFileName);
    if (this.fileToWriteTo.exists()) {
      throw new RuntimeException("File should not exist, it will be created: " + this.fileToWriteTo.getAbsolutePath());
    }
    if (!this.fileToWriteTo.getParentFile().exists()) {
      throw new RuntimeException("Parent folder should not exist, it will be created: " + this.fileToWriteTo.getParentFile().getAbsolutePath());
    }
    return this;
  }
  
  /**
   * write GSH to the file
   */
  public void exportGsh() {
    
    if (this.stems.size() == 0 && this.stemNames.size() == 0 && this.objectNames.size() == 0) {
      throw new RuntimeException("A stem or objectName is required, pass one in");
    }
    if (this.fileToWriteTo == null) {
      throw new RuntimeException("A file to write to is required, pass one in");
    }
    
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(this.fileToWriteTo);
      
      XmlExportMain xmlExportMain = new XmlExportMain();
      
      if (this.stems.size() > 0 || this.stemNames.size() > 0) {

        //if the root stem is in there, then everything is exported, so dont filter on stems
        boolean exportEverything = false;
        for (Stem theStem : this.stems) {
          if (theStem.isRootStem()) {
            exportEverything = true;
            break;
          }
        }
        
        if (!exportEverything) {
          for (Stem theStem : this.stems) {
            xmlExportMain.addStem(theStem.getName() + ":%");
            xmlExportMain.addStem(theStem.getName());
          }
          for (String theStemName : this.stemNames) {
            xmlExportMain.addStem(theStemName + ":%");
            xmlExportMain.addStem(theStemName);
          }
        }
      } else if (this.objectNames.size() > 0) {
        for (String theObjectName : this.objectNames) {
          xmlExportMain.addObjectName(theObjectName);
        }
      }

      xmlExportMain.writeAllTablesGsh(fileWriter, GrouperUtil.fileCanonicalPath(this.fileToWriteTo));
    } catch (IOException ioe) {
      throw new RuntimeException("Problem writing GSH to file: " + GrouperUtil.fileCanonicalPath(this.fileToWriteTo), ioe);
    } finally {
      GrouperUtil.closeQuietly(fileWriter);
    }

  }
  
}
