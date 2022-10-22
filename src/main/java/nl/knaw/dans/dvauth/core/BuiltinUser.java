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

public class BuiltinUser {
    private Integer id;
    private String encryptedpassword;
    private Integer passwordencryptionversion;
    private String username;

    public BuiltinUser(String encryptedpassword, Integer passwordencryptionversion, String username) {
        this.encryptedpassword = encryptedpassword;
        this.passwordencryptionversion = passwordencryptionversion;
        this.username = username;
    }

    public BuiltinUser() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEncryptedpassword() {
        return encryptedpassword;
    }

    public void setEncryptedpassword(String encryptedpassword) {
        this.encryptedpassword = encryptedpassword;
    }

    public Integer getPasswordencryptionversion() {
        return passwordencryptionversion;
    }

    public void setPasswordencryptionversion(Integer passwordencryptionversion) {
        this.passwordencryptionversion = passwordencryptionversion;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "BuiltinUser{" +
            "id=" + id +
            ", passwordencryptionversion=" + passwordencryptionversion +
            ", username='" + username + '\'' +
            '}';
    }
}
