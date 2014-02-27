package org.onehippo.forge.webservices.v1.jcr;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jreijn on 23/02/14.
 */
public class RepositoryConnectionUtils {

    private static Logger log = LoggerFactory.getLogger(RepositoryConnectionUtils.class);

    private RepositoryConnectionUtils() {
    }

    public static Session createSession(String username, String password) {
        try {
            final HippoRepository repository = HippoRepositoryFactory.getHippoRepository("vm://");
            return repository.login(username, password.toCharArray());
        } catch (RepositoryException e) {
            log.error("Error creating repository connection", e);
        }
        return null;
    }

    public static void cleanupSession(final Session session) {
        if (session != null && session.isLive()) {
            session.logout();
        }
    }

}
