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
package org.xmind.ui.animation;

public abstract class AlphaAnimationTask extends AbstractAnimationTask {

    public AlphaAnimationTask(Object source, int start, int end) {
        super(source, start, end);
    }

    public Object getCurrentValue(int current, int total) {
        int startAlpha = (Integer) getStartValue();
        int endAlpha = (Integer) getEndValue();
        return startAlpha + (endAlpha - startAlpha) * current / total;
    }

}