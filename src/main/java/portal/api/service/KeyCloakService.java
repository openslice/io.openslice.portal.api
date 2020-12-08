/*-
 * ========================LICENSE_START=================================
 * io.openslice.portal.api
 * %%
 * Copyright (C) 2019 - 2020 openslice.io
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

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.openslice.model.PortalUser;
import io.openslice.model.UserRoleType;


/**
 * @author ctranoris
 *
 */
@Component
@Profile("!testing")
public class KeyCloakService {
	

	private static final transient Log logger = LogFactory.getLog( KeyCloakService.class.getName() );

	@Value("${keycloak.credentials.secret}")
	private String SECRETKEY;

	@Value("${keycloak.resource}")
	private String CLIENTID;

	@Value("${keycloak-internal-auth-server-url}")
	private String AUTHURL;

	@Value("${keycloak.realm}")
	private String REALM;

	@Value("${keycloak-admin-password}")
	private String ACCESS_PASSWORD;
	

	public static void main(String args[]) {
//		
//		Keycloak keycloak = Keycloak.getInstance( 
//	    "http://localhost:28080/auth",
//	    "master",
//	    "admin",
//	    "Pa55w0rd",
//	    "admin-cli");
//
//		RealmResource realmResource = keycloak.realm("openslice");
//
//		for (UserRepresentation u :realmResource.users().list() ) {
//			logger.info(u.getUsername() );				
//		}
		
//		for (RoleRepresentation u :realmResource.roles().list() ) {
//			logger.info(u.getName()  );				
//		}
		
//		PortalUser userDTO = new PortalUser();
//		userDTO.setUsername("kokos");
//		userDTO.setPassword("lala");
//		userDTO.setEmail("lala@example.org");
//		userDTO.setFirstname("Kokos");
//		userDTO.setLastname("Lalakis");
//		k.createUserInKeyCloak(userDTO );
		
	}
	
	
	public UsersResource getKeycloakUserResource() {	
		
		RealmResource realmResource = getRealmResource();
//			for (UserRepresentation u :realmResource.users().list() ) {
//				logger.info(u.getUsername() );				
//			}
//			for (RoleRepresentation u :realmResource.roles().list() ) {
//				logger.info(u.getName()  );				
//			}
		
		return realmResource.users();			
	}
	
	/**
	 * @param username
	 * @return
	 */
	public UserRepresentation findFirstByUsername( String username) {
		List<UserRepresentation> results = getRealmResource().users().search( username );
		if ( results.size()>0 ) {
			return results.get(0); 
		}
		
		return null;
	}
	
