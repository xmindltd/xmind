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
package org.xmind.ui.internal.spelling;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.xmind.ui.texteditor.IControlContentAdapter2;
import org.xmind.ui.texteditor.IMenuContributor;
import org.xmind.ui.texteditor.ISpellingActivation;
import org.xmind.ui.texteditor.ISpellingSupport;

public class JazzySpellingSupport implements ISpellingSupport {

    private static final String KEY_SPELLING_HELPER = "KEY_SPELLING_HELPER"; //$NON-NLS-1$

    public JazzySpellingSupport() {
    }

    public void install(final Control control, IControlContentAdapter2 adapter) {
        if (!SpellingPlugin.isSpellingCheckEnabled())
            return;

        if (control.getData(KEY_SPELLING_HELPER) instanceof SpellingHelper)
            return;

        install(control, activateSpelling(control, adapter));
    }

    public void install(ITextViewer textViewer) {
        if (!SpellingPlugin.isSpellingCheckEnabled())
            return;

        Control control = textViewer.getTextWidget();
        if (control.getData(KEY_SPELLING_HELPER) instanceof SpellingHelper)
            return;

        install(control, activateSpelling(textViewer));
    }

    private void install(final Control control,
            final ISpellingActivation activation) {
        control.setData(KEY_SPELLING_HELPER, activation);
        final MenuManager menu = new MenuManager();
        menu.setRemoveAllWhenShown(true);
        menu.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                IMenuContributor contibutor = (IMenuContributor) activation
                        .getAdapter(IMenuContributor.class);
                if (contibutor != null)
                    contibutor.fillMenu(manager);
            }
        });
        control.setMenu(menu.createContextMenu(control));
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                deactivateSpelling(activation);
                control.setMenu(null);
                menu.dispose();
                control.setData(KEY_SPELLING_HELPER, null);
            }
        });
    }

    public ISpellingActivation activateSpelling(Control control,
            IControlContentAdapter2 adapter) {
        if (!SpellingPlugin.isSpellingCheckEnabled())
            return null;
        return new SpellingHelper(this, control, adapter);
    }

    public ISpellingActivation activateSpelling(ITextViewer viewer) {
        if (!SpellingPlugin.isSpellingCheckEnabled())
            return null;
        return new SpellingHelper(this, viewer);
    }

    public void deactivateSpelling(ISpellingActivation activation) {
        if (activation instanceof SpellingHelper)
            ((SpellingHelper) activation).dispose();
    }

}