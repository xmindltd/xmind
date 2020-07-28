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
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.prefs.PrefConstants;

public class AllowOverlapsAction extends BooleanPrefAction {

    public AllowOverlapsAction(IPreferenceStore prefStore) {
        super(prefStore, PrefConstants.OVERLAPS_ALLOWED);
        setId("org.xmind.ui.allowOverlaps"); //$NON-NLS-1$
        setText(MindMapMessages.AllowOverlaps_text);
        setToolTipText(MindMapMessages.AllowOverlaps_toolTip);
    }

    @Override
    public void run() {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_MODIFY,
                UserDataConstants.ALLOW_OVERLAP);
        super.run();
    }

}
