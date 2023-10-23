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
import java.util.UUID;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.openslice.centrallog.client.CLevel;
import io.openslice.centrallog.client.CentralLogger;
import io.openslice.model.ConstituentVxF;
import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.DeploymentDescriptorStatus;
import io.openslice.model.DeploymentDescriptorVxFPlacement;
import io.openslice.model.ExperimentMetadata;
import io.openslice.model.ExperimentOnBoardDescriptor;
import io.openslice.model.Infrastructure;
import io.openslice.model.MANOprovider;
import io.openslice.model.PortalUser;
import jakarta.persistence.EntityManagerFactory;
//import portal.api.centrallog.CLevel;
//import portal.api.centrallog.CentralLogger;
import portal.api.repo.DeploymentDescriptorRepository;

@Service
public class DeploymentDescriptorService {

	@Autowired
	DeploymentDescriptorRepository ddRepo;

	@Autowired
    UsersService usersService;

	@Autowired
	NSDService nsdService;

	@Autowired
	VxFOBDService vxfOBDService;
		
	@Autowired
	InfrastructureService infrastructureService;
	


	@Autowired
	NSDOBDService nsdOBDService;

	@Autowired
	ManoProviderService manoProviderService;
	
	
	@Value("${spring.application.name}")
	private String compname;

	
	@Autowired
	VxFService vxfService;
	
	private SessionFactory  sessionFactory;

	@Autowired
	private CentralLogger centralLogger;



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




