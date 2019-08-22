package portal.api.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.VxFMetadata;
import portal.api.mano.MANOController;
import portal.api.repo.DeploymentDescriptorRepository;

@Service
public class DeploymentDescriptorService {

	@Autowired
	DeploymentDescriptorRepository ddRepo;


	private static final transient Log logger = LogFactory.getLog( DeploymentDescriptorService.class.getName());


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




	public void deleteDeployment(DeploymentDescriptor entity) {
		this.ddRepo.delete(entity);		
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




	public List<DeploymentDescriptor> getRunningInstantiatingAndTerminatingDeployments() {
		List<DeploymentDescriptor> RunningDeploymentDescriptor_list = this.ddRepo.readRunningInstantiatingAndTerminatingDeployments();
		return RunningDeploymentDescriptor_list;
	}




}
