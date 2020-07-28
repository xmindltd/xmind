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
package org.xmind.core.internal.experiments;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.xmind.core.*;
import org.xmind.core.internal.zip.*;

@SuppressWarnings("nls")
public class CompressionTest {

    private static final Random rand = new Random();

    private static final byte[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ".getBytes();

    public CompressionTest() {
    }

    private void createWorkbookContents(IWorkbook workbook) {
        ITopic root = workbook.getPrimarySheet().getRootTopic();
        for (int i = 0; i < 1000; i++) {
            ITopic topic = workbook.createTopic();
            topic.setTitleText("Topic " + i);
            INotesContent notes = workbook.createNotesContent(INotes.PLAIN);
            ((IPlainNotesContent) notes).setTextContent(createRandomString(256));
            topic.getNotes().setContent(INotes.PLAIN, notes);
            root.add(topic);
        }
    }

    private String createRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) chars[rand.nextInt(chars.length)]);
        }
        return sb.toString();
    }

    public void testCompressionSpeed() throws Exception {
        IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();
        createWorkbookContents(workbook);

        save(workbook, true, false);
        save(workbook, false, false);

        int sum1 = 0, sum2 = 0, iter = 100;
        for (int i = 0; i < iter; i++) {
            sum1 += save(workbook, true, false);
            sum2 += save(workbook, false, false);
        }
        System.out.println("Compressed (ms): " + (sum1 * 1.0 / iter));
        System.out.println("Uncompressed (ms): " + (sum2 * 1.0 / iter));
    }

    public void testCompressionSize() throws Exception {
        IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();
        createWorkbookContents(workbook);

        int size1 = save(workbook, true, true);
        int size2 = save(workbook, false, true);
        System.out.println("Compressed (bytes): " + size1);
        System.out.println("Uncompressed (bytes): " + size2);
    }

    private int save(IWorkbook workbook, boolean compressed, boolean returnSizeOrTime)
            throws IOException, CoreException {
        long start;
        long end;
        ByteArrayOutputStream stream = new ByteArrayOutputStream(524288);
        try {
            start = System.currentTimeMillis();
            ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
            serializer.setWorkbook(workbook);
            serializer.setOutputTarget(new ZipStreamOutputTarget(new ZipOutputStream(stream), compressed));
            serializer.serialize(null);
            end = System.currentTimeMillis();
            if (returnSizeOrTime)
                return stream.size();
            return (int) (end - start);
        } finally {
            stream.close();
        }
    }

    public void run() {
        try {
            testCompressionSize();
            testCompressionSpeed();

            System.out.println("Done.");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CompressionTest().run();
    }

}
