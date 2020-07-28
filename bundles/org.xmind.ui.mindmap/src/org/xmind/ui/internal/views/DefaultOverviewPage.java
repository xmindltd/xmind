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
package org.xmind.ui.internal.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.Page;
import org.xmind.ui.internal.MindMapMessages;

@Deprecated
public class DefaultOverviewPage extends Page {

    private Control control;

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());

        Label label = new Label(composite, SWT.WRAP);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        label.setText(MindMapMessages.DefaultOverviewPage_message);

        control = composite;
    }

    @Override
    public Control getControl() {
        return control;
    }

    @Override
    public void setFocus() {
        control.setFocus();
    }

}
