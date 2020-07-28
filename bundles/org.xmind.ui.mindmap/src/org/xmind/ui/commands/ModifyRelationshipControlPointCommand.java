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
package org.xmind.ui.commands;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.IControlPoint;
import org.xmind.core.IRelationship;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

/**
 * @deprecated
 * @author frankshaka
 * 
 */
public class ModifyRelationshipControlPointCommand extends ModifyCommand {

    private int index;

    public ModifyRelationshipControlPointCommand(IRelationship source,
            int index, double angle, double amount) {
        super(source, null);
        Assert.isTrue(index == 0 || index == 1);
        this.index = index;
        setNewValue(new ControlPointData(angle, amount));
    }

    public ModifyRelationshipControlPointCommand(
            Collection<IRelationship> sources, int index, double angle,
            double amount) {
        super(sources, null);
        Assert.isTrue(index == 0 || index == 1);
        this.index = index;
        setNewValue(new ControlPointData(angle, amount));
    }

    public ModifyRelationshipControlPointCommand(
            ISourceProvider sourceProvider, int index, double angle,
            double amount) {
        super(sourceProvider, null);
        Assert.isTrue(index == 0 || index == 1);
        this.index = index;
        setNewValue(new ControlPointData(angle, amount));
    }

    protected ModifyRelationshipControlPointCommand(
            ISourceProvider sourceProvider, int index, ControlPointData data) {
        super(sourceProvider, data);
        Assert.isTrue(index == 0 || index == 1);
        this.index = index;
    }

    protected Object getValue(Object source) {
        if (source != null && source instanceof IRelationship) {
            IControlPoint cp = ((IRelationship) source).getControlPoint(index);
            if (cp == null)
                return null;
//            return new ControlPointData(cp.getAngle(), cp.getAmount());
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof IRelationship) {
            IRelationship rel = (IRelationship) source;
            if (value == null) {
                resetRelationship(rel);
            } else if (value instanceof ControlPointData) {
                ControlPointData cp = (ControlPointData) value;
                if (cp.angle == null || cp.amount == null) {
                    resetRelationship(rel);
                } else {
//                    rel.setControlPoint(index, cp.angle.doubleValue(),
//                            cp.amount.doubleValue());
                }
            }
        }
    }

    private void resetRelationship(IRelationship rel) {
//        rel.resetControlPoint(index);
    }

}