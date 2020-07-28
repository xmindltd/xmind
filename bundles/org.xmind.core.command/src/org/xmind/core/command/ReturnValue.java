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
package org.xmind.core.command;

import java.util.Arrays;

import org.eclipse.core.runtime.Status;
import org.xmind.core.command.arguments.Attributes;
import org.xmind.core.command.binary.IBinaryStore;

public class ReturnValue extends Status {

    private Object value;

    public ReturnValue(String pluginId, Attributes values) {
        super(OK, pluginId, null);
        this.value = values;
    }

    public ReturnValue(String pluginId, String[] values) {
        super(OK, pluginId, null);
        this.value = values;
    }

    public ReturnValue(String pluginId, IBinaryStore files) {
        super(OK, pluginId, null);
        this.value = files;
    }

    public ReturnValue(int severity, String pluginId, int code, String message,
            Attributes values) {
        super(severity, pluginId, code, message, null);
        this.value = values;
    }

    public ReturnValue(int severity, String pluginId, int code, String message,
            String[] values) {
        super(severity, pluginId, code, message, null);
        this.value = values;
    }

    public ReturnValue(int severity, String pluginId, int code, String message,
            IBinaryStore values) {
        super(severity, pluginId, code, message, null);
        this.value = values;
    }

    public Object getValue() {
        return value;
    }

    public Attributes getAttributes() {
        return (Attributes) value;
    }

    public String[] getStrings() {
        return (String[]) value;
    }

    public IBinaryStore getBinaryEntries() {
        return (IBinaryStore) value;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("ReturnValue["); //$NON-NLS-1$
        int severity = getSeverity();
        if (severity == OK) {
            buf.append("OK"); //$NON-NLS-1$
        } else if (severity == ERROR) {
            buf.append("ERROR"); //$NON-NLS-1$
        } else if (severity == WARNING) {
            buf.append("WARNING"); //$NON-NLS-1$
        } else if (severity == INFO) {
            buf.append("INFO"); //$NON-NLS-1$
        } else if (severity == CANCEL) {
            buf.append("CANCEL"); //$NON-NLS-1$
        } else {
            buf.append("severity="); //$NON-NLS-1$
            buf.append(severity);
        }
        buf.append(';');
        buf.append(getCode());
        buf.append(';');
        buf.append(getPlugin());
        String message = getMessage();
        if (message != null && !"".equals(message)) { //$NON-NLS-1$
            buf.append(';');
            buf.append('\'');
            buf.append(message);
            buf.append('\'');
        }
        Throwable exception = getException();
        if (exception != null) {
            buf.append(";exception="); //$NON-NLS-1$
            buf.append(exception);
        }
        if (value != null) {
            buf.append(";value="); //$NON-NLS-1$
            if (value instanceof String[]) {
                buf.append(Arrays.toString((String[]) value));
            } else {
                buf.append(value);
            }
        }
        buf.append(']');
        return buf.toString();
    }

}
