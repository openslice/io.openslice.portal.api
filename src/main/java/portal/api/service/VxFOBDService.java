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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.MANOprovider;
import io.openslice.model.VxFMetadata;
import io.openslice.model.VxFOnBoardedDescriptor;
import portal.api.repo.VxFOBDRepository;

@Service
public class VxFOBDService {
	
	@Autowired
	VxFOBDRepository vxfOBDRepository;
	@Autowired
	VxFService vxfService;
	@Autowired
	ManoProviderService mpService;
	
	private static final transient Log logger = LogFactory.getLog( VxFOBDService.class.getName());	

	public VxFOnBoardedDescriptor updateVxFOnBoardedDescriptor(VxFOnBoardedDescriptor obd) {
		//Optional<VxFOnBoardedDescriptor> o = vxfOBDRepository.
		return this.vxfOBDRepository.save( obd );
	}

	public VxFOnBoardedDescriptor getVxFOnBoardedDescriptorByID(long vxfobdid) {
		Optional<VxFOnBoardedDescriptor> o = vxfOBDRepository.findById( vxfobdid );
		return o.orElse(null);
	}

	public List<VxFOnBoardedDescriptor> getVxFOnBoardedDescriptors() {
		return (List<VxFOnBoardedDescriptor>) this.vxfOBDRepository.findAll();
	}

	public void deleteVxFOnBoardedDescriptor(VxFOnBoardedDescriptor entity) {
		this.vxfOBDRepository.delete(entity);
		
	}

	public VxFOnBoardedDescriptor updateVxFOBDByJSON(VxFOnBoardedDescriptor vxfOBD) {

		VxFOnBoardedDescriptor aVxFOBD = getVxFOnBoardedDescriptorByID( vxfOBD.getId() );														
		logger.info("Previous Status is :"+aVxFOBD.getOnBoardingStatus()+",New Status is:"+vxfOBD.getOnBoardingStatus()+" and Instance Id is "+vxfOBD.getId());
				
		aVxFOBD.setOnBoardingStatus(vxfOBD.getOnBoardingStatus()); 		
		aVxFOBD.setDeployId(vxfOBD.getDeployId());
		aVxFOBD.setVxfMANOProviderID(vxfOBD.getVxfMANOProviderID());
		aVxFOBD.setLastOnboarding(vxfOBD.getLastOnboarding());
		aVxFOBD.setFeedbackMessage(vxfOBD.getFeedbackMessage());
		aVxFOBD.setOnBoardingStatus(vxfOBD.getOnBoardingStatus());
		//aVxFOBD.getVxf().setCertified(vxfOBD.getVxf().isCertified());
		logger.info("updateVxFODB for id: " + aVxFOBD.getId());				
		aVxFOBD = updateVxFOnBoardedDescriptor(aVxFOBD);
			
		return aVxFOBD;
	}
	
	
	public String updateVxFOBDEagerDataJson(VxFOnBoardedDescriptor receivedVxFOBD) throws JsonProcessingException {

		VxFOnBoardedDescriptor vxfobd = this.updateVxFOBDByJSON(receivedVxFOBD);
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( vxfobd );
		
		return res;
	}

	public VxFOnBoardedDescriptor addVxFOnBoardedDescriptor(VxFOnBoardedDescriptor aVxFOnBoardedDescriptor) {
		
		return this.vxfOBDRepository.save(aVxFOnBoardedDescriptor);
	}	
	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */
	public String addVxFOnBoardedDescriptorEagerDataJson(VxFOnBoardedDescriptor receivedVxFOBD) throws JsonProcessingException {

		receivedVxFOBD.setVxf(vxfService.getVxFById(receivedVxFOBD.getVxfid()));
		VxFOnBoardedDescriptor vxfobd = this.addVxFOnBoardedDescriptor(receivedVxFOBD);
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( vxfobd );
		
		return res;	
	}		
	
	public String getVxFOnBoardedDescriptorByVxFAndMP(String input)
	{
		String vnfd_id=input.split("##")[0];
		long mp_id = Long.parseLong(input.split("##")[1]);
		MANOprovider mp_obj=mpService.getMANOproviderByID(mp_id);
		VxFOnBoardedDescriptor tmp = this.vxfOBDRepository.findByVxFAndMP(vnfd_id,mp_obj);
		if(tmp!=null)
			return tmp.getUuid();
		else
			return null;
	}

	public List<VxFOnBoardedDescriptor> getVxFOnBoardedDescriptorListByMP(Long mp_id)
	{
		MANOprovider mp_obj=mpService.getMANOproviderByID(mp_id);
		List<VxFOnBoardedDescriptor> tmp = this.vxfOBDRepository.findByMP(mp_obj);
		if(tmp!=null)
			return tmp;
		else
			return null;
	}
	
	public List<VxFOnBoardedDescriptor> getVxFOnBoardedDescriptorList()
	{
		List<VxFOnBoardedDescriptor> tmp = this.vxfOBDRepository.findAll();
		if(tmp!=null)
			return tmp;
		else
			return null;
	}
	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */
	public String getVxFOnBoardedDescriptorListDataJson() throws JsonProcessingException {

		List<VxFOnBoardedDescriptor> tmp = this.getVxFOnBoardedDescriptorList();
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( tmp );
		
		return res;		
	}
	
	public VxFMetadata getVxFIdByVxFMANOProviderIDAndMP(String vxf_mp_id,long mp_id)
	{
		MANOprovider mp_obj=mpService.getMANOproviderByID(mp_id);
		VxFOnBoardedDescriptor tmp = this.vxfOBDRepository.VxFIdByVxFMANOProviderIDAndMP(vxf_mp_id,mp_obj);
		return tmp.getVxf();
	}
	
}
