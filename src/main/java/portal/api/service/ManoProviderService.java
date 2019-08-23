package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.MANOplatform;
import io.openslice.model.MANOprovider;
import portal.api.repo.ManoProvidersRepository;

@Service
public class ManoProviderService {
	

	@Autowired
	ManoProvidersRepository manoProvidersRepo;

	public List<MANOprovider> getMANOprovidersEnabledForOnboarding() {
		
		return (List<MANOprovider>) this.manoProvidersRepo.findAllEnabled() ;
	}

	public MANOprovider getMANOproviderByID(long id) {
		Optional<MANOprovider> o = this.manoProvidersRepo.findById(id);
		return o.orElse(null);
	}

	public MANOprovider updateMANOproviderInfo(MANOprovider c) {
		
		return this.manoProvidersRepo.save(c);
	}

	public void deleteMANOprovider(MANOprovider prev) {
		this.manoProvidersRepo.delete(prev);
		
	}

	public MANOprovider addMANOprovider(MANOprovider c) {
		return this.manoProvidersRepo.save(c);
	}

	public List<MANOprovider>  getMANOproviders() {
		return (List<MANOprovider>) this.manoProvidersRepo.findAll();
	}

}
