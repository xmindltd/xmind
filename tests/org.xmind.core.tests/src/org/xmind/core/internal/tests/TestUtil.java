/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core.internal.tests;

import java.io.*;
import java.util.*;

import org.xmind.core.util.*;

/**
 * @author Frank Shaka
 *
 */
public class TestUtil {

    /**
     * @return
     */
    public static String randString() {
        return UUID.randomUUID().toString();
    }

    public static byte[] readBytes(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileUtils.transfer(input, output, true);
        return output.toByteArray();
    }

}
