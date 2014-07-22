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
package edu.internet2.middleware.grouper.esb.listener;

public class EsbListenerEvents {
	private EsbListenerEvent[] esbListenerEvent;

	public EsbListenerEvent[] getEsbListenerEvent() {
		return esbListenerEvent;
	}

	public void setEsbListenerEvent(EsbListenerEvent[] esbListenerEvent) {
		this.esbListenerEvent = esbListenerEvent;
	}
	
	public void addEsbListenerEvent(EsbListenerEvent esbListenerEvent) {
		if (this.esbListenerEvent == null) {
		      this.esbListenerEvent = new EsbListenerEvent[1];
		      this.esbListenerEvent[0] = esbListenerEvent;
		    } else {
		      EsbListenerEvent[] newArray = new EsbListenerEvent[this.esbListenerEvent.length + 1];
		      System.arraycopy(this.esbListenerEvent, 0, newArray, 0,
		          this.esbListenerEvent.length);
		      newArray[this.esbListenerEvent.length + 1] = esbListenerEvent;
		      this.esbListenerEvent= newArray;
		    }
	}
	
	
}
