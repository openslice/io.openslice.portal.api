package portal.api.repo;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import io.openslice.model.PortalProperty;

/**
 * @author ctranoris
 *
 */
public interface PortalPropertiesRepository  extends PagingAndSortingRepository<PortalProperty, Long> {

	Optional<PortalProperty> findByName(String aname);
	

}
