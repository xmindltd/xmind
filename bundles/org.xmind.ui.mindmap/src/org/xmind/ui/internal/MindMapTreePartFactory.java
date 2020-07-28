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
package org.xmind.ui.internal;

import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.ui.internal.outline.RelationshipTreePart;
import org.xmind.ui.internal.outline.SheetTreePart;
import org.xmind.ui.internal.outline.TopicTreePart;
import org.xmind.ui.internal.outline.WorkbookTreePart;

public class MindMapTreePartFactory implements IPartFactory {

    public IPart createPart(IPart parent, Object model) {
        if (model instanceof ISheet) {
            return new SheetTreePart((ISheet) model);
        } else if (model instanceof ITopic) {
            return new TopicTreePart((ITopic) model);
        } else if (model instanceof IWorkbook) {
            return new WorkbookTreePart((IWorkbook) model);
        } else if (model instanceof IRelationship) {
            return new RelationshipTreePart((IRelationship) model);
        }
        return null;
    }

}