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
package org.xmind.ui.tabfolder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;

public class DelegatedSelectionProvider
        implements IDelegatedSelectionProvider, IPostSelectionProvider {

    private Display display;

    private ISelectionProvider delegate = null;

    private ISelectionChangedListener listener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            delegateSelectionChanged(event.getSelection());
        }
    };

    private ISelectionChangedListener postListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            postDelegateSelectionChanged(event.getSelection());
        }
    };

    private List<ISelectionChangedListener> listeners = null;

    private List<ISelectionChangedListener> postListeners = null;

    private List<ISelectionChangedListener> asyncListeners = null;

    private List<ISelectionChangedListener> asyncPostListeners = null;

    public void setDisplay(Display display) {
        this.display = display;
    }

    public void setDelegate(ISelectionProvider delegate) {
        if (delegate == this.delegate)
            return;

        ISelectionProvider oldDelegate = this.delegate;
        ISelectionProvider newDelegate = delegate;
        this.delegate = delegate;

        if (oldDelegate != null) {
            oldDelegate.removeSelectionChangedListener(listener);
            if (oldDelegate instanceof IPostSelectionProvider) {
                ((IPostSelectionProvider) oldDelegate)
                        .removePostSelectionChangedListener(postListener);
            }
        }

        final ISelection newSelection = getSelection();
        if (newDelegate != null) {
            newDelegate.addSelectionChangedListener(listener);
            delegateSelectionChanged(newSelection);

            if (newDelegate instanceof IPostSelectionProvider) {
                ((IPostSelectionProvider) newDelegate)
                        .addPostSelectionChangedListener(postListener);
            } else {
                newDelegate.addSelectionChangedListener(postListener);
            }
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    postDelegateSelectionChanged(newSelection);
                }
            });
        } else {
            delegateSelectionChanged(newSelection);
            postDelegateSelectionChanged(newSelection);
        }
    }

    public ISelectionProvider getDelegate() {
        return delegate;
    }

    public void addSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ISelectionChangedListener>();
        }
        listeners.add(listener);
    }

    public ISelection getSelection() {
        return delegate == null ? StructuredSelection.EMPTY
                : delegate.getSelection();
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void setSelection(ISelection selection) {
        if (delegate != null) {
            delegate.setSelection(selection);
        }
    }

    public void addPostSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (postListeners == null) {
            postListeners = new ArrayList<ISelectionChangedListener>();
        }
        postListeners.add(listener);
    }

    public void removePostSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (postListeners != null) {
            postListeners.remove(listener);
        }
    }

    public void addAsyncSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (asyncListeners == null) {
            asyncListeners = new ArrayList<ISelectionChangedListener>();
        }
        asyncListeners.add(listener);
    }

    public void removeAsyncSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (asyncListeners != null) {
            asyncListeners.remove(listener);
        }
    }

    public void addAsyncPostSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (asyncPostListeners == null) {
            asyncPostListeners = new ArrayList<ISelectionChangedListener>();
        }
        asyncPostListeners.add(listener);
    }

    public void removeAsyncPostSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (asyncPostListeners != null) {
            asyncPostListeners.remove(listener);
        }
    }

    private void delegateSelectionChanged(ISelection selection) {
        fireSelectionChangedEvent(new SelectionChangedEvent(this, selection),
                listeners);
        fireAsyncSelectionChangedEvent(
                new SelectionChangedEvent(this, selection), asyncListeners);
    }

    private void postDelegateSelectionChanged(ISelection selection) {
        fireSelectionChangedEvent(new SelectionChangedEvent(this, selection),
                postListeners);
        fireAsyncSelectionChangedEvent(
                new SelectionChangedEvent(this, selection), asyncPostListeners);
    }

    private void fireSelectionChangedEvent(final SelectionChangedEvent event,
            List<ISelectionChangedListener> listeners) {
        if (listeners == null)
            return;
        Object[] ls = listeners.toArray();
        for (int i = 0; i < ls.length; ++i) {
            final ISelectionChangedListener l = (ISelectionChangedListener) ls[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
    }

    private void fireAsyncSelectionChangedEvent(
            final SelectionChangedEvent event,
            List<ISelectionChangedListener> listeners) {
        if (listeners == null)
            return;
        Object[] ls = listeners.toArray();

        if (display != null && !display.isDisposed()) {
            display.asyncExec(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < ls.length; ++i) {
                        final ISelectionChangedListener l = (ISelectionChangedListener) ls[i];
                        SafeRunner.run(new SafeRunnable() {
                            public void run() {
                                l.selectionChanged(event);
                            }
                        });
                    }
                }
            });
        } else {
            for (int i = 0; i < ls.length; ++i) {
                final ISelectionChangedListener l = (ISelectionChangedListener) ls[i];
                SafeRunner.run(new SafeRunnable() {
                    public void run() {
                        l.selectionChanged(event);
                    }
                });
            }
        }
    }

}
