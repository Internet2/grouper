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
 * @author mchyzer $Id: Encrypt.java,v 1.1 2008-11-30 10:57:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString;

import java.io.IOException;
import java.io.InputStream;

/**
 * from sh or bat file to encrypt a string
 * @deprecated use edu.internet2.middleware.morphString.Encrypt instead!
 */
@Deprecated
public class Encrypt {

  /**
   * if an arg is dontMask, then it wont try the fancy masking stuff
   * @param args
   */
  public static void main(String[] args) {

    edu.internet2.middleware.morphString.Encrypt.main(args);
  }

  /**
   * encrypt user input, perhaps dont mask
   * @param dontMask
   */
  public static void encryptInput(boolean dontMask) {
    edu.internet2.middleware.morphString.Encrypt.encryptInput(dontMask);
  }
  
  /**
   * @param in stream to be used (e.g. System.in)
   * @param prompt The prompt to display to the user.
   * @return The password as entered by the user.
   * @throws IOException 
   */
  public static final char[] password(InputStream in, String prompt) throws IOException {
    return edu.internet2.middleware.morphString.Encrypt.password(in, prompt);
  }

}
