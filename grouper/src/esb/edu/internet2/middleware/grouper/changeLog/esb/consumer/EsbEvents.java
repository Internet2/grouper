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
package edu.internet2.middleware.grouper.changeLog.esb.consumer;

/**
 * container around esb event
 * @author mchyzer
 *
 */
public class EsbEvents {
  
  /** */
	private EsbEvent[] esbEvent;

	/**
	 * 
	 * @return event array
	 */
	public EsbEvent[] getEsbEvent() {
		return esbEvent;
	}

	/**
	 * 
	 * @param esbEvent
	 */
	public void setEsbEvent(EsbEvent[] esbEvent) {
		this.esbEvent = esbEvent;
	}

	/**
	 * 
	 * @param esbEvent
	 */
	public void addEsbEvent(EsbEvent esbEvent) {
		if(this.esbEvent ==null) {
			this.esbEvent = new EsbEvent[] {esbEvent};
		} else {
			EsbEvent[] newArray = new EsbEvent[this.esbEvent.length + 1];
		      System.arraycopy(this.esbEvent, 0, newArray, 0,
		          this.esbEvent.length);
		      newArray[this.esbEvent.length + 1] = esbEvent;
		      this.esbEvent= newArray;
		}
	}
}
