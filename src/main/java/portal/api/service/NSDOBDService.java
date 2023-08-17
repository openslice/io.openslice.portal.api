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

import io.openslice.model.ExperimentOnBoardDescriptor;
import portal.api.repo.NSDOBDRepository;

@Service
public class NSDOBDService {
	
	@Autowired
	NSDOBDRepository nsdOBDRepository;
	@Autowired
	NSDService nsdService;
	
	private static final transient Log logger = LogFactory.getLog( VxFOBDService.class.getName());	
	
	public ExperimentOnBoardDescriptor updateExperimentOnBoardDescriptor(ExperimentOnBoardDescriptor obd) {
		
		return nsdOBDRepository.save( obd );
	}

	public ExperimentOnBoardDescriptor getExperimentOnBoardDescriptorByID(long vxfobdid) {
		Optional<ExperimentOnBoardDescriptor> o = nsdOBDRepository.findById( vxfobdid );
		return o.orElse(null);
	}

	public List<ExperimentOnBoardDescriptor> getExperimentOnBoardDescriptors() {
		
		return (List<ExperimentOnBoardDescriptor>) this.nsdOBDRepository.findAll();
	}

	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */
	public String getExperimentOnBoardDescriptorsDataJson() throws JsonProcessingException {

		List<ExperimentOnBoardDescriptor> tmp = this.getExperimentOnBoardDescriptors();
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( tmp );
		
		return res;		
	}
	
	public void deleteExperimentOnBoardDescriptor(ExperimentOnBoardDescriptor u) {
		this.nsdOBDRepository.delete(u);
		
	}
	
	public ExperimentOnBoardDescriptor updateNSDOBDByJSON(ExperimentOnBoardDescriptor NSDOBD) {

		ExperimentOnBoardDescriptor aNSDOBD = getExperimentOnBoardDescriptorByID( NSDOBD.getId() );														
		logger.info("Previous Status is :"+aNSDOBD.getOnBoardingStatus()+",New Status is:"+NSDOBD.getOnBoardingStatus()+" and Instance Id is "+NSDOBD.getId());

		aNSDOBD.setExperimentMANOProviderID(NSDOBD.getExperimentMANOProviderID());
		aNSDOBD.setOnBoardingStatus(NSDOBD.getOnBoardingStatus()); 		
		aNSDOBD.setDeployId(NSDOBD.getDeployId());
		aNSDOBD.setLastOnboarding(NSDOBD.getLastOnboarding());
		aNSDOBD.setFeedbackMessage(NSDOBD.getFeedbackMessage());
		aNSDOBD.setOnBoardingStatus(NSDOBD.getOnBoardingStatus());
		aNSDOBD.setObMANOprovider(NSDOBD.getObMANOprovider());
		//aNSDOBD.getExperiment().setCertified(NSDOBD.getExperiment().isCertified());

		logger.info("updateExperimentODB for id: " + aNSDOBD.getId());				
		aNSDOBD = updateExperimentOnBoardDescriptor(aNSDOBD);

		return aNSDOBD;
	}


	public String updateNSDOBDEagerDataJson(ExperimentOnBoardDescriptor receivedExperimentOBD) throws JsonProcessingException {

		ExperimentOnBoardDescriptor vxfobd = this.updateNSDOBDByJSON(receivedExperimentOBD);
		ObjectMapper mapper = new ObjectMapper();

        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( vxfobd );

		return res;
	}	


	public ExperimentOnBoardDescriptor addExperimentOnBoardedDescriptor(ExperimentOnBoardDescriptor aNSDOnBoardedDescriptor) {
		
		return this.nsdOBDRepository.save(aNSDOnBoardedDescriptor);
	}	
	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */
	public String addExperimentOnBoardedDescriptorEagerDataJson(ExperimentOnBoardDescriptor receivedNSDOBD) throws JsonProcessingException {

		receivedNSDOBD.setExperiment(nsdService.getProductByID(receivedNSDOBD.getExperimentid()));
		ExperimentOnBoardDescriptor nsdobd = this.addExperimentOnBoardedDescriptor(receivedNSDOBD);
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( nsdobd );
		
		return res;	
	}
	
	public ExperimentOnBoardDescriptor getExperimentOnBoardedDescriptorByUUid(String UUid)
	{		
		return nsdOBDRepository.findByDeployId( UUid );
	}
}
