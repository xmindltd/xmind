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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

public class CathyPrefInitializer extends AbstractPreferenceInitializer {

    public CathyPrefInitializer() {
    }

    public void initializeDefaultPreferences() {
        IScopeContext context = DefaultScope.INSTANCE;
        IEclipsePreferences node = context.getNode(
                CathyPlugin.getDefault().getBundle().getSymbolicName());
        node.putBoolean(CathyPlugin.AUTO_SAVE_ENABLED, false);
        node.putInt(CathyPlugin.AUTO_SAVE_INTERVALS, 5);
        node.putBoolean(CathyPlugin.RESTORE_LAST_SESSION, false);
        node.putBoolean(CathyPlugin.CHECK_UPDATES_ON_STARTUP, true);
        node.putInt(CathyPlugin.STARTUP_ACTION,
                CathyPlugin.STARTUP_ACTION_WIZARD);

//        ColorRegistry colorRegistry = PlatformUI.getWorkbench()
//                .getThemeManager().getCurrentTheme().getColorRegistry();
//        colorRegistry.put("org.eclipse.ui.workbench.ACTIVE_NOFOCUS_TAB_BG_END", //$NON-NLS-1$
//                new RGB(191, 205, 219));
//        colorRegistry.put(
//                "org.eclipse.ui.workbench.ACTIVE_NOFOCUS_TAB_BG_START", //$NON-NLS-1$
//                new RGB(191, 205, 219));
//        colorRegistry.put("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END", //$NON-NLS-1$
//                new RGB(153, 180, 209));
//        colorRegistry.put("org.eclipse.ui.workbench.ACTIVE_TAB_BG_START", //$NON-NLS-1$
//                new RGB(153, 180, 209));
//        colorRegistry.put("org.eclipse.ui.workbench.INACTIVE_TAB_BG_END", //$NON-NLS-1$
//                new RGB(240, 240, 240));
//        colorRegistry.put("org.eclipse.ui.workbench.INACTIVE_TAB_BG_START", //$NON-NLS-1$
//                new RGB(240, 240, 240));

    }

}