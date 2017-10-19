#!/usr/bin/env python

import sys
import os.path

import normalize



def main():
    if len(sys.argv) < 2:
        print "Usage: python check.py <plugin_dir>"
        return sys.exit(1)

    plugin_dir = sys.argv[1]
    if not os.path.isdir(plugin_dir):
        print "Invalid plugin dir: " + plugin_dir
        return sys.exit(1)

    normalize.Normalizer(plugin_dir, test_only=True).normalize()



if __name__ == "__main__":
    main()

