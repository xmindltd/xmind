/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.gef.part;

import org.xmind.gef.Request;
import org.xmind.gef.policy.IEditPolicy;

/**
 * @author Brian Sun
 */
public interface IRequestHandler {

    IEditPolicy getPolicy(String role);

    void handleRequest(Request request, String role);

    boolean hasRole(String role);

    void installEditPolicy(String role, IEditPolicy editPolicy);

    void installEditPolicy(String role, String editPolicyId);

    void uninstallEditPolicy(String role);

}