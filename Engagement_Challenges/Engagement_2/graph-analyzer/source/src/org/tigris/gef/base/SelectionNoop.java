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
// File: SelectionNoop.java
// Classes: SelectionNoop
// Original Author: jrobbins@ics.uci.edu
// $Id: SelectionNoop.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import java.awt.Graphics;
import java.awt.Rectangle;

import org.tigris.gef.presentation.*;

/**
 * Selection object that does not allow the user to do anything. This might be
 * useful for some special Figs. it is not used by the Figs provided by GEF.
 */
public class SelectionNoop extends Selection {

    private static final long serialVersionUID = -8883328105257650811L;

    /**
     * Construct a new SelectionNoop around the given DiagramElement
     */
    public SelectionNoop(Fig f) {
        super(f);
    }

    /**
     * Paint the selection.
     */
    public void paint(Graphics g) {
        int x = getContent().getX();
        int y = getContent().getY();
        int w = getContent().getWidth();
        int h = getContent().getHeight();
        g.setColor(Globals.getPrefs().handleColorFor(getContent()));
        g.drawRect(x - BORDER_WIDTH, y - BORDER_WIDTH,
                w + BORDER_WIDTH * 2 - 1, h + BORDER_WIDTH * 2 - 1);
        g.drawRect(x - BORDER_WIDTH - 1, y - BORDER_WIDTH - 1, w + BORDER_WIDTH
                * 2 + 2 - 1, h + BORDER_WIDTH * 2 + 2 - 1);
        g.fillOval(x - HAND_SIZE, y - HAND_SIZE, HAND_SIZE, HAND_SIZE);
        g.fillOval(x + w, y - HAND_SIZE, HAND_SIZE, HAND_SIZE);
        g.fillOval(x - HAND_SIZE, y + h, HAND_SIZE, HAND_SIZE);
        g.fillOval(x + w, y + h, HAND_SIZE, HAND_SIZE);
    }

    /**
     * SelectionNoop is used when there are no handles, so dragHandle does
     * nothing. Actually, hitHandle always returns -1 , so this method should
     * never even get called.
     */
    public void dragHandle(int mx, int my, int an_x, int an_y, Handle h) {
        /* do nothing */
    }

    /**
     * Returns -2 as a special code to indicate that the Fig cannot be moved.
     */
    public void hitHandle(Rectangle r, Handle h) {
        h.index = -2;
        h.instructions = "Object cannot be moved or resized";
    }
} /* end class SelectionNoop */
