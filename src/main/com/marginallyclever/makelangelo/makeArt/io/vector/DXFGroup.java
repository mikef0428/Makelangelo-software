package com.marginallyclever.makelangelo.makeArt.io.vector;

import java.util.LinkedList;

/**
 * A collection of DXFBucketEntities
 * @author Dan Royer
 *
 */
public class DXFGroup {
	public LinkedList<DXFBucketEntity> entities;
	public enum ClosedState {
		UNKNOWN,
		CLOSED,
		OPEN,
	};
	public ClosedState isClosed; 
	
	
	public DXFGroup() {
		isClosed = ClosedState.UNKNOWN; 
		entities = new LinkedList<DXFBucketEntity>();
	}
	
	public void addLast(DXFBucketEntity e) {
		entities.add(e);
	}

	public void addFirst(DXFBucketEntity e) {
		entities.addFirst(e);
	}
}
