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

/**
 * A listener of resource allocation and deallocation in a resource pool.
 * 
 * @author Philip Diffenderfer
 *
 * @param <R>
 * 		The resource type.
 */
public interface ResourceListener<R extends Resource>
{
	
	/**
	 * Invoked when the pool has allocated a resource because a new resource is
	 * required. A new resource is required when the pool is queried via the 
	 * allocate or populate methods.
	 * 
	 * @param pool
	 * 		The pool which the resource was allocated to.
	 * @param resource
	 * 		The resource that has been allocated to the pool.
	 */
	public void onResourceAllocate(ResourcePool<R> pool, R resource);
	
	/**
	 * Invoked when the pool has deallocated a resource because the pool
	 * capacity exceeded the maximum or the resource wasn't needed anymore.
	 * 
	 * @param pool
	 * 		The pool which the resource was deallocated from.
	 * @param resource
	 * 		The resource that has been deallocated from the pool.
	 */
	public void onResourceDeallocate(ResourcePool<R> pool, R resource);
	
}
