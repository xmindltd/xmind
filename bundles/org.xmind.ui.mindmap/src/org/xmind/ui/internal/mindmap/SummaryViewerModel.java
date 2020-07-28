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
package org.xmind.ui.internal.mindmap;

import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.gef.part.IPart;

public class SummaryViewerModel extends ViewerModel {

    private ISummary summary;

    public SummaryViewerModel(Class<? extends IPart> partType,
            ISummary summary, ITopic summaryTopic) {
        super(partType, summaryTopic);
        this.summary = summary;
    }

    public ISummary getSummary() {
        return summary;
    }

    public Object getAdapter(Class adapter) {
        if (adapter == ISummary.class)
            return getSummary();
        if (adapter == ITopic.class)
            return getRealModel();
        return super.getAdapter(adapter);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof SummaryViewerModel))
            return false;
        SummaryViewerModel that = (SummaryViewerModel) obj;
        return this.getPartType() == that.getPartType()
                && this.getRealModel().equals(that.getRealModel())
                && this.summary.equals(that.summary);
    }

    public int hashCode() {
        return super.hashCode() ^ summary.hashCode();
    }

}