/**
 * Copyright 2014 Internet2
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
/*
 * $Id: Morph.java,v 1.3 2008-10-29 05:32:23 mchyzer Exp $
 * 
 * Copyright University of Pennsylvania 2004
 */
package edu.internet2.middleware.grouperInstaller.morphString;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils;

/**
 * @author Chris Hyzer
 */
public class Morph {

  /**
   * 
   */
  public static final String ENCRYPT_KEY = "encrypt.key";

  /**
   * strip the prefix off
   * @param string
   * @param prefix
   * @return the string without the prefix
   */
  public static String stripPrefix(String string, String prefix) {
    if (string == null || prefix == null) {
      return string;
    }
    if (string.startsWith(prefix)) {
      return string.substring(prefix.length(), string.length());
    }
    return string;
  }
  
  
  /**
   * 
   * @param file
   *          is the file to read into a string
   * 
   * @return String
   */
  public static String readFileIntoStringUtf8(File file) {
  
    if (file == null) {
      return null;
    }
    try {
      return GrouperInstallerUtils.readFileToString(file, "UTF-8");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
  
  
  public static String readFromFileIfFileUtf8(String in, boolean disableExternalFileLookup) {
    
    if (in == null || "".equals(in)) {
      return in;
    }
    
    boolean isFile = false;
    if (in.startsWith("file:")) {
      isFile = true;
      in = stripPrefix(in, "file:");
      File file = new File(in);
      if (!file.exists() || !file.isFile()) {
        throw new RuntimeException("Cant find or read file: '" + in + "'");
      }
    } else {
      if (!disableExternalFileLookup) {
        File file = new File(in);
        if (file.exists() && file.isFile()) {
          isFile = true;
        }
      }
    }
    
    //see if it is a file reference
    if (isFile) {
      //read the contents of the file into a string
      return readFileIntoStringUtf8(new File(in));
    }
    return in;
  
  }

  /** if testing, and not relying on morph key being there, use this */
  public static String testMorphKey = null;

  /**
   * Constructor for Morph.
   */
  private Morph() {
  }

}
