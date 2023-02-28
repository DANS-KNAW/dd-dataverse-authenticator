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

package nl.knaw.dans.dvauth;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.dans.dvauth.auth.AuthUser;
import nl.knaw.dans.dvauth.auth.CombinedAuthenticator;
import nl.knaw.dans.dvauth.auth.DataverseBasicAuthenticator;
import nl.knaw.dans.dvauth.auth.DataverseTokenAuthenticator;
import nl.knaw.dans.dvauth.auth.CombinedAuthenticationFilter;
import nl.knaw.dans.dvauth.core.PasswordValidatorImpl;
import nl.knaw.dans.dvauth.db.DataverseDao;
import nl.knaw.dans.dvauth.resources.AuthCheckResource;

public class DdDataverseAuthenticatorApplication extends Application<DdDataverseAuthenticatorConfiguration> {

    public static void main(final String[] args) throws Exception {
        new DdDataverseAuthenticatorApplication().run(args);
    }

    @Override
    public String getName() {
        return "Dd Dataverse Authenticator";
    }

    @Override
    public void initialize(final Bootstrap<DdDataverseAuthenticatorConfiguration> bootstrap) {
        bootstrap.addBundle(new JdbiExceptionsBundle());
    }

    @Override
    public void run(final DdDataverseAuthenticatorConfiguration configuration, final Environment environment) {
        var factory = new JdbiFactory();
        var jdbi = factory.build(environment, configuration.getDataSourceFactory(), "dataverse");

        var dataverseDao = jdbi.onDemand(DataverseDao.class);
        var passwordValidator = new PasswordValidatorImpl();
        var dataverseTokenAuthenticator = new DataverseTokenAuthenticator(dataverseDao);
        var dataverseBasicAuthenticator = new DataverseBasicAuthenticator(dataverseDao, passwordValidator);

        environment.jersey().register(new AuthDynamicFeature(
            new CombinedAuthenticationFilter.Builder<AuthUser>()
                .setHeaderName("X-Dataverse-Key")
                .setRealm("Dataverse")
                .setAuthenticator(new CombinedAuthenticator(dataverseBasicAuthenticator, dataverseTokenAuthenticator))
                .buildAuthFilter()));

        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(AuthUser.class));
        environment.jersey().register(new AuthCheckResource());
    }

}
