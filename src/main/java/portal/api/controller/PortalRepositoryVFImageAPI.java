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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.openslice.model.ExperimentMetadata;
import io.openslice.model.PortalUser;
import io.openslice.model.UserRoleType;
import io.openslice.model.VFImage;
import portal.api.bus.BusController;
import portal.api.service.PortalPropertiesService;
import portal.api.service.UsersService;
import portal.api.service.VFImageService;
import portal.api.util.AttachmentUtil;


/**
 * @author ctranoris
 *
 */

@RestController
public class PortalRepositoryVFImageAPI {
	

	
	private static final String VFIMAGESDIR = System.getProperty("user.home") + File.separator + ".vfimages"
			+ File.separator ;

	private static final transient Log logger = LogFactory.getLog( PortalRepositoryVFImageAPI.class.getName());
	
	@Autowired
	PortalPropertiesService propsService;

	@Autowired
    UsersService usersService;
	

	@Autowired
	VFImageService vfImageService;
	

	@Autowired
	ObjectMapper objectMapper;
	
	
	/**
	 * 
	 * Image object API
	 */

	@GetMapping( value = "/admin/vfimages/", produces = "application/json" )
	public ResponseEntity<?> getAdminVFImages() {

		//
		PortalUser u =  usersService.findByUsername( SecurityContextHolder.getContext().getAuthentication().getName() );
		
		
		
		if (u != null) {
			List<VFImage> vfimagess;

			if (u.getRoles().contains(UserRoleType.ROLE_ADMIN) || u.getRoles().contains(UserRoleType.ROLE_TESTBED_PROVIDER)) {
				vfimagess = vfImageService.getVFImages();
			} else {
				vfimagess = vfImageService.getVFImagesByUserID((long) u.getId());
			}

			return ResponseEntity.ok( vfimagess  );	

		} else {
			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
		
	}


	@PostMapping( value = "/admin/vfimages", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> addVFImage(
			final @ModelAttribute("vfimage") String v,
			@RequestParam(name = "prodFile", required = false) MultipartFile  prodFile,
			HttpServletRequest request
			) {

		//
		PortalUser u =  usersService.findByUsername( SecurityContextHolder.getContext().getAuthentication().getName() );

		if (u == null) {
			return (ResponseEntity<?>) ResponseEntity.notFound();
		}

		VFImage vfimg = null;
		
		String emsg = "";

		try {
			
			try {
				vfimg = objectMapper.readValue( v, VFImage.class);	
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}  

			logger.info("Received @POST for VFImage : " + vfimg.getName());
			
			String uuid = UUID.randomUUID().toString();
			vfimg.setUuid(uuid);
			vfimg.setDateCreated(new Date());
			
			vfimg = addNewVFImage(vfimg,	prodFile, request);

		} catch (JsonProcessingException e) {
			vfimg = null;
			e.printStackTrace();
			logger.error( e.getMessage() );
			emsg =  e.getMessage();
		} catch (IOException e) {
			vfimg = null;
			e.printStackTrace();
			logger.error( e.getMessage() );
			emsg =  e.getMessage();
		}


		if (vfimg != null) {

			BusController.getInstance().newVFImageAdded( vfimg );	
			return ResponseEntity.ok( vfimg  );
		} else {
			return  (ResponseEntity<?>) ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).body( "Requested Image cannot be inserted" );
		}
		
	
	}

	/**
	 * @param vfimg
	 * @param vfimagefile
	 * @return
	 * @throws IOException
	 */
	private VFImage addNewVFImage(VFImage vfimg, MultipartFile vfimagefile, HttpServletRequest request) throws IOException {

		logger.info("image name = " + vfimg.getName());
		logger.info("shortDescription = " +vfimg.getShortDescription());

		String endpointUrl = request.getRequestURI();
		String tempDir = VFIMAGESDIR + vfimg.getUuid() + File.separator;
	
		if (vfimagefile != null) {
			String imageFileNamePosted = vfimagefile.getOriginalFilename() ; //AttachmentUtil.getFileName(vfimagefile.getHeaders());
			logger.info("vfimagefile = " + imageFileNamePosted);
			if (!imageFileNamePosted.equals("") && !imageFileNamePosted.equals("unknown")) {
				Files.createDirectories(Paths.get(tempDir));
				String imgfile = AttachmentUtil.saveFile( vfimagefile, tempDir + imageFileNamePosted);
				logger.info("vfimagefile saved to = " + imgfile);
				
				vfimg.setPackageLocation(endpointUrl.toString().replace("http:", "") + "repo/vfimages/image/" + vfimg.getUuid() + "/"
						+ imageFileNamePosted);
			}
		}
		
		VFImage registeredvfimg =  vfImageService.saveVFImage( vfimg ); 
		
				
		return registeredvfimg;
	}
	
	
	/**
	 * @param vfimgnew
	 * @param vfimagefile
	 * @return
	 * @throws IOException
	 */
	private VFImage updateVFImage(VFImage vfimgnew, MultipartFile vfimagefile, HttpServletRequest request) throws IOException {

		logger.info("image name = " + vfimgnew.getName());
		logger.info("shortDescription = " +vfimgnew.getShortDescription());

		String endpointUrl = request.getRequestURI();
		String tempDir = VFIMAGESDIR + vfimgnew.getUuid() + File.separator;
		
		VFImage prevfImage = vfImageService.getVFImageByID( vfimgnew.getId() );
		
	
		if (vfimagefile != null) {
			String imageFileNamePosted = vfimagefile.getOriginalFilename() ; //AttachmentUtil.getFileName(vfimagefile.getHeaders());
			logger.info("vfimagefile = " + imageFileNamePosted);
			if (!imageFileNamePosted.equals("") && !imageFileNamePosted.equals("unknown")) {
				Files.createDirectories(Paths.get(tempDir));
				String imgfile = AttachmentUtil.saveFile( vfimagefile, tempDir + imageFileNamePosted);
				logger.info("vfimagefile saved to = " + imgfile);
				
				prevfImage.setPackageLocation(endpointUrl.toString().replace("http:", "") + "repo/vfimages/image/" + prevfImage.getUuid() + "/"
						+ imageFileNamePosted);
			}
		}
		
		prevfImage.setShortDescription( vfimgnew.getShortDescription() );
		prevfImage.setDateUpdated( new Date() );
		prevfImage.setPublicURL( vfimgnew.getPublicURL());
		prevfImage.setPublished( vfimgnew.isPublished()  );
		prevfImage.setTermsOfUse( vfimgnew.getTermsOfUse() );
		
		VFImage registeredvfimg =  vfImageService.updateVFImageInfo( prevfImage ); 
		
				
		return registeredvfimg;
	}

	
	@GetMapping( value = "/vfimages/image/{uuid}/{vfimagefile}", produces = "application/gzip" )
	public @ResponseBody byte[] downloadVxFPackage(@PathVariable("uuid") String uuid, @PathVariable("vfimagefile") String vfimagefile) throws IOException {

		logger.info("vfimagefile: " + vfimagefile);
		logger.info("uuid: " + uuid);

		String vxfAbsfile = VFIMAGESDIR + uuid + File.separator + vfimagefile;
		logger.info("VxF RESOURCE FILE: " + vxfAbsfile);
		File file = new File(vxfAbsfile);


		InputStream in = new FileInputStream( file );
		return IOUtils.toByteArray(in);
		
	}
	
	
	

	@PutMapping( value =  "/admin/vfimages", produces = "application/json", consumes = "multipart/form-data" )
	public ResponseEntity<?> updateVFImage(@PathVariable("uuid") int infraid, 
			final @ModelAttribute("vfimage") String v,
			@RequestParam(name = "prodFile", required = false) MultipartFile  prodFile,
			HttpServletRequest request) {
		
		VFImage vfimg = null;
		
		String emsg = "";

		try {
			try {
				vfimg = objectMapper.readValue( v, VFImage.class);	
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}  
			
			if ( !checkUserIDorIsAdmin( -1 ) ){
				return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.FORBIDDEN) ;
			}

			logger.info("Received @PUT for VFImage : " + vfimg.getName());
			
			vfimg = updateVFImage(vfimg, prodFile,	request);

		} catch (JsonProcessingException e) {
			vfimg = null;
			e.printStackTrace();
			logger.error( e.getMessage() );
			emsg =  e.getMessage();
		} catch (IOException e) {
			vfimg = null;
			e.printStackTrace();
			logger.error( e.getMessage() );
			emsg =  e.getMessage();
		}


		if (vfimg != null) {

			BusController.getInstance().aVFImageUpdated( vfimg );	
			return ResponseEntity.ok( vfimg  );
		} else {
			return (ResponseEntity<?>) ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).body( "Requested Image cannot be inserted" );
			
		}

	}

	@DeleteMapping( value =  "/admin/vfimages/{id}", produces = "application/json", consumes = "application/json" )
	public ResponseEntity<?> deleteVFImage(@PathVariable("id") int id) {
		
		VFImage sm = vfImageService.getVFImageByID(id);
		
		if ( !checkUserIDorIsAdmin( -1 ) ){
			return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.FORBIDDEN) ;
		}

		vfImageService.deleteVFImage( sm );
	
		return ResponseEntity.ok( "{}"  );

	}


	@GetMapping( value = "/admin/vfimages/{id}", produces = "application/json" )
	public ResponseEntity<?> getVFImageById(@PathVariable("id") int id) {
		VFImage sm = vfImageService.getVFImageByID(id);

		if (sm != null) {
			
			if ( !checkUserIDorIsAdmin( -1 ) ){
				return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.FORBIDDEN) ;
			}
			
			return ResponseEntity.ok( sm );
		} else {
			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}
	

	@GetMapping( value = "/admin/vfimages/name/{imagename}", produces = "application/json" )
	public ResponseEntity<?> getVFImageByName(@PathVariable("imagename") String imagename) {
		VFImage sm = vfImageService.getVFImageByName( imagename );

		if (sm != null) {
			if ( !checkUserIDorIsAdmin( -1 ) ){
				return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.FORBIDDEN) ;
			}
	
			return ResponseEntity.ok( sm  );
		} else {
			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}


	/**
	 * @param userID
	 * @return true if user logged is equal to the requested id of owner, or is ROLE_ADMIN
	 */
	private boolean checkUserIDorIsAdmin(long userID){

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		logger.info("principal 1=  " + authentication.getAuthorities().contains( new SimpleGrantedAuthority( UserRoleType.ROLE_ADMIN.getValue()  ) ));
		logger.info("principal 2=  " + authentication.getAuthorities().contains( new SimpleGrantedAuthority(  UserRoleType.ROLE_TESTBED_PROVIDER.getValue() ) ));
		logger.info("principal 2=  " + authentication.getAuthorities().contains( new SimpleGrantedAuthority(  UserRoleType.ROLE_MENTOR.getValue() ) ));

		if ( authentication.getAuthorities().contains( new SimpleGrantedAuthority( UserRoleType.ROLE_ADMIN.getValue() ))){
			logger.info("checkUserIDorIsAdmin, authentication role =  " + authentication.getAuthorities().contains( new SimpleGrantedAuthority( UserRoleType.ROLE_ADMIN.getValue()  ) ));
			return true;
		}

		PortalUser uToFind = usersService.findById(  userID );
		if ( (uToFind !=null )  && ( uToFind.getUsername()  == authentication.getName()) ){
			logger.info("checkUserIDorIsAdmin, user is equal with request");
			return true;
		} 
		

		return false;
	}

}
