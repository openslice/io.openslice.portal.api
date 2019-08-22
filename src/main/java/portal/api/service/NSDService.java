package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.ExperimentMetadata;
import io.openslice.model.MANOprovider;
import portal.api.repo.ManoProvidersRepository;
import portal.api.repo.NSDsRepository;

@Service
public class NSDService {

	@Autowired
	NSDsRepository nsdRepo;

	public ExperimentMetadata getProductByID(long id) {

		Optional<ExperimentMetadata> o = this.nsdRepo.findById(id);

		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find ExperimentMetadata with id: " + id));
	}

	public ExperimentMetadata updateProductInfo( ExperimentMetadata  refNSD) {
		return this.nsdRepo.save(refNSD);
	}

	public List<ExperimentMetadata> getPublishedNSDsByCategory(Long categoryid) {
		if ((categoryid != null) && (categoryid >= 0)) {
			return (List<ExperimentMetadata>) this.nsdRepo.getPublishedNSDsByCategory(categoryid);
		} else {
			return (List<ExperimentMetadata>) this.nsdRepo.getPublishedNSDs();
		}
	}

	public List<ExperimentMetadata> getdNSDsByCategory(Long categoryid) {
		if ( categoryid >=0 ) {
			return (List<ExperimentMetadata>) this.nsdRepo.getNSDsByCategory(categoryid);
		} else {
			return (List<ExperimentMetadata>) this.nsdRepo.findAll();
		}
	}

	public List<ExperimentMetadata> gedNSDsByUserID(long userid) {
		return (List<ExperimentMetadata>) this.nsdRepo.getNSDsByUserID(userid);
	}

	public ExperimentMetadata getdNSDByUUID(String uuid) {
		Optional<ExperimentMetadata> o = this.nsdRepo.findByUUID( uuid );
		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find ExperimentMetadata with id: " + uuid));
	}

	public void deleteProduct(ExperimentMetadata nsd) {
		this.nsdRepo.delete( nsd );
		
	}

	public ExperimentMetadata getNSDByName(String name) {
		Optional<ExperimentMetadata> o = this.nsdRepo.findByName( name );
		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find ExperimentMetadata with name: " + name));
	}

}