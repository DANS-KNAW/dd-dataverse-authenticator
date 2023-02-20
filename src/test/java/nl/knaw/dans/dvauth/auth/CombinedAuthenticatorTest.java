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
import io.dropwizard.auth.basic.BasicCredentials;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombinedAuthenticatorTest {

    @Test
    void authenticate_should_return_empty_optional_if_no_methods_are_provided() throws Exception {
        var dataverseTokenAuthenticator = Mockito.mock(DataverseTokenAuthenticator.class);
        var dataverseBasicAuthenticator = Mockito.mock(DataverseBasicAuthenticator.class);
        var authenticator = new CombinedAuthenticator(dataverseBasicAuthenticator, dataverseTokenAuthenticator);

        var credentials = new CombinedCredentials(null, null);
        var result = authenticator.authenticate(credentials);

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_should_return_AuthUser_if_only_basic_is_provided() throws Exception {
        var dataverseTokenAuthenticator = Mockito.mock(DataverseTokenAuthenticator.class);
        var dataverseBasicAuthenticator = Mockito.mock(DataverseBasicAuthenticator.class);
        var authenticator = new CombinedAuthenticator(dataverseBasicAuthenticator, dataverseTokenAuthenticator);

        // here we test if 1 method (basic) is provided, if it returns the user
        // because the header credentials are empty, they will be ignored
        Mockito.doReturn(Optional.of(new AuthUser("user")))
            .when(dataverseBasicAuthenticator).authenticate(Mockito.any());

        var credentials = new CombinedCredentials(new BasicCredentials("user", "password"), null);
        var result = authenticator.authenticate(credentials);

        assertFalse(result.isEmpty());
    }

    @Test
    void authenticate_should_return_AuthUser_if_only_header_is_provided() throws Exception {
        var dataverseTokenAuthenticator = Mockito.mock(DataverseTokenAuthenticator.class);
        var dataverseBasicAuthenticator = Mockito.mock(DataverseBasicAuthenticator.class);
        var authenticator = new CombinedAuthenticator(dataverseBasicAuthenticator, dataverseTokenAuthenticator);

        // here we test if 1 method (basic) is provided, if it returns the user
        // because the basic credentials are empty, they will be ignored
        Mockito.doReturn(Optional.of(new AuthUser("user")))
            .when(dataverseTokenAuthenticator).authenticate(Mockito.any());

        var credentials = new CombinedCredentials(null, new HeaderCredentials("token"));
        var result = authenticator.authenticate(credentials);

        assertFalse(result.isEmpty());
    }

    @Test
    void authenticate_should_return_AuthUser_from_basic_auth_if_both_methods_succeed() throws Exception {
        var dataverseTokenAuthenticator = Mockito.mock(DataverseTokenAuthenticator.class);
        var dataverseBasicAuthenticator = Mockito.mock(DataverseBasicAuthenticator.class);
        var authenticator = new CombinedAuthenticator(dataverseBasicAuthenticator, dataverseTokenAuthenticator);

        Mockito.doReturn(Optional.of(new AuthUser("user1")))
            .when(dataverseTokenAuthenticator).authenticate(Mockito.any());

        Mockito.doReturn(Optional.of(new AuthUser("user2")))
            .when(dataverseBasicAuthenticator).authenticate(Mockito.any());

        var credentials = new CombinedCredentials(new BasicCredentials("user", "pass"),
            new HeaderCredentials("token"));

        var result = authenticator.authenticate(credentials);

        assertFalse(result.isEmpty());
        // user2 is returned by the basic authentication
        // TODO perhaps we should throw exceptions if the user names do not match?
        assertEquals("user2", result.get().getName());
    }

    @Test
    void authenticate_should_return_empty_optional_if_one_method_fails() throws Exception {
        var dataverseTokenAuthenticator = Mockito.mock(DataverseTokenAuthenticator.class);
        var dataverseBasicAuthenticator = Mockito.mock(DataverseBasicAuthenticator.class);
        var authenticator = new CombinedAuthenticator(dataverseBasicAuthenticator, dataverseTokenAuthenticator);

        // the token is provided, but does not match any users, so it will return an empty optional
        Mockito.doReturn(Optional.empty())
            .when(dataverseTokenAuthenticator).authenticate(Mockito.any());

        Mockito.doReturn(Optional.of(new AuthUser("user2")))
            .when(dataverseBasicAuthenticator).authenticate(Mockito.any());

        var credentials = new CombinedCredentials(new BasicCredentials("user", "pass"),
            new HeaderCredentials("token"));

        var result = authenticator.authenticate(credentials);

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_should_propagate_AuthenticationException_from_authenticators() throws Exception {
        var dataverseTokenAuthenticator = Mockito.mock(DataverseTokenAuthenticator.class);
        var dataverseBasicAuthenticator = Mockito.mock(DataverseBasicAuthenticator.class);
        var authenticator = new CombinedAuthenticator(dataverseBasicAuthenticator, dataverseTokenAuthenticator);

        // the token is provided, but does not match any users, so it will return an empty optional
        Mockito.doThrow(AuthenticationException.class)
            .when(dataverseTokenAuthenticator).authenticate(Mockito.any());

        Mockito.doReturn(Optional.of(new AuthUser("user2")))
            .when(dataverseBasicAuthenticator).authenticate(Mockito.any());

        var credentials = new CombinedCredentials(new BasicCredentials("user", "pass"),
            new HeaderCredentials("token"));

        assertThrows(AuthenticationException.class, () -> authenticator.authenticate(credentials));
    }
}