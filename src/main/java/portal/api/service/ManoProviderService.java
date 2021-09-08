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

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.MANOplatform;
import io.openslice.model.MANOprovider;
import portal.api.repo.ManoProvidersRepository;

@Service
public class ManoProviderService {
	

	@Autowired
	ManoProvidersRepository manoProvidersRepo;

	public List<MANOprovider> getMANOprovidersEnabledForOnboarding() {
		
		return (List<MANOprovider>) this.manoProvidersRepo.findAllEnabled() ;
	}

	@Transactional
	public MANOprovider getMANOproviderByID(long id) {
		Optional<MANOprovider> o = this.manoProvidersRepo.findById(id);
		return o.orElse(null);
	}

	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */

	@Transactional
	public String getMANOproviderByIDEagerDataJson( long id ) throws JsonProcessingException {

		MANOprovider dd = this.getMANOproviderByID( id );
		ObjectMapper mapper = new ObjectMapper();
        // Registering Hibernate5Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5Module()); 
		String res = mapper.writeValueAsString( dd );
		
		return res;
	}
	
	@Transactional
	public String getMANOprovidersEagerDataJson() throws JsonProcessingException {

		List<MANOprovider> mps = this.getMANOproviders();
		ObjectMapper mapper = new ObjectMapper();
        // Registering Hibernate5Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5Module()); 
		String res = mapper.writeValueAsString( mps );
		
		return res;
	}
	

	public MANOprovider updateMANOproviderInfo(MANOprovider c) {
		
		return this.manoProvidersRepo.save(c);
	}

	public void deleteMANOprovider(MANOprovider prev) {
		this.manoProvidersRepo.delete(prev);
		
	}

	public MANOprovider addMANOprovider(MANOprovider c) {
		return this.manoProvidersRepo.save(c);
	}

	public List<MANOprovider>  getMANOproviders() {
		return (List<MANOprovider>) this.manoProvidersRepo.findAll();
	}

}
