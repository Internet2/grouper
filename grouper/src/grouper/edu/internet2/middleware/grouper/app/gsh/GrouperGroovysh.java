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
package edu.internet2.middleware.grouper.app.gsh;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.shell.Groovysh;
import org.codehaus.groovy.tools.shell.IO;
import org.codehaus.groovy.tools.shell.Interpreter;

import groovy.lang.MissingPropertyException;

/**
 * @author shilen
 */
public class GrouperGroovysh extends Groovysh {

  private boolean exitOnError;
  
  /**
   * @param io
   * @param compilerConfiguration
   * @param exitOnError
   */
  public GrouperGroovysh(IO io, CompilerConfiguration compilerConfiguration, boolean exitOnError) {
    super(io, compilerConfiguration);

    this.exitOnError = exitOnError;
  }
  
  protected void displayError(final Throwable cause) {

    if (exitOnError) {
      System.err.println(ExceptionUtils.getFullStackTrace(cause));
      System.exit(1);
    }
    
    if (cause instanceof MissingPropertyException) {
      if (((MissingPropertyException) cause).getType() != null && Interpreter.getSCRIPT_FILENAME().equals(((MissingPropertyException) cause).getType().getCanonicalName())) {
          this.getIo().err.println("@|bold,red Unknown property|@: " + ((MissingPropertyException) cause).getProperty());
          return;
      }
    }

    super.getErrorHook().call(cause);
  }
}
