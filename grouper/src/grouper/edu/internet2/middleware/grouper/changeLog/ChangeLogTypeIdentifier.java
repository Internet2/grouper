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
 * @author mchyzer
 * $Id: ChangeLogTypeIdentifier.java,v 1.1 2009-05-08 05:28:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;


/**
 * interface which could be on the enum, or class, or whatever.
 * generally you will use the enum AuditTypeBuiltin
 */
public interface ChangeLogTypeIdentifier {

  /**
   * get the audit category
   * @return the id
   */
  public String getChangeLogCategory();

  /**
   * get the action name of the audit type
   * @return the name
   */
  public String getActionName();
  
}
