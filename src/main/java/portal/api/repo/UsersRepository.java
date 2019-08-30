package portal.api.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.openslice.model.PortalUser;

/**
 * 
 * @author ctranoris
 * @see
 * https://www.amitph.com/spring-data-jpa-query-methods/, 
 * https://www.amitph.com/pagination-sorting-spring-data-jpa/, 
 * https://www.baeldung.com/spring-data-jpa-query,
 * for usefull methods on spring repository
 * 
 * 
 */
@Repository
public interface UsersRepository  extends PagingAndSortingRepository<PortalUser, Long> {
	
	PortalUser findDistinctFirstByUsername( String username );

	@Query( value = "SELECT m FROM PortalUser m INNER JOIN m.roles r WHERE r = io.openslice.model.UserRoleType.ROLE_MENTOR" ) //
	Collection<PortalUser> findAllMentors();

	Optional<PortalUser> findByUsername(String username);

	Optional<PortalUser> findByEmail(String email); 
	
	

}
