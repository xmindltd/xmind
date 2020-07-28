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
package org.xmind.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.ui.internal.wizards.WizardMessages;

public abstract class DocumentExportWizard extends AbstractMindMapExportWizard {

    protected void doExport(IProgressMonitor monitor, Display display,
            Shell parentShell) throws InvocationTargetException,
            InterruptedException {
        monitor.beginTask(null, 100);

        monitor.subTask(WizardMessages.Export_Initializing);
        IExporter exporter = createExporter();
        if (!exporter.canStart())
            throw new InterruptedException();

        exporter.start(display, parentShell);
        monitor.worked(10);

        int total = exporter.getTotalWork();
        int worked = 0;
        int uiTotal = 88;
        int uiWorked = 0;

        while (exporter.hasNext()) {
            monitor.subTask(cleanFileName(exporter.getNextName()));
            exporter.writeNext(monitor);

            worked++;
            int newUIWorked = worked * uiTotal / total;
            if (newUIWorked > uiWorked) {
                monitor.worked(newUIWorked - uiWorked);
                uiWorked = newUIWorked;
            }
        }
        if (uiWorked < uiTotal) {
            monitor.worked(uiTotal - uiWorked);
        }

        monitor.subTask(WizardMessages.Export_Finishing);
        exporter.end();
        monitor.worked(1);

        launchTargetFile(true, monitor, display, parentShell);
        monitor.done();
    }

    protected abstract IExporter createExporter();

}