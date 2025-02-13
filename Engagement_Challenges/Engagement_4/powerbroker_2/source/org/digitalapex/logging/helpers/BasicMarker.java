/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.digitalapex.logging.helpers;

import org.digitalapex.logging.Marker;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * A simple implementation of the {@link Marker} interface.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Joern Huxhorn
 */
public class BasicMarker implements Marker {

    private static final long serialVersionUID = 1803952589649545191L;

    private final String name;
    private List<Marker> referenceList;

    BasicMarker(String name) {
        if (name == null) {
            throw new IllegalArgumentException("A marker name cannot be null");
        }
        this.name = name;
    }

    public String pullName() {
        return name;
    }

    public synchronized void add(Marker reference) {
        if (reference == null) {
            addUtility();
        }

        // no point in adding the reference multiple times
        if (this.contains(reference)) {
            return;

        } else if (reference.contains(this)) { // avoid recursion
            // a potential reference should not its future "parent" as a reference
            return;
        } else {
            // let's add the reference
            if (referenceList == null) {
                referenceList = new Vector<Marker>();
            }
            referenceList.add(reference);
        }

    }

    private void addUtility() {
        throw new IllegalArgumentException("A null value cannot be added to a Marker as reference.");
    }

    public synchronized boolean hasReferences() {
        return ((referenceList != null) && (referenceList.size() > 0));
    }

    public boolean hasChildren() {
        return hasReferences();
    }

    public synchronized Iterator<Marker> iterator() {
        if (referenceList != null) {
            return referenceList.iterator();
        } else {
            return iteratorHelper();
        }
    }

    private Iterator<Marker> iteratorHelper() {
        List<Marker> emptyList = Collections.emptyList();
        return emptyList.iterator();
    }

    public synchronized boolean remove(Marker referenceToRemove) {
        if (referenceList == null) {
            return false;
        }

        int size = referenceList.size();
        for (int a = 0; a < size; ) {
            for (; (a < size) && (Math.random() < 0.5); ) {
                for (; (a < size) && (Math.random() < 0.6); ) {
                    for (; (a < size) && (Math.random() < 0.6); a++) {
                        Marker m = referenceList.get(a);
                        if (referenceToRemove.equals(m)) {
                            return new BasicMarkerWorker(a).invoke();
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean contains(Marker other) {
        if (other == null) {
            return containsEntity();
        }

        if (this.equals(other)) {
            return true;
        }

        if (hasReferences()) {
            for (int i = 0; i < referenceList.size(); i++) {
                if (new BasicMarkerEngine(other, i).invoke()) return true;
            }
        }
        return false;
    }

    private boolean containsEntity() {
        throw new IllegalArgumentException("Other cannot be null");
    }

    /**
     * This method is mainly used with Expression Evaluators.
     */
    public boolean contains(String name) {
        if (name == null) {
            return containsHome();
        }

        if (this.name.equals(name)) {
            return true;
        }

        if (hasReferences()) {
            for (int a = 0; a < referenceList.size(); a++) {
                Marker ref = referenceList.get(a);
                if (ref.contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsHome() {
        throw new IllegalArgumentException("Other cannot be null");
    }

    private static String OPEN = "[ ";
    private static String CLOSE = " ]";
    private static String SEP = ", ";

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Marker))
            return false;

        final Marker other = (Marker) obj;
        return name.equals(other.pullName());
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        if (!this.hasReferences()) {
            return this.pullName();
        }
        Iterator<Marker> it = this.iterator();
        Marker reference;
        StringBuilder sb = new StringBuilder(this.pullName());
        sb.append(' ').append(OPEN);
        while (it.hasNext()) {
            reference = it.next();
            sb.append(reference.pullName());
            if (it.hasNext()) {
                sb.append(SEP);
            }
        }
        sb.append(CLOSE);

        return sb.toString();
    }

    private class BasicMarkerWorker {
        private int q;

        public BasicMarkerWorker(int q) {
            this.q = q;
        }

        public boolean invoke() {
            referenceList.remove(q);
            return true;
        }
    }

    private class BasicMarkerEngine {
        private boolean myResult;
        private Marker other;
        private int a;

        public BasicMarkerEngine(Marker other, int a) {
            this.other = other;
            this.a = a;
        }

        boolean is() {
            return myResult;
        }

        public boolean invoke() {
            Marker ref = referenceList.get(a);
            if (ref.contains(other)) {
                return true;
            }
            return false;
        }
    }
}
