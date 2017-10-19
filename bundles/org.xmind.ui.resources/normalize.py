#!/usr/bin/env python

import sys
import os
import os.path
import zipfile
import xml.dom.minidom as dom


PATH_TEMPLATES = "templates"

PATH_MANIFEST = "META-INF/manifest.xml"
PATH_CONTENT = "content.xml"
PATH_META = "meta.xml"
PATH_THUMBNAIL = "Thumbnails/thumbnail.png"
PATH_COMMENTS = "comments.xml"

TAG_FILE_ENTRY = "file-entry"

KEY_FULL_PATH = "full-path"
KEY_MEDIA_TYPE = "media-type"


class ByteArrayStorage(object):

    def __init__(self):
        self._entries = {}

    def names(self):
        return self._entries.keys()

    def exists(self, name):
        return name in self._entries

    def read(self, name):
        return self._entries.get(name)

    def write(self, name, content):
        self._entries[name] = content

    def delete(self, name):
        if name in self._entries:
            del self._entries[name]


class DirectoryStorage(object):

    def __init__(self, base_path):
        self._base_path = base_path

    def names(self):
        all_names = []
        self._add_names(all_names, self._base_path)
        all_names.sort()
        return all_names

    def _add_names(self, all_names, dir_path):
        for name in os.listdir(dir_path):
            path = os.path.join(dir_path, name)
            if os.path.isdir(path):
                self._add_names(all_names, path)
            elif os.path.isfile(path):
                all_names.append(os.path.relpath(path, self._base_path))

    def exists(self, name):
        return os.path.isfile(os.path.join(self._base_path, name))

    def read(self, name):
        with open(os.path.join(self._base_path, name), "rb") as f:
            return f.read()

    def write(self, name, content):
        with open(os.path.join(self._base_path, name), "wb") as f:
            f.write(content)

    def delete(self, name):
        os.remove(os.path.join(self._base_path, name))


