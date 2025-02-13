// Copyright (c) 1996-2007 The Regents of the University of California. All
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
// File: LayerPerspective.java
// Classes: LayerPerspective
// Original Author: jrobbins@ics.uci.edu
// $Id: LayerPerspective.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.base;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;
import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.tigris.gef.presentation.*;
import org.tigris.gef.graph.*;
import org.tigris.gef.graph.presentation.*;

/**
 * A Layer like found in many drawing applications. It contains a collection of
 * Figs, ordered from back to front. Each LayerPerspective contains part of the
 * overall picture that the user is drawing. LayerPerspective is different from
 * LayerDiagram in that it assumes that you are drawing a connected graph that
 * is represented in a GraphModel and controlled by a GraphController.
 */
public class LayerPerspective extends LayerDiagram implements GraphListener {

    private static final long serialVersionUID = -3219953846728127850L;

    /**
     * The space between node FigNodes that are automatically places.
     */
    public static final int GAP = 16;

    /**
     * The underlying connected graph to be visualized.
     */
    private GraphModel _gm;

    private GraphController controller;

    private GraphNodeRenderer _nodeRenderer = new DefaultGraphNodeRenderer();

    private GraphEdgeRenderer _edgeRenderer = new DefaultGraphEdgeRenderer();

    /**
     * Classes of NetNodes and NetEdges that are to be visualized in this
     * perspective.
     */
    private Vector _allowedNetClasses = new Vector();

    /**
     * Rectangles of where to place nodes that are automatically added.
     */
    private Hashtable _nodeTypeRegions = new Hashtable();

    /**
     * The diagram containing this layer.
     */
    private Diagram diagram;

    private static Log LOG = LogFactory.getLog(LayerPerspective.class);

    // //////////////////////////////////////////////////////////////
    // constructors
    /**
     * Construct a new LayerPerspective with the given name, and add it to the
     * menu of layers. Needs-More-Work: I have not implemented a menu of layers
     * yet. I don't know if that is really the right user interface
     */
    public LayerPerspective(String name, GraphModel gm) {
        super(name);
        _gm = gm;
        controller = null;
        _gm.addGraphEventListener(this);
    }

    public LayerPerspective(String name, GraphModel gm,
            GraphController controller) {
        super(name);
        _gm = gm;
        this.controller = controller;
        _gm.addGraphEventListener(this);
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * Reply the GraphModel of the underlying connected graph.
     */
    public GraphModel getGraphModel() {
        return _gm;
    }

    public void setGraphModel(GraphModel gm) {
        _gm.removeGraphEventListener(this);
        _gm = gm;
        _gm.addGraphEventListener(this);
    }

    /**
     * Reply the GraphController of the underlying connected graph.
     */
    public GraphController getGraphController() {
        return controller;
    }

    public void setGraphController(GraphController controller) {
        this.controller = controller;
    }

    public GraphNodeRenderer getGraphNodeRenderer() {
        return _nodeRenderer;
    }

    public void setGraphNodeRenderer(GraphNodeRenderer rend) {
        _nodeRenderer = rend;
    }

    public GraphEdgeRenderer getGraphEdgeRenderer() {
        return _edgeRenderer;
    }

    public void setGraphEdgeRenderer(GraphEdgeRenderer rend) {
        _edgeRenderer = rend;
    }

    /**
     * Add a node class of NetNodes or NetEdges to what will be shown in this
     * perspective.
     */
    public void allowNetClass(Class c) {
        _allowedNetClasses.addElement(c);
    }

    // //////////////////////////////////////////////////////////////
    // Layer API
    /**
     * Add a Fig to the contents of this Layer. Items are added on top of all
     * other items. If a node is explicitly added then accept it regardless of
     * the predicate, and add it to the net.
     */
    // public void add(Fig f) { super.add(f); }
    /**
     * Remove the given Fig from this layer.
     */
    // public void remove(Fig f) { super.remove(f); }
    // //////////////////////////////////////////////////////////////
    // node placement
    public void addNodeTypeRegion(Class nodeClass, Rectangle region) {
        _nodeTypeRegions.put(nodeClass, region);
    }

    public void putInPosition(Fig f) {
        Class nodeClass = f.getOwner().getClass();
        Rectangle placementRegion = (Rectangle) _nodeTypeRegions.get(nodeClass);
        if (placementRegion != null) {
            f.setLocation(placementRegion.x, placementRegion.y);
            bumpOffOtherNodesIn(f, placementRegion, false, true);
        }
    }

    /**
     * Try and find a blank area of the diagram to place the new Fig.
     */
    public void bumpOffOtherNodesIn(Fig newFig, Rectangle bounds,
            boolean stagger, boolean vertical) {
        Rectangle bbox = newFig.getBounds();
        int origX = bbox.x, origY = bbox.y;
        int col = 0, row = 0, i = 1;
        while (bounds.intersects(bbox)) {
            Enumeration overlappers = nodesIn(bbox);
            // If there is nothing overlapping then we are done:
            if (!overlappers.hasMoreElements()) {
                return;
            }
            // Idem if the only found fig is the one we try to place:
            if ((overlappers.nextElement() == newFig)
                    && (!overlappers.hasMoreElements())) {
                return;
            }
            int unitOffset = ((i + 1) / 2) * ((i % 2 == 0) ? -1 : 1);
            if (vertical) {
                bbox.y = origY + unitOffset * (bbox.height + GAP);
            } else {
                bbox.x = origX + unitOffset * (bbox.width + GAP);
            }
            newFig.setLocation(bbox.x, bbox.y);
            if (!(bounds.intersects(bbox))) {
                int x = bounds.x;
                int y = bounds.y;
                if (vertical) {
                    col++;
                    x = bbox.x + bbox.width + GAP;
                    if (stagger) {
                        y += (col % 2) * (bbox.height + GAP) / 2;
                    }
                } else {
                    row++;
                    y = bbox.y + bbox.height + GAP;
                    if (stagger) {
                        x += (row % 2) * (bbox.width + GAP) / 2;
                    }
                }
                newFig.setLocation(x, y);
                bbox.setLocation(x, y);
            }
            i++;
        }
    }

    // //////////////////////////////////////////////////////////////
    // nofitications and updates
    public void nodeAdded(GraphEvent ge) {
        Object node = ge.getArg();
        Fig oldDE = presentationFor(node);
        // assumes each node can only appear once in a given layer
        if (null == oldDE) {
            if (!shouldShow(node)) {
                org.graph.commons.logging.LogFactory.getLog(null).info("node rejected");
                return;
            }
            FigNode newFigNode = _nodeRenderer.getFigNodeFor(_gm, this, node,
                    null);
            if (newFigNode != null) {
                newFigNode.setLayer(this);
                putInPosition(newFigNode);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding node");
                }
                add(newFigNode);
            }
            // else org.graph.commons.logging.LogFactory.getLog(null).info("added node de is null");
        }
    }

