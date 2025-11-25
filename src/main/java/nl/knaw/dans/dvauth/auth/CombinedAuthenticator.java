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

import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

public class CombinedAuthenticator implements Authenticator<CombinedCredentials, AuthUser> {
    private final DataverseBasicAuthenticator dataverseBasicAuthenticator;
    private final DataverseTokenAuthenticator dataverseTokenAuthenticator;

    public CombinedAuthenticator(DataverseBasicAuthenticator dataverseBasicAuthenticator, DataverseTokenAuthenticator dataverseTokenAuthenticator) {
        this.dataverseBasicAuthenticator = dataverseBasicAuthenticator;
        this.dataverseTokenAuthenticator = dataverseTokenAuthenticator;
    }

    @Override
    public Optional<AuthUser> authenticate(CombinedCredentials credentials) throws AuthenticationException {
        var results = new ArrayList<Optional<AuthUser>>();

        // Only check the credentials if they are provided. If provided, they must all be correct
        if (credentials.getBasicCredentials() != null) {
            var basicResult = dataverseBasicAuthenticator.authenticate(credentials.getBasicCredentials());
            results.add(basicResult);
        }

        if (credentials.getHeaderCredentials() != null) {
            var headerResult = dataverseTokenAuthenticator.authenticate(credentials.getHeaderCredentials());
            results.add(headerResult);
        }

        if (results.isEmpty()) {
            return Optional.empty();
        }

        // one of the results is not correct
        if (results.contains(Optional.<AuthUser> empty())) {
            return Optional.empty();
        }

        // the first result will be returned
        return results.get(0);
    }

}
