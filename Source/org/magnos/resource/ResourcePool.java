/* 
 * NOTICE OF LICENSE
 * 
 * This source file is subject to the Open Software License (OSL 3.0) that is 
 * bundled with this package in the file LICENSE.txt. It is also available 
 * through the world-wide-web at http://opensource.org/licenses/osl-3.0.php
 * If you did not receive a copy of the license and are unable to obtain it 
 * through the world-wide-web, please send an email to pdiffenderfer@gmail.com 
 * so we can send you a copy immediately. If you use any of this software please
 * notify me via my website or email, your feedback is much appreciated. 
 * 
 * @copyright   Copyright (c) 2011 Magnos Software (http://www.magnos.org)
 * @license     http://opensource.org/licenses/osl-3.0.php
 * 				Open Software License (OSL 3.0)
 */

package org.magnos.resource;

import java.util.ArrayList;
import java.util.Random;

import org.magnos.util.Notifier;



/**
 * A pool of resources controlled through bounds, timing, and allocation and
 * deallocation sizes.
 * 
 * @author Philip Diffenderfer
 *
 * @param <R>
 * 		The resource to pool.
 */
public class ResourcePool<R extends Resource> implements ResourceFactory<R>
{

	// Random number generator for picking from usable resources.
	protected final Random rnd = new Random(); 
	
	
	// The maximum number of resources that can exist in the pool.
	protected int maxCapacity = 10;
	
	// The minimum number of resources that can exist in the pool.
	protected int minCapacity = 2;
	
	// The number of resources to allocate at once when they're required.
	protected int allocateSize = 1;

	// The number of resources to deallocate at once.
	protected int deallocateSize = 1;
	
	// The minimum amount of time to wait since the last allocation before 
	// resources can be deallocated.
	protected long allocateThreshold = 60000;
	
	// The last time resources were allocated.
	protected long lastAllocateTime = Long.MAX_VALUE;

	
	// The factory used to create resources.
	protected final ResourceFactory<R> factory;
	
	// The internal list of resources.
	protected final ArrayList<R> resources;
	
	// The notifier of resources on the pools events.
	protected final Notifier<ResourceListener<R>> listeners;

	

	/**
	 * Instantiates a new ResourcePool.
	 *
	 * @param factory
	 * 		The factory used to create new resources.
	 */
	public ResourcePool(ResourceFactory<R> factory) 
	{
		this.factory = factory;
		this.resources = new ArrayList<R>();
		this.listeners = Notifier.create(ResourceListener.class);
	}
	
	/**
	 * Returns a resource to use. If a resource needs to be allocated one will
	 * be, else a resource will be taken from the pool which can be reused or
	 * null will be returned if no more resources can be contained in the pool.
	 * 
	 * @return
	 * 		A usable resource.
	 */
	public R allocate()
	{
		synchronized (resources) 
		{
			R resource = null;
			
			// Do a quick check on allocating, if this is true, invoke grow which 
			// will obtain the lock and double check it.
			if (isUnderflow()) {
				allocate(allocateSize);
			}
			
			// Gather a list of usable resources.
			ArrayList<R> usable = new ArrayList<R>(); 
			for (R r : resources) {
				if (r.isReusable()) {
					usable.add(r);
				}
			}
			
			// If there are usable resources pick a random on...
			if (!usable.isEmpty()) {
				resource = usable.get(rnd.nextInt(usable.size()));
			}
			// Else try adding one if possible.
			else if (resources.size() < maxCapacity) {
				resource = addResource();
			}

			// Do a quick check on deallocating, if this is true invoke shrink which
			// will obtain the lock and double check it, while proceeding.
			if (isOverflow()) {
				deallocate(deallocateSize);
			}
			
			return resource;
		}
	}

	/**
	 * Allocates the requested number of resources. If the number of resources
	 * requested exceeds the number of maximum possible resources the pool will
	 * be filled to its maximum size.
	 * 
	 * @param desired
	 * 		The number of resources to add to the pool.
	 * @return
	 * 		The number of resources added to the pool.
	 */
	public int allocate(int desired)
	{
		synchronized (resources) 
		{
			int resourceCount = StrictMath.min(maxCapacity - resources.size(), desired);

			for (int i = 0; i < resourceCount; i++) {
				addResource();	
			}
			
			return Math.max(0, resourceCount);
		}
	}

