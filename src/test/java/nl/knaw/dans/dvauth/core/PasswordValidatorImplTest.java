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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorImplTest {

    @Test
    void validatePassword() {
        var validator = new PasswordValidatorImpl();
        assertTrue(validator.validatePassword("user001", "hN/rof975YOfV0wcZVXCrpU8ZlY=", PasswordValidator.PasswordAlgorithm.SHA));
        assertTrue(validator.validatePassword("user002", "EwkfCo7O85qPEM/39U2hTw+3ehE=", PasswordValidator.PasswordAlgorithm.SHA));
        assertTrue(validator.validatePassword("user003", "CxMv+h/czMwZA554OwNVpibqxxA=", PasswordValidator.PasswordAlgorithm.SHA));
        assertTrue(validator.validatePassword("admin1", "$2a$10$nBBwLlls757bzXY30ts8duy1ymEJKhGxZgdWDBEOwmMkXXvv2C3CC", PasswordValidator.PasswordAlgorithm.BCRYPT));
    }

    @Test
    void validateInvalidPassword() {
        var validator = new PasswordValidatorImpl();
        assertFalse(validator.validatePassword("other", "hN/rof975YOfV0wcZVXCrpU8ZlY=", PasswordValidator.PasswordAlgorithm.SHA));
        assertFalse(validator.validatePassword("other", "$2a$10$nBBwLlls757bzXY30ts8duy1ymEJKhGxZgdWDBEOwmMkXXvv2C3CC", PasswordValidator.PasswordAlgorithm.BCRYPT));
    }

    @Test
    void validateNullPassword() {
        var validator = new PasswordValidatorImpl();
        assertThrows(NullPointerException.class, () -> validator.validatePassword("other", null, PasswordValidator.PasswordAlgorithm.SHA));
        assertThrows(NullPointerException.class, () -> validator.validatePassword(null, "value", PasswordValidator.PasswordAlgorithm.SHA));
        assertThrows(NullPointerException.class, () -> validator.validatePassword("other", "value", null));
    }
}