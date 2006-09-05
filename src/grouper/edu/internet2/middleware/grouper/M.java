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

/**
 * Grouper Messages.
 * @author  blair christensen.
 * @version $Id: M.java,v 1.5 2006-09-05 19:42:14 blair Exp $
 * @since   1.0
 */
class M {

  // PROTECTED CLASS CONSTANTS //
  protected static final String CACHE_EMPTIED       = "emptied cache: ";
  protected static final String COMP_ADD            = "composite add: group=";
  protected static final String COMP_DEL            = "composite delete: group=";
  protected static final String COMP_MEMADD         = "add composite member: group=";
  protected static final String COMP_MEMDEL         = "delete composite member: group=";
  protected static final String GOT_INNER_WITHIN_INNER  = "got inner session within inner session";
  protected static final String GROUP_ADD           = "add group: ";
  protected static final String GROUP_ADDTYPE       = "group add type: group=";
  protected static final String GROUP_DEL           = "delete group: ";
  protected static final String GROUP_DELTYPE       = "group delete type: group=";
  protected static final String GROUPTYPE_ADD       = "add group type: ";
  protected static final String GROUPTYPE_ADDFIELD  = "add group field: field=";
  protected static final String GROUPTYPE_DEL       = "delete group type: ";
  protected static final String GROUPTYPE_DELFIELD  = "delete group field: field=";
  protected static final String MEMBER_CHANGESID    = "changed member subjectId: uuid=";
  protected static final String S_START             = "session: start";
  protected static final String S_STOP              = "session: stop duration=";
  protected static final String STEM_ADD            = "add stem: ";
  protected static final String STEM_DEL            = "delete stem: ";
  protected static final String STEM_ROOTINSTALL    = "root stem installed";

}

