/**
 * Copyright 2017 University of Patras 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and limitations under the License.
 */

package portal.api.controller;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import io.openslice.model.Category;
import io.openslice.model.ExperimentMetadata;
import io.openslice.model.PortalUser;
import io.openslice.model.Product;
import io.openslice.model.UserRoleType;
import io.openslice.model.UserSession;
import io.openslice.model.VxFMetadata;
import portal.api.bus.BusController;
import portal.api.service.CategoryService;
import portal.api.service.PortalPropertiesService;
import portal.api.service.UsersService;
import portal.api.util.EmailUtil;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@RestController
//@RequestMapping("/repo")
public class PortalRepositoryAPIImpl {
//	/** */
//	private MANOController aMANOController;

	private static final transient Log logger = LogFactory.getLog(PortalRepositoryAPIImpl.class.getName());
//
//	private static final String METADATADIR = System.getProperty("user.home") + File.separator + ".portal"
//			+ File.separator + "metadata" + File.separator;
//	
//
//
	// PortalUser related API
	
    @Resource(name="authenticationManager")
    private AuthenticationManager authManager;


    @Autowired
    BCryptPasswordEncoder passwordEncoder;
    
	@Autowired
    UsersService usersService;
	

	@Autowired
	CategoryService categoryService;
	
	@Autowired
	PortalPropertiesService propsService;
	
	
//	@GetMapping
//	public ResponseEntity<?>  getmain() {
//		return ResponseEntity.ok( "ok-main" );
//	}

	/*************** Users API *************************/

	@Secured({ "ROLE_ADMIN" })
	@GetMapping( value = "/admin/users", produces = "application/json" )
	public ResponseEntity<List<PortalUser>>  getUsers() {

		
		return ResponseEntity.ok( usersService.findAll() );
	}
	
	@GetMapping( value =  "/admin/users/mentors", produces = "application/json" )	
	public ResponseEntity<List<PortalUser>> getMentors() {
		
		return ResponseEntity.ok( usersService.getUserMentorsValues());
	}

	@Secured({ "ROLE_ADMIN" })
	@GetMapping( value = "/admin/users/{userid}", produces = "application/json" )
	public ResponseEntity<?> getUserById( @PathVariable(required = true) long userid) {
		
		PortalUser u = usersService.findById( userid );		
		return ResponseEntity.ok( u  );		
	}


	@PostMapping( value =  "/admin/users/", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> addUser( @Valid @RequestBody PortalUser user) {

		logger.info("Received POST for usergetUsername: " + user.getUsername());
		// logger.info("Received POST for usergetPassword: " +
		// user.getPassword());
		// logger.info("Received POST for usergetOrganization: " +
		// user.getOrganization());

		if ((user.getUsername() == null)
				|| (user.getUsername().equals("") || (user.getEmail() == null) || (user.getEmail().equals("")))) {
			
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "New user with username=" + user.getUsername() + " cannot be registered" );
			
		}
		
		if ( user.getActive() ) {

			logger.info("New user with username=" + user.getUsername() + " cannot be registered BAD_REQUEST, seems ACTIVE already");
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "New user with username=" + user.getUsername() + " cannot be registered, seems ACTIVE already");
		}

		PortalUser portaluser = usersService.findByUsername(user.getUsername());  
		
