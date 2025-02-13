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
// File: PrefsEditor.java
// Classes: PrefsEditor
// Original Author: jrobbins@ics.uci.edu
// $Id: PrefsEditor.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;

/**
 * A dialog box to let the user edit various preferences that effect the
 * application. Needs-More-Work: right now the only preferences that can be
 * edited relate to redrawing. I should add prefs for handle colors, grid
 * snap(?), and some behaviors... Needs-More-Work: This code could be deleted if
 * I used a PropSheet to edit preferences instead.
 *
 */
public class PrefsEditor extends Frame {

    private static final long serialVersionUID = -4707206125138468506L;

    /**
     * A check box for using flicker-free graphics
     */
    private Checkbox _tryOffScreenCB;

    /**
     * A check box for using straight or rectilinear arcs
     */
    private Checkbox _printGridCB;
    private Checkbox _printBackgroundCB;

    /**
     * The "More Repaints" button.
     */
    private Button _moreRepaints;

    /**
     * The "Fewer Repaints" button.
     */
    private Button _fewerRepaints;

    /**
     * The close button
     */
    private Button _close;

    /**
     * Construct a new PrefsEditor and get ready to open it. This can be invoked
     * via ActionOpenWindow.
     *
     * @see CmdOpenWindow
     */
    public PrefsEditor() {
        setTitle("Prefs");
        setBackground(Color.lightGray);
        _tryOffScreenCB = new Checkbox("Flicker-free repaint");
        _moreRepaints = new Button("More Repaints");
        _fewerRepaints = new Button("Fewer Repaints");
        _printGridCB = new Checkbox("Print Grid");
        _printBackgroundCB = new Checkbox("Print Background");
        Panel p = new Panel();
        p.setLayout(new GridLayout(6, 1));
        _tryOffScreenCB.setState(Globals.getPrefs().getTryOffScreen());
        _printGridCB.setState(Globals.getPrefs().getPrintGrid());
        _printBackgroundCB.setState(Globals.getPrefs().getPrintBackground());
        p.add(_tryOffScreenCB);
        p.add(_moreRepaints);
        p.add(_fewerRepaints);
        p.add(_printGridCB);
        p.add(_printBackgroundCB);
        add("Center", p);
        _close = new Button("Close");
        Panel bottomPanel = new Panel();
        bottomPanel.add("Center", _close);
        add("South", bottomPanel);
        pack();
    }

    /**
     * Close the PrefEditor window
     */
    public void close() {
        dispose();
    }

    /**
     * If the user wants to close the window, do it.
     */
    public void processEvent(AWTEvent e) {
        switch (e.getID()) {
            case Event.WINDOW_DESTROY:
                if (e.getSource() == this) {
                    close();
                }
                break;
        }
        super.processEvent(e);
    }

    /**
     * If the use pushes one of the buttons, update Globals.prefs().
     * Needs-More-Work: It would be nice to have a more compact way to code
     * this. I expect that every preference that can be edited will affect
     * Globals.prefs() in a similiar way.
     */
    public boolean action(Event e, Object what) {
        Prefs p = Globals.getPrefs();
        if (e.target == _tryOffScreenCB) {
            p.setTryOffScreen(!p.getTryOffScreen());
            _tryOffScreenCB.setState(p.getTryOffScreen());
            return true;
        }
        if (e.target == _moreRepaints) {
            // RedrawManager.moreRepairs();
            return true;
        }
        if (e.target == _fewerRepaints) {
            // RedrawManager.fewerRepairs();
            return true;
        } else if (e.target == _close) {
            close();
            return true;
        } else if (e.target == _printGridCB) {
            p.setPrintGrid(!p.getPrintGrid());
            _printGridCB.setState(p.getPrintGrid());
            return true;
        } else if (e.target == _printBackgroundCB) {
            p.setPrintBackground(!p.getPrintBackground());
            _printBackgroundCB.setState(p.getPrintBackground());
            return true;
        }
        return false;
    }

} /* end class PrefsEditor */
