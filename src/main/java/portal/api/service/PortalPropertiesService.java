package portal.api.service;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private static final transient Log logger = LogFactory.getLog( PortalPropertiesService.class.getName() );
	
	public PortalProperty getPropertyByName(String aname) {
		Optional<PortalProperty> optionalUser = this.propsRepo.findByName( aname );
		return optionalUser.orElse(null);
	}

	public List<PortalProperty> getProperties() {
		
		return (List<PortalProperty>) propsRepo.findAll();
	}

	public PortalProperty getPropertyByID(long propid) {
		Optional<PortalProperty> optionalUser = this.propsRepo.findById(propid);
		return optionalUser.orElse(null);
	}

	public PortalProperty updateProperty(PortalProperty p) {
		return propsRepo.save(p);
	}
	
	@PostConstruct
	public void initRepo() {
		PortalProperty pn = null;
		try {
			pn = getPropertyByID(1);
			logger.info("======================== PortalProperty  = " + pn);
		} catch (Exception e) {
			logger.info("======================== PortalProperty NOT FOUND, initializing");			
		}

		if ( pn  == null) {
			PortalProperty p = new PortalProperty("adminEmail", "info@example.org");
			propsRepo.save(p);
			p = new PortalProperty("activationEmailSubject", "Activation Email Subject");
			propsRepo.save(p);
			p = new PortalProperty("mailhost", "example.org");
			propsRepo.save(p);
			p = new PortalProperty("mailuser", "exampleusername");
			propsRepo.save(p);
			p = new PortalProperty("mailpassword", "pass");
			propsRepo.save(p);
			p = new PortalProperty("maindomain", "https://portal.5ginfire.eu");
			propsRepo.save(p);
			p = new PortalProperty("bugzillaurl", "portal.5ginfire.eu:443/bugstaging");
			propsRepo.save(p);
			p = new PortalProperty("bugzillakey", "");
			propsRepo.save(p);
			p = new PortalProperty("jenkinsciurl", "ci.5ginfire.eu");
			propsRepo.save(p);
			p = new PortalProperty("jenkinscikey", "");
			propsRepo.save(p);
			p = new PortalProperty("pipelinetoken", "");
			propsRepo.save(p);
			p = new PortalProperty("centrallogerurl", "");
			propsRepo.save(p);
			p = new PortalProperty("portaltitle", "5GinFIRE");
			propsRepo.save(p);
			
		}
				
	}
}