	/**
	 * Deallocates the requested number of resources. If the number of resources
	 * requested exceeds the number of minimum possible resources the pool will
	 * be emptied to its minimum size.
	 * 
	 * @param desired
	 * 		The number of resources to remove from the pool.
	 * @return
	 * 		The number of resources removed from the pool.
	 */
	public int deallocate(int desired)
	{
		synchronized (resources) 
		{
			int resourceCount = StrictMath.min(resources.size() - minCapacity, desired);
			
			for (int i = 0; i < resourceCount; i++) {
				removeResource();
			}
			
			return Math.max(0, resourceCount);
		}
	}
	
	/**
	 * Adds a resource to the pool.
	 */
	private R addResource() 
	{
		R resource = factory.allocate();

		resources.add(resource);
		listeners.proxy().onResourceAllocate(this, resource);
		
		lastAllocateTime = System.currentTimeMillis();
		
		return resource;
	}

	/**
	 * Removes a resource from the pool and frees it.
	 */
	private boolean removeResource() 
	{
		boolean removed = false;
		for (int i = resources.size() - 1; i >= 0 && !removed; i--) {
			R resource = resources.get(i);
			if (resource.isUnused()) {
				resource.free();
				resources.remove(i);
				listeners.proxy().onResourceDeallocate(this, resource);
				removed = true;
			}
		}
		return removed;
	}

	/**
	 * Starts this pool by filling it with its initial set of resources.
	 */
	public void populate()
	{
		synchronized (resources)
		{
			while (resources.size() < minCapacity) {
				addResource();
			}
		}
	}
	
	/**
	 * Removes all resources from the pool freeing them.
	 */
	public void empty()
	{
		synchronized (resources) 
		{
			// Free resources nicely.
			while (removeResource()) {
				// 
			}
			// Forcefully free a resource
			for (int i = resources.size() - 1; i >= 0; i--) {
				R resource = resources.remove(i);
				resource.free();
				listeners.proxy().onResourceDeallocate(this, resource);
			}
		}
	}
	
	/**
	 * Returns the lock used to control access to the pool of services.
	 * 	
	 * @return
	 * 		The reference to the lock object.
	 */
	public Object lock()
	{
		return resources;
	}
	
	/**
	 * Returns whether the number of resources in the pool is less than the
	 * desired minimum capacity.
	 * 
	 * @return
	 * 		True if this pool could use more resources, otherwise false.
	 */
	public boolean isUnderflow()
	{
		return (resources.size() < minCapacity);
	}
	
	/**
	 * Returns whether the number of resources in the pool is greater than the
	 * desired maximum capacity or the required amount of time has passed since
	 * the last allocated resource to free resources.
	 * 
	 * @return
	 * 		True if this pool could do without resource, otherwise false.
	 */
	public boolean isOverflow()
	{
		long elapsed = System.currentTimeMillis() - lastAllocateTime;
		
		return (elapsed > allocateThreshold || resources.size() > maxCapacity);
	}
	
	/**
	 * Sets the minimum and maximum capacity of the pool. This creates a pool
	 * with a fixed number of resources which never allocates or deallocates.
	 * 
	 * @param capacity
	 * 		The minimum and maximum capacity of the pool.
	 */
	public void setCapacity(int capacity)
	{
		maxCapacity = capacity;
		minCapacity = capacity;
	}
	
	/**
	 * Returns the maximum number of resources that can exist in the pool.  This 
	 * is not thread safe, therefore values returned may be out of date.
	 * 
	 * @return
	 * 		The maximum number of resources that can exist.
	 */
	public int getMaxCapacity() 
	{
		return maxCapacity;
	}

	/**
	 * Sets the maximum number of resources that can exist in the pool. This is 
	 * not thread safe, therefore its affects are not guaranteed to happen
	 * immediately.
	 * 
	 * @param maxCapacity
	 * 		The maximum number of resources.
	 */
	public void setMaxCapacity(int maxCapacity) 
	{
		this.maxCapacity = maxCapacity;
	}

