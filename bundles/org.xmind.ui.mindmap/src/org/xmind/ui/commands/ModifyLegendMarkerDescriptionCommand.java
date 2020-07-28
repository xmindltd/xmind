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
import org.xmind.core.ILegend;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyLegendMarkerDescriptionCommand extends ModifyCommand {

    private String markerId;

    public ModifyLegendMarkerDescriptionCommand(ILegend legend,
            String markerId, String description) {
        super(legend, description);
        Assert.isNotNull(markerId);
        this.markerId = markerId;
    }

    public ModifyLegendMarkerDescriptionCommand(
            Collection<? extends ILegend> legends, String markerId,
            String description) {
        super(legends, description);
        Assert.isNotNull(markerId);
        this.markerId = markerId;
    }

    public ModifyLegendMarkerDescriptionCommand(ISourceProvider legendProvider,
            String markerId, String description) {
        super(legendProvider, description);
        Assert.isNotNull(markerId);
        this.markerId = markerId;
    }

    protected Object getValue(Object source) {
        if (source instanceof ILegend) {
            ILegend legend = (ILegend) source;
            return legend.getMarkerDescription(markerId);
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ILegend) {
            ILegend legend = (ILegend) source;
            if (value == null) {
                legend.setMarkerDescription(markerId, null);
            } else if (value instanceof String) {
                legend.setMarkerDescription(markerId, (String) value);
            }
        }
    }

}