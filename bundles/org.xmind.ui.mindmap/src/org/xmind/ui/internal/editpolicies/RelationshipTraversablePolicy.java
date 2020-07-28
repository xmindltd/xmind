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
package org.xmind.ui.internal.editpolicies;

import java.util.List;

import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IRelationshipPart;

public class RelationshipTraversablePolicy extends MindMapTraversablePolicyBase {

    protected void findTraversables(Request request, IPart source,
            List<IPart> result) {
        if (source instanceof IRelationshipPart) {
            IRelationshipPart rel = (IRelationshipPart) source;
            addTraversableResult(rel.getSourceNode(), result);
            addTraversableResult(rel.getTargetNode(), result);
        }
    }

}