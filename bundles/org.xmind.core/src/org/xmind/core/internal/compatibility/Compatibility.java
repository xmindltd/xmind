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
package org.xmind.core.internal.compatibility;

import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.internal.dom.DeserializerImpl;
import org.xmind.core.internal.dom.FileFormat_0_1;
import org.xmind.core.internal.dom.FileFormat_1;
import org.xmind.core.internal.dom.WorkbookImpl;
import org.xmind.core.io.CoreIOException;

public class Compatibility {

    public static WorkbookImpl loadCompatibleWorkbook(
            DeserializerImpl deserializer) throws CoreException {
        WorkbookImpl workbook = null;

        if (workbook == null) {
            workbook = loadForFormat(new FileFormat_0_1(deserializer));
        }

        if (workbook == null) {
            workbook = loadForFormat(new FileFormat_1(deserializer));
        }

        return workbook;
    }

    private static WorkbookImpl loadForFormat(FileFormat format)
            throws CoreException {
        try {
            if (!format.identifies())
                return null;
            return format.load();
        } catch (CoreIOException e) {
            CoreException ce = e.getCoreException();
            if (ce.getType() == Core.ERROR_WRONG_PASSWORD
                    || ce.getType() == Core.ERROR_CANCELLATION) {
                throw new CoreException(ce.getType(), ce.getCodeInfo(), e);
            }
        } catch (CoreException e) {
            if (e.getType() == Core.ERROR_WRONG_PASSWORD
                    || e.getType() == Core.ERROR_CANCELLATION) {
                // if we encountered wrong password or cancellation,
                // interrupt the loading process
                throw e;
            }
            // otherwise, just continue to the next available format
        } catch (Throwable e) {
            // just continue to the next available format
        }
        return null;
    }

}