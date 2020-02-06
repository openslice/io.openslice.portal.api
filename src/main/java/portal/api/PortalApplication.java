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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * Our API docs are at:
 * http://localhost:13000/osapi/swagger-ui.html
 * http://localhost:13000/osapi/v2/api-docs
 * 
 * @author ctranoris
 *
 */
@EntityScan( basePackages = {"io.openslice.model", "io.openslice.centrallog.client"})
@ComponentScan(basePackages = {	"io.openslice.centrallog.client"})
@SpringBootApplication()
public class PortalApplication{

	public static void main(String[] args) {
		SpringApplication.run(PortalApplication.class, args);
	}

}
