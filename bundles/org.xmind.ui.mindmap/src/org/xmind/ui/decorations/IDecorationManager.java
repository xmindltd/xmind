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
package org.xmind.ui.decorations;

import java.util.List;

public interface IDecorationManager {

    // Decoration Categories:
    String CATEGORY_BRANCH_CONNECTIONS = "org.xmind.ui.branchConnections"; //$NON-NLS-1$

    String CATEGORY_TOPIC_SHAPE = "org.xmind.ui.topicDecorations"; //$NON-NLS-1$

    String CATEGORY_REL_SHAPE = "org.xmind.ui.relationshipDecorations"; //$NON-NLS-1$

    String CATEGORY_BOUNDARY_SHAPE = "org.xmind.ui.boundaryDecorations"; //$NON-NLS-1$

    String CATEGORY_SUMMARY_SHAPE = "org.xmind.ui.summaryDecorations"; //$NON-NLS-1$

    String CATEGORY_ARROW_SHAPE = "org.xmind.ui.arrowDecorations"; //$NON-NLS-1$

    String CATEGORY_CALLOUT_SHAPE = "org.xmind.ui.calloutTopicDecorations"; //$NON-NLS-1$

    String CATEGORY_CALLOUT_BRANCH_CONNECTIONS = "org.xmind.ui.calloutConnections"; //$NON-NLS-1$

    IDecorationDescriptor getDecorationDescriptor(String decorationId);

    List<IDecorationDescriptor> getDescriptors(String categoryId);

}