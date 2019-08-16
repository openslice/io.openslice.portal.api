package portal.api.repo;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.PortalProperty;

/**
 * @author ctranoris
 *
 */
@Repository
public interface PortalPropertiesRepository  extends PagingAndSortingRepository<PortalProperty, Long> {

	Optional<PortalProperty> findByName(String aname);
	

}
