package portal.api.repo;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.Category;
import io.openslice.model.MANOprovider;


/**
 * @author ctranoris
 *
 */
@Repository
public interface ManoProvidersRepository extends PagingAndSortingRepository<MANOprovider, Long> {

	@Query( value="SELECT mp FROM MANOprovider mp WHERE mp.enabledForONBOARDING = TRUE")
	Collection<MANOprovider> findAllEnabled();

}
