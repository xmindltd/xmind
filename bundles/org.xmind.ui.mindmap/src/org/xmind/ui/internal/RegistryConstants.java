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

import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

public class RegistryConstants implements IWorkbenchRegistryConstants {

    // ====================================
    //   Branch Policies Extension Point
    // ------------------------------------
    public static final String EXT_BRANCH_POLICIES = "branchPolicies"; //$NON-NLS-1$

    public static final String TAG_BRANCH_POLICY = "branchPolicy"; //$NON-NLS-1$
    public static final String TAG_STRUCTURE = "structure"; //$NON-NLS-1$
    public static final String TAG_ADDITIONAL_STRUCTURES = "additionalStructures"; //$NON-NLS-1$
    public static final String TAG_ADDITIONAL_STRUCTURE = "additionalStructure"; //$NON-NLS-1$
    public static final String TAG_PROPERTY_TESTER = "propertyTester"; //$NON-NLS-1$
    public static final String TAG_BRANCH_HOOK = "branchHook"; //$NON-NLS-1$
    public static final String TAG_STYLE_SELECTOR = "styleSelector"; //$NON-NLS-1$
    public static final String TAG_OVERRIDED_STYLE = "overridedStyle"; //$NON-NLS-1$
//    public static final String TAG_INHERITED_STYLE = "inheritedStyle"; //$NON-NLS-1$
    public static final String TAG_UNMODIFIABLE_PROPERTIES = "unmodifiableProperties"; //$NON-NLS-1$
    public static final String TAG_UNMODIFIABLE_PROPERTY = "unmodifiableProperty"; //$NON-NLS-1$
    public static final String TAG_STYLE_VALUE_PROVIDER = "styleValueProvider"; //$NON-NLS-1$
    public static final String TAG_LAYER = "layer"; //$NON-NLS-1$
    public static final String TAG_STRUCTURE_CACHES = "structureCaches"; //$NON-NLS-1$
    public static final String TAG_STRUCTURE_CACHE = "structureCache"; //$NON-NLS-1$
    public static final String TAG_OVERRIDE = "override"; //$NON-NLS-1$
    public static final String TAG_ADVISOR = "advisor"; //$NON-NLS-1$
    public static final String TAG_BRANCH_POLICY_CATEGORY = "branchPolicyCategory"; //$NON-NLS-1$

    public static final String ATT_DEFAULT_STRUCTURE_ID = "defaultStructureId"; //$NON-NLS-1$
    public static final String ATT_STRUCTURE_ID = "structureId"; //$NON-NLS-1$
    public static final String ATT_PROPERTY_TESTER_ID = "propertyTesterId"; //$NON-NLS-1$
    public static final String ATT_LAYER = "layer"; //$NON-NLS-1$
    public static final String ATT_VALUE_PROVIDER_ID = "valueProviderId"; //$NON-NLS-1$
    public static final String ATT_PRIMARY_KEY = "primaryKey"; //$NON-NLS-1$
    public static final String ATT_SECONDARY_KEY = "secondaryKey"; //$NON-NLS-1$

    // ===============================
    //   Decorations Extension Point
    // -------------------------------
    public static final String EXT_DECORATIONS = "decorations"; //$NON-NLS-1$
    public static final String TAG_DECORATION = "decoration"; //$NON-NLS-1$
    public static final String TAG_FACTORY = "factory"; //$NON-NLS-1$
    public static final String TAG_DEFAULT_VALUE = "defaultValue"; //$NON-NLS-1$

    // =====================
    //   Other
    // ---------------------