    public void edgeAdded(GraphEvent ge) {
        // org.graph.commons.logging.LogFactory.getLog(null).info("LayerPerspective got edgeAdded");
        Object edge = ge.getArg();
        Fig oldFig = presentationFor(edge);
        if (null == oldFig) {
            if (!shouldShow(edge)) {
                org.graph.commons.logging.LogFactory.getLog(null).info("edge rejected");
                return;
            }
            FigEdge newFigEdge = _edgeRenderer.getFigEdgeFor(_gm, this, edge,
                    null);
            if (newFigEdge != null) {
                newFigEdge.setLayer(this);
                add(newFigEdge);
                // insertAt(newFigEdge, 0);
                newFigEdge.computeRoute();
                // newFigEdge.reorder(CmdReorder.SEND_TO_BACK, this);
                newFigEdge.endTrans();
            }
            // else org.graph.commons.logging.LogFactory.getLog(null).info("added arc fig is null!!!!!!!!!!!!!!!!");
        }
    }

    public void nodeRemoved(GraphEvent ge) {
        // handled through NetNode subclasses
        // no, it is not handled through subclasses.
        // this method body was added by hendrik@freiheit.com
        // 2003-02-20
        Fig node = presentationFor(ge.getArg());
        if (null != node) {
            // this is a dirty quickfix to deal with highlighted
            // FigNodes.
            if (node instanceof FigNode && ((FigNode) node).getHighlight()) {
                ((FigNode) node).setHighlight(false);
            }
            remove(node);
        }
    }

    public void edgeRemoved(GraphEvent ge) {
        // handled through NetEdge subclasses
        // no, it is not handled through subclasses.
        // this method body was added by hendrik@freiheit.com
        // 2003-02-20
        Fig edge = presentationFor(ge.getArg());
        if (null != edge) {
            remove(edge);
        }
    }

    public void graphChanged(GraphEvent ge) {
        // needs-more-work
    }

    /**
     * Test to determine if a given NetNode should have a FigNode in this layer.
     * Normally checks NetNode class against a list of allowable classes. For
     * more sophisticated rules, override this method. <A
     * HREF="../features.html#multiple_perspectives">
     * <TT>FEATURE: multiple_perspectives</TT></A>
     */
    public boolean shouldShow(Object obj) {
        if (_allowedNetClasses.size() > 0
                && !_allowedNetClasses.contains(obj.getClass())) {
            return false;
        }
        if (obj instanceof NetEdge) {
            if (getPortFig(((NetEdge) obj).getSourcePort()) == null
                    || getPortFig(((NetEdge) obj).getDestPort()) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param diagram The diagram to set.
     */
    void setDiagram(Diagram diagram) {
        this.diagram = diagram;
    }

    /**
     * @return Returns the diagram.
     */
    public Diagram getDiagram() {
        return diagram;
    }

    /**
     * Test to determine if a given NetEdge should have an FigEdge in this
     * layer. Normally checks NetNode class against a list of allowable classes.
     * For more sophisticated rules, override this method. <A
     * HREF="../features.html#multiple_perspectives">
     * <TT>FEATURE: multiple_perspectives</TT></A>
     */
    // public boolean shouldShow(NetEdge a) {
    // if (_allowedNetClasses.size() > 0 &&
    // !_allowedNetClasses.contains(a.getClass()))
    // return false;
    // if (getPortFig(a.getSourcePort()) == null ||
    // getPortFig(a.getDestPort()) == null)
    // return false;
    // return true;
    // }
} /* end class LayerPerspective */
