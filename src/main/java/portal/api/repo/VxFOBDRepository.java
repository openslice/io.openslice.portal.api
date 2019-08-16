package portal.api.repo;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.VxFOnBoardedDescriptor;

/**
 * @author ctranoris
 *
 */
@Repository
public interface VxFOBDRepository extends PagingAndSortingRepository<VxFOnBoardedDescriptor, Long> {

}
