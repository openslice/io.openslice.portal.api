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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import OSM5NBIClient.OSM5Client;
import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.DeploymentDescriptorStatus;
import io.openslice.model.ExperimentOnBoardDescriptor;
import io.openslice.model.OnBoardingStatus;
import io.openslice.model.VxFOnBoardedDescriptor;
//import portal.api.centrallog.CLevel;
//import portal.api.centrallog.CentralLogger;
import portal.api.repo.DeploymentDescriptorRepository;
import portal.api.repo.VxFOBDRepository;

@Service
public class VxFOBDService {
	
	@Autowired
	VxFOBDRepository vxfOBDRepository;
	
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
        mapper.registerModule(new Hibernate5Module()); 
		String res = mapper.writeValueAsString( vxfobd );
		
		return res;
	}
	
}
