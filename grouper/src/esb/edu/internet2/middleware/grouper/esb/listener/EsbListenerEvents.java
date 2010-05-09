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
