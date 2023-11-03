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
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.PortalUser;

/**
 * 
 * @author ctranoris
 * @see
 * https://www.amitph.com/spring-data-jpa-query-methods/, 
 * https://www.amitph.com/pagination-sorting-spring-data-jpa/, 
 * https://www.baeldung.com/spring-data-jpa-query,
 * for usefull methods on spring repository
 * 
 * 
 */
@Repository
public interface UsersRepository  extends CrudRepository<PortalUser, Long> {
	
	PortalUser findDistinctFirstByUsername( String username );

	@Query( value = "SELECT m FROM PortalUser m INNER JOIN m.roles r WHERE r = 'ROLE_MENTOR'" ) //
	Collection<PortalUser> findAllMentors();

	Optional<PortalUser> findByUsername(String username);

	Optional<PortalUser> findByEmail(String email); 
	
	

}
