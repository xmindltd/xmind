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
package org.xmind.gef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 * @version 2005
 */
public class Request {

    private static final List<IPart> EMPTY_SOURCES = Collections.emptyList();

    private static final Map<String, Object> EMPTY_PARAMETERS = Collections
            .emptyMap();

    private static final Map<String, Object> EMPTY_RESULTS = Collections
            .emptyMap();

    private static final Collection<String> EMPTY_NAMES = Collections
            .emptyList();

    private String type;

    private EditDomain domain;

    private IViewer viewer;

    private List<IPart> targets;

    private Map<String, Object> parameters = null;

    private Map<String, Object> results = null;

    private boolean handled = false;

    /**
     * @param type
     */
    public Request(String type) {
        if (type == null)
            throw new IllegalArgumentException(
                    "A request's type must not be null."); //$NON-NLS-1$
        this.type = type;
    }

    /**
     * @return
     */
    public String getType() {
        return type;
    }

//    /**
//     * Change the type of this request.
//     * <p>
//     * <b>NOTE:</b> FOR INTERNAL USE ONLY!!
//     * </p>
//     * 
//     * @param type
//     */
//    public void internalChangeType(String type) {
//        this.type = type;
//    }

    public EditDomain getDomain() {
        return domain;
    }

    public EditDomain getTargetDomain() {
        if (domain != null)
            return domain;
        if (getTargetViewer() != null)
            return getTargetViewer().getEditDomain();
        return null;
    }

    public ICommandStack getTargetCommandStack() {
        EditDomain targetDomain = getTargetDomain();
        if (targetDomain != null)
            return targetDomain.getCommandStack();
        return null;
    }

    public Request setDomain(EditDomain domain) {
        this.domain = domain;
        return this;
    }

    public Request setViewer(IViewer viewer) {
        this.viewer = viewer;
        return this;
    }

    public IViewer getTargetViewer() {
        if (viewer != null)
            return viewer;
        IPart target = getPrimaryTarget();
        if (target != null)
            return target.getSite().getViewer();
        return null;
    }

    public List<IPart> getTargets() {
        return targets == null ? EMPTY_SOURCES : targets;
    }

    public boolean hasTargets() {
        return targets != null && !targets.isEmpty();
    }

    public Request setTargets(List<? extends IPart> parts) {
        if (this.targets == null) {
            if (!parts.isEmpty())
                this.targets = new ArrayList<IPart>(parts);
        } else {
            this.targets.clear();
            this.targets.addAll(parts);
        }
        return this;
    }

    public IPart getPrimaryTarget() {
        return targets == null || targets.isEmpty() ? null : targets.get(0);
    }

    public Request setPrimaryTarget(IPart part) {
        if (part != null) {
            if (targets == null) {
                targets = new ArrayList<IPart>(1);
                targets.add(part);
            } else {
                targets.remove(part);
                targets.add(0, part);
            }
        }
        return this;
    }

    public Map<String, Object> getParameters() {
        return parameters == null ? EMPTY_PARAMETERS : Collections
                .unmodifiableMap(parameters);
    }

    public Request setParameter(String paramName, Object paramValue) {
        if (parameters == null)
            parameters = new HashMap<String, Object>();
        parameters.put(paramName, paramValue);
        return this;
    }

    public Request removeParameter(String paramName) {
        if (parameters != null) {
            parameters.remove(paramName);
            if (parameters.isEmpty())
                parameters = null;
        }
        return this;
    }

    public Request removeAllParameters() {
        parameters = null;
        return this;
    }

    public Collection<String> getParameterNames() {
        if (parameters != null)
            return parameters.keySet();
        return EMPTY_NAMES;
    }

    public Request setParameters(Map<String, Object> parameters) {
        if (parameters.isEmpty()) {
            this.parameters = null;
        } else {
            if (this.parameters == null) {
                this.parameters = new HashMap<String, Object>(parameters);
            } else {
                this.parameters.putAll(parameters);
            }
        }
        return this;
    }

    public Object getParameter(String paramName) {
        return parameters == null ? null : parameters.get(paramName);
    }

    public boolean isParameter(String paramName) {
        if (parameters == null)
            return false;
        return Boolean.TRUE.equals(parameters.get(paramName));
    }

    public int getIntParameter(String paramName, int defaultValue) {
        Object param = getParameter(paramName);
        if (param instanceof Integer)
            return ((Integer) param).intValue();
        return defaultValue;
    }

    public double getDoubleParameter(String paramName, double defaultValue) {
        Object param = getParameter(paramName);
        if (param instanceof Double)
            return ((Double) param).doubleValue();
        return defaultValue;
    }

    public boolean hasParameter(String paramName) {
        return parameters != null && parameters.containsKey(paramName);
    }

    public Map<String, Object> getResults() {
        return results == null ? EMPTY_RESULTS : Collections
                .unmodifiableMap(results);
    }

    public Request setResult(String resultName, Object resultValue) {
        if (results == null)
            results = new HashMap<String, Object>();
        results.put(resultName, resultValue);
        return this;
    }

    public Request removeResult(String resultName) {
        if (results != null) {
            results.remove(resultName);
            if (results.isEmpty())
                results = null;
        }
        return this;
    }

    public Request removeAllResults() {
        results = null;
        return this;
    }

    public Request setResults(Map<String, Object> results) {
        if (results.isEmpty()) {
            this.results = null;
        } else {
            if (this.results == null) {
                this.results = new HashMap<String, Object>(results);
            } else {
                this.results.putAll(results);
            }
        }
        return this;
    }

    public Object getResult(String resultName) {
        return results == null ? null : results.get(resultName);
    }

    public boolean hasResult(String resultName) {
        return results != null && results.containsKey(resultName);
    }

    public boolean isHandled() {
        return this.handled;
    }

    public void markHandled() {
        this.handled = true;
    }

}