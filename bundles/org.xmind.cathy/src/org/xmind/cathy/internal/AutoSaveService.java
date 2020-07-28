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

package org.xmind.cathy.internal;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.xmind.ui.internal.editor.BackgroundSaveWorkbook;

/**
 * @author Frank Shaka
 * 
 */
public class AutoSaveService
        implements IStartup, IWorkbenchListener, IPropertyChangeListener {

    private IWorkbench workbench;

    /**
     * 
     */
    public AutoSaveService() {
    }

    /**
     * 
     */
    private void checkState() {
        IPreferenceStore ps = CathyPlugin.getDefault().getPreferenceStore();
        boolean enabled = ps.getBoolean(CathyPlugin.AUTO_SAVE_ENABLED);
        int intervals = ps.getInt(CathyPlugin.AUTO_SAVE_INTERVALS) * 60000;
        BackgroundSaveWorkbook.getInstance().reset(intervals, enabled);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
     * .jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (CathyPlugin.AUTO_SAVE_ENABLED.equals(property)
                || CathyPlugin.AUTO_SAVE_INTERVALS.equals(property)) {
            checkState();
        }
    }

    public void earlyStartup() {
        this.workbench = PlatformUI.getWorkbench();
        this.workbench.addWorkbenchListener(this);
        CathyPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(this);
        checkState();
    }

    public void postShutdown(IWorkbench workbench) {
        if (this.workbench == null)
            return;
        this.workbench.removeWorkbenchListener(this);
        this.workbench = null;
        CathyPlugin.getDefault().getPreferenceStore()
                .removePropertyChangeListener(this);
        BackgroundSaveWorkbook.getInstance().stopAll();
    }

    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        return true;
    }

}
