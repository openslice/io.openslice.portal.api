package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.ExperimentMetadata;
import io.openslice.model.MANOprovider;
import io.openslice.model.VxFMetadata;
import portal.api.repo.ManoProvidersRepository;
import portal.api.repo.VxFsRepository;

@Service
public class VxFService {

	@Autowired
	VxFsRepository vxfsRepo;

	public VxFMetadata getProductByID(long id) {

		Optional<VxFMetadata> o = this.vxfsRepo.findById(id);

		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find VxFMetadata with id: " + id));
	}

	public VxFMetadata updateProductInfo(VxFMetadata refVxF) {
		return this.vxfsRepo.save(refVxF);
	}

	public List<VxFMetadata> getPublishedVxFsByCategory(Long categoryid) {
		if ((categoryid != null) && (categoryid >= 0)) {
			return (List<VxFMetadata>) this.vxfsRepo.getPublishedVxFsByCategory(categoryid);
		} else {
			return (List<VxFMetadata>) this.vxfsRepo.getPublishedVxF();
		}
	}

	public List<VxFMetadata> getVxFsByCategory(Long categoryid) {
		if ((categoryid != null) && (categoryid >= 0)) {
			return (List<VxFMetadata>) this.vxfsRepo.getVxFsByCategory(categoryid);			
		} else {
			return (List<VxFMetadata>) this.vxfsRepo.findAll();
		}
	}

	public List<VxFMetadata> getVxFsByUserID(long userid) {
		return (List<VxFMetadata>) this.vxfsRepo.getVxFsByUserID(userid);
	}

	public VxFMetadata getVxFtByUUID(String uuid) {
		Optional<VxFMetadata> o = this.vxfsRepo.findByUUID( uuid );
		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find VxFMetadata with id: " + uuid));
	}

	public void deleteProduct(VxFMetadata vxf) {
		this.vxfsRepo.delete( vxf );
		
	}

	public VxFMetadata getVxFByName(String name) {
		Optional<VxFMetadata> o = this.vxfsRepo.findByName( name );
		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find VxFMetadata with name: " + name));
	}

}
