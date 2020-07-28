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
package org.xmind.ui.internal.mindmap;

import org.xmind.gef.part.IPart;

public class ChildSorter {

    private IPart part;

    private final java.util.Comparator<IPart> childComparator = new java.util.Comparator<IPart>() {
        public int compare(IPart o1, IPart o2) {
            java.util.List<IPart> children = part.getChildren();
            return children.indexOf(o1) - children.indexOf(o2);
        }
    };

    public ChildSorter(IPart part) {
        this.part = part;
    }

    public void sort(java.util.List<? extends IPart> children) {
        java.util.Collections.sort(children, childComparator);
    }

}