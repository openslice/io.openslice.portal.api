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

import io.openslice.model.VFImage;
import io.openslice.model.VxFMetadata;


/**
 * @author ctranoris
 *
 */
@Repository
public interface VFImageRepository extends PagingAndSortingRepository<VFImage, Long> {


	@Query( value ="SELECT a FROM VFImage a WHERE a.name LIKE ?1" )
	Optional<VFImage> findByName(String name);

	
	@Query( value ="SELECT a FROM VFImage a JOIN a.owner aon WHERE aon.id LIKE ?1" )
	Collection<VFImage> findAllByUserid(long id);

}
