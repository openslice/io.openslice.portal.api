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

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.ExperimentMetadata;
import io.openslice.model.MANOprovider;
import io.openslice.model.VFImage;
import io.openslice.model.VxFMetadata;
import portal.api.repo.ManoProvidersRepository;
import portal.api.repo.VFImageRepository;
import portal.api.repo.VxFsRepository;

@Service
public class VFImageService {

	@Autowired
	VFImageRepository vfRepo;


	public VFImage getVFImageByName(String imageName) {
		Optional<VFImage> o = this.vfRepo.findByName( imageName );
		return o.orElse(null);
	}


	public VFImage saveVFImage(VFImage sm) {
		
		return this.vfRepo.save( sm ) ;
	}


	public VFImage updateVFImageInfo(VFImage img) {
		return this.vfRepo.save( img ) ;
		
	}


	public VFImage getVFImageByID( long vfimageid) {
		Optional<VFImage> o = this.vfRepo.findById( vfimageid );
		return o.orElse(null);
	}


	public List<VFImage> getVFImages() {		
		return (List<VFImage>) this.vfRepo.findAll();
	}


	public List<VFImage> getVFImagesByUserID(long id) {
		return (List<VFImage>) this.vfRepo.findAllByUserid( id );
	}


	public void deleteVFImage( VFImage sm ) {
		 this.vfRepo.delete( sm );
		
	}

}
