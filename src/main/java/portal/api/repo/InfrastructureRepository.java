package portal.api.repo;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.Infrastructure;


/**
 * @author ctranoris
 *
 */
@Repository
public interface InfrastructureRepository extends PagingAndSortingRepository<Infrastructure, Long> {



}
