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

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
@SecurityScheme(name = "security_auth", type = SecuritySchemeType.OAUTH2, bearerFormat = "JWT", 
scheme = "bearer",
flows = @OAuthFlows(authorizationCode = @OAuthFlow(
		authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}"
		, tokenUrl = "${springdoc.oAuthFlow.tokenUrl}", scopes = {
		@OAuthScope(name = "read", description = "read scope"),
		@OAuthScope(name = "write", description = "write scope") })))
public class SwaggerConfig {
	

	@Value("${swagger.authserver}")
	private String AUTH_SERVER;
	@Value("${swagger.clientid}")
	private String CLIENT_ID;
	@Value("${swagger.clientsecret}")
	private String CLIENT_SECRET;
	

    
    @Bean
    public GroupedOpenApi customnfvportal(){
    	
      	SpringDocUtils.getConfig().replaceWithClass(java.time.LocalDate.class, java.sql.Date.class);
	  	SpringDocUtils.getConfig().replaceWithClass(java.time.OffsetDateTime.class, java.util.Date.class);
      return GroupedOpenApi.builder()
      		.group("nfv-portal.api.controller-v1.0.0")
    		.addOpenApiCustomizer( this.apiInfoPortalAPI() )
            .packagesToScan("portal.api.controller")
            .build();
      
    }
    
    OpenApiCustomizer apiInfoPortalAPI() {

		
		return openApi -> openApi
				.specVersion( SpecVersion.V30 ).addSecurityItem(new SecurityRequirement().addList("security_auth")) 
	              .info(new Info().title("NFV portal API")
	            		  .description("## NFV portal API")
	                      
		              .version("1.0.0")
		              .license(new License().name("Apache 2.0").url("http://openslice.io")));
    }
    
    
}
