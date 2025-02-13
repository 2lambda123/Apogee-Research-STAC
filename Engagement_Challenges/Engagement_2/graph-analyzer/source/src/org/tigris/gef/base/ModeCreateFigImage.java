// Copyright (c) 1996-2009 The Regents of the University of California. All
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
// File: ModeCreateFigImage.java
// Classes: ModeCreateFigImage
// Original Author: jrobbins@ics.uci.edu
// $Id: ModeCreateFigImage.java 1259 2009-08-18 06:53:37Z mvw $
package org.tigris.gef.base;

import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigImage;
import org.tigris.gef.util.Localizer;

/**
 * A Mode to interpret user input while creating a FigImage. All of the actual
 * event handling is inherited from ModeCreate. This class just implements the
 * differences needed to make it specific to images.
 */
public class ModeCreateFigImage extends ModeCreate {

    private static final long serialVersionUID = -3062009802693268691L;

    /**
     * The image to be placed.
     */
    protected Image _image;

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * get and set the image to be used for the new FigImage.
     */
    public Image image() {
        return _image;
    }

    public void image(Image i) {
        _image = i;
    }

    // //////////////////////////////////////////////////////////////
    // Mode API
    public String instructions() {
        return Localizer.localize("GefBase", "ModeCreateFigImageInstructions");
    }

    // //////////////////////////////////////////////////////////////
    // ModeCreate API
    /**
     * Create a new FigImage instance based on the given mouse down event and
     * the state of the parent Editor.
     */
    public Fig createNewItem(MouseEvent me, int snapX, int snapY) {
        if (null == _image) {
            String dURL = "http://www.ics.uci.edu/~jrobbins/banners/gef_banner.gif";
            String urlString = (String) _args.get("imageURL");
            if (urlString == null) {
                urlString = dURL;
            }
            _image = Globals.getImage(urlString);
            Globals.waitForImages();
        }
        return new FigImage(snapX, snapY, _image);
    }

    // //////////////////////////////////////////////////////////////
    // event handlers
    /**
     * When the mouse enters an Editor, create the FigImage and place it at the
     * mouse position.
     */
    public void mouseEntered(MouseEvent me) {
        int x = me.getX(), y = me.getY();
        start();
        anchorX = x;
        anchorY = y;
        Point snapPt = new Point(x, y);
        editor.snap(snapPt);
        if (null == _newItem) {
            _newItem = createNewItem(me, snapPt.x, snapPt.y);
        }
        me.consume();
    }

    /**
     * When the mouse exits the editor, clean up the display a little.
     */
    public void mouseExited(MouseEvent me) {
        editor.damageAll();
        me.consume();
    }

    /**
     * On mouse down, do nothing.
     */
    public void mousePressed(MouseEvent me) {
        me.consume();
    }

    /**
     * Whem the user drags or moves the mouse, move the FigImage to the current
     * mouse position.
     */
    public void mouseMoved(MouseEvent me) {
        int x = me.getX(), y = me.getY();
        if (_newItem == null) {
            org.graph.commons.logging.LogFactory.getLog(null).info("null _newItem");
            me.consume();
            return;
        }
        editor.damageAll();
        Point snapPt = new Point(x, y);
        editor.snap(snapPt);
        _newItem.setLocation(snapPt.x, snapPt.y);
        editor.damageAll(); /* needed? */

        me.consume();
    }

    /**
     * Exactly the same as mouseMove.
     */
    public void mouseDragged(MouseEvent me) {
        mouseMoved(me);
    }
} /* end class ModeCreateFigImage */
