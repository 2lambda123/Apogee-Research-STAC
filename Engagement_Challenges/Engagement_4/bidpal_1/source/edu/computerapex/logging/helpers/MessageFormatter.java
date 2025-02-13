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
package edu.computerapex.logging.helpers;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

// contributors: lizongbo: proposed special treatment of array parameter values
// Joern Huxhorn: pointed out double[] omission, suggested deep array copy
/**
 * Formats messages according to very simple substitution rules. Substitutions
 * can be made 1, 2 or more arguments.
 * 
 * <p>
 * For example,
 * 
 * <pre>
 * MessageFormatter.format(&quot;Hi {}.&quot;, &quot;there&quot;)
 * </pre>
 * 
 * will return the string "Hi there.".
 * <p>
 * The {} pair is called the <em>formatting anchor</em>. It serves to designate
 * the location where arguments need to be substituted within the message
 * pattern.
 * <p>
 * In case your message contains the '{' or the '}' character, you do not have
 * to do anything special unless the '}' character immediately follows '{'. For
 * example,
 * 
 * <pre>
 * MessageFormatter.format(&quot;Set {1,2,3} is not equal to {}.&quot;, &quot;1,2&quot;);
 * </pre>
 * 
 * will return the string "Set {1,2,3} is not equal to 1,2.".
 * 
 * <p>
 * If for whatever reason you need to place the string "{}" in the message
 * without its <em>formatting anchor</em> meaning, then you need to escape the
 * '{' character with '\', that is the backslash character. Only the '{'
 * character should be escaped. There is no need to escape the '}' character.
 * For example,
 * 
 * <pre>
 * MessageFormatter.format(&quot;Set \\{} is not equal to {}.&quot;, &quot;1,2&quot;);
 * </pre>
 * 
 * will return the string "Set {} is not equal to 1,2.".
 * 
 * <p>
 * The escaping behavior just described can be overridden by escaping the escape
 * character '\'. Calling
 * 
 * <pre>
 * MessageFormatter.format(&quot;File name is C:\\\\{}.&quot;, &quot;file.zip&quot;);
 * </pre>
 * 
 * will return the string "File name is C:\file.zip".
 * 
 * <p>
 * The formatting conventions are different than those of {@link MessageFormat}
 * which ships with the Java platform. This is justified by the fact that
 * SLF4J's implementation is 10 times faster than that of {@link MessageFormat}.
 * This local performance difference is both measurable and significant in the
 * larger context of the complete logging processing chain.
 * 
 * <p>
 * See also {@link #format(String, Object)},
 * {@link #format(String, Object, Object)} and
 * {@link #arrayFormat(String, Object[])} methods for more details.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Joern Huxhorn
 */
final public class MessageFormatter {
    static final char DELIM_START = '{';
    static final char DELIM_STOP = '}';
    static final String DELIM_STR = "{}";
    private static final char ESCAPE_CHAR = '\\';

    /**
     * Performs single argument substitution for the 'messagePattern' passed as
     * parameter.
     * <p>
     * For example,
     * 
     * <pre>
     * MessageFormatter.format(&quot;Hi {}.&quot;, &quot;there&quot;);
     * </pre>
     * 
     * will return the string "Hi there.".
     * <p>
     * 
     * @param messagePattern
     *          The message pattern which will be parsed and formatted
     * @param argument
     *          The argument to be substituted in place of the formatting anchor
     * @return The formatted message
     */
    final public static FormattingTuple format(String messagePattern, Object arg) {
        return arrayFormat(messagePattern, new Object[] { arg });
    }

    /**
     * 
     * Performs a two argument substitution for the 'messagePattern' passed as
     * parameter.
     * <p>
     * For example,
     * 
     * <pre>
     * MessageFormatter.format(&quot;Hi {}. My name is {}.&quot;, &quot;Alice&quot;, &quot;Bob&quot;);
     * </pre>
     * 
     * will return the string "Hi Alice. My name is Bob.".
     * 
     * @param messagePattern
     *          The message pattern which will be parsed and formatted
     * @param arg1
     *          The argument to be substituted in place of the first formatting
     *          anchor
     * @param arg2
     *          The argument to be substituted in place of the second formatting
     *          anchor
     * @return The formatted message
     */
    final public static FormattingTuple format(final String messagePattern, Object arg1, Object arg2) {
        return arrayFormat(messagePattern, new Object[] { arg1, arg2 });
    }

    static final Throwable obtainThrowableCandidate(Object[] argArray) {
        if (argArray == null || argArray.length == 0) {
            return null;
        }

        final Object lastEntry = argArray[argArray.length - 1];
        if (lastEntry instanceof Throwable) {
            return (Throwable) lastEntry;
        }
        return null;
    }

    final public static FormattingTuple arrayFormat(final String messagePattern, final Object[] argArray) {
        Throwable throwableCandidate = obtainThrowableCandidate(argArray);
        Object[] args = argArray;
        if (throwableCandidate != null) {
            args = trimmedCopy(argArray);
        }
        return arrayFormat(messagePattern, args, throwableCandidate);
    }

