package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.ExperimentMetadata;
import io.openslice.model.Infrastructure;
import io.openslice.model.MANOprovider;
import io.openslice.model.Product;
import io.openslice.model.VxFMetadata;
import portal.api.repo.InfrastructureRepository;
import portal.api.repo.ManoProvidersRepository;
import portal.api.repo.ProductRepository;
import portal.api.repo.VxFsRepository;

@Service
public class InfrastructureService {

	@Autowired
	InfrastructureRepository infraRepo;


	public List<Infrastructure> getInfrastructures() {
		return (List<Infrastructure>) this.infraRepo.findAll();
	}

	public Infrastructure addInfrastructure(Infrastructure c) {
		return this.infraRepo.save(c);
	}

	public Infrastructure updateInfrastructureInfo(Infrastructure infrastructure) {
		return this.infraRepo.save(infrastructure);
	}

	public Infrastructure getInfrastructureByID( long infraid) {
		Optional<Infrastructure> o = this.infraRepo.findById(infraid);

		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find Infrastructure with id: " + infraid));
	}

	public void deleteInfrastructure( Infrastructure infrastructure ) {
		this.infraRepo.delete( infrastructure );
		
	}



}
