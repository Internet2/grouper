/*--
$Id: SubsystemPart.java,v 1.7 2006-02-09 10:25:54 lmcrae Exp $
$Date: 2006-02-09 10:25:54 $

Copyright 2006 Internet2, Stanford University

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
package edu.internet2.middleware.signet;

/**
* Every sub-part of the Signet Subsystem implements this interface, which
* ensures that each of those parts has its full complement of common
* attributes.
* 
*/
interface SubsystemPart extends NonGrantable
{
 /**
  * Gets the <code>Subsystem</code> associated with this entity.
  * 
  * @return Returns the <code>Subsystem</code>.
  */
 public Subsystem getSubsystem();
}
