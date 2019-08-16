package portal.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.MANOprovider;
import portal.api.repo.ManoProvidersRepository;

@Service
public class ManoProviderService {
	

	@Autowired
	ManoProvidersRepository manoProvidersRepo;

	public List<MANOprovider> getMANOprovidersEnabledForOnboarding() {
		
		return (List<MANOprovider>) this.manoProvidersRepo.findAllEnabled() ;
	}

}
