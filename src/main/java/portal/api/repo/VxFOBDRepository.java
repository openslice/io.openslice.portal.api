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
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.MANOprovider;
import io.openslice.model.VxFOnBoardedDescriptor;

/**
 * @author ctranoris
 *
 */
@Repository
public interface VxFOBDRepository extends CrudRepository<VxFOnBoardedDescriptor, Long> {
	
	@Query( value ="SELECT a FROM VxFOnBoardedDescriptor a WHERE a.vxfMANOProviderID=?1 and a.obMANOprovider=?2 and a.onBoardingStatus=2" )
	VxFOnBoardedDescriptor findByVxFAndMP(String vxf, MANOprovider mp);

	@Query( value ="SELECT a FROM VxFOnBoardedDescriptor a WHERE a.obMANOprovider=?1" )
	List<VxFOnBoardedDescriptor> findByMP(MANOprovider mp);

	@Query( value ="SELECT a FROM VxFOnBoardedDescriptor a WHERE 1=1" )
	List<VxFOnBoardedDescriptor> findAll();

	@Query( value ="SELECT a FROM VxFOnBoardedDescriptor a WHERE a.vxfMANOProviderID=?1 and a.obMANOprovider=?2" )
	VxFOnBoardedDescriptor VxFIdByVxFMANOProviderIDAndMP(String vxf_id, MANOprovider mp);
}
