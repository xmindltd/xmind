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
package org.xmind.ui.branch;

import org.xmind.ui.mindmap.IBranchPart;

/**
 * A branch property tester is responsible for testing policy-specified property
 * on a branch. This class is used by <code>org.xmind.ui.branchPolicies</code>
 * extension point. Clients who extend <code>branchPolicy</code> may take
 * advantage of this class to define their own property test mechanism.
 * <p>
 * Example:
 * </p>
 * <p>
 * Define branch policy and declare property tester in <code>plugin.xml</code>:
 * 
 * <pre>
 * &lt;branchPolicy 
 *       id=&quot;org.example.branchPolicy.example&quot;
 *       propertyTester=&quot;org.example.ExampleBranchPropertyTester&quot;&gt;
 * &lt;/branchPolicy&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * Implement the tester:
 * 
 * <pre>
 * public class ExampleBranchPolicyPropertyTester implements IBranchPropertyTester {
 *     public boolean test(IBranchPart branch, String property, Object[] args,
 *             Object expectedValue) {
 *         if (&quot;foo&quot;.equals(property)) {
 *             return getFoo(branch).equals(expectedValue);
 *         }
 *         Assert.isTrue(false);
 *         return false;
 *     }
 * 
 *     private String getFoo(IBranchPart branch) {
 *         return &quot;bar&quot;;
 *     }
 * }
 * </pre>
 * 
 * <p>
 * Then use this property tester inside branchPolicy extension:
 * 
 * <pre>
 * &lt;branchPolicy 
 *       id=&quot;org.example.branchPolicy.example&quot;
 *       propertyTester=&quot;org.example.ExampleBranchPropertyTester&quot;&gt;
 *    &lt;additionalStructure
 *          structureId=&quot;org.example.branchStructure.example&quot;&gt;
 *       &lt;enablement&gt;
 *          &lt;test
 *                property=&quot;org.xmind.ui.branch.property&quot;
 *                args=&quot;foo&quot;
 *                value=&quot;bar&quot;&gt;
 *          &lt;/test&gt;
 *       &lt;/enablement&gt;
 *    &lt;/additionalStructure&gt;
 * &lt;/branchPolicy&gt;
 * </pre>
 * 
 * Note that the first argument in the <code>args</code> attribute will be
 * considered as the policy-specified property and passed to the branch property
 * tester as the <code>property</code>. The following parts of the
 * <code>args</code> will be directly passed as the <code>args</code> argument.
 * If there's no argument in the <code>args</code> attribute, the evaluation
 * will fail by returning FALSE.
 * </p>
 * 
 * 
 * @author MANGOSOFT
 * 
 */
public interface IBranchPropertyTester {

    /**
     * Cache key for a {@link IBranchPropertyTester} object on a branch.
     * <p>
     * A branch property tester is cached by branch policy when activated on a
     * branch and flushed when deactivated.
     * </p>
     */
    String CACHE_PROPERTY_TESTER = "org.xmind.ui.branchCache.propertyTester"; //$NON-NLS-1$

    boolean test(IBranchPart branch, String property, Object[] args,
            Object expectedValue);

}