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
package org.xmind.cathy.internal.jobs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.core.command.transfer.IncomingCommandHandler;

public class OpenXMindCommandFileJob extends Job {

    private static final String DEBUG_OPTION = "/debug/openXMindCommandFile"; //$NON-NLS-1$

    private static boolean DEBUGGING = CathyPlugin.getDefault()
            .isDebugging(DEBUG_OPTION);

    private static class InternalOpenXMindCommandFileHandler
            extends IncomingCommandHandler {

        protected IStatus createReadingErrorStatus(Throwable e) {
            return super.createReadingErrorStatus(e);
        }

        protected IStatus createWritingErrorStatus(Throwable e) {
            return super.createWritingErrorStatus(e);
        }

    }

    private static class NullOutputStream extends OutputStream {

        public void write(int b) throws IOException {
            // ignore all bits
        }

        public void write(byte[] b) throws IOException {
            // ignore all bits
        }

        public void write(byte[] b, int off, int len) throws IOException {
            // ignore all bits
        }

    }

    private String commandFilePath;

    public OpenXMindCommandFileJob(String commandFilePath) {
        super(NLS.bind(WorkbenchMessages.OpenXMindCommandFileJob_name,
                commandFilePath));
        this.commandFilePath = commandFilePath;
        setUser(false);
    }

    protected IStatus run(IProgressMonitor monitor) {
        if (commandFilePath == null)
            return new Status(IStatus.WARNING, CathyPlugin.PLUGIN_ID,
                    WorkbenchMessages.OpenXMindCommandFileJob_failed_noCommandFilePath_text);

        File commandFile = new File(commandFilePath);
        if (!commandFile.exists() || !commandFile.isFile()
                || !commandFile.canRead())
            return new Status(IStatus.WARNING, CathyPlugin.PLUGIN_ID,
                    NLS.bind(
                            WorkbenchMessages.OpenXMindCommandFileJob_failed_fileIsNotReadable,
                            commandFilePath));

        try {
            InputStream input = new FileInputStream(commandFile);
            try {
                OutputStream output = createOutputStream();
                try {
                    IncomingCommandHandler handler = new InternalOpenXMindCommandFileHandler();
                    handler.setPluginId(CathyPlugin.PLUGIN_ID);
                    IStatus result = handler.handleIncomingCommand(monitor,
                            input, output);
                    if (DEBUGGING) {
                        logOutput(output);
                    }
                    return result;
                } finally {
                    output.close();
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            return new Status(IStatus.WARNING, CathyPlugin.PLUGIN_ID,
                    NLS.bind(
                            WorkbenchMessages.OpenXMindCommandFileJob_failed_openXMindCommandFile,
                            e.getLocalizedMessage()),
                    e);
        }
    }

    private void logOutput(OutputStream output) {
        if (!(output instanceof ByteArrayOutputStream))
            return;
        try {
            String outputText = ((ByteArrayOutputStream) output)
                    .toString("UTF-8"); //$NON-NLS-1$
            System.out.println("Response of XMind command file: " //$NON-NLS-1$
                    + commandFilePath);
            System.out.println(outputText);
        } catch (UnsupportedEncodingException e2) {
            // Ignore encoding error
        }
    }

    private OutputStream createOutputStream() {
        if (DEBUGGING)
            return new ByteArrayOutputStream(4096);
        return new NullOutputStream();
    }

}
