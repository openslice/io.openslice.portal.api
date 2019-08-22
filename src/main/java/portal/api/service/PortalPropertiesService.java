package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.PortalProperty;
import portal.api.repo.PortalPropertiesRepository;

/**
 * @author ctranoris
 *
 */
@Service
public class PortalPropertiesService {


	@Autowired
	PortalPropertiesRepository propsRepo;

	public PortalProperty getPropertyByName(String aname) {
		Optional<PortalProperty> optionalUser = this.propsRepo.findByName( aname );
		return optionalUser.orElse(null);
	}

	public List<PortalProperty> getProperties() {
		
		return (List<PortalProperty>) propsRepo.findAll();
	}

	public PortalProperty getPropertyByID(Long propid) {
		Optional<PortalProperty> optionalUser = this.propsRepo.findById(propid);
		return optionalUser.orElse(null);
	}

	public PortalProperty updateProperty(PortalProperty p) {
		return propsRepo.save(p);
	}
}
