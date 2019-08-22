package portal.api.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.ExperimentMetadata;


/**
 * @author ctranoris
 *
 */
@Repository
public interface NSDsRepository extends PagingAndSortingRepository<ExperimentMetadata, Long> {


	@Query( value = "SELECT a FROM ExperimentMetadata a WHERE a.published=TRUE AND a.categories.id=?1 ORDER BY a.name" ) //	
	Collection<ExperimentMetadata> getPublishedNSDsByCategory(Long categoryid);

	@Query( value = "SELECT a FROM ExperimentMetadata a WHERE a.published=TRUE ORDER BY a.name" ) //
	Collection<ExperimentMetadata> getPublishedNSDs();

	@Query( value = "SELECT a FROM ExperimentMetadata a WHERE a.categories.id=?1 ORDER BY a.name" ) //
	Collection<ExperimentMetadata> getNSDsByCategory(Long categoryid);

	@Query( value ="SELECT a FROM ExperimentMetadata a WHERE a.owner.id=?1 ORDER BY a.id" )
	Collection<ExperimentMetadata> getNSDsByUserID(long userid);

	@Query( value ="SELECT a FROM ExperimentMetadata a WHERE a.uuid=?1" )
	Optional<ExperimentMetadata> findByUUID(String uuid);

	@Query( value ="SELECT a FROM ExperimentMetadata a WHERE a.name LIKE ?1" )
	Optional<ExperimentMetadata> findByName(String name);


}