	/**
	 * Returns the minimum number of resources that can exist in the pool. This
	 * is not thread safe, therefore values returned may be out of date.
	 * 
	 * @return
	 * 		The minimum number of resources that can exist.
	 */
	public int getMinCapacity() 
	{
		return minCapacity;
	}

	/**
	 * Sets the minimum number of resources that can exist in the pool. This is 
	 * not thread safe, therefore its affects are not guaranteed to happen
	 * immediately.
	 * 
	 * @param minCapacity
	 * 		The minimum number of resources.
	 */
	public void setMinCapacity(int minCapacity) 
	{
		this.minCapacity = minCapacity;
	}

	/**
	 * Returns the number of resources to allocate at once when they're required.
	 * This is not thread safe, therefore values returned may be out of date.
	 * 
	 * @return
	 * 		The number of resources to allocate at once.
	 */
	public int getAllocateSize() 
	{
		return allocateSize;
	}

	/**
	 * Sets the number of resources to allocate at once when they're required. 
	 * This is not thread safe, therefore its affects are not guaranteed to 
	 * happen immediately.
	 * 
	 * @param allocateSize
	 * 		The number of resources to allocate at once.
	 */
	public void setAllocateSize(int allocateSize) 
	{
		this.allocateSize = allocateSize;
	}

	/**
	 * Returns the number of resources to deallocate at once. This is not thread 
	 * safe, therefore values returned may be out of date.
	 * 
	 * @return
	 * 		The number of resources to deallocate at once.
	 */
	public int getDeallocateSize() 
	{
		return deallocateSize;
	}

	/**
	 * Sets the number of resources to allocate at once when they're required. 
	 * This is not thread safe, therefore its affects are not guaranteed to 
	 * happen immediately.
	 * 
	 * @param deallocateSize
	 * 		The number of resources to allocate at once.
	 */
	public void setDeallocateSize(int deallocateSize) 
	{
		this.deallocateSize = deallocateSize;
	}

	/**
	 * Returns the minimum amount of time to wait since the last allocation 
	 * before resources can be deallocated. This is not thread safe, therefore 
	 * values returned may be out of date.
	 * 
	 * @return
	 * 		The minimum amount of time in milliseconds.
	 */
	public long getAllocateThreshold() 
	{
		return allocateThreshold;
	}

	/**
	 * Sets the minimum amount of time to wait since the last allocation before 
	 * resources can be deallocated. This is not thread safe, therefore its 
	 * affects are not guaranteed to happen immediately.
	 * 
	 * @param allocateThreshold
	 * 		The minimum amount of time in milliseconds.
	 */
	public void setAllocateThreshold(long allocateThreshold) 
	{
		this.allocateThreshold = allocateThreshold;
	}

	/**
	 * Returns the factory used to create new resources. This is not thread 
	 * safe, therefore values returned may be out of date.
	 * 
	 * @return
	 * 		The reference to the ResourceFactory.
	 */
	public ResourceFactory<R> getFactory() 
	{
		return factory;
	}

	/**
	 * Returns the last time resources were allocated for the pool.
	 * 
	 * @return
	 * 		The last allocation time in milliseconds.
	 */
	public long getLastAllocateTime() 
	{
		return lastAllocateTime;
	}
	
	/**
	 * Returns the number of resources currently in this pool. This is not 
	 * thread safe, therefore values returned may be out of date.
	 * 
	 * @return
	 * 		The number of resources currently in this pool.
	 */
	public int getResourceCount()
	{
		return resources.size();
	}
	
	/**
	 * Returns the notifier which manages the listeners to events in the pool. 
	 * ResourceListeners can be directly added and removed to the notifier.
	 * Avoid invoking the methods of the proxy object in the notifier since it
	 * may notify the listeners falsely when an event has not actually occurred 
	 * with the pool. 
	 * 
	 * @return
	 * 		The reference to the ResourceListener notifier.
	 */
	public Notifier<ResourceListener<R>> getListeners() 
	{
		return listeners;
	}
	
}