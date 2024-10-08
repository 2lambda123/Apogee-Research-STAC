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
// File: NetPort.java
// Classes: NetPort
// Original Author: ics125 spring 1996
// $Id: NetPort.java 1153 2008-11-30 16:14:45Z bobtarling $
package org.tigris.gef.graph.presentation;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;
import java.util.*;

import org.tigris.gef.graph.GraphModel;
import org.tigris.gef.graph.GraphPortHooks;

/**
 * This class models a port in our underlying connected graph model. A port is
 * place on a node where an edge can connect. For example, the power socket in a
 * wall, ot the power cord socket on the back of a computer. This class is used
 * by the DefaultGraphModel. You can also define your own GraphModel that uses
 * your own application-specific objects as ports.
 */
public class NetPort extends NetPrimitive implements GraphPortHooks,
        java.io.Serializable {

    private static final long serialVersionUID = -3506978147166333303L;

    // needs-more-work: main framework should not depend on any demo code
    public static String DEFAULT_EDGE_CLASS = "org.tigris.gef.graph.presentation.NetEdge";

    // //////////////////////////////////////////////////////////////
    // instance variables
    /**
     * The NetEdges that are connected to this port.
     */
    private Vector edges;

    /**
     * The NetNode that this port is a part of.
     */
    private Object _parent;

    private static Log LOG = LogFactory.getLog(NetPort.class);

    // //////////////////////////////////////////////////////////////
    // constructors
    /**
     * Construct a new NetPort with the given parent node and no arcs.
     */
    public NetPort(Object parent) {
        _parent = parent;
        edges = new Vector();
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    public String getId() {
        return toString();
    }

    /**
     * Reply the NetNode that owns this port.
     */
    public NetNode getParentNode() {
        return (NetNode) _parent;
    }

    public NetEdge getParentEdge() {
        return (NetEdge) _parent;
    }

    public Object getParent() {
        return _parent;
    }

    /**
     * Reply a vector of NetEdges that are connected here.
     */
    public Vector getEdges() {
        return edges;
    }

    // //////////////////////////////////////////////////////////////
    // net-level operations
    /**
     * Add an edge to the list of edge connected to this port. Called when the
     * user defines a new edge. Normally, you would not call this directly, you
     * would call NetEdge#connect().
     */
    public void addEdge(NetEdge edge) {
        edges.addElement(edge);
    }

    /**
     * Remove an edge from the list of edge connected to this port. Called when
     * the user disposes an edge. Normally, you would not call this directly,
     * you would call NetEdge#deleteFromModel().
     */
    public void removeEdge(NetEdge edge) {
        edges.removeElement(edge);
    }

    /**
     * Remove this port from the underlying connected graph model and dispose
     * all arcs connected to it.
     */
    public void deleteFromModel() {
        LOG.debug("Deleting from model");
        int size = edges.size();
        for (int i = 0; i < size; i++) {
            // We always just dispose the first edge as each dispose
            // results in a call-back to removing that same edge from the
            // edges list making what was the next item the new first
            // item.
            NetEdge edge = (NetEdge) edges.get(0);
            edge.deleteFromModel();
        }
        firePropertyChange("disposed", false, true);
    }

    // //////////////////////////////////////////////////////////////
    // net-level hooks
    /**
     * Application specific hook that is called after a successful connection.
     */
    public void postConnect(GraphModel gm, Object otherPort) {
        NetPort otherNetPort = (NetPort) otherPort;
        NetNode parent = getParentNode();
        parent
                .postConnect(gm, otherNetPort.getParentNode(), this,
                        otherNetPort);
    }

    /**
     * Application specific hook that is called after a disconnection. (for now,
     * all disconnections are assumed legal).
     */
    public void postDisconnect(GraphModel gm, Object otherPort) {
        NetPort otherNetPort = (NetPort) otherPort;
        NetNode parent = getParentNode();
        parent.postDisconnect(gm, otherNetPort.getParentNode(), this,
                otherNetPort);
    }

    /**
     * reply the java Class to be used to make new arcs. This is a utility
     * function called from NetPort#makeEdgeFor
     */
    protected Class defaultEdgeClass(NetPort otherPort) {
        try {
            return Class.forName(DEFAULT_EDGE_CLASS);
        } catch (java.lang.ClassNotFoundException ignore) {
            return null;
        }
    }

    /**
     * reply a new NetEdge from this port to the given NetPort.
     */
    public NetEdge makeEdgeFor(NetPort otherPort) {
        Class edgeClass;
        NetEdge edge;
        edgeClass = defaultEdgeClass(otherPort);
        if (edgeClass == null) {
            LOG.error("defaultEdgeClass is null");
            return null;
        }
        try {
            edge = (NetEdge) edgeClass.newInstance();
        } catch (java.lang.IllegalAccessException e) {
            LOG.error("Failed to create edge ", e);
            return null;
        } catch (java.lang.InstantiationException e) {
            LOG.error("Failed to create edge ", e);
            return null;
        }
        LOG.debug("Made edge " + edge.getClass().getName());
        return edge;
    }

    // //////////////////////////////////////////////////////////////
    // net-level constraints
    /**
     * Reply true if this port can legally be connected to the given port.
     * Subclasses may implement this to reflect application specific connection
     * constraints. By default, each port just defers that decision to its
     * parent NetNode. By convention, your implementation should return false if
     * super.canConnectTo() would return false (i.e., deeper subclasses get more
     * constrained). I don't know if that is a good convention.
     */
    public boolean canConnectTo(GraphModel gm, Object anotherPort) {
        NetNode myNode = getParentNode();
        NetNode otherNode = ((NetPort) anotherPort).getParentNode();
        return myNode.canConnectTo(gm, otherNode, anotherPort, this);
    }
} /* end class NetPort */
