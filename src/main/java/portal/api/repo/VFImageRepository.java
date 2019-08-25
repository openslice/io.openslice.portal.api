package portal.api.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.VFImage;
import io.openslice.model.VxFMetadata;


/**
 * @author ctranoris
 *
 */
@Repository
public interface VFImageRepository extends PagingAndSortingRepository<VFImage, Long> {


	@Query( value ="SELECT a FROM VFImage a WHERE a.name LIKE ?1" )
	Optional<VFImage> findByName(String name);

	
	@Query( value ="SELECT a FROM VFImage a JOIN a.owner aon WHERE aon.id LIKE ?1" )
	Collection<VFImage> findAllByUserid(long id);

}
