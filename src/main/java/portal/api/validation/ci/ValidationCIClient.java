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

package portal.api.validation.ci;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.openslice.model.VxFMetadata;

/**
 * @author ctranoris
 *
 */
public class ValidationCIClient {
		

	private static final transient Log logger = LogFactory.getLog(ValidationCIClient.class.getName());

	/** */
	private static ValidationCIClient instance;

	

	public static ValidationCIClient getInstance() {
		if (instance == null) {
			instance = new ValidationCIClient();
		}
		return instance;
	}
	
	
	public static void transformVxF2ValidationRequest(VxFMetadata vxf) {
		
	}

}
