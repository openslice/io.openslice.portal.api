package portal.api.service;

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
}
