package portal.api.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.VFImage;
import io.openslice.model.VxFMetadata;


/**
 * @author ctranoris
 *
 */
@Repository
public interface DeploymentDescriptorRepository extends PagingAndSortingRepository<DeploymentDescriptor, Long> {


	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE "
			+ "( m.status=io.openslice.model.DeploymentDescriptorStatus.COMPLETED "
			+ " OR m.status=io.openslice.model.DeploymentDescriptorStatus.TERMINATED )"
			+ "ORDER BY m.id" )
	List<DeploymentDescriptor> getAllCompletedDeploymentDescriptors();

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE "
			+ "m.status=io.openslice.model.DeploymentDescriptorStatus.REJECTED ORDER BY m.id" )
	List<DeploymentDescriptor> getAllRejectedDeploymentDescriptors();

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE "
	+ "( m.status=io.openslice.model.DeploymentDescriptorStatus.FAILED ) "
	+ " ORDER BY m.id")
	List<DeploymentDescriptor> getAllFailedDeploymentDescriptors();

	@Query( value =  "SELECT m FROM DeploymentDescriptor m  WHERE "
			+ "( m.status=io.openslice.model.DeploymentDescriptorStatus.FAILED "			
			+ " OR m.status=io.openslice.model.DeploymentDescriptorStatus.FAILED_OSM_REMOVED "
			+ " OR m.status=io.openslice.model.DeploymentDescriptorStatus.DELETION_FAILED "
			+ " OR m.status=io.openslice.model.DeploymentDescriptorStatus.TERMINATION_FAILED ) "
			+ " ORDER BY m.id")
	List<DeploymentDescriptor> getAllRemovedDeploymentDescriptors();

	@Query(value = "SELECT m FROM DeploymentDescriptor m "
			+ " WHERE m.status<>io.openslice.model.DeploymentDescriptorStatus.COMPLETED "
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.TERMINATED "
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.REJECTED "
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.FAILED "
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.DELETION_FAILED "
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.TERMINATION_FAILED "				
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.FAILED_OSM_REMOVED "
			+ " ORDER BY m.id")
	List<DeploymentDescriptor> readDeploymentDescriptors();

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE m.owner.id=?1" 
			+ " AND (m.status=io.openslice.model.DeploymentDescriptorStatus.COMPLETED "
			+ " OR m.status=io.openslice.model.DeploymentDescriptorStatus.TERMINATED ) "
			+ " ORDER BY m.id" )
	List<DeploymentDescriptor> getAllByUserCompleted(long id);

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE m.owner.id=?1"  
			+ " AND m.status=io.openslice.model.DeploymentDescriptorStatus.REJECTED ORDER BY m.id")
	List<DeploymentDescriptor> getAllByUserRejected(long id);

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE m.owner.id=?1"  
						+ " AND ( m.status=io.openslice.model.DeploymentDescriptorStatus.FAILED_OSM_REMOVED " 			
						+ " OR m.status=io.openslice.model.DeploymentDescriptorStatus.FAILED"			
						+ " OR m.status=io.openslice.model.DeploymentDescriptorStatus.DELETION_FAILED "
						+ " OR m.status=io.openslice.model.DeploymentDescriptorStatus.TERMINATION_FAILED ) "
						+ " ORDER BY m.id")
	List<DeploymentDescriptor> getAllByUserFAILED_OSM_REMOVEDd(long id);

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE m.owner.id=?1" 
	+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.COMPLETED "
	+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.TERMINATED "
	+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.REJECTED "
	+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.FAILED "
	+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.DELETION_FAILED "
	+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.TERMINATION_FAILED "
	+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.FAILED_OSM_REMOVED "
	+ " ORDER BY m.id")
	List<DeploymentDescriptor> getAllByUserStatusUnknown(long id);

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE m.owner.id=?1"  
			+ " AND m.status=io.openslice.model.DeploymentDescriptorStatus.FAILED"			
			+ " ORDER BY m.id")
	List<DeploymentDescriptor> getAllByUserFAILED(long id);

	
	
	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE "
			+ "( m.mentor.id=?1 OR m.owner.id=?1)" 
			+ " AND (m.status=io.openslice.model.DeploymentDescriptorStatus.COMPLETED "
			+ " OR m.status=io.openslice.model.DeploymentDescriptorStatus.TERMINATED ) "
			+ " ORDER BY m.id")
	List<DeploymentDescriptor> getAllByMentorCompleted(long id);

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE "
			+ "( m.mentor.id=?1 OR m.owner.id=?1)" 
			+ " AND m.status=io.openslice.model.DeploymentDescriptorStatus.REJECTED ORDER BY m.id" )
	List<DeploymentDescriptor> getAllByMentorRejected(long id);

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE "
			+ "( m.mentor.id=?1 OR m.owner.id=?1)" 
			+ " AND m.status=io.openslice.model.DeploymentDescriptorStatus.FAILED"			
			+ " ORDER BY m.id")
	List<DeploymentDescriptor> getAllByMentorFAILED(long id);

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE "
			+ "( m.mentor.id=?1 OR m.owner.id=?1)"  
			+ " AND ( m.status=io.openslice.model.DeploymentDescriptorStatus.FAILED_OSM_REMOVED " 
			+ " OR m.status=io.openslice.model.DeploymentDescriptorStatus.DELETION_FAILED "
			+ " OR m.status=io.openslice.model.DeploymentDescriptorStatus.TERMINATION_FAILED ) "
			+ " ORDER BY m.id")
	List<DeploymentDescriptor> getAllByMentorFAILED_OSM_REMOVEDd(long id);

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE " 
			+ "( m.mentor.id=?1 OR m.owner.id=?1)"  
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.COMPLETED "
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.TERMINATED "
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.REJECTED "
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.FAILED "
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.DELETION_FAILED "
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.TERMINATION_FAILED "					
			+ " AND m.status<>io.openslice.model.DeploymentDescriptorStatus.FAILED_OSM_REMOVED "
			+ "ORDER BY m.id")
	List<DeploymentDescriptor> getAllByMentorStatusUnknown(long id);

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE m.status=portal.api.model.DeploymentDescriptorStatus.SCHEDULED ORDER BY m.id" )
	List<DeploymentDescriptor> getAllScheduled();

	

}
