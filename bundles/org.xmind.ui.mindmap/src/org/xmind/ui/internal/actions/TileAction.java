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
package org.xmind.ui.internal.actions;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;

public class TileAction extends RequestAction
        implements IPropertyChangeListener {

    private IPreferenceStore prefStore;

    public TileAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.TILE.getId(), page, MindMapUI.REQ_TILE);
        this.prefStore = MindMapUIPlugin.getDefault().getPreferenceStore();
        if (prefStore != null) {
            prefStore.addPropertyChangeListener(this);
            setEnabled(prefStore.getBoolean(PrefConstants.OVERLAPS_ALLOWED));
        }
    }

    public void dispose() {
        if (prefStore != null) {
            prefStore.removePropertyChangeListener(this);
            prefStore = null;
        }
        super.dispose();
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (PrefConstants.OVERLAPS_ALLOWED.equals(event.getProperty())) {
            setEnabled(((Boolean) event.getNewValue()).booleanValue());
        }
    }

    @Override
    public void run() {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_MODIFY, UserDataConstants.TILE);
        super.run();
    }

}
