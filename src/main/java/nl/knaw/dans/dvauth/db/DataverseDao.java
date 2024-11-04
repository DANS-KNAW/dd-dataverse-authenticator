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
package nl.knaw.dans.dvauth.db;

import nl.knaw.dans.dvauth.core.BuiltinUser;
import nl.knaw.dans.dvauth.core.TokenUser;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Optional;

public interface DataverseDao {

    @SqlQuery("select id, encryptedpassword, passwordencryptionversion, username from builtinuser where username = :username limit 1")
    @RegisterBeanMapper(BuiltinUser.class)
    Optional<BuiltinUser> findUserByName(@Bind("username") String username);

    @SqlQuery("select a.useridentifier as username from apitoken t join authenticateduser a on a.id = t.authenticateduser_id "
        + "where a.deactivated = false and t.disabled = false and t.expiretime > CURRENT_DATE and t.tokenstring = :token")
    @RegisterBeanMapper(TokenUser.class)
    Optional<TokenUser> findUserByApiToken(@Bind("token") String token);
}
