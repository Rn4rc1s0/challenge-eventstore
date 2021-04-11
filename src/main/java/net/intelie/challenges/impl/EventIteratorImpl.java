package net.intelie.challenges.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.intelie.challenges.Event;
import net.intelie.challenges.EventIterator;

public class EventIteratorImpl implements EventIterator{
	
	private final Iterator<Event> eventsIterator;
	
	public EventIteratorImpl(Collection<Event> events) {
		if(events != null) {
			eventsIterator = events.iterator();
		} else {
			eventsIterator = new ArrayList<Event>().iterator();
		}
	}

	@Override
	public void close() throws Exception {
	}

	@Override
	public boolean moveNext() {
		return eventsIterator.hasNext();
	}

	@Override
	public Event current() {
		if(!this.moveNext()) {
			throw new IllegalStateException();
		}
		return eventsIterator.next();
	}

	@Override
	public void remove() {
		eventsIterator.remove();
		
	}

}
