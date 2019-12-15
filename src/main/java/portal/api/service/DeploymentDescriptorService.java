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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import io.openslice.model.DeploymentDescriptor;
import portal.api.repo.DeploymentDescriptorRepository;

@Service
public class DeploymentDescriptorService {

	@Autowired
	DeploymentDescriptorRepository ddRepo;


	private SessionFactory  sessionFactory;



	private static final transient Log logger = LogFactory.getLog( DeploymentDescriptorService.class.getName());


	/**
	 * from https://stackoverflow.com/questions/25063995/spring-boot-handle-to-hibernate-sessionfactory
	 * @param factory
	 */
	@Autowired
	public DeploymentDescriptorService(EntityManagerFactory factory) {
	    if(factory.unwrap(SessionFactory.class) == null){
	        throw new NullPointerException("factory is not a hibernate factory");
	      }
	      this.sessionFactory = factory.unwrap(SessionFactory.class);
	    }

	
	
	public List<DeploymentDescriptor> getAllCompletedDeploymentDescriptors() {
		return this.ddRepo.getAllCompletedDeploymentDescriptors();
	}




	public List<DeploymentDescriptor> getAllRejectedDeploymentDescriptors() {
		return this.ddRepo.getAllRejectedDeploymentDescriptors();
	}




	public List<DeploymentDescriptor> getAllFailedDeploymentDescriptors() {
		return this.ddRepo.getAllFailedDeploymentDescriptors();
	}




	public List<DeploymentDescriptor> getAllRemovedDeploymentDescriptors() {
		return this.ddRepo.getAllRemovedDeploymentDescriptors();
	}




	public List<DeploymentDescriptor> getAllDeploymentDescriptors() {
		return (List<DeploymentDescriptor>) this.ddRepo.readDeploymentDescriptors();
	}



	public List<DeploymentDescriptor> getAllDeploymentDescriptorsByUser(long id, String status) {
		
		if ( (status!=null) && status.equals("COMPLETED") ){
			return this.ddRepo.getAllByUserCompleted( id );
		}else if ( (status!=null) && status.equals("REJECTED") ){
			return this.ddRepo.getAllByUserRejected( id );
		}else if ( (status!=null) && status.equals("FAILED") ){
			return this.ddRepo.getAllByUserFAILED( id );
		}else if ( (status!=null) && status.equals("FAILED_OSM_REMOVED") ){
			return this.ddRepo.getAllByUserFAILED_OSM_REMOVEDd( id );			
		}
		return this.ddRepo.getAllByUserStatusUnknown( id );
	}


	public List<DeploymentDescriptor> getAllDeploymentDescriptorsByMentor(long id, String status) {
		if ( (status!=null) && status.equals("COMPLETED") ){
			return this.ddRepo.getAllByMentorCompleted( id );
		}else if ( (status!=null) && status.equals("REJECTED") ){
			return this.ddRepo.getAllByMentorRejected( id );
		}else if ( (status!=null) && status.equals("FAILED") ){
			return this.ddRepo.getAllByMentorFAILED( id );
		}else if ( (status!=null) && status.equals("FAILED_OSM_REMOVED") ){
			return this.ddRepo.getAllByMentorFAILED_OSM_REMOVEDd( id );			
		}
		return this.ddRepo.getAllByMentorStatusUnknown( id );
		
	}




	public List<DeploymentDescriptor> getAllDeploymentDescriptorsScheduled() {
		
		return this.ddRepo.getAllScheduled();
	}




	public DeploymentDescriptor updateDeploymentDescriptor(DeploymentDescriptor deployment) {
		return this.ddRepo.save(deployment);
	}




	public DeploymentDescriptor getDeploymentByID(long id) {

		Optional<DeploymentDescriptor> o = this.ddRepo.findById(id);

		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find DeploymentDescriptor with id: " + id));
	}
	



	/**
	 * @param id
	 * @return
	 */
	public DeploymentDescriptor getDeploymentByIdEager(long id) {
//		Optional<DeploymentDescriptor> o = this.ddRepo.findById(id);
//		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find DeploymentDescriptor with id: " + id));
		 Session session = sessionFactory.openSession();
		    Transaction tx = session.beginTransaction();
		    DeploymentDescriptor dd = null;
		    try {
		        dd = (DeploymentDescriptor) session.get(DeploymentDescriptor.class, id);
		        Hibernate.initialize( dd.getExperimentFullDetails() );
		        Hibernate.initialize( dd.getExperimentFullDetails().getExperimentOnBoardDescriptors() );
		        Hibernate.initialize( dd.getVxfPlacements() );		        
		        tx.commit();
		        dd.getExperimentFullDetails().getExperimentOnBoardDescriptors().size();
		    } finally {
		        session.close();
		    }
		    return dd;
	}

	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */
	public String getDeploymentByIdEagerDataJson( long id ) throws JsonProcessingException {

		DeploymentDescriptor dd = this.getDeploymentByIdEager( id );
		ObjectMapper mapper = new ObjectMapper();
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5Module()); 
		String res = mapper.writeValueAsString( dd );
		
