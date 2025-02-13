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
// From Sun's Beanbox
// Support for PropertyEditors that use tags.
//package sun.beanbox;
package org.tigris.gef.properties.ui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.beans.*;

class PropertySelector extends JComboBox implements ItemListener {

    public PropertySelector(PropertyEditor pe) {
        editor = pe;
        String tags[] = editor.getTags();
        for (int i = 0; i < tags.length; i++) {
            addItem(tags[i]);
        }
        setSelectedIndex(0);
        // This is a noop if the getAsText is not a tag.
        setSelectedItem(editor.getAsText());
        addItemListener(this);
    }

    public Dimension getMinimumSize() {
        return new Dimension(80, 20);
    }

    public Dimension getPreferredSize() {
        return new Dimension(80, 20);
    }

    public void itemStateChanged(ItemEvent evt) {
        String s = getSelectedItem().toString();
        editor.setAsText(s);
    }

    // public void repaint() {
    // super.repaint();
    // //setSelectedItem(editor.getAsText());
    // }
    PropertyEditor editor;
}
