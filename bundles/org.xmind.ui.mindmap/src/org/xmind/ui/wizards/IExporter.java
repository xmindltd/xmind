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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;

public interface IExporter {

    ISheet getSheet();

    ITopic getCentralTopic();

    void init();

    void append(IExportPart part);

    List<IExportPart> getParts();

    int getTotalWork();

    boolean canStart();

    void start(Display display, Shell shell) throws InvocationTargetException;

    boolean hasNext();

    String getNextName();

    void writeNext(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException;

    void end() throws InvocationTargetException;

    Display getDisplay();

    Shell getShell();

    List<IRelationship> getRelationships();

    String getString(String propertyName, String defaultValue);

    boolean getBoolean(String propertyName);

    int getInt(String propertyName, int defaultValue);

    double getDouble(String propertyName, double defaultValue);

}