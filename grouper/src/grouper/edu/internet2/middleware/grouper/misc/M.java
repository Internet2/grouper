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

package edu.internet2.middleware.grouper.misc;

/**
 * Grouper Messages.
 * @author  blair christensen.
 * @version $Id: M.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.0
 */
public class M {

  // public CLASS CONSTANTS //
  public static final String CACHE_EMPTIED       = "emptied cache: ";
  public static final String COMP_ADD            = "composite add: group=";
  public static final String COMP_DEL            = "composite delete: group=";
  public static final String COMP_MEMADD         = "add composite member: group=";
  public static final String COMP_MEMDEL         = "delete composite member: group=";
  public static final String G_AM                = "add member: group=";
  public static final String G_AM_E              = "add effective member: group=";
  public static final String G_DA                = "delete group attr: group=";
  public static final String G_DM                = "delete member: group=";
  public static final String G_DM_E              = "delete effective member: group=";
  public static final String G_GP                = "grant access priv: group=";
  public static final String G_GP_E              = "grant effective access priv: group=";
  public static final String G_RP                = "revoke access priv: group=";
  public static final String G_RP_E              = "revoke effective access priv: group=";
  public static final String G_SA                = "set group attr: group=";
  public static final String GROUP_ADD           = "add group: ";
  public static final String GROUP_ADDTYPE       = "group add type: group=";
  public static final String GROUP_DEL           = "delete group: ";
  public static final String GROUP_DELTYPE       = "group delete type: group=";
  public static final String GROUPTYPE_ADD       = "add group type: ";
  public static final String GROUPTYPE_ADDFIELD  = "add group field: field=";
  public static final String GROUPTYPE_DEL       = "delete group type: ";
  public static final String GROUPTYPE_DELFIELD  = "delete group field: field=";
  public static final String INNER_WITHIN_INNER  = "got inner session within inner session";
  public static final String MEMBER_CHANGESID    = "changed member subjectId: uuid=";
  public static final String MEMBER_CHANGE_SSID  = "changed member subject source id: uuid=";
  public static final String S_GP                = "grant naming priv: stem=";
  public static final String S_GP_E              = "grant effective naming priv: stem=";
  public static final String S_RP                = "revoke naming priv: stem=";
  public static final String S_RP_E              = "revoke effective naming priv: stem=";
  public static final String S_SA                = "set stem attr: stem=";
  public static final String S_START             = "session: start";
  public static final String STEM_ADD            = "add stem: ";
  public static final String STEM_DEL            = "delete stem: ";
  public static final String STEM_ROOTINSTALL    = "root stem installed";
  
} // class M

