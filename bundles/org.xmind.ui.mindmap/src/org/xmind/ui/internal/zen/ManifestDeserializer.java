package org.xmind.ui.internal.zen;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_ALGORITHM_NAME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_ITERATION_COUNT;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_KEY_DERIVATION_NAME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_KEY_IV;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_KEY_SIZE;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_SALT;
import static org.xmind.core.internal.dom.DOMConstants.TAG_ALGORITHM;
import static org.xmind.core.internal.dom.DOMConstants.TAG_KEY_DERIVATION;

import org.json.JSONObject;
import org.xmind.core.IEncryptionData;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;

public class ManifestDeserializer {

    public void deserialize(IManifest manifest, JSONObject manifestObject) {

        JSONObject entriesObject = manifestObject
                .optJSONObject(ZenConstants.KEY_FILE_ENTRIES);
        if (entriesObject != null) {
            for (String path : entriesObject.keySet()) {
                IFileEntry fileEntry = manifest.createFileEntry(path);

                JSONObject pathObject = entriesObject.optJSONObject(path);
                if (pathObject != null) {
                    JSONObject encryptionDataObject = pathObject
                            .optJSONObject(ZenConstants.KEY_ENCRYPTION_DATA);
                    if (encryptionDataObject != null) {
                        deserializeEncryptionData(fileEntry,
                                encryptionDataObject);
                    }
                }
            }
        }

        String passwordHint = JsonUtils.parseString(
                manifestObject.opt(ZenConstants.KEY_PASSWORD_HINT));
        if (passwordHint != null) {
            manifest.setPasswordHint(passwordHint);
        }
    }

    private void deserializeEncryptionData(IFileEntry fileEntry,
            JSONObject encryptionDataObject) {

        int iterationCount = encryptionDataObject
                .optInt(ZenConstants.KEY_ITERATION_COUNT);
        String algorithmName = encryptionDataObject
                .optString(ZenConstants.KEY_ALGORITHM_NAME);
        String derivationName = encryptionDataObject
                .optString(ZenConstants.KEY_DERIVATION_NAME);
        int size = encryptionDataObject.optInt(ZenConstants.KEY_SIZE);
        String salt = encryptionDataObject.optString(ZenConstants.KEY_SALT);
        String iv = encryptionDataObject.optString(ZenConstants.KEY_IV);

        IEncryptionData encryptionData = fileEntry.createEncryptionData();

        encryptionData.setAttribute("" + iterationCount, //$NON-NLS-1$
                TAG_KEY_DERIVATION, ATTR_ITERATION_COUNT);
        encryptionData.setAttribute(algorithmName, TAG_ALGORITHM,
                ATTR_ALGORITHM_NAME);
        encryptionData.setAttribute(derivationName, TAG_KEY_DERIVATION,
                ATTR_KEY_DERIVATION_NAME);
        encryptionData.setAttribute("" + size, //$NON-NLS-1$
                TAG_KEY_DERIVATION, ATTR_KEY_SIZE);
        encryptionData.setAttribute(salt, TAG_KEY_DERIVATION, ATTR_SALT);
        encryptionData.setAttribute(iv, TAG_KEY_DERIVATION, ATTR_KEY_IV);
    }

}
