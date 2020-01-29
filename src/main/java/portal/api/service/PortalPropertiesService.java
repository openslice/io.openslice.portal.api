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
package portal.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.PortalProperty;
import portal.api.repo.PortalPropertiesRepository;

/**
 * @author ctranoris
 *
 */
@Service
public class PortalPropertiesService {


	@Autowired
	PortalPropertiesRepository propsRepo;

	private static final transient Log logger = LogFactory.getLog( PortalPropertiesService.class.getName() );
	
	public PortalProperty getPropertyByName(String aname) {
		Optional<PortalProperty> optionalUser = this.propsRepo.findByName( aname );
		return optionalUser.orElse(null);
	}

	public List<PortalProperty> getProperties() {
		
		return (List<PortalProperty>) propsRepo.findAll();
	}

	public PortalProperty getPropertyByID(long propid) {
		Optional<PortalProperty> optionalUser = this.propsRepo.findById(propid);
		return optionalUser.orElse(null);
	}

	public PortalProperty updateProperty(PortalProperty p) {
		return propsRepo.save(p);
	}
	
	@PostConstruct
	public void initRepo() {
		PortalProperty pn = null;
		try {
			pn = getPropertyByID(1);
			logger.info("======================== PortalProperty  = " + pn);
		} catch (Exception e) {
			logger.info("======================== PortalProperty NOT FOUND, initializing");			
		}

		if ( pn  == null) {
			PortalProperty p = new PortalProperty("adminEmail", "info@example.org");
			propsRepo.save(p);
			p = new PortalProperty("activationEmailSubject", "OpenSlice Activation Email ");
			propsRepo.save(p);
			p = new PortalProperty("mailhost", "example.org");
			propsRepo.save(p);
			p = new PortalProperty("mailuser", "exampleusername");
			propsRepo.save(p);
			p = new PortalProperty("mailpassword", "pass");
			propsRepo.save(p);
			p = new PortalProperty("maindomain", "https://portal.example.org");
			propsRepo.save(p);
			p = new PortalProperty("jenkinsciurl", "ci.example.org");
			propsRepo.save(p);
			p = new PortalProperty("jenkinscikey", "");
			propsRepo.save(p);
			p = new PortalProperty("pipelinetoken", "");
			propsRepo.save(p);
			p = new PortalProperty("centrallogerurl", "");
			propsRepo.save(p);
			p = new PortalProperty("portaltitle", "OpenSlice Dev");
			propsRepo.save(p);
			
		}
				
	}

	public Map<String, String> getPropertiesAsMap() {
		Map<String, String> m = new HashMap<>();

		m.put( "maindomain" , getPropertyByName("maindomain").getValue() );
		m.put( "portaltitle" ,getPropertyByName("portaltitle").getValue() );
		m.put( "centrallogerurl" , getPropertyByName("centrallogerurl").getValue() );

		
		return m;
	}	
}
