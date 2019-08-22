package portal.api.repo;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.ExperimentOnBoardDescriptor;

/**
 * @author ctranoris
 *
 */
@Repository
public interface NSDOBDRepository extends PagingAndSortingRepository<ExperimentOnBoardDescriptor, Long> {

}
