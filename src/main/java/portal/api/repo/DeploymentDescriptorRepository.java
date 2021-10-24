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
package portal.api.repo;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.DeploymentDescriptor;


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

	@Query( value = "SELECT m FROM DeploymentDescriptor m  WHERE m.status=io.openslice.model.DeploymentDescriptorStatus.SCHEDULED ORDER BY m.id" )
	List<DeploymentDescriptor> getAllScheduled();
	
	
	@Query( value = "SELECT m FROM DeploymentDescriptor m WHERE m.status = io.openslice.model.DeploymentDescriptorStatus.TERMINATED "
			+ "OR m.status = io.openslice.model.DeploymentDescriptorStatus.FAILED "
			+ "OR m.status = io.openslice.model.DeploymentDescriptorStatus.TERMINATION_FAILED")
	List<DeploymentDescriptor> readDeploymentsToBeDeleted();

	@Query( value = "SELECT m FROM DeploymentDescriptor m")
	List<DeploymentDescriptor> readAllDeployments();

	@Query( value = "SELECT m FROM DeploymentDescriptor m WHERE m.status = io.openslice.model.DeploymentDescriptorStatus.SCHEDULED")
	List<DeploymentDescriptor> readScheduledDeployments();

	@Query( value = "SELECT m FROM DeploymentDescriptor m WHERE m.status = io.openslice.model.DeploymentDescriptorStatus.RUNNING "
			+ "OR m.status = io.openslice.model.DeploymentDescriptorStatus.INSTANTIATING "
			+ "OR m.status = io.openslice.model.DeploymentDescriptorStatus.TERMINATING")
	List<DeploymentDescriptor> readRunningInstantiatingDeployments();

	@Query( value = "SELECT m FROM DeploymentDescriptor m WHERE m.status = io.openslice.model.DeploymentDescriptorStatus.RUNNING "
			+ "OR m.status = io.openslice.model.DeploymentDescriptorStatus.INSTANTIATING "
			+ "OR m.status = io.openslice.model.DeploymentDescriptorStatus.TERMINATING")
	List<DeploymentDescriptor> readRunningInstantiatingAndTerminatingDeployments();

	@Query( value = "SELECT m FROM DeploymentDescriptor m WHERE m.experiment.id = ?1")
	List<DeploymentDescriptor> readDeploymentsByExperimentID(long id);	

	@Query( value = "SELECT m FROM DeploymentDescriptor m WHERE m.instanceId = ?1")
	DeploymentDescriptor readDeploymentByInstanceID(String id);	
	
}
