package portal.api.repo;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.MANOplatform;


/**
 * @author ctranoris
 *
 */
@Repository
public interface ManoPlatformRepository extends PagingAndSortingRepository<MANOplatform, Long> {


}
