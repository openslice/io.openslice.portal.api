package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.MANOprovider;
import io.openslice.model.VxFMetadata;
import portal.api.repo.ManoProvidersRepository;
import portal.api.repo.VxFsRepository;

@Service
public class VxFService {
	

	@Autowired
	VxFsRepository vxfsRepo;

	public VxFMetadata getProductByID( long id) {
		
		Optional<VxFMetadata> o = this.vxfsRepo.findById(id);
		
		return o
				.orElseThrow(() -> new ItemNotFoundException("Couldn't find VxFMetadata with id: " + id));
	}

	public VxFMetadata updateProductInfo(VxFMetadata refVxF) {
		return this.vxfsRepo.save( refVxF );
	}

}