class Normalizer(object):

    def __init__(self, root, test_only=False):
        self._root = root
        self._test_only = test_only
        self._template_changed = False


    def print_title(self, title):
        print "=" * 70
        print " " + title
        print "-" * 70


    def normalize(self):
        self.print_title("Templates")

        templates_dir = os.path.join(self._root, PATH_TEMPLATES)
        for template_name in os.listdir(templates_dir):
            if not template_name.endswith(".xmt"):
                continue

            if self._test_only:
                print "Checking '" + template_name + "'...."
            else:
                print "Normalizing '" + template_name + "'...."
            template_path = os.path.join(templates_dir, template_name)
            self.normalize_template(template_path)



    def normalize_template(self, template_path):
        if os.path.isdir(template_path):
            self.normalize_template_storage(DirectoryStorage(template_path))
            return

        storage = ByteArrayStorage()

        info_items = {}

        zf = zipfile.ZipFile(template_path, "r")
        try:
            for info_item in zf.infolist():
                name = info_item.filename
                info_items[name] = info_item
                if not name.endswith("/"):
                    f = zf.open(name, "r")
                    try:
                        storage.write(name, f.read())
                    finally:
                        f.close()
        finally:
            zf.close()

        self._template_changed = False
        self.normalize_template_storage(storage)

        if self._template_changed and not self._test_only:
            zf = zipfile.ZipFile(template_path, "w", zipfile.ZIP_STORED)
            try:
                names = list(info_items.keys())
                names.remove(PATH_CONTENT)
                names.remove(PATH_META)
                names.remove(PATH_THUMBNAIL)
                names.insert(0, PATH_THUMBNAIL)
                names.insert(0, PATH_META)
                names.insert(0, PATH_CONTENT)

                for name in names:
                    info_item = self.copy_zip_info(info_items[name])
                    if name.endswith("/"):
                        zf.writestr(info_item, "")
                    elif storage.exists(name):
                        zf.writestr(info_item, storage.read(name))
            finally:
                zf.close()


    def copy_zip_info(self, old_info):
        new_info = zipfile.ZipInfo()
        new_info.filename = old_info.filename
        new_info.date_time = old_info.date_time
        new_info.compress_type = old_info.compress_type
        new_info.comment = old_info.comment
        new_info.create_system = old_info.create_system
        new_info.create_version = old_info.create_version
        new_info.extract_version = old_info.extract_version
        new_info.reserved = old_info.reserved
        return new_info

    def normalize_template_storage(self, storage):
        manifest = storage.read(PATH_MANIFEST)
        manifest_document = dom.parseString(manifest)

        manifest_element = manifest_document.documentElement
        file_entries = {}
        for file_entry_element in manifest_element.childNodes:
            file_entries[file_entry_element.getAttribute(KEY_FULL_PATH)] = file_entry_element

        # make sure these file entries exist
        storage.read(PATH_CONTENT)
        storage.read(PATH_META)
        storage.read(PATH_THUMBNAIL)

        self.ensure_file_entry_element(file_entries, manifest_document,
            PATH_MANIFEST)
        self.ensure_file_entry_element(file_entries, manifest_document,
            PATH_CONTENT)
        self.ensure_file_entry_element(file_entries, manifest_document,
            PATH_META)
        self.ensure_file_entry_element(file_entries, manifest_document,
            PATH_THUMBNAIL)
        if storage.exists(PATH_COMMENTS):
            self.ensure_file_entry_element(file_entries, manifest_document,
                PATH_COMMENTS)

        for file_entry_path in file_entries:
            if file_entry_path.endswith("/"):
                # skip directory entries
                continue

            if not storage.exists(file_entry_path):
                print "   [!] Removing missing entry '" + file_entry_path + "' from manifest...."
                manifest_element.removeChild(file_entries[file_entry_path])
                self._template_changed = True
            elif file_entry_path.endswith(".xml"):
                dom.parseString(storage.read(file_entry_path))

        for name in storage.names():
            if name not in file_entries:
                print "   [!] Removing unnecessary file entry '" + name + "' from storage...."
                storage.delete(name)
                self._template_changed = True

        manifest = manifest_document.toxml()
        storage.write(PATH_MANIFEST, manifest)


    def ensure_file_entry_element(self, file_entries, manifest_document, name):
        if name in file_entries:
            return

        if name.endswith("/"):
            parent, sep, file_name = name[:-1].rpartition("/")
            if parent:
                self.ensure_file_entry_element(file_entries, manifest_document, parent + sep)

            print "   [!] Adding file entry for '" + name + "' to manifest...."
            file_entry_element = manifest_document.createElement(TAG_FILE_ENTRY)
            file_entry_element.setAttribute(KEY_FULL_PATH, name)
            file_entry_element.setAttribute(KEY_MEDIA_TYPE, "")
            manifest_document.documentElement.appendChild(file_entry_element)
            file_entries[name] = file_entry_element
            self._template_changed = True

            return

        parent, sep, file_name = name.rpartition("/")
        if parent:
            self.ensure_file_entry_element(file_entries, manifest_document, parent + sep)

        print "   [!] Adding file entry for '" + name + "' to manifest...."
        file_entry_element = manifest_document.createElement(TAG_FILE_ENTRY)
        file_entry_element.setAttribute(KEY_FULL_PATH, name)
        file_entry_element.setAttribute(KEY_MEDIA_TYPE, "text/xml")
        manifest_document.documentElement.appendChild(file_entry_element)
        file_entries[name] = file_entry_element
        self._template_changed = True



def main():
    if len(sys.argv) < 2:
        print "Usage: python normalize.py <plugin_dir>"
        return sys.exit(1)

    plugin_dir = sys.argv[1]
    if not os.path.isdir(plugin_dir):
        print "Invalid plugin dir: " + plugin_dir
        return sys.exit(1)

    Normalizer(plugin_dir).normalize()



if __name__ == "__main__":
    main()

