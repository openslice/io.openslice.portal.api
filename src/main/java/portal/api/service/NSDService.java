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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.ExperimentMetadata;
import portal.api.repo.NSDsRepository;

@Service
public class NSDService {

	@Autowired
	NSDsRepository nsdRepo;

	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */
	public String getExperimentsEagerDataJson() throws JsonProcessingException {

		List<ExperimentMetadata> il = this.getExperiments();
		ObjectMapper mapper = new ObjectMapper();
        // Registering Hibernate5Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( il );
		
		return res;
	}
	
	public List<ExperimentMetadata> getExperiments() {
		return (List<ExperimentMetadata>) this.nsdRepo.findAll();
	}
	
	/**
	 * @param id
	 * @return a Json containing all data
	 * @throws JsonProcessingException
	 */
	public String getProductByIDEagerDataJson(long id) throws JsonProcessingException {
		
		ObjectMapper mapper = new ObjectMapper();
        //Registering Hibernate4Module to support lazy objects
        mapper.registerModule(new Hibernate5JakartaModule());
		
        ExperimentMetadata o = this.getProductByIDEagerData(id);
        
		String res = mapper.writeValueAsString( o );

		return res;
	}

	public ExperimentMetadata getProductByIDEagerData(long id) {
		Optional<ExperimentMetadata> o = this.nsdRepo.findByIdEager(id);

		return o.orElse(null);
	}

	/**
	 * @param id
	 * @return a Json containing all data
	 * @throws JsonProcessingException
	 */
	public String getProductByIDDataJson(long id) throws JsonProcessingException {
		
		ObjectMapper mapper = new ObjectMapper();
        //Registering Hibernate4Module to support lazy objects
        mapper.registerModule(new Hibernate5JakartaModule());
		
        ExperimentMetadata o = this.getProductByID(id);
        
		String res = mapper.writeValueAsString( o );

		return res;
	}
	
	public ExperimentMetadata getProductByID(long id) {

		Optional<ExperimentMetadata> o = this.nsdRepo.findById(id);

		return o.orElse(null);
	}
	
	public ExperimentMetadata updateProductInfo( ExperimentMetadata  refNSD) {
		return this.nsdRepo.save(refNSD);
	}

	public List<ExperimentMetadata> getPublishedNSDsByCategory(Long categoryid) {
		if ((categoryid != null) && (categoryid >= 0)) {
			return (List<ExperimentMetadata>) this.nsdRepo.getPublishedNSDsByCategory(categoryid);
		} else {
			return (List<ExperimentMetadata>) this.nsdRepo.getPublishedNSDs();
		}
	}

	public List<ExperimentMetadata> getdNSDsByCategory(Long categoryid) {
		if ((categoryid != null) && (categoryid >= 0)) {
			return (List<ExperimentMetadata>) this.nsdRepo.getNSDsByCategory(categoryid);
		} else {
			return (List<ExperimentMetadata>) this.nsdRepo.findAll();
		}
	}

	public List<ExperimentMetadata> gedNSDsByUserID(long userid) {
		return (List<ExperimentMetadata>) this.nsdRepo.getNSDsByUserID(userid);
	}

	public ExperimentMetadata getdNSDByUUID(String uuid) {
		Optional<ExperimentMetadata> o = this.nsdRepo.findByUUID( uuid );
		return o.orElse(null);
	}

	public void deleteProduct(ExperimentMetadata nsd) {
		this.nsdRepo.delete( nsd );
		
	}

	public ExperimentMetadata getNSDByName(String name) {
		Optional<ExperimentMetadata> o = this.nsdRepo.findByName( name );
		return o.orElse(null);
	}

	public ExperimentMetadata addExperimentMetadata(ExperimentMetadata c) {
		return this.nsdRepo.save(c);
	}
	
	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */
	public String addNSDMetadataEagerDataJson(ExperimentMetadata receivedVxFMetadata) throws JsonProcessingException {

		ExperimentMetadata vxfmetadata = this.addExperimentMetadata(receivedVxFMetadata);
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( vxfmetadata );
		
		return res;		
	}		
}
