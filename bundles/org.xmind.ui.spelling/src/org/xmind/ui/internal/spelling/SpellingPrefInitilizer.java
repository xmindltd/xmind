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
package org.xmind.ui.internal.spelling;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.swabunga.spell.engine.Configuration;

public class SpellingPrefInitilizer extends AbstractPreferenceInitializer {

    public SpellingPrefInitilizer() {
    }

    public void initializeDefaultPreferences() {
        IScopeContext context = DefaultScope.INSTANCE;
        IEclipsePreferences node = context.getNode(SpellingPlugin.getDefault()
                .getBundle().getSymbolicName());

        node.putBoolean(SpellingPlugin.SPELLING_CHECK_ENABLED, true);

        Configuration configuration = Configuration.getConfiguration();
        node.putBoolean(Configuration.SPELL_IGNOREDIGITWORDS,
                configuration.getBoolean(Configuration.SPELL_IGNOREDIGITWORDS));
        node.putBoolean(
                Configuration.SPELL_IGNOREINTERNETADDRESSES,
                configuration
                        .getBoolean(Configuration.SPELL_IGNOREINTERNETADDRESSES));
        node.putBoolean(Configuration.SPELL_IGNOREMIXEDCASE,
                configuration.getBoolean(Configuration.SPELL_IGNOREMIXEDCASE));
        node.putBoolean(
                Configuration.SPELL_IGNORESENTENCECAPITALIZATION,
                configuration
                        .getBoolean(Configuration.SPELL_IGNORESENTENCECAPITALIZATION));
        node.putBoolean(Configuration.SPELL_IGNOREUPPERCASE,
                configuration.getBoolean(Configuration.SPELL_IGNOREUPPERCASE));

    }

}