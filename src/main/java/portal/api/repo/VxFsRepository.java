package portal.api.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.VxFMetadata;


/**
 * @author ctranoris
 *
 */
@Repository
public interface VxFsRepository extends PagingAndSortingRepository<VxFMetadata, Long> {


	@Query( value = "SELECT a FROM VxFMetadata a JOIN a.categories ac WHERE a.published=TRUE AND ac.id=?1 ORDER BY a.name" ) //
	Collection<VxFMetadata> getPublishedVxFsByCategory(long categoryid);

	@Query( value = "SELECT a FROM VxFMetadata a WHERE a.published=TRUE ORDER BY a.name" ) //
	Collection<VxFMetadata> getPublishedVxF();


	@Query( value ="SELECT a FROM VxFMetadata a  WHERE a.owner.id=?1 ORDER BY a.id" )
	Collection<VxFMetadata> getVxFsByUserID(long userid);

	@Query( value = "SELECT a FROM VxFMetadata a JOIN a.categories ac WHERE ac.id=?1 ORDER BY a.name" ) //
	Collection<VxFMetadata> getVxFsByCategory(long categoryid);
	
	@Query( value ="SELECT a FROM VxFMetadata a WHERE a.uuid=?1" )
	Optional<VxFMetadata> findByUUID(String uuid);

	@Query( value ="SELECT a FROM VxFMetadata a WHERE a.name LIKE ?1" )
	Optional<VxFMetadata> findByName(String name);


	

}
