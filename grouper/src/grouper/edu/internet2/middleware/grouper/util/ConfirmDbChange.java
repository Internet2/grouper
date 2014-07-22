/**
 * Copyright 2012 Internet2
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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.util;

import org.apache.commons.logging.Log;



/**
 * Make sure the user is ok with db changes
 * @author Chris Hyzer
 * @version $Id: ConfirmDbChange.java,v 1.3 2008-09-29 03:38:30 mchyzer Exp $
 */
public class ConfirmDbChange {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(ConfirmDbChange.class);

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
//    if (StringUtils.isBlank(args[0])) {
//      System.out.println("Need to pass arg to confirm class");
//      System.exit(1);
//    }
    GrouperUtil.promptUserAboutDbChanges("run ant", true);
  }
} 

