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
package portal.api;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
	@Autowired
	Environment env;

	public MvcConfig() {
		super();
	}
	

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/index.html");

    }

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// registry.addResourceHandler("/testweb/**").addResourceLocations("file:///C:/Users/ctranoris/git/io.openslice.portal.web/src/").setCachePeriod(0);
		String a = ( new File("../io.openslice.portal.web/src/")).getAbsoluteFile().toURI().toString()  ;
		System.out.println("======================> " + a);
		registry.addResourceHandler("/testweb/**")
				.addResourceLocations( a ) //"file:///./../io.openslice.portal.web/src/")
				.setCachePeriod(0)
				.resourceChain(true)
				.addResolver(new EncodedResourceResolver())
				.addResolver(new PathResourceResolver());
		
		registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");

		registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");

	}
	

}
