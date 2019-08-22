package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.VxFMetadata;
import portal.api.repo.DeploymentDescriptorRepository;

@Service
public class DeploymentDescriptorService {

	@Autowired
	DeploymentDescriptorRepository ddRepo;




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




}
