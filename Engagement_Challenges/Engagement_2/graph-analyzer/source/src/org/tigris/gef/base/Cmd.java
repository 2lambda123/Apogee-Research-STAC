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
// File: Action.java
// Classes: Action
// Original Author: jrobbins@ics.uci.edu
// $Id: Cmd.java 1167 2008-12-03 09:43:39Z bobtarling $
package org.tigris.gef.base;

import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.tigris.gef.util.Localizer;
import org.tigris.gef.util.ResourceLoader;

/**
 * Abstract class for all editor commands.
 * <p>
 * The editor serves as a command shell for executing actions in much the same
 * way that a DOS or UNIX commmand command shell executes programs. Each command
 * can have a Hashtable of "command-line" arguments and also look at global
 * variables (its environment). Once an instance of a Cmd is made, it can be
 * sent the doIt() and undoIt() messages to perform that action.
 * </p>
 *
 * <p>
 * Since this is subclassed from class AbstractAction in the Swing user
 * interface library, Cmd objects can be easily added to menus and toolbars.
 * </p>
 * <p>
 * TODO: Implement an undo queue externally from GEF
 * </p>
 */
public abstract class Cmd extends AbstractAction implements
        java.io.Serializable {

    // Arguments that configure the Cmd instance.
    /**
     * in 0.13 will become private use getArg()
     */
    protected Hashtable _args;

    /**
     * in 0.13 will become private use getResource()
     */
    protected String _resource;

    /**
     * Construct a new Cmd with the given arguments
     */
    public Cmd(Hashtable args, String resource, String name) {
        super(Localizer.localize(resource, name));
        Icon icon = ResourceLoader.lookupIconResource(name, name);
        if (icon != null) {
            putValue(Action.SMALL_ICON, icon);
        }
        _args = args;
        _resource = resource;
    }

    public Cmd(String resource, String name) {
        this(null, resource, name);
    }

    public Cmd(Hashtable args, String resource, String name, ImageIcon icon) {
        super(Localizer.localize(resource, name), icon);
        _args = args;
        _resource = resource;
    }

    /**
     * Constructors with no resource name
     */
    protected Cmd(String name) {
        this(null, "GefBase", name);
    }

    protected Cmd(Hashtable args, String name, ImageIcon icon) {
        this(args, "GefBase", name, icon);
    }

    protected Cmd(Hashtable args, String name) {
        this(args, "GefBase", name);
    }

    // //////////////////////////////////////////////////////////////
    // enabling and disabling
    /**
     * Determine if this Cmd should be shown as grayed out in menus and
     * toolbars.
     */
    public void updateEnabled() {
        setEnabled(shouldBeEnabled());
    }

    /**
     * Return true if this action should be available to the user. This method
     * should examine the ProjectBrowser that owns it. Sublass implementations
     * of this method should always call super.shouldBeEnabled first.
     */
    public boolean shouldBeEnabled() {
        return true;
    }

    /**
     * Set a new resource as basis for the localization of this command.
     */
    public void setResource(String resource) {
        _resource = resource;
    }

    public String getResource() {
        return _resource;
    }

    /**
     * Return a name for this Cmd suitable for display to the user
     */
    public String getName() {
        return (String) getValue(NAME);
    }

    public void setName(String n) {
        putValue(NAME, Localizer.localize(_resource, n));
    }

    /**
     * Get the object stored as an argument under the given name.
     */
    protected Object getArg(String key) {
        if (_args == null) {
            return null;
        } else {
            return _args.get(key);
        }
    }

    /**
     * Get an argument by name. If it's not defined then use the given default.
     */
    protected Object getArg(String key, Object defaultValue) {
        if (_args == null) {
            return defaultValue;
        }
        Object res = _args.get(key);
        if (res == null) {
            return defaultValue;
        }
        return res;
    }

    /**
     * Store the given argument under the given name.
     */
    protected void setArg(String key, Object value) {
        if (_args == null) {
            _args = new Hashtable();
        }
        _args.put(key, value);
    }

    /**
     * Reply true if this Cmd instance has the named argument defined.
     */
    protected boolean containsArg(String key) {
        return _args != null && _args.containsKey(key);
    }

    // //////////////////////////////////////////////////////////////
    // Cmd API
    /**
     * Return a URL that has user and programmer documentation. <A
     * HREF="../features.html#view_Cmd_documentation">
     * <TT>FEATURE: view_Cmd_documentation</TT></A>
     */
    public String about() {
        return "http://gef.tigris.org" + getClass().getName();
    }

    public void actionPerformed(ActionEvent ae) {
        doIt();
    }

    /**
     * Perform whatever Cmd this Cmd is meant to do. Subclasses should override
     * this to do whatever is intended. When the Cmd executes, it should store
     * enough information to undo itself later if needed.
     */
    public abstract void doIt();

    /**
     * Undo the Cmd using information stored during its execution.
     * <p>
     * TODO Abandon this. We need the memento pattern for undo
     */
    public abstract void undoIt();

    // needs-more-work: do I need a separate redo()? Should doIt() take
    // flag to indicate if this is the first time the Cmd is being
    // done, or it it is actually being redone? What information does
    // undoIt() need to store to support redo?
    // TODO Abandon this. We need the memento pattern for undo
    // //////////////////////////////////////////////////////////////
    // registered Cmds
    /**
     * A list of Cmd instances that should appear in lists for the user to pick
     * from. Registered Cmds serve mainly to support user interface prototyping:
     * you can add Cmds to the CmdOpenWindow and not have to worry about where
     * it should eventually go in the user interface. TODO Does this really
     * belong in GEF?
     */
    private static Vector _registeredCmds = new Vector();

    /**
     * Return a list of "well-known" Cmd instances that should appear in lists
     * for the user to pick from. TODO Does this really belong in GEF?
     *
     * @see CmdOpenWindow
     */
    public static Enumeration registeredCmds() {
        return _registeredCmds.elements();
    }

    /**
     * Add a "well-known" Cmd TODO Does this really belong in GEF?
     */
    public static void register(Cmd c) {
        _registeredCmds.addElement(c);
    }

    /**
     * Return the "well-known" Cmd at a given index. Useful for displaying a
     * list of "well-known" cmds. TODO Does this really belong in GEF?
     */
    public static Cmd cmdAtIndex(int i) {
        return (Cmd) _registeredCmds.elementAt(i);
    }
} /* end class Cmd */
