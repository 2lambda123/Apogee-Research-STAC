package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.template.TemplateEngine;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InviteHandler extends AbstractTemplateSnapBuddyHandler {

    private static final String PATH = "/invite";

    private static final String TITLE = "Invite Friends";

    private static final String FIELD_NAME = "invite";

    private static final TemplateEngine TEMPLATE = new  TemplateEngine("<tr>" + "    <td>" + "        <input type=\"checkbox\" name=\"" + FIELD_NAME + "\" value=\"{{identity}}\"> " + "    </td>" + "    <td><img src=\"{{photoURL}}\" alt=\"{{name}} Profile Photo\" class=\"snapshot\"/></td>" + "    <td>{{name}}</td>" + "</tr>");

    public InviteHandler(SnapService snapService) {
        super(snapService);
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected String getTitle(SnapContext context) {
        return TITLE;
    }

    @Override
    protected String getContents(SnapContext context) {
        ClassgetContents replacementClass = new  ClassgetContents(context);
        ;
        replacementClass.doIt0();
        return replacementClass.doIt1();
    }

    /**
     * Determines if the active Person can invite the somebody Person.
     * All Persons are permitted to be invited except for following:
     * <ul>
     * <li>A Person may not invite themself</li>
     * <li>A Person may not invite an existing friend</li>
     * <li>A Person may not invite someone who is already invited</li>
     * </ul>
     *
     * @param activePerson doing the inviting
     * @param invited      set of Persons who are already involved in an invitation
     * @param somebody     who would like to be invited
     * @return boolean true if somebody can be invited
     */
    private static boolean canInvite(Person activePerson, Set<Person> invited, Person somebody) {
        ClasscanInvite replacementClass = new  ClasscanInvite(activePerson, invited, somebody);
        ;
        return replacementClass.doIt0();
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        Person activePerson = getPerson(httpExchange);
        List<String> identities = MultipartHelper.getMultipartFieldItems(httpExchange, FIELD_NAME);
        for (String identity : identities) {
            Person invitedPerson = getSnapService().getPerson(identity);
            if (invitedPerson != null) {
                getSnapService().sendInvitation(activePerson, invitedPerson);
            }
        }
        return getDefaultRedirectResponse();
    }

    protected class ClassgetContents {

        public ClassgetContents(SnapContext context) {
            this.context = context;
        }

        private SnapContext context;

        private Person activePerson;

        private Map<String, String> map;

        private StringBuilder sb;

        private Set<Person> invited;

        public void doIt0() {
            if (context == null) {
                throw new  IllegalArgumentException("Context may not be null");
            }
            activePerson = context.getActivePerson();
            map = new  HashMap();
            sb = new  StringBuilder();
            sb.append("<form action=\"");
            sb.append(PATH);
            sb.append("\" method=\"post\" enctype=\"multipart/form-data\">");
            sb.append("<table>");
            invited = getSnapService().getInvitations(activePerson);
            for (String identity : getSnapService().getPeople()) {
                Person somebody = getSnapService().getPerson(identity);
                if (canInvite(activePerson, invited, somebody)) {
                    map.clear();
                    map.put("identity", somebody.getIdentity());
                    map.put("photoURL", getProfilePhotoUrl(somebody));
                    map.put("name", somebody.getName());
                    sb.append(TEMPLATE.replaceTags(map));
                }
            }
            sb.append("</table>");
            sb.append("<input type=\"submit\" value=\"Invite\" name=\"submit\" >");
            sb.append("</form>");
        }

        public String doIt1() {
            return sb.toString();
        }
    }

    private static class ClasscanInvite {

        public ClasscanInvite(Person activePerson, Set<Person> invited, Person somebody) {
            this.activePerson = activePerson;
            this.invited = invited;
            this.somebody = somebody;
        }

        private Person activePerson;

        private Set<Person> invited;

        private Person somebody;

        public boolean doIt0() {
            return ((somebody != null) && !activePerson.getIdentity().equals(somebody.getIdentity()) && !activePerson.getFriends().contains(somebody.getIdentity()) && !invited.contains(somebody));
        }
    }
}
