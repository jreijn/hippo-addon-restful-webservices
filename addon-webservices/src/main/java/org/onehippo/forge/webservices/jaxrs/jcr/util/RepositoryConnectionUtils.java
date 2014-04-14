package org.onehippo.forge.webservices.jaxrs.jcr.util;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServletRequest;

import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.onehippo.forge.webservices.AuthenticationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryConnectionUtils {

    private static Logger log = LoggerFactory.getLogger(RepositoryConnectionUtils.class);

    private RepositoryConnectionUtils() {
    }

    public static Session createSession(HttpServletRequest request) throws LoginException {
        Session session = null;
        try {
            final HippoRepository repository = HippoRepositoryFactory.getHippoRepository();
            session = repository.login((SimpleCredentials) request.getAttribute(AuthenticationConstants.HIPPO_CREDENTIALS));
        } catch (LoginException le) {
            throw new LoginException(le);
        } catch (RepositoryException e) {
            log.error("An exception occurred: {}", e);
        }
        return session;
    }

    public static Session createSession(String username, String password) throws LoginException {
        Session session = null;
        try {
            final HippoRepository repository = HippoRepositoryFactory.getHippoRepository();
            session = repository.login(username, password.toCharArray());
        } catch (LoginException le) {
            throw new LoginException(le);
        } catch (RepositoryException e) {
            log.error("An exception occurred: {}", e);
        }
        return session;
    }

    public static void cleanupSession(final Session session) {
        if (session != null && session.isLive()) {
            session.logout();
        }
    }

}
