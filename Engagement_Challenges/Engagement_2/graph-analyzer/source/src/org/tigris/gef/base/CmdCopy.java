// Copyright (c) 1996-99 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
// File: CmdCopy.java
// Classes: CmdCopy
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdCopy.java 1187 2008-12-09 21:17:38Z bobtarling $
package org.tigris.gef.base;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tigris.gef.presentation.*;

/**
 * Copies the selected Figs to the clipboard. This does not copy the owners
 */
public class CmdCopy extends Cmd {

    private static final long serialVersionUID = -7316080407001846501L;

    public CmdCopy() {
        super("Copy");
    }

    public void doIt() {
        Editor ce = Globals.curEditor();
        List<Selection> copiedElements
                = ce.getSelectionManager().getSelections();
        List<Fig> figs = new ArrayList<Fig>();
        Iterator<Selection> copies = copiedElements.iterator();
        while (copies.hasNext()) {
            Selection s = copies.next();
            Fig f = s.getContent();
            if (f instanceof FigEdge) {
                continue;
            }
            // needs-more-work: add support for cut-and-paste of edges
            f = (Fig) f.clone();
            figs.add(f);
        }
        Globals.clipBoard = figs;
    }

    public void undoIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("Undo does not make sense for CmdCopy");
    }

    // Awaiting jdk 1.2
    /**
     * The DataFlavor used for our particular type of cut-and-paste data. This
     * one will transfer data in the form of a serialized Vector object. Note
     * that in Java 1.1.1, this works intra-application, but not between
     * applications. Java 1.1.1 inter-application data transfer is limited to
     * the pre-defined string and text data flavors.
     */
    // public static final DataFlavor dataFlavor =
    // new DataFlavor(Fig.class, "Fig");
    // protected Vector figs = new Vector(256,256); // Store the Figs.
    /**
     * Copy the current scribble and store it in a SimpleSelection object
     * (defined below). Then put that object on the clipboard for pasting.
     */
    // Going to have to wait for jdk 1.2 for this code to work.
    // public void copy(Fig fig) {
    // Get system clipboard
    // Clipboard c =
    // ProjectBrowser.TheInstance.getToolkit().getSystemClipboard();
    // Copy and save the scribble in a Transferable object
    // SimpleSelection f = new SimpleSelection(fig, dataFlavor);
    // Put that object on the clipboard
    // c.setContents(f, f);
    // Transferable t = c.getContents(ProjectBrowser.TheInstance);
    // if (t instanceof Transferable)
    // org.graph.commons.logging.LogFactory.getLog(null).info("Copy, success!");
    // org.graph.commons.logging.LogFactory.getLog(null).info("copy has been executed" + " t = " + t);
    // }
    /**
     * This nested class implements the Transferable and ClipboardOwner
     * interfaces used in data transfer. It is a simple class that remembers a
     * selected object and makes it available in only one specified flavor.
     */
    // Awaiting jdk 1.2
    static class SimpleSelection implements Transferable, ClipboardOwner {

        protected Fig selection; // The data to be transferred.
        protected DataFlavor flavor; // The one data flavor supported.

        public SimpleSelection(Fig selection, DataFlavor flavor) {
            this.selection = selection; // Specify data.
            this.flavor = flavor; // Specify flavor.
        }

        /**
         * Return the list of supported flavors. Just one in this case
         */
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{flavor};
        }

        /**
         * Check whether we support a specified flavor
         */
        public boolean isDataFlavorSupported(DataFlavor f) {
            return f.equals(flavor);
        }

        /**
         * If the flavor is right, transfer the data (i.e. return it)
         */
        public Object getTransferData(DataFlavor f)
                throws UnsupportedFlavorException {
            if (f.equals(flavor)) {
                return selection;
            } else {
                throw new UnsupportedFlavorException(f);
            }
        }

        /**
         * This is the ClipboardOwner method. Called when the data is no longer
         * on the clipboard. In this case, we don't need to do much.
         */
        public void lostOwnership(Clipboard c, Transferable t) {
            selection = null;
        }
    }

} /* end class CmdCopy */