		return res;
	}

	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */
	public String getDeploymentEagerDataJson( DeploymentDescriptor d ) throws JsonProcessingException {

		DeploymentDescriptor dd = this.getDeploymentByIdEager( d.getId() );
		ObjectMapper mapper = new ObjectMapper();
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5Module()); 
		String res = mapper.writeValueAsString( dd );
		
		return res;
	}
	
	public void deleteDeployment(DeploymentDescriptor entity) {
		this.ddRepo.delete(entity);		
	}

	public List<DeploymentDescriptor> getRunningInstantiatingAndTerminatingDeployments() {
		List<DeploymentDescriptor> RunningDeploymentDescriptor_list = this.ddRepo.readRunningInstantiatingAndTerminatingDeployments();
		return RunningDeploymentDescriptor_list;
	}

	public String getRunningInstantiatingAndTerminatingDeploymentsEagerDataJson() throws JsonProcessingException {

		List<DeploymentDescriptor> dds = this.getRunningInstantiatingAndTerminatingDeployments();
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5Module()); 
		String res = mapper.writeValueAsString( dds );
		
		return res;
	}
	
	public List<DeploymentDescriptor> getDeploymentsToInstantiate() {
		List<DeploymentDescriptor> DeploymentDescriptorsToRun = new ArrayList<>();
		List<DeploymentDescriptor> DeploymentDescriptor_list = this.ddRepo.readScheduledDeployments();
		for(DeploymentDescriptor d : DeploymentDescriptor_list)
		{
			d.getExperimentFullDetails();
			d.getInfrastructureForAll();			
			//if(d.getStartDate().before(new Date(System.currentTimeMillis())))
			OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
			if(d.getStartDate().before(Date.from(utc.toInstant())))
			{
				logger.info("Deployment "+d.getName()+" is scheduled to run at "+d.getStartDate()+". It will be Deployed now.");
				DeploymentDescriptorsToRun.add(d);
			}
		}
		return DeploymentDescriptorsToRun;
	}
	
	public String getDeploymentsToInstantiateEagerDataJson() throws JsonProcessingException {

		List<DeploymentDescriptor> dds = this.getDeploymentsToInstantiate();
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5Module()); 
		String res = mapper.writeValueAsString( dds );
		
		return res;
	}

	public List<DeploymentDescriptor> getDeploymentsToBeCompleted() {
		List<DeploymentDescriptor> DeploymentDescriptorsToComplete = new ArrayList<>();
		//List<DeploymentDescriptor> DeploymentDescriptor_list = portalJpaController.readRunningInstantiatingAndTerminatingDeployments();
		List<DeploymentDescriptor> DeploymentDescriptor_list = this.ddRepo.readRunningInstantiatingDeployments();		
		for(DeploymentDescriptor d : DeploymentDescriptor_list)
		{
			d.getExperimentFullDetails();
			d.getInfrastructureForAll();			
			OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
			if(d.getEndDate().before(Date.from(utc.toInstant())))
			{
				logger.info("Deployment "+d.getName()+" is scheduled to be COMPLETED now.");
				DeploymentDescriptorsToComplete.add(d);
			}
		}
		return DeploymentDescriptorsToComplete;
	}

	public String getDeploymentsToBeCompletedEagerDataJson() throws JsonProcessingException {

		List<DeploymentDescriptor> dds = this.getDeploymentsToBeCompleted();
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5Module()); 
		String res = mapper.writeValueAsString( dds );
		
		return res;
	}

	public List<DeploymentDescriptor> getDeploymentsToBeDeleted() {
		List<DeploymentDescriptor> deploymentDescriptorsToDelete = new ArrayList<>();
		List<DeploymentDescriptor> deploymentDescriptor_list = this.ddRepo.readDeploymentsToBeDeleted();
		for(DeploymentDescriptor d : deploymentDescriptor_list)
		{
			d.getExperimentFullDetails();
			d.getInfrastructureForAll();			
			OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
			if(d.getEndDate().before(Date.from(utc.toInstant())))
			{
				logger.info("Deployment id:" + d.getId() + ", name:"+ d.getName() + ", status:"+ d.getStatus()  +" is scheduled to be DELETED now.");
				deploymentDescriptorsToDelete.add(d);
			}
			
		}
		return deploymentDescriptorsToDelete;
	}

	public String getDeploymentsToBeDeletedEagerDataJson() throws JsonProcessingException {

		List<DeploymentDescriptor> dds = this.getDeploymentsToBeDeleted();
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5Module()); 
		String res = mapper.writeValueAsString( dds );
		
		return res;
	}
	
}
