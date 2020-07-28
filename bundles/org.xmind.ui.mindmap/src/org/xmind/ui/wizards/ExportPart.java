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

import org.xmind.core.INamed;
import org.xmind.core.ITitled;
import org.xmind.core.ITopic;
import org.xmind.ui.internal.wizards.WizardMessages;

public abstract class ExportPart implements IExportPart {

    protected static final String TAGS = WizardMessages.Export_Tags;

    protected static final String CONTENTS = WizardMessages.Export_Contents;

    protected static final String SEE_ALSO = WizardMessages.Export_SeeAlso;

    protected static final String GROUPS = WizardMessages.Export_Groups;

    private final Object element;

    private final IExporter exporter;

    public ExportPart(IExporter exporter, Object element) {
        this.exporter = exporter;
        this.element = element;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.wizards.IExportPart#getExporter()
     */
    public IExporter getExporter() {
        return exporter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.wizards.IExportPart#getElement()
     */
    public Object getElement() {
        return element;
    }

    protected boolean isCentralTopic(ITopic topic) {
        return topic.equals(getExporter().getCentralTopic());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.wizards.IExportPart#toString()
     */
    public String toString() {
        if (element == null)
            return ""; //$NON-NLS-1$
        if (element instanceof ITitled)
            return ((ITitled) element).getTitleText();
        if (element instanceof INamed)
            return ((INamed) element).getName();
        return element.toString();
    }

}