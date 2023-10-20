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

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;

import org.checkerframework.checker.nullness.qual.Nullable;
import javax.annotation.Priority;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.HashSet;
import java.util.Objects;

@Priority(Priorities.AUTHENTICATION)
public class CombinedAuthenticationFilter<P extends Principal> extends AuthFilter<CombinedCredentials, P> {
    private String headerName;

    public CombinedAuthenticationFilter() {
    }

    public CombinedAuthenticationFilter(String headerName) {
        this.headerName = headerName;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var principals = getPrincipals(requestContext);

        // only allow a single authentication method
        if (countMethods(principals) > 1) {
            throw new BadRequestException("Only one of X-Dataverse-Key and Basic Authentication allowed per request");
        }

        // not sure what will break if we put our custom auth method in here, so lets stick with BASIC_AUTH
        if (!authenticate(requestContext, principals, SecurityContext.BASIC_AUTH)) {
            throw unauthorizedHandler.buildException(prefix, realm);
        }
    }

    private CombinedCredentials getPrincipals(ContainerRequestContext requestContext) {
        var result = new CombinedCredentials();
        var value = requestContext.getHeaders().getFirst(headerName);

        if (value != null) {
            result.setHeaderCredentials(new HeaderCredentials(value));
        }

        var basicCredentials = getBasicCredentials(requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));

        if (basicCredentials != null) {
            result.setBasicCredentials(basicCredentials);
        }

        return result;
    }

    // Copied from io.dropwizard.auth.basic.BasicCredentialAuthFilter
    @Nullable
    private BasicCredentials getBasicCredentials(String header) {
        if (header == null) {
            return null;
        }

        final int space = header.indexOf(' ');
        if (space <= 0) {
            return null;
        }

        final String method = header.substring(0, space);
        if (!prefix.equalsIgnoreCase(method)) {
            return null;
        }

        final String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(header.substring(space + 1)), StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException e) {
            logger.warn("Error decoding credentials", e);
            return null;
        }

        // Decoded credentials is 'username:password'
        final int i = decoded.indexOf(':');
        if (i <= 0) {
            return null;
        }

        final String username = decoded.substring(0, i);
        final String password = decoded.substring(i + 1);
        return new BasicCredentials(username, password);
    }

    private long countMethods(CombinedCredentials credentials) {
        var counter = new HashSet<>();
        counter.add(credentials.getHeaderCredentials());
        counter.add(credentials.getBasicCredentials());

        return counter.stream().filter(Objects::nonNull).count();
    }

    public static class Builder<P extends Principal> extends
        AuthFilterBuilder<CombinedCredentials, P, CombinedAuthenticationFilter<P>> {

        private String headerName;

        public Builder<P> setHeaderName(String headerName) {
            this.headerName = headerName;
            return this;
        }

        @Override
        protected CombinedAuthenticationFilter<P> newInstance() {
            return new CombinedAuthenticationFilter<>(headerName);
        }
    }
}
