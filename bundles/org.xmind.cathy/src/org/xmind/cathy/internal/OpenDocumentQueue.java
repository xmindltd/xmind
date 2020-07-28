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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class OpenDocumentQueue {

    private static final String[] EMPTY_QUEUE = new String[0];

    private class OpenDocumentHook implements Listener {

        public void handleEvent(Event event) {
            if (event.text != null && !"".equals(event.text)) //$NON-NLS-1$
                enqueue(event.text);
        }

    }

    private static final OpenDocumentQueue instance = new OpenDocumentQueue();

    private List<String> files = new ArrayList<String>();

    private Listener hook = null;

    private OpenDocumentQueue() {
    }

    public void hook(Display display) {
        if (hook == null) {
            hook = new OpenDocumentHook();
        }
        display.addListener(SWT.OpenDocument, hook);
    }

    public void unhook(Display display) {
        if (hook != null)
            display.removeListener(SWT.OpenDocument, hook);
    }

    public void enqueue(String path) {
        if (new File(path).exists()) {
            synchronized (this) {
                files.add(path);
            }
            CathyPlugin.log("Path queued to be opened: " + path); //$NON-NLS-1$
        } else {
            CathyPlugin.log("Non-existing path skipped: " + path); //$NON-NLS-1$
        }
    }

    public String[] drain() {
        synchronized (this) {
            if (files.isEmpty())
                return EMPTY_QUEUE;
            String[] array = files.toArray(new String[files.size()]);
            files.clear();
            return array;
        }
    }

    public static OpenDocumentQueue getInstance() {
        return instance;
    }

}
