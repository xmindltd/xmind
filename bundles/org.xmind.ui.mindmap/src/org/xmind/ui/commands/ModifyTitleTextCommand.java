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

import org.xmind.core.ITitled;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyTitleTextCommand extends ModifyCommand {

    public ModifyTitleTextCommand(Collection<? extends ITitled> sources,
            String newValue) {
        super(sources, newValue);
    }

    public ModifyTitleTextCommand(ITitled source, String newValue) {
        super(source, newValue);
    }

    public ModifyTitleTextCommand(ISourceProvider sourceProvider,
            String newValue) {
        super(sourceProvider, newValue);
    }

    protected Object getValue(Object source) {
        if (source instanceof ITitled) {
            ITitled t = (ITitled) source;
            if (t.hasTitle())
                return t.getTitleText();
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ITitled
                && (value == null || value instanceof String)) {
            ((ITitled) source).setTitleText((String) value);
        }
    }

}