    public static final String EXT_HYPERLINKPAGE = "hyperlinkPages"; //$NON-NLS-1$
    public static final String TAG_HYPER_PAGE = "page"; //$NON-NLS-1$

//    public static final String EXT_ELEMENT_TYPES = "elementTypes"; //$NON-NLS-1$
    public static final String EXT_DND_CLIENTS = "dndClients"; //$NON-NLS-1$
    public static final String EXT_PROPERTY_SECTIONS = "propertySections"; //$NON-NLS-1$
    public static final String EXT_EDIT_POLICIES = "editPolicies"; //$NON-NLS-1$
    //public static final String EXT_GLOBAL_CORE_EVENT_HANDLERS = "globalCoreEventHandlers"; //$NON-NLS-1$
    public static final String EXT_ICONTIPS = "iconTips"; //$NON-NLS-1$
    public static final String EXT_NUMBER_FORMATS = "numberFormats"; //$NON-NLS-1$
    public static final String EXT_NUMBER_SEPARATORS = "numberSeparators"; //$NON-NLS-1$
    public static final String EXT_INFOITMES = "infoItems"; //$NON-NLS-1$

//    public static final String TAG_ELEMENT_TYPE = "elementType"; //$NON-NLS-1$
    public static final String TAG_DND_CLIENT = "dndClient"; //$NON-NLS-1$
    public static final String TAG_SECTION = "section"; //$NON-NLS-1$
//    public static final String TAG_TYPE_OF = "typeOf"; //$NON-NLS-1$
//    public static final String TAG_MEMBER_OF = "memberOf"; //$NON-NLS-1$
    public static final String TAG_EDIT_POLICY = "editPolicy"; //$NON-NLS-1$
    public static final String TAG_ICONTIP = "iconTip"; //$NON-NLS-1$
    public static final String TAG_FORMAT = "format"; //$NON-NLS-1$
    public static final String TAG_SEPARATOR = "separator"; //$NON-NLS-1$
    public static final String TAG_INFOITEM = "infoItem"; //$NON-NLS-1$

    public static final String ATT_MODE = "defaultMode"; //$NON-NLS-1$
    public static final String ATT_AVAILABLEMODES = "availableModes"; //$NON-NLS-1$
    public static final String ATT_CARD_LABEL = "cardLabel"; //$NON-NLS-1$

//    public static final String TAG_HANDLER = "handler"; //$NON-NLS-1$
//    public static final String TAG_HANDLER_BINDING = "handlerBinding"; //$NON-NLS-1$

//    public static final String ATT_ELEMENT_TYPE = "elementType"; //$NON-NLS-1$
//    public static final String ATT_BRANCH_FAMILY = "branchFamily"; //$NON-NLS-1$
//    public static final String ATT_EVENT_TYPE = "eventType"; //$NON-NLS-1$
//    public static final String ATT_HANDLER_ID = "handlerId"; //$NON-NLS-1$

//    // ==================
//    //   Branch Families
//    // -------------------
//    public static final String BRANCH_FAMILY_CENTRAL = "central"; //$NON-NLS-$
//    public static final String BRANCH_FAMILY_MAIN = "main"; //$NON-NLS-1$
//    public static final String BRANCH_FAMILY_SUB = "sub"; //$NON-NLS-1$
//    public static final String BRANCH_FAMILY_FLOATING = "floating"; //$NON-NLS-1$
//    public static final String BRANCH_FAMILY_ALL = "all"; //$NON-NLS-1$
//
//    public static final Set<String> ALL_BRANCH_FAMILIES = Collections
//            .unmodifiableSet(new HashSet<String>(Arrays.asList(
//                    BRANCH_FAMILY_CENTRAL, BRANCH_FAMILY_FLOATING,
//                    BRANCH_FAMILY_MAIN, BRANCH_FAMILY_SUB)));

    public static final String EXT_WORKBOOK_REF_FACTORIES = "workbookRefFactories"; //$NON-NLS-1$
    public static final String TAG_AVAILABLE_FOR_URI_SCHEME = "availableForURIScheme"; //$NON-NLS-1$
    public static final String ATT_SCHEME = "scheme"; //$NON-NLS-1$

    public static final String EXT_SAVE_WIZARDS = "saveWizards"; //$NON-NLS-1$
    public static final String TAG_SAVE_WIZARD = "saveWizard"; //$NON-NLS-1$

    public static final String EXT_SHARE_OPTIONS = "shareOptions"; //$NON-NLS-1$
    public static final String TAG_OPTION = "option"; //$NON-NLS-1$
    public static final String VAL_CATEGORY_NORMAL = "normal"; //$NON-NLS-1$
    public static final String VAL_CATEGORY_POPULAR = "popular"; //$NON-NLS-1$

}