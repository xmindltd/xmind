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
package org.xmind.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.gef.dnd.DndData;
import org.xmind.gef.dnd.IDndClient;
import org.xmind.gef.dnd.IDndSupport;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.util.Logger;

public class MindMapDndSupport extends RegistryReader implements IDndSupport {

    private List<String> ids = null;

    private Map<String, DndClientDescriptor> map = null;

    private Transfer[] transfers = null;

    /* package */MindMapDndSupport() {
    }

    public DndData parseData(TransferData[] dataTypes, Object source,
            boolean usePendingData) {
        List<String> clientIds = getClientIds();
        for (TransferData dataType : dataTypes) {
            for (String clientId : clientIds) {
                IDndClient client = getDndClient(clientId);
                if (client != null) {
                    Transfer transfer = client.getTransfer();
                    if (transfer != null) {
                        if (transfer.isSupportedType(dataType)) {
                            Object data = parseData(client, transfer, dataType,
                                    source);
                            if (data == null && usePendingData) {
                                data = PENDING_DATA;
                            }
                            if (data != null)
                                return new DndData(clientId, data, dataType);
                        }
                    }
                }
            }
        }
        return null;
    }

    private Object parseData(IDndClient dndClient, Transfer transfer,
            TransferData transferData, Object source) {
        if (source instanceof Clipboard)
            return ((Clipboard) source).getContents(transfer);
        return dndClient.getData(transfer, transferData);
    }

    public IDndClient getDndClient(String id) {
        DndClientDescriptor desc = getDescriptor(id);
        if (desc != null)
            return desc.getDndClient();
        return null;
    }

    public DndClientDescriptor getDescriptor(String id) {
        return getRegistry().get(id);
    }

    public String[] getDndClientIds() {
        return getClientIds().toArray(new String[0]);
    }

    private List<String> getClientIds() {
        ensureLoaded();
        return ids;
    }

    private Map<String, DndClientDescriptor> getRegistry() {
        ensureLoaded();
        return map;
    }

    public int getStyle() {
        return DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_LINK;
    }

    public Transfer[] getTransfers() {
        if (transfers == null) {
            List<String> ids = getClientIds();
            ArrayList<Transfer> ts = new ArrayList<Transfer>(ids.size());
            for (String id : ids) {
                IDndClient dndClient = getDndClient(id);
                if (dndClient != null) {
                    Transfer t = dndClient.getTransfer();
                    if (t != null) {
                        ts.add(t);
                    }
                }
            }
            transfers = ts.toArray(new Transfer[ts.size()]);
        }
        return transfers;
    }

    private void ensureLoaded() {
        if (ids != null && map != null)
            return;

        if (Platform.isRunning()) {
            readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                    RegistryConstants.EXT_DND_CLIENTS);
        }
        if (ids == null)
            ids = Collections.emptyList();
        else
            sortIds();
        if (map == null)
            map = Collections.emptyMap();
    }

    private void registerDndClient(DndClientDescriptor descriptor) {
        String id = descriptor.getId();
        if (ids == null)
            ids = new ArrayList<String>();
        ids.add(id);
        if (map == null)
            map = new HashMap<String, DndClientDescriptor>();
        map.put(id, descriptor);
    }

    private void sortIds() {
        MindMapUIPlugin plugin = MindMapUIPlugin.getDefault();
        if (plugin != null) {
            IPreferenceStore ps = plugin.getPreferenceStore();
            final String order = ps
                    .getString(PrefConstants.DND_CLIENT_ID_ORDER);
            if (order != null && !"".equals(order)) { //$NON-NLS-1$
                boolean hasDefault = ids
                        .contains(MindMapUI.DND_MINDMAP_ELEMENT);
                if (hasDefault) {
                    ids.remove(MindMapUI.DND_MINDMAP_ELEMENT);
                }
                List<String> notOrdered = new ArrayList<String>(ids.size());
                for (int i = 0; i < ids.size();) {
                    String id = ids.get(i);
                    if (order.contains(id)) {
                        i++;
                    } else {
                        ids.remove(i);
                        notOrdered.add(id);
                    }
                }
                if (ids.size() > 1) {
                    Collections.sort(ids, new Comparator<String>() {
                        public int compare(String id1, String id2) {
                            int index1 = order.indexOf(id1);
                            int index2 = order.indexOf(id2);
                            return index1 - index2;
                        }
                    });
                }
                for (String id : notOrdered) {
                    ids.add(id);
                }
                if (hasDefault) {
                    ids.add(0, MindMapUI.DND_MINDMAP_ELEMENT);
                }
            }
        }
    }

    protected boolean readElement(IConfigurationElement element) {
        if (RegistryConstants.TAG_DND_CLIENT.equals(element.getName())) {
            readDndClient(element);
            return true;
        }
        return false;
    }

    private void readDndClient(IConfigurationElement element) {
        try {
            DndClientDescriptor desc = new DndClientDescriptor(element);
            registerDndClient(desc);
        } catch (CoreException e) {
            Logger.log(e, "Unable to load dnd client: " + element.toString()); //$NON-NLS-1$
        }
    }

}