		if (portaluser != null) {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "Username exists");
		}

		portaluser = usersService.findByEmail(user.getEmail() );  
		if (portaluser != null) {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "Email exists");
		}

		user.setApikey( UUID.randomUUID().toString() );
		user.setPassword(  passwordEncoder.encode( user.getPassword() ) );
		portaluser = usersService.addPortalUserToUsers(user);

		if (portaluser != null) {
			BusController.getInstance().newUserAdded( portaluser );		
			return ResponseEntity.ok( portaluser  );	
		} else {
			logger.info( "Requested user with username=" + user.getUsername() + " cannot be installed" );
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "Requested user with username=" + user.getUsername() + " cannot be added" );
		}
	}

	
	@PostMapping( value =  "/register/", produces = "application/json", consumes = "multipart/form-data" )
	public ResponseEntity<?> addNewRegisterUser(  
			@RequestPart("portaluser") PortalUser user,
			@RequestPart("emailmessage") String emailmessage) {

		user.setActive(false);// in any case the user should be not active
		user.getRoles().clear();
		user.addRole(UserRoleType.ROLE_EXPERIMENTER); // otherwise in post he can choose
		user.addRole(UserRoleType.ROLE_VXF_DEVELOPER); // otherwise in post he can choose


		String msg = emailmessage;
		logger.info("Received register for usergetUsername: " + user.getUsername());

		ResponseEntity<?> r = addUser(user);

		if (r.getStatusCode()  == HttpStatus.OK ) {
			logger.info("Email message: " + msg);
			String subj = "[" + propsService.getPropertyByName("portaltitle").getValue() + "] " + propsService.getPropertyByName("activationEmailSubject").getValue();
			EmailUtil.SendRegistrationActivationEmail(user.getEmail(), msg, subj);
		}

		return r;
	}

	@PostMapping( value =  "/register/verify", produces = "application/json", consumes = "multipart/form-data" )
	public ResponseEntity<?> addNewRegisterUserVerify(
			@RequestPart("username") String username,
			@RequestPart("rid") String rid) {


		PortalUser u = usersService.findByUsername(username);
		if (u.getOrganization().contains("^^")) {
			u.setOrganization(u.getOrganization().substring(0, u.getOrganization().indexOf("^^")));
			u.setActive(true);
		}
		u = usersService.updateUserInfo(  u);

		if (u != null) {
			return ResponseEntity.ok( u  );
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "Requested user with username=" + u.getUsername() + " cannot be updated");
		}
	}


	@Secured({ "ROLE_ADMIN" })
	@PutMapping( value =  "/admin/users/{userid}", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?>  updateUserInfo(  @PathVariable(required = true) long userid ,  @Valid @RequestBody PortalUser user) {
		logger.info("Received PUT for user: " + user.getUsername());
		

		PortalUser previousUser = usersService.findById(userid);

//		List<Product> previousProducts = previousUser.getProducts();
//
//		if (user.getProducts().size() == 0) {
//			user.getProducts().addAll(previousProducts);
//		}
//
		previousUser.setActive( user.getActive() );
		previousUser.setEmail( user.getEmail() );
		previousUser.setName( user.getName());
		previousUser.setOrganization( user.getOrganization() );
		if ( (user.getPassword()!=null) && (!user.getPassword().equals(""))){//else will not change it
			//previousUser.setPasswordUnencrypted( user.getPassword() ); 	//the unmarshaled object user has already called setPassword, so getPassword provides the encrypted password
			previousUser.setPassword(  passwordEncoder.encode( user.getPassword() ) );
		}
		
		previousUser.getRoles().clear();
		for (UserRoleType rt : user.getRoles()) {
			previousUser.getRoles().add(rt);
		}
		
		if ( (user.getApikey()!=null) && ( !user.getApikey().equals("")) ){
			previousUser.setApikey( user.getApikey() );			
		} else {
			previousUser.setApikey( UUID.randomUUID().toString() );			
		}

		
		PortalUser u = usersService.updateUserInfo( previousUser );
		

		if (u != null) {

			return ResponseEntity.ok( u  );
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "Requested user with username=" + u.getUsername() + " cannot be updated");
		}
	}

	@Secured({ "ROLE_ADMIN" })
	@DeleteMapping( value =  "/admin/users/{userid}", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> deleteUser(@PathVariable("userid") int userid) {
		logger.info("Received DELETE for userid: " + userid);
		

		return (ResponseEntity<?>) ResponseEntity.badRequest().body( "Requested user cannot be deleted");
		
		/**
		 * do not allow for now to delete users!
		 */
				
//		portalRepositoryRef.deleteUser(userid);
//		return Response.ok().build();
		
	}
	
	/**
	 * @param userID
	 * @return true if user logged is equal to the requested id of owner, or is ROLE_ADMIN
	 */
	private boolean checkUserIDorIsAdmin(int userID){

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		logger.info("principal 1=  " + authentication.getAuthorities().contains( new SimpleGrantedAuthority( UserRoleType.ROLE_ADMIN.getValue()  ) ));
		logger.info("principal 2=  " + authentication.getAuthorities().contains( new SimpleGrantedAuthority(  UserRoleType.ROLE_TESTBED_PROVIDER.getValue() ) ));
		logger.info("principal 2=  " + authentication.getAuthorities().contains( new SimpleGrantedAuthority(  UserRoleType.ROLE_MENTOR.getValue() ) ));
		

		PortalUser uToFind = usersService.findById(  userID );
		if ( (uToFind !=null )  && ( uToFind.getUsername()  == authentication.getName()) ){
			logger.info("checkUserIDorIsAdmin, user is equal with request");
			return true;
		} 
		if ( authentication.getAuthorities().contains( new SimpleGrantedAuthority( UserRoleType.ROLE_ADMIN.getValue() ))){
			logger.info("checkUserIDorIsAdmin, authentication role =  " + authentication.getAuthorities().contains( new SimpleGrantedAuthority( UserRoleType.ROLE_ADMIN.getValue()  ) ));
			return true;
		}
		

		return false;
	}

	@GetMapping( value =  "/admin/users/{userid}/vxfs", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> getAllVxFsofUser(@PathVariable("userid") int userid) {
		logger.info("getAllVxFsofUser for userid: " + userid);
		
		if ( !checkUserIDorIsAdmin( userid ) ){
			return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.FORBIDDEN);
		}
		PortalUser u = usersService.findById(userid);

		if (u != null) {
			List<Product> prods = u.getProducts();
			List<VxFMetadata> vxfs = new ArrayList<VxFMetadata>();
			for (Product p : prods) {
				if (p instanceof VxFMetadata)
					vxfs.add((VxFMetadata) p);
			}

			return ResponseEntity.ok( vxfs  );
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "User with id=" + userid + " not found in portal registry");
			
		}
	}


	@GetMapping( value =  "/admin/users/{userid}/experiments", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> getAllAppsofUser(@PathVariable("userid") int userid) {
		logger.info("getAllAppsofUser for userid: " + userid);
		
		if ( !checkUserIDorIsAdmin( userid ) ){
			return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.FORBIDDEN);
		}

		
		PortalUser u = usersService.findById(userid);

		if (u != null) {
			List<Product> prods = u.getProducts();
			List<ExperimentMetadata> apps = new ArrayList<ExperimentMetadata>();
			for (Product p : prods) {
				if (p instanceof ExperimentMetadata)
					apps.add((ExperimentMetadata) p);
			}

			return ResponseEntity.ok( apps  );
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "User with id=" + userid + " not found in portal registry");
		}
	}

	@GetMapping( value =  "/admin/users/{userid}/vxfs/{vxfid}", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> getVxFofUser(@PathVariable("userid") int userid, @PathVariable("vxfid") int vxfid) {
		logger.info("getVxFofUser for userid: " + userid + ", vxfid=" + vxfid);

		if ( !checkUserIDorIsAdmin( userid ) ){
			return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.FORBIDDEN);
		}

		
		PortalUser u = usersService.findById(userid);

		if (u != null) {
			VxFMetadata vxf = (VxFMetadata) u.getProductById(vxfid);
			return ResponseEntity.ok( vxf  );
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "User with id=" + userid + " not found in portal registry");
		}
	}

	@GetMapping( value =  "/admin/users/{userid}/experiments/{appid}", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> getAppofUser( @PathVariable("userid") int userid, @PathVariable("appid") int appid ) {
		logger.info("getAppofUser for userid: " + userid + ", appid=" + appid);
		if ( !checkUserIDorIsAdmin( userid ) ){
			return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.FORBIDDEN);
		}
		
		PortalUser u = usersService.findById(userid);

		if (u != null) {
			ExperimentMetadata appmeta = (ExperimentMetadata) u.getProductById(appid);
			return ResponseEntity.ok( appmeta );
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "User with id=" + userid + " not found in portal registry");
		}
	}
	
	// Sessions related API

	// @OPTIONS
	// @Path("/sessions/")
	// @Produces("application/json")
	// @Consumes("application/json")
	// @LocalPreflight
	// public ResponseEntity<?> addUserSessionOption(){
	//
	//
	// logger.info("Received OPTIONS addUserSessionOption ");
	// String origin = headers.getRequestHeader("Origin").get(0);
	// if (origin != null) {
	// return Response.ok()
	// .header(CorsHeaderConstants.HEADER_AC_ALLOW_METHODS, "GET POST DELETE PUT
	// HEAD OPTIONS")
	// .header(CorsHeaderConstants.HEADER_AC_ALLOW_CREDENTIALS, "true")
	// .header(CorsHeaderConstants.HEADER_AC_ALLOW_HEADERS, "Origin,
	// X-Requested-With, Content-Type, Accept")
	// .header(CorsHeaderConstants.HEADER_AC_ALLOW_ORIGIN, origin)
	// .build();
	// } else {
	// return Response.ok().build();
	// }
	// }


	@PostMapping( value =  "/sessions", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> addUserSession( @Valid @RequestBody UserSession userSession, final HttpServletRequest request) {

		logger.info("Received POST addUserSession usergetUsername: " + userSession.getUsername());
		// logger.info("DANGER, REMOVE Received POST addUserSession password: "
		// + userSession.getPassword());
		
		Authentication authentication = 
                SecurityContextHolder.getContext().getAuthentication();		

		logger.info("authentication=  " + authentication);
		

		if (authentication != null) {
			if (authentication.getPrincipal() != null)
				logger.info(" securityContext.getPrincipal().toString() >"
						+ authentication.getPrincipal().toString() + "<");

		}
		
		  UsernamePasswordAuthenticationToken authReq =
		            new UsernamePasswordAuthenticationToken( userSession.getUsername(), userSession.getPassword() );
		       

		
			try {
				 Authentication auth = authManager.authenticate(authReq);
			        SecurityContext sc = SecurityContextHolder.getContext();
			        sc.setAuthentication(auth);
			        HttpSession session = request.getSession(true);
			        session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
			        
				PortalUser portalUser =  usersService.findByUsername(userSession.getUsername());
				if (portalUser == null ) {
					return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.NOT_FOUND ).body("user not found");
				}
				

				logger.info(" securityContext.getPrincipal().toString() = "
						+ sc.getAuthentication().getPrincipal().toString()  );
				
				
				if (!portalUser.getActive()) {
					logger.info("User [" + portalUser.getUsername() + "] is not Active");

					return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.UNAUTHORIZED);
				}

				portalUser.setCurrentSessionID( request.getSession().getId() );
				userSession.setPortalUser(portalUser);
				userSession.setPassword("");
				;// so not tosend in response

				logger.info("User [" + portalUser.getUsername() + "] logged in successfully.");
				PortalUser u = usersService.updateUserInfo( portalUser );
				
//				if ( currentUser.getPrincipal().toString().length()>2 ){
//					CentralLogger.log( CLevel.INFO, "User [" + currentUser.getPrincipal().toString().substring(0, 3) + "xxx" + "] logged in");					
//				}

				return ResponseEntity.ok( userSession );
			} catch (Exception ae) {

				return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body( "exception: " + ae.getMessage());
			}
	

	}


	@GetMapping( value = "/sessions/logout", produces = "application/json" )
	public ResponseEntity<?> logoutUser() {

		logger.info("Received logoutUser ");
		
		SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
		
//
//		if (sc != null) {
//			if (sc.getUserPrincipal() != null)
//				logger.info(" securityContext.getUserPrincipal().toString() >"
//						+ sc.getUserPrincipal().toString() + "<");
//
//			SecurityUtils.getSubject().logout();
//		}

		return ResponseEntity.ok().body("{}");
	}
	

	// categories API
	
	@GetMapping( value = "/categories", produces = "application/json" )
	public ResponseEntity<List<Category>> getCategories() {
		return ResponseEntity.ok( categoryService.findAll() );
	}

	@GetMapping( value = "/admin/categories", produces = "application/json" )
	public ResponseEntity<List<Category>>  getAdminCategories() {
		return ResponseEntity.ok( categoryService.findAll() );
	}

	
	@Secured({ "ROLE_ADMIN" })
	@PostMapping( value =  "/admin/categories/", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> addCategory(Category c) {
		
		
		Category u = categoryService.addCategory(c);

		if (u != null) {
			return ResponseEntity.ok(u);
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "Requested category cannot be added" );
		}
	}

	@Secured({ "ROLE_ADMIN" })
	@PutMapping( value =  "/admin/categories/{catid}", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> updateCategory(@PathVariable("catid") long catid, @RequestBody Category c) {
		
		
		Category previousCategory = categoryService.findById(catid);

		previousCategory.setName( c.getName() );
		
		Category u = categoryService.updateCategoryInfo( previousCategory );

		if (u != null) {
			return ResponseEntity.ok( u  );
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "Requested category cannot be updated" );
		}

	}

	@Secured({ "ROLE_ADMIN" })
	@DeleteMapping( value =  "/admin/categories/{catid}", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> deleteCategory( @PathVariable("catid") long catid) {
		

		Category previousCategory = categoryService.findById(catid);
		if ( previousCategory != null ) {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "Requested category cannot be deleted" );
		} else {
			categoryService.deleteCategory( previousCategory );
			return ResponseEntity.ok().body("OK");
		}
	}


	@GetMapping( value = "/categories/{catid}", produces = "application/json" )
	public ResponseEntity<?> getCategoryById( @PathVariable("catid") int catid) {
		Category c = categoryService.findById(catid);

		if (c != null) {
			return ResponseEntity.ok( categoryService.findAll() );
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body( "Requested category not found" );
		}
	}
	@GetMapping( value = "/admin/categories/{catid}", produces = "application/json" )
	public ResponseEntity<?> getAdminCategoryById(@PathVariable("catid") int catid) {		
		return getCategoryById(catid);
	}


}
