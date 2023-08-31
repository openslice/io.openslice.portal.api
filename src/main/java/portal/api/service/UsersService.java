/*-
 * ========================LICENSE_START=================================
 * io.openslice.portal.api
 * %%
 * Copyright (C) 2019 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package portal.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.openslice.model.PortalUser;
import io.openslice.model.UserRoleType;
import jakarta.annotation.PostConstruct;
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
		
//		PortalUser admin = null;
//		try {
//			admin = findById(1);
//			logger.info("======================== admin  = " + admin);
//		} catch (Exception e) {
//			logger.info("======================== admin NOT FOUND, initializing");			
//		}
//
//		if (admin == null) {
//			PortalUser bu = new PortalUser();
//			bu.setFirstname("Portal Administrator");
//			bu.setUsername( "admin" );			
//			bu.setPassword( "changeme" );
//			bu.setApikey( UUID.randomUUID().toString() );		
//			
//			bu.setEmail("");
//			bu.setOrganization("");
//			bu.addRole( UserRoleType.ROLE_ADMIN );
//			bu.addRole( UserRoleType.ROLE_MENTOR  );
//			bu.setActive(true);
//			addPortalUserToUsers( bu );
//			
////			Category c = new Category();
////			c.setName("None");
////			saveCategory(c);
//		}	
		
		PortalUser manoService = null;
		try
		{
			manoService = findByUsername("manoService");
			logger.info("======================== manoService  = " + manoService);			
		}
		catch(Exception e)
		{
			logger.info("======================== manoService NOT FOUND, initializing");						
		}
		if (manoService == null) {
			PortalUser bu = new PortalUser();
			bu.setFirstname("MANO Service System User");
			bu.setUsername( "manoService" );
		    int length = 16;
		    boolean useLetters = true;
		    boolean useNumbers = false;
		    String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);			
			bu.setPassword( generatedString );
			bu.setApikey( UUID.randomUUID().toString() );		
			
			bu.setEmail("");
			bu.setOrganization("");
			bu.addRole( UserRoleType.ROLE_ADMIN );
			bu.addRole( UserRoleType.ROLE_MENTOR );
			bu.setActive(true);
			usersRepo.save( bu );
			//addPortalUserToUsers( bu );			
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
				.orElse(null);
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
	
	public PortalUser addPortalUserToUsersFromAuthServer(String username, String email, String firstname, String lastname) {

		PortalUser userDTO  = new  PortalUser();
		
		userDTO.setUsername( username );
		userDTO.setEmail( email );
		userDTO.setFirstname( firstname );
		userDTO.setLastname( lastname );
		userDTO.setActive( true );
		
		userDTO.setApikey( UUID.randomUUID().toString() );
		usersRepo.save( userDTO );			
		return updateUserInfo( userDTO, true );	
	}

	/**
	 * @param user
	 * @param userInfoChanged 
	 * @return
	 */
	public PortalUser updateUserInfo(PortalUser user, Boolean userInfoChanged) {
		
		return usersRepo.save( user );
		

	}
	
	

	public void delete(PortalUser u) {
		usersRepo.delete(u);		
	}

	@Transactional
	public String getPortalUserByUserNameDataJson(String username) throws JsonProcessingException {
		PortalUser user = findByUsername( username );
		if ( user != null ) {
			ObjectMapper mapper = new ObjectMapper();		
	        mapper.registerModule(new Hibernate5JakartaModule()); 
			String res = mapper.writeValueAsString( user );		
			return res;			
		}
		return "";
	}

	public void logout( String username ) {
		//keyCloakService.logoutUser(username);
	}

	
}
