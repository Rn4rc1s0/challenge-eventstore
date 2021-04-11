package net.intelie.challenges.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import net.intelie.challenges.Event;
import net.intelie.challenges.EventIterator;
import net.intelie.challenges.EventStore;

public class EventStoreImpl implements EventStore{
	
	private static EventStoreImpl eventStore;
	
	private static final Map<String, List<Event>> mapEvents = new ConcurrentHashMap<String, List<Event>>();
	
	/*
	 * Using Reentrant Lock seems to be the best option here
	 * this way we have a full lock only for write operations  
	 */
	private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
	
	public static EventStoreImpl getEventStore() {
		if(eventStore == null) {
			synchronized (EventStoreImpl.class) {
				if(eventStore == null) {
					eventStore = new EventStoreImpl();
				}
			}
		}
		return eventStore;
	}
	
	private EventStoreImpl() {

	}

	@Override
	public void insert(Event event) {
		if (event == null || event.type() == null)
			return;

		reentrantReadWriteLock.writeLock().lock();
		try {

			if (mapEvents.containsKey(event.type())) {
				mapEvents.get(event.type()).add(event);

			} else {
				List<Event> eventsList = new ArrayList<Event>();
				eventsList.add(event);
				mapEvents.put(event.type(), eventsList);
			}

		} finally {
			reentrantReadWriteLock.writeLock().unlock();
		}
		
	}

	@Override
	public void removeAll(String type) {
		if (type == null)
			return;

		reentrantReadWriteLock.writeLock().lock();
		try {
			mapEvents.remove(type);
		} finally {
			reentrantReadWriteLock.writeLock().unlock();
		}
		
	}

	@Override
	public EventIterator query(String type, long startTime, long endTime) {
		if (type == null)
			return new EventIteratorImpl(new ArrayList<Event>());

		reentrantReadWriteLock.readLock().lock();
		try {

			if (mapEvents.containsKey(type)) {
				List<Event> eventsQueryList = new ArrayList<>(mapEvents.get(type));
				return new EventIteratorImpl(eventsQueryList.stream()
						.filter(event -> event.timestamp() >= startTime && event.timestamp() < endTime)
						.collect(Collectors.toList()));

			} else {
				return new EventIteratorImpl(new ArrayList<Event>());
			}

		} finally {
			reentrantReadWriteLock.readLock().unlock();
		}
	}

}
