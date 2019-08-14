package portal.api.service;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import io.openslice.model.PortalUser;
import io.openslice.model.UserRoleType;
import portal.api.repo.UsersRepository;

/**
 * @author ctranoris
 *
 */
@Service
public class UsersService {

	@Autowired
	UsersRepository usersRepo;
	

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


	private static final transient Log logger = LogFactory.getLog( UsersService.class.getName() );

	
	public UsersService() {
		super();
		
	}
	
	@PostConstruct
	public void initRepo() {
		
		PortalUser admin = null;
		try {
			admin = findById(1);
			logger.info("======================== admin  = " + admin);
		} catch (Exception e) {
			logger.info("======================== admin NOT FOUND, initializing");			
		}

		if (admin == null) {
			PortalUser bu = new PortalUser();
			bu.setName("Portal Administrator");
			bu.setUsername("admin");			
			bu.setPassword(  passwordEncoder.encode("changeme") );
			
			bu.setEmail("");
			bu.setOrganization("");
			bu.addRole( UserRoleType.PORTALADMIN );
			bu.addRole( UserRoleType.MENTOR  );
			bu.setActive(true);
			usersRepo.save( bu );


//			Category c = new Category();
//			c.setName("None");
//			saveCategory(c);
		}		
	}

	public List<PortalUser> findAll() {
		return (List<PortalUser>) this.usersRepo.findAll(); // findAll(new Sort(Sort.Direction.ASC, "name"));
	}

	public List<PortalUser> getUserMentorsValues() {
		return (List<PortalUser>) this.usersRepo.findAllMentors();
	}

	public PortalUser findById( long id ) {

		Optional<PortalUser> optionalUser = this.usersRepo.findById( id );
		return optionalUser
				.orElseThrow(() -> new PortalUserNotFoundException("Couldn't find a Portal User with id: " + id));
	}

	public PortalUser findByUsername(String username) {
		Optional<PortalUser> optionalUser = this.usersRepo.findByUsername( username );
		return optionalUser.orElse(null);
	}

	public PortalUser findByEmail(String email) {
		Optional<PortalUser> optionalUser = this.usersRepo.findByEmail( email );
		return optionalUser.orElse(null);
	}

	public PortalUser addPortalUserToUsers(PortalUser user) {
		return usersRepo.save( user );
	}

	public PortalUser updateUserInfo(PortalUser user) {
		return usersRepo.save( user );
	}

}