	@Transactional
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
	    // Open a new session
	    try (Session session = sessionFactory.openSession()) {
	        // Begin a transaction
	        session.beginTransaction();
	        DeploymentDescriptor dd = (DeploymentDescriptor) session.get(DeploymentDescriptor.class, id);
	        Hibernate.initialize(dd.getExperimentFullDetails());
	        if (dd.getExperimentFullDetails() != null) {
	            Hibernate.initialize(dd.getExperimentFullDetails().getExperimentOnBoardDescriptors());
	        }
	        Hibernate.initialize(dd.getVxfPlacements());
	        Hibernate.initialize(dd.getDeploymentDescriptorVxFInstanceInfo());
	        Hibernate.initialize(dd.getMentor().getRoles());
	        Hibernate.initialize(dd.getInfrastructureForAll().getRefSupportedImages());
	        Hibernate.initialize(dd.getExperiment().getConstituentVxF());
	        Hibernate.initialize(dd.getExperimentFullDetails().getCategories());
	        Hibernate.initialize(dd.getExperimentFullDetails().getExtensions());
	        Hibernate.initialize(dd.getExperimentFullDetails().getValidationJobs());
	        if (dd.getExperimentFullDetails() != null) {
	            dd.getExperimentFullDetails().getExperimentOnBoardDescriptors().size();  // This line forces initialization, consider revising if not necessary
	        }
	        session.getTransaction().commit();
	        return dd;
	    } catch (Exception e) {
	    	logger.error("getDeploymentByIdEager failed!");
	        throw e;  // Re-throw the exception (or handle it in some way)
	    }	        
	}

	/**
	 * @param id
	 * @return
	 */
	public DeploymentDescriptor getDeploymentByInstanceIdEager(String id) {
	    DeploymentDescriptor dd = null;
        dd = (DeploymentDescriptor) this.ddRepo.readDeploymentByInstanceID(id);
        return this.getDeploymentByID(dd.getId());
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
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( dd );
		
		return res;
	}

	/**
	 * @param d
	 * @return as json
	 * @throws JsonProcessingException
	 */
	public String getDeploymentByInstanceIdEagerDataJson( String id ) throws JsonProcessingException {

		logger.info("****************************"+id+"****************************");
		DeploymentDescriptor dd = this.getDeploymentByInstanceIdEager( id );
		ObjectMapper mapper = new ObjectMapper();
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
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
        mapper.registerModule(new Hibernate5JakartaModule()); 
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

	public List<DeploymentDescriptor> getDeploymentsByExperimentId(long id) {
		List<DeploymentDescriptor> DeploymentDescriptor_list = this.ddRepo.readDeploymentsByExperimentID(id);
		return DeploymentDescriptor_list;
	}

	public String getRunningInstantiatingAndTerminatingDeploymentsEagerDataJson() throws JsonProcessingException {

		List<DeploymentDescriptor> dds = this.getRunningInstantiatingAndTerminatingDeployments();
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( dds );
		
		return res;
	}
	
	public List<DeploymentDescriptor> getAllDeployments() {
		List<DeploymentDescriptor> DeploymentDescriptors = new ArrayList<>();
		List<DeploymentDescriptor> DeploymentDescriptor_list = this.ddRepo.readAllDeployments();
		for(DeploymentDescriptor d : DeploymentDescriptor_list)
		{
			d.getExperimentFullDetails();
			d.getInfrastructureForAll();			
			DeploymentDescriptors.add(d);
		}
		return DeploymentDescriptors;
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

	public String getAllDeploymentsEagerDataJson() throws JsonProcessingException {

		List<DeploymentDescriptor> dds = this.getAllDeployments();
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( dds );
		
		return res;
	}
	
	public String getDeploymentsToInstantiateEagerDataJson() throws JsonProcessingException {

		List<DeploymentDescriptor> dds = this.getDeploymentsToInstantiate();
		ObjectMapper mapper = new ObjectMapper();
		
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5JakartaModule()); 
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
        mapper.registerModule(new Hibernate5JakartaModule()); 
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
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( dds );
		
		return res;
	}

	public DeploymentDescriptor updateDeploymentByJSON(DeploymentDescriptor receivedDeployment) {

		//DeploymentDescriptor aDeployment = getDeploymentByID( receivedDeployment.getId() );
		DeploymentDescriptor aDeployment =  getDeploymentByIdEager( receivedDeployment.getId() );
		//logger.info("Existing deployment descriptor"+aDeployment.toJSON());
		logger.info("Received deployment descriptor"+receivedDeployment.toJSON());
		logger.info("Previous Status is :"+aDeployment.getStatus()+",New Status is:"+receivedDeployment.getStatus()+" and Instance Id is "+receivedDeployment.getInstanceId());
		
		aDeployment.setConstituentVnfrIps(receivedDeployment.getConstituentVnfrIps());		
		aDeployment.setConfigStatus(receivedDeployment.getConfigStatus());
		aDeployment.setDetailedStatus(receivedDeployment.getDetailedStatus());
		aDeployment.setOperationalStatus(receivedDeployment.getOperationalStatus());
		aDeployment.setNsr(receivedDeployment.getNsr());
		aDeployment.setNsLcmOpOccId(receivedDeployment.getNsLcmOpOccId());
		aDeployment.setNs_nslcm_details(receivedDeployment.getNs_nslcm_details());
		
		logger.info("Update VxF Instance Info");
		try
		{
			logger.info(receivedDeployment.getDeploymentDescriptorVxFInstanceInfo().toArray().toString());
			aDeployment.setDeploymentDescriptorVxFInstanceInfo(receivedDeployment.getDeploymentDescriptorVxFInstanceInfo());
		}
		catch(Exception e)
		{				
			logger.info("Update of Vxf Instance Info failed with " + e.getMessage());
		}
		if(receivedDeployment.getStatus() != aDeployment.getStatus() )
		{
			aDeployment.setStatus( receivedDeployment.getStatus() );
			centralLogger.log( CLevel.INFO, "Status change of deployment "+aDeployment.getName()+" to "+aDeployment.getStatus(), compname );
			logger.info( "Status change of deployment "+aDeployment.getName()+" to "+aDeployment.getStatus() );			
			aDeployment.setInstanceId( receivedDeployment.getInstanceId() );
			centralLogger.log( CLevel.INFO, "Instance Id of deployment set to"+aDeployment.getInstanceId(), compname );
			logger.info( "Instance Id of deployment set to"+aDeployment.getInstanceId() );			
			aDeployment.setFeedback( receivedDeployment.getFeedback() );
			centralLogger.log( CLevel.INFO, "Feedback of deployment set to "+aDeployment.getFeedback(), compname );
			logger.info( "Feedback of deployment set to "+aDeployment.getFeedback() );			
			aDeployment.getExperimentFullDetails();
			aDeployment.getInfrastructureForAll();
			logger.info("updateDeployment for id: " + aDeployment.getId());
				
			if( receivedDeployment.getStatus() == DeploymentDescriptorStatus.SCHEDULED && aDeployment.getInstanceId() == null)
			{
				for (ExperimentOnBoardDescriptor tmpExperimentOnBoardDescriptor : aDeployment.getExperimentFullDetails().getExperimentOnBoardDescriptors())
				{
					aDeployment.setStatus( receivedDeployment.getStatus() );
					centralLogger.log( CLevel.INFO, "Status change of deployment "+aDeployment.getName()+" to "+aDeployment.getStatus(), compname);
					logger.info( "Status change of deployment "+aDeployment.getName()+" to "+aDeployment.getStatus());							
					aDeployment = updateDeploymentDescriptor(aDeployment);
					logger.info("NS status change is now "+aDeployment.getStatus());															
					//BusController.getInstance().scheduleExperiment( aDeployment );								
				}
			}
			else if( receivedDeployment.getStatus() == DeploymentDescriptorStatus.RUNNING && aDeployment.getInstanceId() == null)
			{
				for (ExperimentOnBoardDescriptor tmpExperimentOnBoardDescriptor : aDeployment.getExperimentFullDetails().getExperimentOnBoardDescriptors())
				{
					aDeployment.setStatus( receivedDeployment.getStatus() );
					centralLogger.log( CLevel.INFO, "Status change of deployment "+aDeployment.getName()+" to "+aDeployment.getStatus(), compname);
					logger.info( "Status change of deployment "+aDeployment.getName()+" to "+aDeployment.getStatus());							
					aDeployment = updateDeploymentDescriptor(aDeployment);
					logger.info("NS status change is now "+aDeployment.getStatus());															
					//BusController.getInstance().deployExperiment( aDeployment );	
				}
			}
			else if( receivedDeployment.getStatus() == DeploymentDescriptorStatus.COMPLETED && aDeployment.getInstanceId() != null)
			{
				aDeployment.setStatus( receivedDeployment.getStatus() );
				centralLogger.log( CLevel.INFO, "Status change of deployment "+aDeployment.getName()+" to "+aDeployment.getStatus(), compname);
				logger.info( "Status change of deployment "+aDeployment.getName()+" to "+aDeployment.getStatus());							
				aDeployment = updateDeploymentDescriptor(aDeployment);
				logger.info("NS status change is now "+aDeployment.getStatus());															
				//BusController.getInstance().completeExperiment( aDeployment );						
			}
			else if( receivedDeployment.getStatus() == DeploymentDescriptorStatus.REJECTED && aDeployment.getInstanceId() == null)
			{
				aDeployment.setStatus( receivedDeployment.getStatus() );
				centralLogger.log( CLevel.INFO, "Status change of deployment "+aDeployment.getName()+" to "+aDeployment.getStatus(), compname);
				logger.info( "Status change of deployment "+aDeployment.getName()+" to "+aDeployment.getStatus());							
				aDeployment = updateDeploymentDescriptor(aDeployment);
				logger.info("NS status change is now "+aDeployment.getStatus());															
				//BusController.getInstance().rejectExperiment( aDeployment );
				logger.info("Deployment Rejected");				
			}
			else
			{
				//Do Nothing
				logger.info( "Inconsistent Status Change tried. Returning the Deployment unchanged.");					
				aDeployment = updateDeploymentDescriptor(aDeployment);
				//BusController.getInstance().updateDeploymentRequest(aDeployment);
			}
		} else {
			logger.info( "Previous status is the same so just update deployment info");					
			aDeployment = updateDeploymentDescriptor(aDeployment);
			//BusController.getInstance().updateDeploymentRequest(aDeployment);
		}
		return aDeployment;
	}
	
	public String updateDeploymentEagerDataJson(DeploymentDescriptor receivedDeployment) throws JsonProcessingException {

	    try (Session session = sessionFactory.openSession()) {  // Use try-with-resources for Session
	        Transaction tx = null;  // Declare a Transaction variable
	        try {
	            tx = session.beginTransaction();  // Start a new transaction
	            
	            DeploymentDescriptor dd = this.updateDeploymentByJSON(receivedDeployment);
	            ObjectMapper mapper = new ObjectMapper();
	            
	            // Registering Hibernate5JakartaModule to support lazy objects
	            // this will fetch all lazy objects of VxF before marshaling
	            mapper.registerModule(new Hibernate5JakartaModule());
	            String res = mapper.writeValueAsString(dd);
	            
	            tx.commit();  // Commit the transaction
	            return res;
	            
	        } catch (Exception e) {
	            if (tx != null && tx.isActive()) {
	                tx.rollback();  // Rollback the transaction in case of an exception
	            }
	            throw e;  // Re-throw the exception
	        }
	    }  // Session will be automatically closed after this block due to try-with-resources
	}
	

	@Transactional
	public DeploymentDescriptor createDeploymentRequest(DeploymentDescriptor depl) {
		PortalUser u;
		if ( depl.getOwner() == null ) {
			u = usersService.findByUsername("admin");
		} else {
			u = usersService.findByUsername( depl.getOwner().getUsername() );
		}
		
		if ( depl.getExperiment() != null ) {
			
		}
		
		String uuid = UUID.randomUUID().toString();
		depl.setUuid(uuid);
		depl.setDateCreated(new Date());
		
		if ( depl.getStatus() == null  ) {
			depl.setStatus(DeploymentDescriptorStatus.UNDER_REVIEW);			
		}


		ExperimentMetadata baseNSD = (ExperimentMetadata) nsdService.getProductByID(depl.getExperiment().getId());
		depl.setExperiment(baseNSD); // reattach from the DB model
		
		ExperimentOnBoardDescriptor uexpobd = nsdOBDService.getExperimentOnBoardDescriptorByID( depl.getObddescriptor_uuid().getId() );
		depl.setObddescriptor_uuid(uexpobd); // reattach from the DB model
		
		if (uexpobd == null) {
			logger.error("uexpobd is NULL. Cannot load VxFOnBoardedDescriptor");
		}
		
		
		logger.info("reattach InfrastructureForAll from the DB model");
		Infrastructure infrDefault = new Infrastructure();
		infrDefault.setVIMid("UNDEFINED-INFRASTRUCTUREID-INOPENSLICE");		
		if ( infrastructureService.getInfrastructures().size()>0 ) {
			infrDefault = infrastructureService.getInfrastructures().get(0); //first replace with any existing default infra			
		}
		
		MANOprovider provider = manoProviderService.getMANOproviderByID( uexpobd.getObMANOprovider().getId() ); //fetch one infra from provider		
		if ( provider!= null) {
			if ( provider.getVims() != null  && provider.getVims().size()>0 ){
				infrDefault = provider.getVims().get(0);
			}
		}		

		depl.setInfrastructureForAll(  infrDefault );

		logger.info("reattach Mentor from the DB model");
		depl.setMentor( usersService.findByUsername("admin") );
		
		logger.info("reattach DeploymentDescriptorVxFPlacement from the DB model");
		int member = 1;
		for (ConstituentVxF cvf : baseNSD.getConstituentVxF()) {
			DeploymentDescriptorVxFPlacement place = new DeploymentDescriptorVxFPlacement();
			place.setInfrastructure(infrDefault);
			place.setConstituentVxF(cvf);
			
//			ConstituentVxF constituentVxF = new ConstituentVxF();
//			constituentVxF.setVxfref( cvf.getVxfref() );
//			constituentVxF.setMembervnfIndex(member);
//			constituentVxF.setVnfdidRef( cvf.getVnfdidRef() );
//			place.setConstituentVxF(constituentVxF );
//			depl.getVxfPlacements().add(place);
		}
		
				
		depl.setOwner( u );
		return this.ddRepo.save( depl );
	}
	

	@Transactional
	public String createDeploymentRequestJson(DeploymentDescriptor depl) throws JsonProcessingException {
		DeploymentDescriptor dd = this.createDeploymentRequest( depl );
		ObjectMapper mapper = new ObjectMapper();		
        mapper.registerModule(new Hibernate5JakartaModule()); 
		String res = mapper.writeValueAsString( dd );		
		return res;
	}
	
}
