package portal.api.repo;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.Category;


/**
 * @author ctranoris
 *
 */
@Repository
public interface CategoriesRepository extends PagingAndSortingRepository<Category, Long> {

}
