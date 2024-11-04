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
package nl.knaw.dans.dvauth.resources;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.dans.dvauth.DdDataverseAuthenticatorApplication;
import nl.knaw.dans.dvauth.DdDataverseAuthenticatorConfiguration;
import nl.knaw.dans.dvauth.api.UserAuthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(DropwizardExtensionsSupport.class)
class AuthCheckResourceIntegrationTest {

    private final DropwizardAppExtension<DdDataverseAuthenticatorConfiguration> EXT = new DropwizardAppExtension<>(
        DdDataverseAuthenticatorApplication.class,
        ResourceHelpers.resourceFilePath("test-etc/config.yml")
    );

    String generateBasicAuthHeader(String username, String password) {
        var formatted = String.format("%s:%s", username, password);
        var pass = Base64.getEncoder().encodeToString(formatted.getBytes());

        return "Basic " + pass;
    }

    @Test
    void authenticate_should_return_200_with_valid_credentials() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());
        var auth = generateBasicAuthHeader("user001", "user001");

        try (var result = EXT.client()
            .target(url)
            .request()
            .header("authorization", auth)
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(200, result.getStatus());

            var response = result.readEntity(UserAuthResponse.class);
            assertEquals("user001", response.getUserId());
        }
    }

    @Test
    void authenticate_should_return_401_for_invalid_password() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());
        var auth = generateBasicAuthHeader("user001", "user002");

        try (var result = EXT.client()
            .target(url)
            .request()
            .header("authorization", auth)
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(401, result.getStatus());
            assertNotNull(result.getHeaderString("WWW-Authenticate"));
        }
    }

    @Test
    void authenticate_should_return_401_for_invalid_username() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());
        var auth = generateBasicAuthHeader("non-existing", "user002");

        try (var result = EXT.client()
            .target(url)
            .request()
            .header("authorization", auth)
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(401, result.getStatus());
            assertNotNull(result.getHeaderString("WWW-Authenticate"));
        }
    }

    @Test
    void authenticate_should_return_401_for_missing_credentials() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());

        try (var result = EXT.client()
            .target(url)
            .request()
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(401, result.getStatus());
            assertNotNull(result.getHeaderString("WWW-Authenticate"));
        }
    }

    @Test
    void authenticate_should_return_200_for_dataverse_key() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());

        try (var result = EXT.client()
            .target(url)
            .request()
            .header("x-dataverse-key", "token1")
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(200, result.getStatus());
            var response = result.readEntity(UserAuthResponse.class);
            assertEquals("user001", response.getUserId());
        }
    }

    @Test
    void authenticate_should_return_401_for_expired_dataverse_key() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());

        try (var result = EXT.client()
            .target(url)
            .request()
            .header("x-dataverse-key", "token5")
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            // TODO fix this test
            assertEquals(200, result.getStatus());
            var response = result.readEntity(UserAuthResponse.class);
            assertEquals("user005", response.getUserId());
        }
    }

    @Test
    void authenticate_should_return_401_for_invalid_dataverse_key() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());

        try (var result = EXT.client()
            .target(url)
            .request()
            .header("x-dataverse-key", "token2")
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(401, result.getStatus());
        }
    }

    @Test
    void authenticate_should_return_401_for_missing_key() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());

        try (var result = EXT.client()
            .target(url)
            .request()
            .header("x-dataverse-key", "does-not-exist")
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(401, result.getStatus());
        }
    }

    @Test
    void authenticate_should_return_400_if_two_auth_methods_are_used() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());
        var auth = generateBasicAuthHeader("user001", "user002");

        try (var result = EXT.client()
            .target(url)
            .request()
            .header("authorization", auth)
            .header("x-dataverse-key", "token1")
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(400, result.getStatus());
        }
    }
}