package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import edu.internet2.middleware.grouper.esb.listener.EsbListenerEvent;

public class EsbEvents {
	private EsbEvent[] esbEvent;

	public EsbEvent[] getEsbEvent() {
		return esbEvent;
	}

	public void setEsbEvent(EsbEvent[] esbEvent) {
		this.esbEvent = esbEvent;
	}
	
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
