package portal.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.VxFOnBoardedDescriptor;
import portal.api.repo.VxFOBDRepository;

@Service
public class VxFOBDService {
	
	@Autowired
	VxFOBDRepository vxfOBDRepository;

	public VxFOnBoardedDescriptor updateVxFOnBoardedDescriptor(VxFOnBoardedDescriptor obd) {
		//Optional<VxFOnBoardedDescriptor> o = vxfOBDRepository.
		return vxfOBDRepository.save( obd );
	}

	public VxFOnBoardedDescriptor getVxFOnBoardedDescriptorByID(long vxfobdid) {
		Optional<VxFOnBoardedDescriptor> o = vxfOBDRepository.findById( vxfobdid );
		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find VxFOnBoardedDescriptor with id: " + vxfobdid));
	}

	public List<VxFOnBoardedDescriptor> getVxFOnBoardedDescriptors() {
		return (List<VxFOnBoardedDescriptor>) this.vxfOBDRepository.findAll();
	}

	public void deleteVxFOnBoardedDescriptor(VxFOnBoardedDescriptor entity) {
		this.vxfOBDRepository.delete(entity);
		
	}

}
