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
package org.xmind.ui.internal.outline;

import java.util.ArrayList;
import java.util.List;

import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.GEF;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.ui.mindmap.MindMapUI;

public class SheetTreePart extends MindMapTreePartBase {

    public SheetTreePart(ISheet model) {
        super(model);
    }

    public ISheet getSheet() {
        return (ISheet) super.getModel();
    }

    protected Object[] getModelChildren(Object model) {
        List<Object> list = new ArrayList<Object>();
        ISheet sheet = getSheet();
        list.add(sheet.getRootTopic());
        //list.addAll(sheet.getRelationships());
        return list.toArray();
    }

    protected void registerCoreEvents(Object source, ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.TitleText);
        register.register(Core.RelationshipAdd);
        register.register(Core.RelationshipRemove);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.TitleText.equals(type)) {
            runInUI(new Runnable() {
                public void run() {
                    update();
                }
            }, false);
        } else if (Core.RelationshipAdd.equals(type)
                || Core.RelationshipRemove.equals(type)) {
            runInUI(new Runnable() {
                public void run() {
                    refresh();
                }
            }, false);
        } else {
            super.handleCoreEvent(event);
        }
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_MODIFIABLE,
                MindMapUI.POLICY_MODIFIABLE);
    }

}