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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.ExperimentMetadata;


/**
 * @author ctranoris
 *
 */
@Repository
public interface NSDsRepository extends PagingAndSortingRepository<ExperimentMetadata, Long> {


	@Query( value = "SELECT a FROM ExperimentMetadata a JOIN a.categories ac WHERE a.published=TRUE AND ac.id=?1 ORDER BY a.name" ) //	
	Collection<ExperimentMetadata> getPublishedNSDsByCategory(Long categoryid);

	@Query( value = "SELECT a FROM ExperimentMetadata a WHERE a.published=TRUE ORDER BY a.name" ) //
	Collection<ExperimentMetadata> getPublishedNSDs();

	@Query( value = "SELECT a FROM ExperimentMetadata a JOIN a.categories ac WHERE ac.id=?1 ORDER BY a.name" ) //
	Collection<ExperimentMetadata> getNSDsByCategory(Long categoryid);

	@Query( value ="SELECT a FROM ExperimentMetadata a WHERE a.owner.id=?1 ORDER BY a.id" )
	Collection<ExperimentMetadata> getNSDsByUserID(long userid);

	@Query( value ="SELECT a FROM ExperimentMetadata a WHERE a.uuid=?1" )
	Optional<ExperimentMetadata> findByUUID(String uuid);

	@Query( value ="SELECT a FROM ExperimentMetadata a WHERE a.name LIKE ?1" )
	Optional<ExperimentMetadata> findByName(String name);

	@Query( value ="SELECT a FROM ExperimentMetadata a WHERE a.id LIKE ?1" )
	Optional<ExperimentMetadata> findById(long id);

	@Query( value ="SELECT e FROM ExperimentMetadata e JOIN FETCH e.experimentOnBoardDescriptors "
			+ "JOIN FETCH e.experimentOnBoardDescriptors obd  "
			+ "JOIN FETCH e.constituentVxF cvxf  "
			+ "JOIN FETCH obd.obMANOprovider  "
			+ "WHERE e.id = ?1" )
	Optional<ExperimentMetadata> findByIdEager(long id);


}
