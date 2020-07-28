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
package org.xmind.core;

public interface INumbering extends IAdaptable, ITopicComponent {

    String getNumberFormat();

    String getPrefix();

    String getSuffix();

    String getSeparator();

    String getDepth();

    boolean prependsParentNumbers();

    void setFormat(String format);

    void setPrefix(String prefix);

    void setSuffix(String suffix);

    void setSeparator(String separator);

    void setDepth(String depth);

    void setPrependsParentNumbers(boolean prepend);

    String getParentFormat();

    String getComputedFormat();

    String getParentSeparator();

    String getComputedSeparator();

    int getComputedDepth();

    boolean isInherited(int min);

}