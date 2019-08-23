package portal.api.service;

import java.util.List;
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

	public ExperimentOnBoardDescriptor getExperimentOnBoardDescriptorByID(long vxfobdid) {
		Optional<ExperimentOnBoardDescriptor> o = nsdOBDRepository.findById( vxfobdid );
		return o.orElse(null);
	}

	public List<ExperimentOnBoardDescriptor> getExperimentOnBoardDescriptors() {
		
		return (List<ExperimentOnBoardDescriptor>) this.nsdOBDRepository.findAll();
	}

	public void deleteExperimentOnBoardDescriptor(ExperimentOnBoardDescriptor u) {
		this.nsdOBDRepository.delete(u);
		
	}

}
