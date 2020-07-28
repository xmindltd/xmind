/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.gef.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IAction;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class InteractiveMessage implements IInteractiveMessage {

    private final int type;

    private final String content;

    private final List<IAction> actions;

    /**
     * 
     */
    public InteractiveMessage(int type, String content) {
        this(type, content, Collections.<IAction> emptyList());
    }

    /**
     * 
     */
    public InteractiveMessage(int type, String content, List<IAction> actions) {
        Assert.isLegal(content != null);
        this.type = type;
        this.content = content;
        this.actions = actions == null ? Collections.<IAction> emptyList()
                : new ArrayList<IAction>(actions);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
     */
    @Override
    public String getMessage() {
        return content;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
     */
    @Override
    public int getMessageType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.ui.editor.IInteractiveMessage#getActions()
     */
    @Override
    public List<IAction> getActions() {
        return Collections.unmodifiableList(actions);
    }

}
