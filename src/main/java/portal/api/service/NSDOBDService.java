package portal.api.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.openslice.model.ExperimentOnBoardDescriptor;
import io.openslice.model.VxFOnBoardedDescriptor;
import portal.api.repo.NSDOBDRepository;

@Service
public class NSDOBDService {
	
	@Autowired
	NSDOBDRepository nsdOBDRepository;

	public ExperimentOnBoardDescriptor updateExperimentOnBoardDescriptor(ExperimentOnBoardDescriptor obd) {
		
		return nsdOBDRepository.save( obd );
	}

	public ExperimentOnBoardDescriptor getVxFOnBoardedDescriptorByID(long vxfobdid) {
		Optional<ExperimentOnBoardDescriptor> o = nsdOBDRepository.findById( vxfobdid );
		return o.orElseThrow(() -> new ItemNotFoundException("Couldn't find ExperimentOnBoardDescriptor with id: " + vxfobdid));
	}


}
