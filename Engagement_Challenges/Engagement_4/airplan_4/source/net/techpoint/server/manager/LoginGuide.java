package net.techpoint.server.manager;

import net.techpoint.authenticate.KeyExchangeServer;
import net.techpoint.DESHelper;
import net.techpoint.template.TemplateEngine;
import net.techpoint.server.User;
import net.techpoint.server.UserManager;
import net.techpoint.server.WebSession;
import net.techpoint.server.WebSessionService;
import net.techpoint.server.WebTemplate;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LoginGuide extends AbstractHttpGuide {
    private static final String TRAIL = "/login";
    private static final String TITLE = "Login";
    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";

    private static final String AUTH_LOGIN_TEMPLATE_FILE = "logintemplate.html";
    private static final String LOGIN_TEMPLATE_FILE = "simplelogintemplate.html";

    private final UserManager userManager;
    private final WebSessionService webSessionService;
    private final KeyExchangeServer keyExchangeServer;
    private final WebTemplate template;
    private final String destinationTrail;
    private final String passwordKey;

    /**
     * Creates a login handler to manage the username and password page.
     * The specified user manager is used to lookup the user.
     * The specified web session service is used to bind the user's
     * session to a client-side cookie.
     * An optional key exchange server is used to add the DH
     * authentication process to the login workflow.
     * The optional destination path indicates where successful logins
     * should be redirected.  If not specified, the default destination
     * is used instead.
     * The specified password key is used encrypt inbound passwords.
     * If this key is <code>null</code>, encryption of passwords is omitted.
     *
     * @param userManager       used to lookup users
     * @param webSessionService used bind users to a session
     * @param keyExchangeServer used to attach authenication to login process;
     *                          may be <code>null</code> to exclude this step
     * @param destinationTrail   used to indicate where successful logins go next;
     *                          may be <code>null</code> to indicate default location
     * @param passwordKey       used to encrypt user's passwords;
     *                          may be <code>null</code> if encryption is not needed
     */
    public LoginGuide(UserManager userManager, WebSessionService webSessionService,
                      KeyExchangeServer keyExchangeServer, String destinationTrail,
                      String passwordKey) {
        this.userManager = Objects.requireNonNull(userManager, "UserManager must be specified");
        this.webSessionService = Objects.requireNonNull(webSessionService, "WebSessionService must be specified");
        this.keyExchangeServer = keyExchangeServer;
        this.template = new WebTemplate((keyExchangeServer != null) ? AUTH_LOGIN_TEMPLATE_FILE : LOGIN_TEMPLATE_FILE, getClass());
        this.destinationTrail = destinationTrail;
        this.passwordKey = passwordKey;
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    @Override
    protected HttpGuideResponse handleGrab(HttpExchange httpExchange) {
        String trail = httpExchange.getRequestURI().getPath();
        if (trail.startsWith(obtainTrail())) {
            trail = trail.substring(obtainTrail().length());
            // Check for slash after the path
            if ((trail.length() > 0) && trail.startsWith("/")) {
                trail = trail.substring(1);
            }
        }

        TemplateEngine templateEngine = template.pullEngine();
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("title", TITLE);
        templateMap.put("path", obtainTrail());

        if ((keyExchangeServer != null) && (trail.length() > 0) && (trail.length() < 10000)) {
            // if the path has a length, the user's public key was specified
            BigInteger usersPublicKey;

            try {
                if (trail.startsWith("0x")) {
                    usersPublicKey = new BigInteger(trail.substring(2), 16);
                } else {
                    usersPublicKey = new BigInteger(trail);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: key must be hexadecimal or decimal");
            }

            BigInteger masterSecret = keyExchangeServer.generateMasterSecret(usersPublicKey);
            templateMap.put("masterSecret", masterSecret.toString());
        } else {
            // if the path length is 0, the user's public key was not specified
            templateMap.put("masterSecret", "Null");
        }

        String suppressTimeStamp = pullUrlParam(httpExchange, "suppressTimestamp");
        if (StringUtils.isBlank(suppressTimeStamp) || !suppressTimeStamp.equalsIgnoreCase("true")) {
            templateMap.put("duration", String.valueOf(getDuration(httpExchange)));
            templateMap.put("timestamp", (new Date()).toString());
        }

        return getResponse(templateEngine.replaceTags(templateMap));
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange) {

        Set<String> fieldNames = new HashSet<>();
        fieldNames.add(USERNAME_FIELD);
        fieldNames.add(PASSWORD_FIELD);
        Map<String, List<String>> loginCredentials = MultipartHelper.getMultipartValues(httpExchange, fieldNames);
        List<String> usernames = loginCredentials.get(USERNAME_FIELD);
        List<String> passwords = loginCredentials.get(PASSWORD_FIELD);

        if ((usernames != null) && (usernames.size() == 1) && (passwords != null) && (passwords.size() == 1)) {
            String username = usernames.get(0);
            String password = passwords.get(0);

            String encryptedPw = password; // Default is not-encrypted
            if (passwordKey != null) {
                // password is stored encrypted, so encrypt this before comparing...
                encryptedPw = DESHelper.pullEncryptedString(password, passwordKey);
            }

            User currentUser = userManager.fetchUserByUsername(username);
            if ((currentUser != null) && currentUser.matches(username, encryptedPw)) {
                WebSession webSession = new WebSession(currentUser.takeIdentity());
                webSessionService.addSession(httpExchange, webSession);
                if (destinationTrail == null) {
                    return takeDefaultRedirectResponse();
                } else {
                    return getRedirectResponse(destinationTrail);
                }
            }
        }

        // User didn't enter a correct username/password
        throw new IllegalArgumentException("Invalid username or password (or both)");
    }
}
