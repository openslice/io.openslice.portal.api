package portal.api.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.Product;
import io.openslice.model.VxFMetadata;


/**
 * @author ctranoris
 *
 */
@Repository
public interface ProductRepository extends PagingAndSortingRepository<Product, Long> {


	@Query( value ="SELECT a FROM Product a WHERE a.uuid=?1" )
	Optional<Product> findByUUID(String uuid);



}
