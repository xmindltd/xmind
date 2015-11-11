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
package org.xmind.core.internal.sharing;

import static org.xmind.core.internal.sharing.AbstractSharedMap.MAP_COMPARATOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.xmind.core.command.remote.IRemoteCommandService;
import org.xmind.core.sharing.IRemoteSharedLibrary;
import org.xmind.core.sharing.ISharedMap;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class RemoteSharedLibrary implements IRemoteSharedLibrary {

    private IRemoteCommandService remoteService;

    private String name;

    private String contactID;

    private List<ISharedMap> maps = new ArrayList<ISharedMap>();

    public RemoteSharedLibrary(IRemoteCommandService remoteService, String name) {
        this.remoteService = remoteService;
        this.name = name;
    }

    public void addMaps(Collection<ISharedMap> maps) {
        this.maps.addAll(maps);
        Collections.sort(this.maps, MAP_COMPARATOR);
    }

    public boolean hasMaps() {
        return !this.maps.isEmpty();
    }

    public int getMapCount() {
        return this.maps.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        if (adapter == IRemoteCommandService.class)
            return remoteService;
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    public String getSymbolicName() {
        return remoteService.getInfo().getId().getName();
    }

    public boolean isLocal() {
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactID() {
        return contactID;
    }

    public void setContactID(String contactID) {
        this.contactID = contactID;
    }

    public ISharedMap[] getMaps() {
        return maps.toArray(new ISharedMap[maps.size()]);
    }

    public IRemoteCommandService getRemoteCommandService() {
        return remoteService;
    }

    public void addMap(ISharedMap map) {
        this.maps.add(map);
        Collections.sort(this.maps, MAP_COMPARATOR);
    }

    public void removeMap(ISharedMap map) {
        this.maps.remove(map);
    }

    public ISharedMap findMapByID(String id) {
        if (id != null) {
            for (ISharedMap map : maps) {
                if (id.equals(map.getID()))
                    return map;
            }
        }
        return null;
    }

    public void sortMaps(Comparator<ISharedMap> c) {
        Collections.sort(maps, c);
    }

}
