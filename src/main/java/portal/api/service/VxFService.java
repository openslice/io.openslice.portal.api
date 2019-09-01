package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

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

		return o.orElse(null);
	}
	
	public String getProductByIDEagerData(long id) throws JsonProcessingException {

		
		ObjectMapper mapper = new ObjectMapper();
        //Registering Hibernate4Module to support lazy objects
		// this will fetch all lazy objects of VxF before marshaling
        mapper.registerModule(new Hibernate5Module());
		
        VxFMetadata o = this.getProductByID(id);        
		String res = mapper.writeValueAsString( o );
		return res;
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
		return o.orElse(null);
	}

	public void deleteProduct(VxFMetadata vxf) {
		this.vxfsRepo.delete( vxf );
		
	}

	public VxFMetadata getVxFByName(String name) {
		Optional<VxFMetadata> o = this.vxfsRepo.findByName( name );
		return o.orElse(null);
	}

}