	private RealmResource getRealmResource() {

		/**
		 * for this to work:
		 * - create user _openslice_internal_access_
		 * - in Role Mappings in Client Roles select realm-management add to Assigned Roles: manage-users, 
		 */
//		Keycloak keycloak = Keycloak.getInstance( 
//			    "http://localhost:28080/auth",
//			    "openslice",
//			    "_openslice_internal_access_",
//			    "apassword",
//			    "openslice-service", "989081f5-2232-4acb-86f6-398f67cb28b9");
		int attempts = 0;
		while (attempts<10) {
			try {
				logger.info(" Keycloak AUTHURL: "+AUTHURL);
				logger.info(" Keycloak CLIENTID: "+CLIENTID);
				Keycloak keycloak = Keycloak.getInstance( 
						AUTHURL,
						"master",
					    "admin",
					    ACCESS_PASSWORD,
					    CLIENTID, 
					    SECRETKEY);
				
				RealmResource realmResource = keycloak.realm( REALM );

				return realmResource;
				
				}catch (Exception e) {
					attempts++;
					logger.error("Keycloak unavailable after " + attempts + " attempts. ! Wait 20 seconds" );
					try {
						Thread.sleep(20*1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			
		}
		logger.error("Keycloak unavailable after " + attempts + " attempts. Final!" );
		return null;

	}
	
	
	/**
	 * @param userDTO
	 * @return keycloak userId
	 */
	public String createUserInKeyCloak(PortalUser userDTO) {

		int statusId = 0;
		try {

			UsersResource userRessource = getKeycloakUserResource();

			UserRepresentation user = new UserRepresentation();
			user.setUsername(userDTO.getUsername());
			user.setEmail(userDTO.getEmail());
			user.setFirstName(userDTO.getFirstname());
			user.setLastName(userDTO.getLastname());
			user.setEnabled( false );

			

			RealmResource realmResource = getRealmResource();
			
			// Create user
			//Response result = userRessource.create(user);
			Response result = realmResource.users().create( user );
			
			logger.info("Keycloak create user response code>>>>" + result.getStatus());

			statusId = result.getStatus();

			if (statusId == 201) {

				String userId = result.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

				logger.info("User created with userId:" + userId);

				// Define password credential
				CredentialRepresentation passwordCred = new CredentialRepresentation();
				passwordCred.setTemporary(false);
				passwordCred.setType(CredentialRepresentation.PASSWORD);
				passwordCred.setValue(userDTO.getPassword());

				// Set password credential
				userRessource.get(userId).resetPassword(passwordCred);

				//RealmResource realmResource = getRealmResource();
				
				
				for (UserRoleType role : userDTO.getRoles()) {
					// set roles
					RoleRepresentation savedRoleRepresentation = realmResource.roles().get( role.getValue().replace("ROLE_", "") ).toRepresentation();
					realmResource.users().get(userId).roles().realmLevel().add(Arrays.asList(savedRoleRepresentation));
				}

				logger.info("Username==" + userDTO.getUsername() + " created in keycloak successfully");
				
				return userId;

			}

			else if (statusId == 409) {
				logger.error("Username==" + userDTO.getUsername() + " already present in keycloak");

			} else {
				logger.error("Username==" + userDTO.getUsername() + " could not be created in keycloak");

			}

		} catch (Exception e) {
			e.printStackTrace();

		}

		return null;

	}
	
	// after logout user from the keycloak system. No new access token will be
		// issued.
		/**
		 * @param username
		 */
		public void logoutUser(String username) {

			String userId = findFirstByUsername( username).getId();
			getKeycloakUserResource().get(userId).logout();

		}

		/**
		 * @param user
		 * @return
		 */
		public String updateUserInKeyCloak( PortalUser userDTO ) {
			
			UserRepresentation user = findFirstByUsername( userDTO.getUsername());
			if ( user == null ) {
				return null;
			}
			user.setUsername(userDTO.getUsername());
			user.setEmail(userDTO.getEmail());
			user.setFirstName(userDTO.getFirstname());
			user.setLastName(userDTO.getLastname());
			user.setEnabled( userDTO.getActive() );
			
			if ( ( userDTO.getPassword() != null ) && ( !userDTO.getPassword().equals("") )) {
				// Define password credential
				CredentialRepresentation passwordCred = new CredentialRepresentation();
				passwordCred.setTemporary(false);
				passwordCred.setType(CredentialRepresentation.PASSWORD);
				passwordCred.setValue(userDTO.getPassword());
				getKeycloakUserResource().get( user.getId()).resetPassword(passwordCred);
			}
			
			getKeycloakUserResource().get( user.getId()  ).update( user );
			RealmResource realmResource = getRealmResource();
			
			//remove roles
			realmResource.users().get(user.getId() ).roles().realmLevel().remove(
					realmResource.users().get(user.getId() ).roles().realmLevel().listAll() 
					);
			
			//add roles
			for (UserRoleType role : userDTO.getRoles()) {
				// set roles
				RoleRepresentation savedRoleRepresentation = realmResource.roles().get( role.getValue().replace("ROLE_", "") ).toRepresentation();
				realmResource.users().get(user.getId() ).roles().realmLevel().add(Arrays.asList(savedRoleRepresentation));
			}

			logger.info("Username==" + userDTO.getUsername() + " updated in keycloak successfully");
			
			return user.getId();			
			
		}
		
		
		/**
		 * @param user
		 * @return
		 */
		public PortalUser updateUserFromKeyCloak( PortalUser userDTO ) {
			
			UserRepresentation user = findFirstByUsername( userDTO.getUsername());
			if ( user == null ) {
				return null;
			}
			
			
			
			userDTO.setUsername(user.getUsername());
			userDTO.setEmail(user.getEmail());
			userDTO.setFirstname(user.getFirstName());
			userDTO.setLastname(user.getLastName());
			userDTO.setActive( user.isEnabled() );
			

			
			getKeycloakUserResource().get( user.getId()  ).update( user );
			RealmResource realmResource = getRealmResource();
			
			
			//remove roles
			userDTO.getRoles().clear();
			for (RoleRepresentation arole : realmResource.users().get(user.getId() ).roles().realmLevel().listAll() ) {
				try {
					UserRoleType e = UserRoleType.getEnum("ROLE_" + arole.getName()) ;	
					userDTO.getRoles().add(e );				
				}catch (Exception e) {

				}
			}
			
			

			logger.info("Username==" + userDTO.getUsername() + " updated FROM keycloak successfully");
			
			return userDTO;			
			
		}


		public PortalUser fetchUserDetails(String username) {

			PortalUser userDTO  = new  PortalUser();
			UserRepresentation user = findFirstByUsername(  username );
			if ( user == null ) {
				return null;
			}
			
			userDTO.setUsername(user.getUsername());
			userDTO.setEmail( user.getEmail());
			userDTO.setFirstname( user.getFirstName());
			userDTO.setLastname( user.getLastName());
			userDTO.setActive( true );
			

			
			return userDTO;
		}

}
