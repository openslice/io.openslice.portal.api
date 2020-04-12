/*-
 * ========================LICENSE_START=================================
 * io.openslice.portal.api
 * %%
 * Copyright (C) 2019 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package portal.api.swagger2;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import springfox.documentation.builders.AuthorizationCodeGrantBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.service.TokenEndpoint;
import springfox.documentation.service.TokenRequestEndpoint;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Profile("!testing")
public class SwaggerConfig {
	

	@Value("${swagger.authserver}")
	private String AUTH_SERVER;
	@Value("${swagger.clientid}")
	private String CLIENT_ID;
	@Value("${swagger.clientsecret}")
	private String CLIENT_SECRET;
	
//    public static final String AUTH_SERVER = "http://localhost:13081/osapi-oauth-server";
//    public static final String CLIENT_ID = "fooClientIdPassword";
//    public static final String CLIENT_SECRET = "secret";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select()
        		.apis(RequestHandlerSelectors.any())
        		.paths(PathSelectors.any())
        		.build()
        		.securitySchemes(Arrays.asList(securityScheme()))
        		.securityContexts(Arrays.asList(securityContext()));
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
        		
        				    .realm("openslice")
        				    .clientId(CLIENT_ID)
        				    .clientSecret(CLIENT_SECRET)
        				    .appName("oauthtoken")
        				    .scopeSeparator(" ")
        		.build();
    }

    private SecurityScheme securityScheme() {
        GrantType grantType = new AuthorizationCodeGrantBuilder()
        		.tokenEndpoint(new TokenEndpoint(AUTH_SERVER + "/protocol/openid-connect/token", "oauthtoken"))
        		.tokenRequestEndpoint(
        		  new TokenRequestEndpoint(AUTH_SERVER + "/protocol/openid-connect/auth", CLIENT_ID, CLIENT_SECRET))
        		.build();

        SecurityScheme oauth = new OAuthBuilder().name("spring_oauth")
        		.grantTypes(Arrays.asList(grantType))
        		.scopes(Arrays.asList(scopes()))
        		.build();
        return oauth;
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
        		.securityReferences(
        		  Arrays.asList(new SecurityReference("spring_oauth", scopes())))
        		.forPaths(PathSelectors.regex("/admin.*"))
        		.build();
    }

    private AuthorizationScope[] scopes() {
        AuthorizationScope[] scopes = { 
          new AuthorizationScope("read", "for read operations"), 
          new AuthorizationScope("write", "for write operations"), 
          new AuthorizationScope("admin", "Access admin API"), 
          new AuthorizationScope("openapi", "Access openapi API") };
        return scopes;
    }
}
