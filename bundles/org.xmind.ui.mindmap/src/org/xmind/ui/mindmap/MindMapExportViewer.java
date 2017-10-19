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

package org.xmind.ui.mindmap;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.xmind.gef.util.Properties;
import org.xmind.ui.internal.mindmap.MindMapViewer;
import org.xmind.ui.viewers.ICompositeProvider;

/**
 * @author Frank Shaka
 * 
 */
public class MindMapExportViewer extends MindMapViewer {

    /**
     * 
     * @param parent
     * @param input
     * @param properties
     */
    public MindMapExportViewer(ICompositeProvider parent, Object input,
            Properties properties) {
        super();
        create(parent.getParent(), input, properties);
    }

    /**
     * 
     * @param parent
     * @param input
     * @param properties
     */
    public MindMapExportViewer(Composite parent, Object input,
            Properties properties) {
        super();
        create(parent, input, properties);
    }

    /**
     * @param shell2
     */
    private void create(Composite parent, Object input, Properties properties) {
        StackLayout layout = new StackLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent.setLayout(layout);

        initProperties(properties);

        createControl(parent);

        setInput(input);

        parent.layout();
        getLightweightSystem().getUpdateManager().performValidation();
    }

    protected void initProperties(Properties properties) {
        if (properties != null) {
            setProperties(properties);
        }
        if (properties == null || !properties.hasKey(VIEWER_CENTERED)) {
            getProperties().set(VIEWER_CENTERED, Boolean.TRUE);
        }
        if (properties == null || !properties.hasKey(VIEWER_CORNERED)) {
            getProperties().set(VIEWER_CORNERED, Boolean.TRUE);
        }
        if (properties == null || !properties.hasKey(VIEWER_MARGIN)) {
            getProperties().set(VIEWER_MARGIN,
                    Integer.valueOf(MindMapUI.DEFAULT_EXPORT_MARGIN));
        }
    }

}
