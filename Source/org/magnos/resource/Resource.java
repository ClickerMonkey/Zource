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
 * A resource is typically expensive to allocate therefore its reused and pooled
 * as much as possible. 
 * 
 * @author Philip Diffenderfer
 *
 */
public interface Resource 
{
	
	/**
	 * Whether this resource is reusable. A reusable resource can be returned
	 * by the pool even though it may be currently used. A reusable resource
	 * should be thread safe and should keep track of its holders in order to 
	 * know whether its in use.
	 * 
	 * @return
	 * 		True if the resource can be used by many holders.
	 */
	public boolean isReusable();
	
	/**
	 * Whether this resource is unused. An unused resource has no holders and
	 * is ready to be deallocated upon request.
	 * 
	 * @return
	 * 		True if the resource can be deallocated.
	 */
	public boolean isUnused();
	
	/**
	 * Permanently releases the resource from use.
	 */
	public void free();
	
}
