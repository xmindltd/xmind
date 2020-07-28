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
package org.xmind.ui.util;

import java.io.File;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.xmind.ui.viewers.ImageCachedLabelProvider;

public class LocalFileLabelProvider extends ImageCachedLabelProvider {

    public String getText(Object element) {
        if (element instanceof String) {
            String path = (String) element;
            File file = new File(path);
            return file.getName();
        }
        return super.getText(element);
    }

    protected Image createImage(Object element) {
        if (element instanceof String) {
            String path = (String) element;
            try {
                return new Image(Display.getCurrent(), path);
            } catch (IllegalArgumentException e) {
            } catch (SWTException e) {
            } catch (SWTError e) {
            }
        }
        return null;
    }

}