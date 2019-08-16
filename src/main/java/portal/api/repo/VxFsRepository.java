package portal.api.repo;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.Category;
import io.openslice.model.VxFMetadata;


/**
 * @author ctranoris
 *
 */
@Repository
public interface VxFsRepository extends PagingAndSortingRepository<VxFMetadata, Long> {

}
