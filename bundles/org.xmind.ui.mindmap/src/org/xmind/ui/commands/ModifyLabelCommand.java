/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.commands;

import java.util.Collection;

import org.xmind.core.ILabeled;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyLabelCommand extends ModifyCommand {

    public ModifyLabelCommand(Collection<? extends ILabeled> sources,
            Collection<String> newLabels) {
        super(sources, newLabels);
    }

    public ModifyLabelCommand(ISourceProvider sourceProvider,
            Collection<String> newLabels) {
        super(sourceProvider, newLabels);
    }

    public ModifyLabelCommand(ILabeled source, Collection<String> newLabels) {
        super(source, newLabels);
    }

    protected Object getValue(Object source) {
        if (source instanceof ILabeled) {
            return ((ILabeled) source).getLabels();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected void setValue(Object source, Object value) {
        if (source instanceof ILabeled) {
            ILabeled labeled = (ILabeled) source;
            if (value instanceof Collection) {
                Collection<String> labels = (Collection<String>) value;
                labeled.setLabels(labels);
            }
        }
    }

}