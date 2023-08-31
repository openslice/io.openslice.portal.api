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

import io.openslice.model.VxFMetadata;
import portal.api.repo.VxFsRepository;

@Service
public class VxFService {

	@Autowired
	VxFsRepository vxfsRepo;

	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */
	public String getVnfdsEagerDataJson() throws JsonProcessingException {

		List<VxFMetadata> il = this.getVxFs();
		ObjectMapper mapper = new ObjectMapper();
        // Registering Hibernate5Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( il );
		
		return res;
	}

	public List<VxFMetadata> getVxFs() {
		return (List<VxFMetadata>) this.vxfsRepo.findAll();
	}
	
	/**
	 * @param id
	 * @return a Json containing all data
	 * @throws JsonProcessingException
	 */
	public String getProductByIDDataJson(long id) throws JsonProcessingException {
	
		ObjectMapper mapper = new ObjectMapper();
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule());
		
        VxFMetadata o = this.getVxFById(id);        
		String res = mapper.writeValueAsString( o );
		return res;
	}

	/**
	 * @param id
	 * @return a Json containing all data
	 * @throws JsonProcessingException
	 */
	public String getProductByNameEagerDataJson(String name) throws JsonProcessingException {
	
		ObjectMapper mapper = new ObjectMapper();
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule());
		
        VxFMetadata o = this.getVxFByName(name);        
		String res = mapper.writeValueAsString( o );
		return res;
	}


	public VxFMetadata updateProductInfo(VxFMetadata refVxF) {
		return this.vxfsRepo.save(refVxF);
	}

	public List<VxFMetadata> getPublishedVxFsByCategory(Long categoryid) {
		if ((categoryid != null) && (categoryid >= 0)) {
			return (List<VxFMetadata>) this.vxfsRepo.getPublishedVxFsByCategory(categoryid);
		} else {
			return (List<VxFMetadata>) this.vxfsRepo.getPublishedVxF();
		}
	}

	public List<VxFMetadata> getVxFsByCategory(Long categoryid) {
		if ((categoryid != null) && (categoryid >= 0)) {
			return (List<VxFMetadata>) this.vxfsRepo.getVxFsByCategory(categoryid);			
		} else {
			return (List<VxFMetadata>) this.vxfsRepo.findAll();
		}
	}

	public List<VxFMetadata> getVxFsByUserID(long userid) {
		return (List<VxFMetadata>) this.vxfsRepo.getVxFsByUserID(userid);
	}

	public VxFMetadata getVxFByUUID(String uuid) {
		Optional<VxFMetadata> o = this.vxfsRepo.findByUUID( uuid );
		return o.orElse(null);
	}
	/**
	 * @param id
	 * @return a Json containing all data
	 * @throws JsonProcessingException
	 */
	public String getVxFByUUIDDataJson(String uuid) throws JsonProcessingException {
	
		ObjectMapper mapper = new ObjectMapper();
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule());
		
        VxFMetadata o = this.getVxFByUUID(uuid);        
		String res = mapper.writeValueAsString( o );
		return res;
	}

	public void deleteProduct(VxFMetadata vxf) {
		this.vxfsRepo.delete( vxf );
		
	}

	public VxFMetadata getVxFByName(String name) {
		Optional<VxFMetadata> o = this.vxfsRepo.findByName( name );
		return o.orElse(null);
	}
	
	public VxFMetadata getVxFById(long vxfId) {
		Optional<VxFMetadata> o = this.vxfsRepo.findById(vxfId);
		return o.orElse(null);
	}

	public List<VxFMetadata> getAllVxFByName(String name) {

		List<VxFMetadata> o = (List<VxFMetadata>) this.vxfsRepo.findAllByName(name);
		return o;
	}
	
	
	public VxFMetadata addVxFMetadata(VxFMetadata c) {
		return this.vxfsRepo.save(c);
	}
	
	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */
	public String addVxFMetadataEagerDataJson(VxFMetadata receivedVxFMetadata) throws JsonProcessingException {

		VxFMetadata vxfmetadata = this.addVxFMetadata(receivedVxFMetadata);
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( vxfmetadata );
		
		return res;		
	}	
}
