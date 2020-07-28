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

import java.lang.reflect.Method;
import java.util.Properties;

import org.eclipse.equinox.app.IApplicationContext;
import org.xmind.ui.internal.app.IApplicationValidator;

public class ApplicationValidator implements IApplicationValidator {

    public ApplicationValidator() {
    }

    public boolean shouldApplicationExitEarly(IApplicationContext appContext) {
        Log log = Log.get(Log.SINGLETON);
        if (!log.exists())
            return false;

        Properties properties = log.getProperties();
        if (properties.isEmpty())
            return false;

        int hWnd = getOpenedWindowHandle(properties);
        if (hWnd == 0 || !isValidWindow(hWnd))
            return false;

        notifyOpenedWindow(hWnd);
        return true;
    }

    private boolean isValidWindow(int hWnd) {
        int length = invokeOSMethod("GetWindowTextLength", hWnd); //$NON-NLS-1$
        return length > 0;
    }

    private int getOpenedWindowHandle(Properties properties) {
        String value = properties.getProperty(Log.K_PRIMARY_WINDOW);
        if (value != null && !"".equals(value)) { //$NON-NLS-1$
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void notifyOpenedWindow(int hWnd) {
        invokeOSMethod("SetForegroundWindow", hWnd); //$NON-NLS-1$
        invokeOSMethod("SetFocus", hWnd); //$NON-NLS-1$
    }

    private static Class<?> OS_CLAZZ = null;

    private static int invokeOSMethod(String methodName, int hWnd) {
        if (OS_CLAZZ == null) {
            try {
                OS_CLAZZ = Class.forName("org.eclipse.swt.internal.win32.OS"); //$NON-NLS-1$
            } catch (Throwable e) {
                OS_CLAZZ = ApplicationValidator.class;
            }
        }
        if (OS_CLAZZ != ApplicationValidator.class) {
            try {
                Method method = OS_CLAZZ.getMethod(methodName, int.class);
                Object result = method.invoke(null, hWnd);
                if (result instanceof Integer)
                    return ((Integer) result).intValue();
            } catch (Throwable e) {
            }
        }
        return -1;
    }

}