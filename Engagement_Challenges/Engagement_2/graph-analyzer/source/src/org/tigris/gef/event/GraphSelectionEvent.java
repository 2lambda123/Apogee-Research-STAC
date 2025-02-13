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
package org.tigris.gef.event;

import java.util.*;

/**
 * An event object that contains information about the current selection(s) in
 * an Editor. These events are sent to registered GraphSelectionListeners
 * whenever the Editor's selection changes.
 *
 * @see GraphSelectionListener
 * @see org.tigris.gef.base.Editor
 * @see org.tigris.gef.base.SelectionManager
 */
public class GraphSelectionEvent extends EventObject {

    private static final long serialVersionUID = 7055361155230503398L;
    private Vector _selections;

    // //////////////////////////////////////////////////////////////
    // constructor
    public GraphSelectionEvent(Object src, Vector selections) {
        super(src);
        _selections = selections;
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    public Vector getSelections() {
        return _selections;
    }

} /* end class GraphSelectionEvent */