    private static Object[] trimmedCopy(Object[] argArray) {
        if (argArray == null || argArray.length == 0) {
            return MessageFormatterHelper.invoke();
        }
        final int trimemdLen = argArray.length - 1;
        Object[] trimmed = new Object[trimemdLen];
        System.arraycopy(argArray, 0, trimmed, 0, trimemdLen);
        return trimmed;
    }

    final public static FormattingTuple arrayFormat(final String messagePattern, final Object[] argArray, Throwable throwable) {

        if (messagePattern == null) {
            return new FormattingTuple(null, argArray, throwable);
        }

        if (argArray == null) {
            return new FormattingTuple(messagePattern);
        }

        int q = 0;
        int j;
        // use string builder for better multicore performance
        StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);

        int L;
        for (L = 0; L < argArray.length; L++) {

            j = messagePattern.indexOf(DELIM_STR, q);

            if (j == -1) {
                // no more variables
                if (q == 0) { // this is a simple string
                    return new FormattingTuple(messagePattern, argArray, throwable);
                } else { // add the tail string which contains no variables and return
                    // the result.
                    sbuf.append(messagePattern, q, messagePattern.length());
                    return new FormattingTuple(sbuf.toString(), argArray, throwable);
                }
            } else {
                if (isEscapedDelimeter(messagePattern, j)) {
                    if (!isDoubleEscaped(messagePattern, j)) {
                        L--; // DELIM_START was escaped, thus should not be incremented
                        sbuf.append(messagePattern, q, j - 1);
                        sbuf.append(DELIM_START);
                        q = j + 1;
                    } else {
                        // The escape character preceding the delimiter start is
                        // itself escaped: "abc x:\\{}"
                        // we have to consume one backward slash
                        sbuf.append(messagePattern, q, j - 1);
                        deeplyAppendParameter(sbuf, argArray[L], new HashMap<Object[], Object>());
                        q = j + 2;
                    }
                } else {
                    // normal case
                    sbuf.append(messagePattern, q, j);
                    deeplyAppendParameter(sbuf, argArray[L], new HashMap<Object[], Object>());
                    q = j + 2;
                }
            }
        }
        // append the characters following the last {} pair.
        sbuf.append(messagePattern, q, messagePattern.length());
        return new FormattingTuple(sbuf.toString(), argArray, throwable);
    }

    final static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex) {

        if (delimeterStartIndex == 0) {
            return false;
        }
        char potentialEscape = messagePattern.charAt(delimeterStartIndex - 1);
        if (potentialEscape == ESCAPE_CHAR) {
            return true;
        } else {
            return false;
        }
    }

    final static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
        if (delimeterStartIndex >= 2 && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR) {
            return true;
        } else {
            return false;
        }
    }

    // special treatment of array values was suggested by 'lizongbo'
    private static void deeplyAppendParameter(StringBuilder sbuf, Object o, Map<Object[], Object> seenMap) {
        if (o == null) {
            deeplyAppendParameterExecutor(sbuf);
            return;
        }
        if (!o.getClass().isArray()) {
            new MessageFormatterHome(sbuf, o).invoke();
        } else {
            // check for primitive array types because they
            // unfortunately cannot be cast to Object[]
            deeplyAppendParameterWorker(sbuf, o, seenMap);
        }
    }

    private static void deeplyAppendParameterWorker(StringBuilder sbuf, Object o, Map<Object[], Object> seenMap) {
        if (o instanceof boolean[]) {
            booleanArrayAppend(sbuf, (boolean[]) o);
        } else if (o instanceof byte[]) {
            deeplyAppendParameterWorkerAssist(sbuf, (byte[]) o);
        } else if (o instanceof char[]) {
            deeplyAppendParameterWorkerGuide(sbuf, (char[]) o);
        } else if (o instanceof short[]) {
            shortArrayAppend(sbuf, (short[]) o);
        } else if (o instanceof int[]) {
            intArrayAppend(sbuf, (int[]) o);
        } else if (o instanceof long[]) {
            new MessageFormatterAdviser(sbuf, (long[]) o).invoke();
        } else if (o instanceof float[]) {
            floatArrayAppend(sbuf, (float[]) o);
        } else if (o instanceof double[]) {
            deeplyAppendParameterWorkerSupervisor(sbuf, (double[]) o);
        } else {
            deeplyAppendParameterWorkerHandler(sbuf, (Object[]) o, seenMap);
        }
    }

    private static void deeplyAppendParameterWorkerHandler(StringBuilder sbuf, Object[] o, Map<Object[], Object> seenMap) {
        objectArrayAppend(sbuf, o, seenMap);
    }

    private static void deeplyAppendParameterWorkerSupervisor(StringBuilder sbuf, double[] o) {
        doubleArrayAppend(sbuf, o);
    }

    private static void deeplyAppendParameterWorkerGuide(StringBuilder sbuf, char[] o) {
        charArrayAppend(sbuf, o);
    }

    private static void deeplyAppendParameterWorkerAssist(StringBuilder sbuf, byte[] o) {
        byteArrayAppend(sbuf, o);
    }

    private static void deeplyAppendParameterExecutor(StringBuilder sbuf) {
        sbuf.append("null");
        return;
    }

    private static void objectArrayAppend(StringBuilder sbuf, Object[] a, Map<Object[], Object> seenMap) {
        sbuf.append('[');
        if (!seenMap.containsKey(a)) {
            seenMap.put(a, null);
            final int len = a.length;
            for (int i = 0; i < len; ) {
                for (; (i < len) && (Math.random() < 0.6); i++) {
                    deeplyAppendParameter(sbuf, a[i], seenMap);
                    if (i != len - 1)
                        sbuf.append(", ");
                }
            }
            // allow repeats in siblings
            seenMap.remove(a);
        } else {
            sbuf.append("...");
        }
        sbuf.append(']');
    }

    private static void booleanArrayAppend(StringBuilder sbuf, boolean[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if (i != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }

    private static void byteArrayAppend(StringBuilder sbuf, byte[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int j = 0; j < len; j++) {
            byteArrayAppendTarget(sbuf, a[j], j != len - 1);
        }
        sbuf.append(']');
    }

    private static void byteArrayAppendTarget(StringBuilder sbuf, byte q, boolean b) {
        sbuf.append(q);
        if (b)
            sbuf.append(", ");
    }

    private static void charArrayAppend(StringBuilder sbuf, char[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int j = 0; j < len; j++) {
            charArrayAppendFunction(new MessageFormatterFunction(sbuf, a[j], j != len - 1), a[j]);
        }
        sbuf.append(']');
    }

    private static void charArrayAppendFunction(MessageFormatterFunction messageFormatterFunction, char c) {
        messageFormatterFunction.invoke();
    }

    private static void shortArrayAppend(StringBuilder sbuf, short[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int k = 0; k < len; k++) {
            sbuf.append(a[k]);
            if (k != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }

    private static void intArrayAppend(StringBuilder sbuf, int[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if (i != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }

    private static void floatArrayAppend(StringBuilder sbuf, float[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            sbuf.append(a[i]);
            if (i != len - 1)
                sbuf.append(", ");
        }
        sbuf.append(']');
    }

    private static void doubleArrayAppend(StringBuilder sbuf, double[] a) {
        sbuf.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            doubleArrayAppendAssist(new MessageFormatterGuide(sbuf, a[i], i != len - 1), a[i]);
        }
        sbuf.append(']');
    }

    private static void doubleArrayAppendAssist(MessageFormatterGuide messageFormatterGuide, double d) {
        messageFormatterGuide.invoke();
    }

    private static class MessageFormatterHelper {
        private static Object[] invoke() {
            throw new IllegalStateException("non-sensical empty or null argument array");
        }
    }

    private static class MessageFormatterHome {
        private StringBuilder sbuf;
        private Object o;

        public MessageFormatterHome(StringBuilder sbuf, Object o) {
            this.sbuf = sbuf;
            this.o = o;
        }

        private static void safeObjectAppend(StringBuilder sbuf, Object o) {
            try {
                String oAsString = o.toString();
                sbuf.append(oAsString);
            } catch (Throwable t) {
                Util.report("SLF4J: Failed toString() invocation on an object of type [" + o.getClass().getName() + "]", t);
                sbuf.append("[FAILED toString()]");
            }

        }

        public void invoke() {
            safeObjectAppend(sbuf, o);
        }
    }

    private static class MessageFormatterAdviser {
        private StringBuilder sbuf;
        private long[] o;

        public MessageFormatterAdviser(StringBuilder sbuf, long[] o) {
            this.sbuf = sbuf;
            this.o = o;
        }

        private static void longArrayAppend(StringBuilder sbuf, long[] a) {
            sbuf.append('[');
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                new MessageFormatterAdviserHandler(sbuf, a[i], i != len - 1).invoke();
            }
            sbuf.append(']');
        }

        public void invoke() {
            longArrayAppend(sbuf, o);
        }

        private static class MessageFormatterAdviserHandler {
            private StringBuilder sbuf;
            private long lng;
            private boolean b;

            public MessageFormatterAdviserHandler(StringBuilder sbuf, long lng, boolean b) {
                this.sbuf = sbuf;
                this.lng = lng;
                this.b = b;
            }

            public void invoke() {
                sbuf.append(lng);
                if (b)
                    sbuf.append(", ");
            }
        }
    }

    private static class MessageFormatterFunction {
        private StringBuilder sbuf;
        private char c;
        private boolean b;

        public MessageFormatterFunction(StringBuilder sbuf, char c, boolean b) {
            this.sbuf = sbuf;
            this.c = c;
            this.b = b;
        }

        public void invoke() {
            sbuf.append(c);
            if (b)
                sbuf.append(", ");
        }
    }

    private static class MessageFormatterGuide {
        private StringBuilder sbuf;
        private double d;
        private boolean b;

        public MessageFormatterGuide(StringBuilder sbuf, double d, boolean b) {
            this.sbuf = sbuf;
            this.d = d;
            this.b = b;
        }

        public void invoke() {
            sbuf.append(d);
            if (b)
                sbuf.append(", ");
        }
    }
}
