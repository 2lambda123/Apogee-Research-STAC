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
// File: ArrowHead.java
// Classes: ArrowHead
// Original Author: abonner@ics.uci.edu
// $Id: ArrowHeadDiamond.java 1238 2009-06-27 17:26:13Z bobtarling $
package org.tigris.gef.presentation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

/**
 * Draws a Diamond at the end of a FigEdge.
 */
public class ArrowHeadDiamond extends ArrowHead {

    private static final long serialVersionUID = 7056384380724728325L;

    public static ArrowHeadDiamond WhiteDiamond = new ArrowHeadDiamond(
            Color.black, Color.white);

    public static ArrowHeadDiamond BlackDiamond = new ArrowHeadDiamond(
            Color.black, Color.black);

    // //////////////////////////////////////////////////////////////
    // constuctors
    public ArrowHeadDiamond() {
    }

    public ArrowHeadDiamond(Color line, Color fill) {
        super(line, fill);
        setHeight(20);
        setWidth(5);
    }

    // //////////////////////////////////////////////////////////////
    // display methods
    public void paint(
            final Graphics g,
            final Point start,
            final Point end,
            final Color lineColor,
            final Color fillColor) {
        double denom, x, y, dx, dy, cos, sin;
        Polygon diamond;

        final int xFrom = start.x;
        final int xTo = end.x;
        final int yFrom = start.y;
        final int yTo = end.y;

        dx = (double) (xTo - xFrom);
        dy = (double) (yTo - yFrom);
        denom = dist(dx, dy);
        if (denom == 0) {
            return;
        }

        cos = (getHeight() / 2) / denom;
        sin = getWidth() / denom;
        x = xTo - cos * dx;
        y = yTo - cos * dy;
        int x1 = (int) (x - sin * dy);
        int y1 = (int) (y + sin * dx);
        int x2 = (int) (x + sin * dy);
        int y2 = (int) (y - sin * dx);

        Point topPoint = pointAlongLine(end, start, getHeight());

        // needs-more-work: should avoid alloacting new objects in paint
        diamond = new Polygon();
        diamond.addPoint(xTo, yTo);
        diamond.addPoint(x1, y1);
        diamond.addPoint(topPoint.x, topPoint.y);
        diamond.addPoint(x2, y2);

        Graphics graphics = (Graphics) g;
        graphics.setColor(getFillColor());
        graphics.fillPolygon(diamond);
        graphics.setColor(lineColor);
        graphics.drawPolygon(diamond);
    }

} /* end class ArrowHeadDiamond */
