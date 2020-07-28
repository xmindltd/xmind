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

public interface IPartListener2 extends IPartListener {

    public void childAdding(PartEvent event);

    public void childRemoved(PartEvent event);

    public class Stub implements IPartListener2 {

        public void childAdded(PartEvent event) {
        }

        public void childRemoving(PartEvent event) {
        }

        public void childAdding(PartEvent event) {
        }

        public void childRemoved(PartEvent event) {
        }
    }
}
