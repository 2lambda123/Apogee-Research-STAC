package org.digitaltip.chatroom;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class JoinConferenceCompleter implements Completer {

    private HangIn withMi;

    public JoinConferenceCompleter(HangIn withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        TreeSet<String> names = new TreeSet<>();
        names.addAll(withMi.grabAllConferenceNames());

        if (buffer == null) {
            candidates.addAll(names);
        } else {
            completeExecutor(buffer, candidates, names);
        }

        if (candidates.size() == 1) {
            completeCoordinator(candidates);
        }
        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeCoordinator(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeExecutor(String buffer, List<CharSequence> candidates, TreeSet<String> names) {
        for (String match : names) {
            if (match.startsWith(buffer)) {
                candidates.add(match);
            }
        }
    }
}
