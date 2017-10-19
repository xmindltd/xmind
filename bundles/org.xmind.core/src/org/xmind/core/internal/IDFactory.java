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
package org.xmind.core.internal;

import java.math.BigInteger;
import java.util.Random;

import org.xmind.core.IIdFactory;

/**
 * @author briansun
 * 
 */
public class IDFactory implements IIdFactory {

    private static final int ID_LENGTH = 26;

    private static final char PADDING_CHAR = '0';

    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * @return
     */
    public String createId() {
        BigInteger bi = new BigInteger(128, random);
        String id = bi.toString(32);
        int paddingLength = ID_LENGTH - id.length();
        if (paddingLength > 0) {
            StringBuffer buf = new StringBuffer(ID_LENGTH);
            for (int i = 0; i < paddingLength; i++) {
                buf.append(PADDING_CHAR);
            }
            buf.append(id);
            return buf.toString();
        }
        return id;
    }
}