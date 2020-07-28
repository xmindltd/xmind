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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class BooleanPrefAction extends Action implements IWorkbenchAction,
        IPropertyChangeListener {

    private IPreferenceStore prefStore;

    private String prefKey;

    public BooleanPrefAction(IPreferenceStore prefStore, String prefKey) {
        super(null, AS_CHECK_BOX);
        this.prefKey = prefKey;
        this.prefStore = prefStore;
        if (this.prefStore != null) {
            this.prefStore.addPropertyChangeListener(this);
            setChecked(this.prefStore.getBoolean(prefKey));
        }
    }

    public void run() {
        if (prefStore == null)
            return;

        prefStore.setValue(prefKey, isChecked());
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (prefKey.equals(event.getProperty())) {
            setChecked(((Boolean) event.getNewValue()).booleanValue());
        }
    }

    public void dispose() {
        if (prefStore != null) {
            prefStore.removePropertyChangeListener(this);
            prefStore = null;
        }
    }

}