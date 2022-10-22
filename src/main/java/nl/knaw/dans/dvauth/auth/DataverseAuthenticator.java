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
import io.dropwizard.auth.basic.BasicCredentials;
import nl.knaw.dans.dvauth.core.PasswordValidator;
import nl.knaw.dans.dvauth.core.PasswordValidator.PasswordAlgorithm;
import nl.knaw.dans.dvauth.db.DataverseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class DataverseAuthenticator implements Authenticator<BasicCredentials, AuthUser> {
    private static final Logger log = LoggerFactory.getLogger(DataverseAuthenticator.class);

    private final DataverseDao dataverseDao;
    private final PasswordValidator passwordValidator;

    public DataverseAuthenticator(DataverseDao dataverseDao, PasswordValidator passwordValidator) {
        this.dataverseDao = dataverseDao;
        this.passwordValidator = passwordValidator;
    }

    @Override
    public Optional<AuthUser> authenticate(BasicCredentials basicCredentials) throws AuthenticationException {
        try {
            var username = basicCredentials.getUsername();
            var password = basicCredentials.getPassword();

            log.info("Authenticating user '{}'", username);

            return this.dataverseDao.findUserByName(username)
                .map(u -> {
                    var alg = getAlgorithm(u.getPasswordencryptionversion());

                    log.info("User with name '{}' found, algorithm to use is {}", username, alg);
                    if (alg != null) {
                        if (passwordValidator.validatePassword(password, u.getEncryptedpassword(), alg)) {
                            return new AuthUser(u.getUsername());
                        }
                    }
                    else {
                        log.warn("Unknown algorithm used, numerical identifier is {}", u.getPasswordencryptionversion());
                    }

                    return null;
                })
                .or(() -> {
                    log.info("User with name '{}' not found", username);
                    return Optional.empty();
                });
        }
        catch (Exception e) {
            log.error("Unable to verify credentials", e);
            throw new AuthenticationException("Unable to verify credentials", e);
        }
    }

    private PasswordAlgorithm getAlgorithm(int version) {
        switch (version) {
            case 0:
                return PasswordAlgorithm.SHA;
            case 1:
                return PasswordAlgorithm.BCRYPT;
            default:
                return null;
        }
    }
}
