/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.share.dialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.ui.resources.FontUtils;

import net.xmind.share.Info;
import net.xmind.share.Messages;
import net.xmind.share.XmindSharePlugin;

public class GeneralUploaderPage extends UploaderPage
        implements PropertyChangeListener {

    private static final String LANGUAGE_CHANNEL = "net.xmind.share.dialog.defaultLanguageChannel"; //$NON-NLS-1$

    private InfoField titleField;

    private InfoField descriptionField;

    private Composite privacyText;

    private Label accessibility;

    private Label allowed;

    public GeneralUploaderPage() {
        setTitle(Messages.UploaderDialog_GeneralPage_title);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());

        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 10;
        composite.setLayout(layout);
        composite.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.FILL, true, false));

        titleField = new InfoField(false, true, true);
        titleField.fill(composite);
        titleField.setName(Messages.UploaderDialog_Title_text);
        titleField.setText(getInfo().getString(Info.TITLE));

        descriptionField = new InfoField(true, false, false);
        descriptionField.fill(composite);
        descriptionField.setName(Messages.UploaderDialog_Description_text);
        descriptionField.setText(getInfo().getString(Info.DESCRIPTION));
        descriptionField.getTextWidget()
                .addModifyListener(new ModifyListener() {
                    public void modifyText(ModifyEvent e) {
                        getInfo().setProperty(Info.DESCRIPTION,
                                descriptionField.getText());
                    }
                });

        createLanguageSection(composite);
        createPrivacySection(composite);

        setControl(composite);

        getInfo().addPropertyChangeListener(Info.PRIVACY, this);
        getInfo().addPropertyChangeListener(Info.DOWNLOADABLE, this);
    }

    private void createPrivacySection(Composite parent) {
        privacyText = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(7, false);
        layout.marginHeight = 0;
        layout.marginLeft = 0;
        layout.marginRight = 100;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        privacyText.setLayout(layout);

        privacyText
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        privacyText.setBackground(parent.getBackground());
        createtePrivacyLabel();
    }

    private void createLanguageSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
        label.setText(Messages.UploaderDialog_LanguageChannel_label);

        final Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
        ((GridData) combo.getLayoutData()).widthHint = 280;
        combo.setItems(new String[] { //
                "English", //$NON-NLS-1$
                "Chinese - \u4e2d\u6587", //$NON-NLS-1$
                "French - fran\u00e7ais", //$NON-NLS-1$
                "German - Deutsch", //$NON-NLS-1$
                "Japanese - \u65e5\u672c\u8a9e", //$NON-NLS-1$
                "Spanish - espa\u00f1ol", //$NON-NLS-1$
                "Worldwide" //$NON-NLS-1$
        });
        final IPreferenceStore prefStore = XmindSharePlugin.getDefault()
                .getPreferenceStore();
        String lang = prefStore.getString(LANGUAGE_CHANNEL);
        if (lang == null || "".equals(lang)) { //$NON-NLS-1$
            lang = Info.getDefaultLanguageCode();
        }
        getInfo().setProperty(Info.LANGUAGE_CHANNEL, lang);

        int index = Info.LANGUAGE_CODES.indexOf(lang);
        if (index < 0) {
            // Default is Worldwide:
            index = Info.LANGUAGE_CODES.size() - 1;
        }
        combo.select(index);
        combo.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                int index = Math.min(combo.getSelectionIndex(),
                        Info.LANGUAGE_CODES.size() - 1);
                String lang = Info.LANGUAGE_CODES.get(index);
                prefStore.setValue(LANGUAGE_CHANNEL, lang);
                getInfo().setProperty(Info.LANGUAGE_CHANNEL, lang);
            }
        });
    }

    public void setFocus() {
        if (descriptionField != null && !descriptionField.isDisposed()) {
            descriptionField.setFocus();
        }
    }

    @Override
    public void dispose() {
        getInfo().removePropertyChangeListener(Info.PRIVACY, this);
        getInfo().removePropertyChangeListener(Info.DOWNLOADABLE, this);
        super.dispose();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (Info.PRIVACY.equals(name) || Info.DOWNLOADABLE.equals(name)) {
            updatePrivacyLabel();
        }
    }

    private void createtePrivacyLabel() {
        if (privacyText == null || privacyText.isDisposed())
            return;

        createPrivacyLabel(privacyText, Messages.UploaderDialog_Privacy_text,
                false);
        accessibility = createPrivacyLabel(privacyText, getAccessibilityText(),
                true);
        createPrivacyLabel(privacyText,
                NLS.bind(Messages.UploaderDialog_Download_text, ". "), //$NON-NLS-1$
                false);
        allowed = createPrivacyLabel(privacyText, getDownloadableText(), true);
        createPrivacyLabel(privacyText, ". (", false); //$NON-NLS-1$
        createPrivacyLink(privacyText);
        createPrivacyLabel(privacyText, ")", false); //$NON-NLS-1$
    }

    private void updatePrivacyLabel() {
        if (accessibility != null && !accessibility.isDisposed()) {
            accessibility.setText(getAccessibilityText());
        }

        if (allowed != null && !allowed.isDisposed()) {
            allowed.setText(getDownloadableText());
        }

        privacyText.layout(true, true);
    }

    private Label createPrivacyLabel(Composite parent, String text,
            boolean isBold) {
        Label label = new Label(parent, SWT.NONE);

        if (isBold)
            label.setFont(FontUtils.getBold(label.getFont()));

        label.setText(text);
        return label;
    }

    private void createPrivacyLink(Composite parent) {
        Hyperlink link = new Hyperlink(parent, SWT.NONE);

        link.setForeground(link.getDisplay().getSystemColor(SWT.COLOR_BLUE));
        link.setUnderlined(true);
        link.setText(Messages.UploaderDialog_Privacy_link);
        link.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                goToPrivacyPage();
            }
        });
    }

    private String getAccessibilityText() {
        Object acc = getInfo().getString(Info.PRIVACY, Info.PRIVACY_PUBLIC);
        if (Info.PRIVACY_PUBLIC.equals(acc))
            return Messages.UploaderDialog_Privacy_Public_title;
        if (Info.PRIVACY_PRIVATE.equals(acc))
            return Messages.UploaderDialog_Privacy_Private_title;
        return Messages.UploaderDialog_Privacy_Unlisted_title;
    }

    private String getDownloadableText() {
        Object value = getInfo().getString(Info.DOWNLOADABLE,
                Info.DOWNLOADABLE_YES);
        if (Info.DOWNLOADABLE_YES.equals(value))
            return Messages.UploaderDialog_Privacy_DownloadAllowed;
        return Messages.UploaderDialog_Privacy_DownloadForbidden;
    }

    private void goToPrivacyPage() {
        getContainer().showPage("org.xmind.ui.uploader.privacy"); //$NON-NLS-1$
    }

    public InfoField getTitleField() {
        return titleField;
    }

}