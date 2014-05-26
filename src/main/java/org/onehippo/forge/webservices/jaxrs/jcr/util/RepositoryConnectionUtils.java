/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.webservices.jaxrs.jcr.util;

import javax.jcr.Credentials;
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
            session = repository.login((Credentials) request.getAttribute(AuthenticationConstants.HIPPO_CREDENTIALS));
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
