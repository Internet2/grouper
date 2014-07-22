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
/**
 * @author mchyzer
 * $Id: GrouperShellWrapper.java,v 1.3 2009-11-30 17:57:38 tzeller Exp $
 */
package edu.internet2.middleware.grouper.app.gsh;



public class GrouperShellWrapper {

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
        GrouperShell.main(args);
    } catch (NoClassDefFoundError cnfe) {
      System.err.println("There was a NoClassDefFoundError.  This could be because you are not using Java 1.6+ which is required for GSH.  This is version " + System.getProperty("java.version") + ".");
      throw cnfe;
    }

  }

}
