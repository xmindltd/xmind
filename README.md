XMind
=====

[XMind](http://www.xmind.net) is an open source project that contributes to
building a cutting-edge brainstorming/mind-mapping facility, focused on both
usability and extendability. It helps people in capturing ideas into visually
self-organized charts and sharing them for collaboration and communication.
Currently supporting mind maps, fishbone diagrams, tree diagrams, org-charts,
logic charts, and even spreadsheets. Often used for knowledge management,
meeting minutes, task management, and GTD.

**[Downloads Available For Windows/Mac/Ubuntu](http://www.xmind.net/download/?ref=github-home)**

License
-------

XMind is dual licensed under 2 open source licenses: the [Eclipse Public
License (EPL) v1.0](http://www.eclipse.org/legal/epl-v10.html) and the [GNU
Lesser General Public License (LGPL) v3](http://www.gnu.org/licenses/lgpl.html).

For licensees that wish to distribute XMind 3, modify the source code, and/or
build extensions, the EPL can be used to maintain copyleft of the original code
base while encouraging innovation with commercial and other open source
offerings incorporating XMind.

At the same time, for licensees that are concerned with incompatibility between
the EPL and GPL, we are providing the LGPL as an option to license XMind.

Please note that we are not providing legal advice here and you should not rely
on the above statements as such. For a full understanding of your rights and
obligations under these licenses, please consult the full text of the EPL
and/or LGPL, and your legal counsel as appropriate.

How To Run/Debug
----------------

1.  Download and install [JDK v1.8 or higher](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
1.  Download and install [Eclipse SDK v4.6 or higher](http://download.eclipse.org/eclipse/downloads/).
1.  Make a clean workspace.
1.  Import all bundles, features and releng projects into the workspace.
1.  Open `org.xmind.cathy.target/cathy.target` with the default *Target Editor*
    and click on 'Set as Target Platform' in the top-right corner of the opened
    editor (you may have to wait for Eclipse to download all necessary
    dependencies).
1.  Open `org.xmind.cathy.product/cathy.product` with the default *Product
    Configuration Editor* and, in the first 'Overview' tab, click on 'Launch an
    Eclipse application' or 'Launch an Eclipse application in Debug mode'.

And, by the way...

-   If you're using an OS *other than* Windows and encounter compiling errors
    in plugin `org.xmind.cathy.win32`, just delete it from the workspace.

How To Contribute
-----------------

Any issue report or pull request will be highly welcomed!

