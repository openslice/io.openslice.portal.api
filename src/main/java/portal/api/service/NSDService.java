package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import io.openslice.model.ExperimentMetadata;
import io.openslice.model.MANOprovider;
import io.openslice.model.VxFMetadata;
import portal.api.repo.ManoProvidersRepository;
import portal.api.repo.NSDsRepository;

@Service
public class NSDService {

	@Autowired
	NSDsRepository nsdRepo;

	public ExperimentMetadata getProductByID(long id) {

		Optional<ExperimentMetadata> o = this.nsdRepo.findById(id);

		return o.orElse(null);
	}
	

	public String getProductByIDEagerData(long id) throws JsonProcessingException {
		
		ObjectMapper mapper = new ObjectMapper();
        //Registering Hibernate4Module to support lazy objects
        mapper.registerModule(new Hibernate5Module());
		
        ExperimentMetadata o = this.getProductByID(id);
        
		String res = mapper.writeValueAsString( o );

		return res;
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
		if ((categoryid != null) && (categoryid >= 0)) {
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
		return o.orElse(null);
	}

	public void deleteProduct(ExperimentMetadata nsd) {
		this.nsdRepo.delete( nsd );
		
	}

	public ExperimentMetadata getNSDByName(String name) {
		Optional<ExperimentMetadata> o = this.nsdRepo.findByName( name );
		return o.orElse(null);
	}

}
