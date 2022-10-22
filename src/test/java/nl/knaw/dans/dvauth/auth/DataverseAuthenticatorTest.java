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
import nl.knaw.dans.dvauth.core.BuiltinUser;
import nl.knaw.dans.dvauth.core.PasswordValidator;
import nl.knaw.dans.dvauth.db.DataverseDao;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataverseAuthenticatorTest {

    final DataverseDao dataverseDao = Mockito.mock(DataverseDao.class);
    final PasswordValidator passwordValidator = Mockito.mock(PasswordValidator.class);

    @Test
    void authenticate() throws AuthenticationException {
        var authenticator = new DataverseAuthenticator(dataverseDao, passwordValidator);
        var credentials = new BasicCredentials("user", "pass");
        var result = authenticator.authenticate(credentials);

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticateSuccess() throws AuthenticationException {
        var authenticator = new DataverseAuthenticator(dataverseDao, passwordValidator);
        var credentials = new BasicCredentials("user", "pass");

        Mockito.when(dataverseDao.findUserByName(Mockito.anyString()))
            .thenReturn(Optional.of(new BuiltinUser("pass", 1, "user")));

        Mockito.when(passwordValidator.validatePassword(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
            .thenReturn(true);

        var result = authenticator.authenticate(credentials);

        assertTrue(result.isPresent());
    }

    @Test
    void authenticateFailedUserNotFound() throws AuthenticationException {
        var authenticator = new DataverseAuthenticator(dataverseDao, passwordValidator);
        var credentials = new BasicCredentials("user", "pass");

        Mockito.when(dataverseDao.findUserByName(Mockito.anyString()))
            .thenReturn(Optional.empty());

        Mockito.when(passwordValidator.validatePassword(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
            .thenReturn(true);

        var result = authenticator.authenticate(credentials);

        assertFalse(result.isPresent());
    }

    @Test
    void authenticateFailedPasswordWrong() throws AuthenticationException {
        var authenticator = new DataverseAuthenticator(dataverseDao, passwordValidator);
        var credentials = new BasicCredentials("user", "pass");

        Mockito.when(dataverseDao.findUserByName(Mockito.anyString()))
            .thenReturn(Optional.of(new BuiltinUser("pass", 1, "user")));

        Mockito.when(passwordValidator.validatePassword(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
            .thenReturn(false);

        var result = authenticator.authenticate(credentials);

        assertFalse(result.isPresent());
    }

    @Test
    void authenticateThrowsException() {
        var authenticator = new DataverseAuthenticator(dataverseDao, passwordValidator);
        var credentials = new BasicCredentials("user", "pass");

        Mockito.when(dataverseDao.findUserByName(Mockito.anyString()))
            .thenThrow(new RuntimeException("Database error"));

        Mockito.when(passwordValidator.validatePassword(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
            .thenReturn(false);

        assertThrows(AuthenticationException.class, () ->
            authenticator.authenticate(credentials));
    }

}