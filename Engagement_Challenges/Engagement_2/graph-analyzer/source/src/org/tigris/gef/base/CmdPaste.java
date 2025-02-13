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
// File: CmdPaste.java
// Classes: CmdPaste
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdPaste.java 1187 2008-12-09 21:17:38Z bobtarling $
package org.tigris.gef.base;

import java.beans.VetoableChangeListener;
import java.util.Iterator;
import java.util.Vector;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.util.VetoableChangeEventSource;

/**
 * in 0.12.3 use PasteAction
 */
public class CmdPaste extends Cmd {

    private static final long serialVersionUID = 8942391014743560735L;

    public CmdPaste() {
        super("Paste");
    }

    // needs-more-work: if the Fig was removed from the model, then I would
    // need to create a new owner.
    public void doIt() {
        SelectionManager sm = Globals.curEditor().getSelectionManager();
        Vector figs = new Vector();
        Iterator cb = Globals.clipBoard.iterator();
        while (cb.hasNext()) {
            Fig f = (Fig) cb.next();
            Editor ce = Globals.curEditor();
            int gridSze = ((GuideGrid) ce.getGuide()).gridSize();
            f.translate(gridSze, gridSze);
            f = (Fig) f.clone();
            Object owner = f.getOwner();
            if (owner instanceof VetoableChangeEventSource
                    && f instanceof VetoableChangeListener) {
                ((VetoableChangeEventSource) owner)
                        .addVetoableChangeListener((VetoableChangeListener) f);
            }
            ce.add(f);
            figs.addElement(f);
        }
        sm.deselectAll();
        sm.select(figs);
    }

    public void undoIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("Undo does not make sense for CmdPaste");
    }

    /**
     * The DataFlavor used for our particular type of cut-and-paste data. This
     * one will transfer data in the form of a serialized Vector object. Note
     * that in Java 1.1.1, this works intra-application, but not between
     * applications. Java 1.1.1 inter-application data transfer is limited to
     * the pre-defined string and text data flavors.
     */
    // Awating jdk 1.2
    // public static final DataFlavor dataFlavor =
    // new DataFlavor(Fig.class, "Fig");
    // Awating jdk 1.2
    // protected Vector figs = new Vector(256,256); // Store the Figs.
    // Awating jdk 1.2
    // public void paste() {
    /**
     * Ask for the Transferable contents of the system clipboard, then ask that
     * object for the scribble data it represents. If either step fails, beep!
     */
    // Clipboard c =
    // ProjectBrowser.TheInstance.getToolkit().getSystemClipboard(); // Get
    // clipboard.
    /*
     * Clipboard c =
     * ProjectBrowser.TheInstance.getToolkit().getSystemClipboard(); // Get
     * clipboard. Transferable t = c.getContents(ProjectBrowser.TheInstance); //
     * Get its contents. //org.graph.commons.logging.LogFactory.getLog(null).info("paste has been executed");
     * org.graph.commons.logging.LogFactory.getLog(null).info("t = " + t);
     * 
     * if (t == null) { // If there is nothing to paste, beep.
     * ProjectBrowser.TheInstance.getToolkit().beep(); org.graph.commons.logging.LogFactory.getLog(null).info("paste
     * has been executed"); return; } try { // Ask for clipboard contents to be
     * converted to our data flavor. // This will throw an exception if our
     * flavor is not supported. Fig newFig = (Fig)
     * t.getTransferData(dataFlavor); if (newFig instanceof Fig)
     * org.graph.commons.logging.LogFactory.getLog(null).info("success!"); org.graph.commons.logging.LogFactory.getLog(null).info("paste1 has been
     * executed"); // Add all those pasted lines to our scribble. //for(int i =
     * 0; i < newFigs.size(); i++) //figs.addElement(newFigs.elementAt(i)); //
     * And redraw the whole thing //repaint(); } catch
     * (UnsupportedFlavorException e) {
     * ProjectBrowser.TheInstance.getToolkit().beep(); // If clipboard has some
     * other type of data } catch (Exception e) {
     * ProjectBrowser.TheInstance.getToolkit().beep(); // Or if anything else
     * goes wrong... } }
     */
} /* end class CmdPaste */
