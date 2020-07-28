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
package org.xmind.ui.internal.branch;

import org.eclipse.jface.resource.ImageDescriptor;
import org.xmind.ui.branch.IBranchStructure;

class DefaultStructureDescriptor implements IStructureDescriptor {

    private static final DefaultStructureDescriptor instance = new DefaultStructureDescriptor();

    private IBranchStructure algorithm;

    public String getId() {
        return "org.xmind.ui.structure.default"; //$NON-NLS-1$
    }

    public String getName() {
        return null;
    }

    public ImageDescriptor getIcon() {
        return null;
    }

    public IBranchStructure getAlgorithm() {
        if (algorithm == null) {
            algorithm = new RightStructure();
        }
        return algorithm;
    }

    public static DefaultStructureDescriptor getInstance() {
        return instance;
    }

}