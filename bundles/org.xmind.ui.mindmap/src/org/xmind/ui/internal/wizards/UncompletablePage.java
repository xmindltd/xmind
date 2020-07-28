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
package org.xmind.ui.internal.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class UncompletablePage extends WizardPage {

    private static final String ERROR_PAGE_NAME = "errorPage"; //$NON-NLS-1$

    private String messages;

    public UncompletablePage(String title, String messages) {
        super(ERROR_PAGE_NAME, title, null);
        this.messages = messages;
    }

    public void createControl(Composite parent) {
        Composite msgContainer = new Composite(parent, SWT.NONE);
        msgContainer.setLayout(new GridLayout());

        Label label = new Label(msgContainer, SWT.WRAP);
        label.setText(messages);
        GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
        gd.widthHint = 400;
        label.setLayoutData(gd);

        setControl(msgContainer);
        setPageComplete(false);
    }

}