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
package nl.knaw.dans.dvauth.core;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

public class PasswordValidatorImpl implements PasswordValidator {
    private static final Logger log = LoggerFactory.getLogger(PasswordValidatorImpl.class);

    @Override
    public boolean validatePassword(String plaintext, String hashed, PasswordAlgorithm algorithm) {
        Objects.requireNonNull(plaintext);
        Objects.requireNonNull(hashed);
        Objects.requireNonNull(algorithm);

        log.debug("Validating password with algorithm {}", algorithm);

        switch (algorithm) {
            case SHA:
                return validateShaPassword(plaintext, hashed);

            case BCRYPT:
                return validateBcryptPassword(plaintext, hashed);
        }

        return false;
    }

    private boolean validateBcryptPassword(String plaintext, String hashed) {
        return BCrypt.checkpw(plaintext, hashed);
    }

    private boolean validateShaPassword(String plaintext, String hashed) {
        try {
            var md = MessageDigest.getInstance("SHA");
            md.update(plaintext.getBytes(StandardCharsets.UTF_8));
            var hash = Base64.getEncoder().encodeToString(md.digest());
            return hash.equals(hashed);
        }
        catch (NoSuchAlgorithmException e) {
            log.error("Algorithm {} not found", "SHA", e);
            throw new RuntimeException(e);
        }
    }
}
