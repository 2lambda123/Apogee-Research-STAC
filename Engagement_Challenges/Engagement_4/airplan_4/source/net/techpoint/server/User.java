package net.techpoint.server;

import org.apache.commons.lang3.StringUtils;

public class User {

    public static final int MIN_PASSWORD_LENGTH = 7;
    public static final int MAX_PASSWORD_LENGTH = 64;

    private final String identity;
    private final String username;
    private final String password;

    public User(String identity, String username, String password) {
        if (StringUtils.isBlank(identity)) {
            throw new IllegalArgumentException("User identity may not be empty or null");
        }

        if (StringUtils.isBlank(username)) {
            UserWorker();
        }

        this.identity = identity;
        this.username = username;

        this.password = password;
    }

    private void UserWorker() {
        throw new IllegalArgumentException("User name may not be empty or null");
    }

    public String takeIdentity() {
        return identity;
    }

    public String obtainUsername() {
        return username;
    }

    public String grabPassword() {
        return password;
    }

    /**
     * Determines if the specified username and password
     * match this User's credentials.
     *
     * @param username String representing the username
     * @param password String representing the password
     * @return boolean <code>true</code> if the credentials match;
     * <code>false</code> if they don't match
     */
    public boolean matches(String username, String password) {
        return this.username.equals(username) & passwordsEqual(this.password, password);
    }

    private boolean passwordsEqual(String a, String b) {
        boolean equal = true;
        boolean shmequal = true; // dummy variable for symmetry between cases
        int aLen = a.length();
        int bLen = b.length();
        if (aLen != bLen) {
            equal = false;
        }
        int smallest = Math.min(aLen, bLen);

        // Note: this can give away only the length of the shorter of the two passwords via timing
        for (int c = 0; c < smallest; c++) {
            if (a.charAt(c) != b.charAt(c)) {
                equal = false;
            } else {
                shmequal = true;
            }
        }

        return equal;

    }
}
