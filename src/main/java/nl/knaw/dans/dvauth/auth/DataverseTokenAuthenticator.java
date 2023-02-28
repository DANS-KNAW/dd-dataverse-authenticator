/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.dvauth.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import nl.knaw.dans.dvauth.core.PasswordValidator;
import nl.knaw.dans.dvauth.db.DataverseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class DataverseTokenAuthenticator implements Authenticator<HeaderCredentials, AuthUser> {
    private static final Logger log = LoggerFactory.getLogger(DataverseTokenAuthenticator.class);

    private final DataverseDao dataverseDao;

    public DataverseTokenAuthenticator(DataverseDao dataverseDao) {
        this.dataverseDao = dataverseDao;
    }

    @Override
    public Optional<AuthUser> authenticate(HeaderCredentials value) throws AuthenticationException {
        log.info("Authenticating user with token");

        try {
            return this.dataverseDao.findUserByApiToken(value.getValue())
                .map(result -> {
                    var user = new AuthUser(result.getUsername());
                    log.info("User with name '{}' found", result.getUsername());
                    return user;
                });
        }
        catch (Exception e) {
            log.error("Unable to verify credentials", e);
            throw new AuthenticationException("Unable to verify credentials", e);
        }
    }
}
