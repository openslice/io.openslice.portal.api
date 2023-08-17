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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.openslice.model.MANOprovider;
import portal.api.repo.ManoProvidersRepository;

@Service
public class ManoProviderService {
	

	@Autowired
	ManoProvidersRepository manoProvidersRepo;

	public List<MANOprovider> getMANOprovidersEnabledForOnboarding() {
		
		List<MANOprovider> tmp_mp_list = new ArrayList<MANOprovider>(); 
		for(MANOprovider tmp : this.manoProvidersRepo.findAllEnabled())
		{
			tmp.getVims();
			tmp_mp_list.add(tmp);
		}
			
		return tmp_mp_list;		
	}

	public List<MANOprovider>  getMANOprovidersForSync() {
		List<MANOprovider> tmp_mp_list = new ArrayList<MANOprovider>(); 
		for(MANOprovider tmp : this.manoProvidersRepo.findAllEnabledForSync())
		{
			tmp.getVims();
			tmp_mp_list.add(tmp);
		}
			
		return tmp_mp_list;				
	}


	@Transactional
	public MANOprovider getMANOproviderByID(long id) {
		Optional<MANOprovider> o = this.manoProvidersRepo.findById(id);
		try
		{
			return (MANOprovider) o.get().getVims();
		}
		catch(Exception e)
		{
			return o.orElse(null);
		}		
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
		// this will fetch all lazy objects of MANOprovider before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( dd );
		
		return res;
	}
	
	@Transactional
	public String getMANOprovidersEagerDataJson() throws JsonProcessingException {

		List<MANOprovider> mps = this.getMANOproviders();
		ObjectMapper mapper = new ObjectMapper();
        // Registering Hibernate5Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( mps );
		
		return res;
	}
	
	@Transactional
	public String getMANOprovidersForSyncEagerDataJson() throws JsonProcessingException {

		List<MANOprovider> mps = this.getMANOprovidersForSync();
		List<MANOprovider> tmp_mp_list = new ArrayList<MANOprovider>(); 
		for(MANOprovider tmp : mps)
		{
			tmp.getVims();
			tmp_mp_list.add(tmp);
		}		
		ObjectMapper mapper = new ObjectMapper();
        // Registering Hibernate5Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( tmp_mp_list );
		
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
		List<MANOprovider> tmp_mp_list = new ArrayList<MANOprovider>(); 
		for(MANOprovider tmp : this.manoProvidersRepo.findAll())
		{
			tmp.getVims();
			tmp_mp_list.add(tmp);
		}
			
		return tmp_mp_list;
	}
	
}
