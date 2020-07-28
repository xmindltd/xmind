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
package org.xmind.ui.mindmap;

import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.gef.IGraphicalViewer;

public interface IMindMapViewer extends IGraphicalViewer {

    /**
     * Viewer property key indicating the viewer is centered or not.
     * <dl>
     * <dt>Values:</dt>
     * <dd>{@link Boolean}</dd>
     * </dl>
     */
    String VIEWER_CENTERED = "centered"; //$NON-NLS-1$

    /**
     * Viewer property key indicating the viewer is constrained within the
     * control.
     * <dl>
     * <dt>Values:</dt>
     * <dd>{@link Boolean}</dd>
     * </dl>
     */
    String VIEWER_CONSTRAINED = "constrained"; //$NON-NLS-1$

    /**
     * Viewer property key indicating the viewer uses extra corners or not.
     * <dl>
     * <dt>Values:</dt>
     * <dd>{@link Boolean}</dd>
     * </dl>
     */
    String VIEWER_CORNERED = "cornered"; //$NON-NLS-1$

    /**
     * Viewer property key for getting the action registry held by this viewer.
     * <dl>
     * <dt>Values:</dt>
     * <dd>{@link org.xmind.gef.ui.actions.IActionRegistry IActionRegistry}, or
     * <code>null</code></dd>
     * </dl>
     */
    String VIEWER_ACTIONS = "actions"; //$NON-NLS-1$

    /**
     * Viewer property for the margin of this viewer's canvas.
     * <dl>
     * <dt>Values:</dt>
     * <dd>magin of {@link Integer}</dd>
     * </dl>
     */
    String VIEWER_MARGIN = "margin"; //$NON-NLS-1$

    /**
     * Viewer property for the max topic level. Topics of which the level
     * measured from the viewer's central topic is more than the specified max
     * level are not intended be shown, which may result in no parts to be
     * created for those topics, e.g., <code>0</code> for only the central
     * topic, <code>1</code> for central topic, main topics and floating topics,
     * <code>2</code> for central topic, main topics and their DIRECT subtopics,
     * and floating topics and their DIRECT subtopics, ..., etc.
     * <dl>
     * <dt>Values:</dt>
     * <dd>{@link Integer}, <code>null</code> or less than zero means no
     * restriction on topic level is specified.</dd>
     * </dl>
     */
    String VIEWER_MAX_TOPIC_LEVEL = "maxTopicLevel"; //$NON-NLS-1$

    /**
     * Viewer property indicating whether figures will be painted in gradient
     * colors.
     * <dl>
     * <dt>Values:</dt>
     * <dd>{@link Boolean}</dd>
     * </dl>
     */
    String VIEWER_GRADIENT = "gradient"; //$NON-NLS-1$

    /**
     * Viewer property indicating whether to select central topic when current
     * page changed.
     * <dl>
     * <dt>Values:</dt>
     * <dd>{@link Boolean}</dd>
     * </dl>
     */
    String VIEWER_SELECT_CENTRALTOPIC = "selectCentralTopic"; //$NON-NLS-1$

    String PLUS_VISIBLE = "plusVisible"; //$NON-NLS-1$

    String MINUS_VISIBLE = "minusVisible"; //$NON-NLS-1$

    String VIEWER_EXPORT = "export"; //$NON-NLS-1$

    IMindMap getMindMap();

    void setMindMap(IMindMap mindMap);

    ISheet getSheet();

    ITopic getCentralTopic();

    boolean isPrimaryCentralTopic();

    ISheetPart getSheetPart();

    IBranchPart getCentralBranchPart();

    ITopicPart getCentralTopicPart();

}
