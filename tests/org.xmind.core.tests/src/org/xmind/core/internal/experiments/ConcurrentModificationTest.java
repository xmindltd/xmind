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
package org.xmind.core.internal.experiments;

import java.io.*;
import java.lang.Thread.*;
import java.util.*;

import org.xmind.core.*;

import junit.framework.*;

/**
 * @author Frank Shaka
 *
 */
public class ConcurrentModificationTest {

    private static final int SAVE_ITER_COUNT = 100000;

    public static void main(String[] args) throws Exception {
        new ConcurrentModificationTest().run();
    }

    private static interface Modifier {
        String getName();

        void modify(IWorkbook workbook);
    }

    private static final Random rand = new Random();

    private static Modifier[] modifiers = new Modifier[] {

            // add topic
            new Modifier() {
                public String getName() {
                    return "add topic";
                }

                public void modify(IWorkbook workbook) {
                    ITopic topic = workbook.createTopic();
                    ITopic r = workbook.getPrimarySheet().getRootTopic();
                    List<ITopic> children = r.getChildren(ITopic.ATTACHED);
                    int size = children.size();
                    int number = size + 1;
                    topic.setTitleText("Topic " + number);
                    r.add(topic, size <= 0 ? -1 : rand.nextInt(size), ITopic.ATTACHED);
                }
            },

            // modify title
            new Modifier() {
                public String getName() {
                    return "modify title";
                }

                public void modify(IWorkbook workbook) {
                    ITopic r = workbook.getPrimarySheet().getRootTopic();
                    List<ITopic> children = r.getChildren(ITopic.ATTACHED);
                    if (children.isEmpty())
                        return;
                    ITopic t = children.get(rand.nextInt(children.size()));
                    t.setTitleText(UUID.randomUUID().toString());
                }
            },

            // delete topic
            new Modifier() {
                public String getName() {
                    return "delete topic";
                }

                public void modify(IWorkbook workbook) {
                    ITopic r = workbook.getPrimarySheet().getRootTopic();
                    List<ITopic> children = r.getChildren(ITopic.ATTACHED);
                    if (children.isEmpty())
                        return;
                    ITopic t = children.get(rand.nextInt(children.size()));
                    r.remove(t);
                }
            }

    };

    private byte[] data;
    private IWorkbook workbook;
    private Thread modificationThread;
    private Thread savingThread;

    private boolean done;

    private volatile boolean saving;

    private int saveCount = 0;
    private int modificaitonCount = 0;
    private int concurrentModificationCount = 0;

    public void run() throws Exception {
        data = null;
        done = false;
        workbook = Core.getWorkbookBuilder().createWorkbook();

        UncaughtExceptionHandler errHandler = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                modificationThread.interrupt();
                savingThread.interrupt();
            }
        };

        modificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                modify();
            }
        });
        modificationThread.setName("ModificationThread");
        modificationThread.setUncaughtExceptionHandler(errHandler);

        saving = false;
        savingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                save();
            }
        });
        savingThread.setName("SavingThread");
        savingThread.setUncaughtExceptionHandler(errHandler);

        modificationThread.start();
        savingThread.start();

        System.out.println("Started");

        modificationThread.join();
        savingThread.join();

        testLoad();

        System.out.println("Test passes!");
        System.out.println("Save Count: " + saveCount);
        System.out.println("Modification Count: " + modificaitonCount);
        System.out.println("Concurrent modifications: " + concurrentModificationCount);
    }

    private void modify() {
        try {
            while (!done) {
                Modifier m = modifiers[rand.nextInt(modifiers.length)];
                // System.out.println(m.getName());
                if (saving) {
                    concurrentModificationCount += 1;
                    // System.out.println("Concurrent modification detected");
                }
                m.modify(workbook);
                modificaitonCount += 1;
                Thread.sleep(2);
            }
        } catch (InterruptedException e) {
        }
    }

    private void save() {
        try {
            for (int i = 0; i < SAVE_ITER_COUNT; i++) {

                testLoad();

                try {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    try {
                        ISerializer ser = Core.getWorkbookBuilder().newSerializer();
                        saving = true;
                        try {
                            ser.setWorkbook(workbook);
                            ser.setOutputStream(output);
                            ser.serialize(null);
                        } finally {
                            saving = false;
                        }
                    } finally {
                        output.close();
                    }
                    data = output.toByteArray();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                saveCount += 1;

                Thread.sleep(0);
            }
        } catch (InterruptedException e) {
        } finally {
            done = true;
        }
    }

    private void testLoad() {
        if (data == null) {
            return;
        }

        try {
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            try {
                IDeserializer des = Core.getWorkbookBuilder().newDeserializer();
                des.setInputStream(input);
                des.deserialize(null);
            } finally {
                input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionFailedError(e.getMessage());
        }
    }

}
