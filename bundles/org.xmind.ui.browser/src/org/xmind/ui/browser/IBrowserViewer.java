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
package org.xmind.ui.browser;

import java.beans.PropertyChangeListener;

import org.eclipse.swt.widgets.Control;
import org.xmind.ui.animation.AnimationViewer;

/**
 * @author briansun
 */
public interface IBrowserViewer {

    String PROPERTY_TITLE = "title"; //$NON-NLS-1$

    String PROPERTY_LOCATION = "location"; //$NON-NLS-1$

    String PROPERTY_STATUS = "status"; //$NON-NLS-1$

    boolean setURL(String url);

    boolean setText(String html);

    String getURL();

    AnimationViewer getBusyIndicator();

    Control getControl();

    IBrowserViewerContainer getContainer();

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

    void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener);

    void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener);

    void setFocus();

}