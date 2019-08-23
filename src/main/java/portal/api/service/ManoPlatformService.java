package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.MANOplatform;
import portal.api.repo.ManoPlatformRepository;

@Service
public class ManoPlatformService {
	

	@Autowired
	ManoPlatformRepository manoPlatformRepo;

	

	public MANOplatform getMANOplatformByID(long id) {
		Optional<MANOplatform> o = this.manoPlatformRepo.findById(id);
		return o.orElse(null);
	}



	public List<MANOplatform> getMANOplatforms() {
		return (List<MANOplatform>) this.manoPlatformRepo.findAll();
	}



	public MANOplatform addMANOplatform(MANOplatform c) {
		
		return this.manoPlatformRepo.save(c);
	}



	public void deleteMANOplatform(MANOplatform m) {
		this.manoPlatformRepo.delete(m);
		
	}



	public MANOplatform updateMANOplatformInfo(MANOplatform c) {
		return this.manoPlatformRepo.save(c);
	}

}
