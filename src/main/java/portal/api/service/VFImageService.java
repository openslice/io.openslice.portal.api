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
		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find VFImage with name: " + imageName));
	}


	public VFImage saveVFImage(VFImage sm) {
		
		return this.vfRepo.save( sm ) ;
	}


	public VFImage updateVFImageInfo(VFImage img) {
		return this.vfRepo.save( img ) ;
		
	}


	public VFImage getVFImageByID( long vfimageid) {
		Optional<VFImage> o = this.vfRepo.findById( vfimageid );
		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find VFImage with vfimageid: " + vfimageid));
	}

}
