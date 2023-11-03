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

package portal.api.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;

import io.openslice.centrallog.client.CLevel;
import io.openslice.centrallog.client.CentralLogger;
import io.openslice.model.Category;
import io.openslice.model.CompositeExperimentOnBoardDescriptor;
import io.openslice.model.CompositeVxFOnBoardDescriptor;
import io.openslice.model.ConstituentVxF;
import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.DeploymentDescriptorStatus;
import io.openslice.model.DeploymentDescriptorVxFPlacement;
import io.openslice.model.ExperimentMetadata;
import io.openslice.model.ExperimentOnBoardDescriptor;
import io.openslice.model.Infrastructure;
import io.openslice.model.MANOplatform;
import io.openslice.model.MANOprovider;
import io.openslice.model.OnBoardingStatus;
import io.openslice.model.PortalProperty;
import io.openslice.model.PortalUser;
import io.openslice.model.Product;
import io.openslice.model.UserRoleType;
import io.openslice.model.VFImage;
import io.openslice.model.ValidationJob;
import io.openslice.model.ValidationStatus;
import io.openslice.model.VxFMetadata;
import io.openslice.model.VxFOnBoardedDescriptor;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import portal.api.bus.BusController;
//import portal.api.centrallog.CLevel;
//import portal.api.centrallog.CentralLogger;
import portal.api.mano.MANOController;
import portal.api.service.CategoryService;
import portal.api.service.DeploymentDescriptorService;
import portal.api.service.InfrastructureService;
import portal.api.service.ManoPlatformService;
import portal.api.service.ManoProviderService;
import portal.api.service.NSDOBDService;
import portal.api.service.NSDService;
import portal.api.service.PortalPropertiesService;
import portal.api.service.ProductService;
import portal.api.service.UsersService;
import portal.api.service.VFImageService;
import portal.api.service.VxFOBDService;
import portal.api.service.VxFService;
import portal.api.util.AttachmentUtil;
import portal.api.validation.ci.ValidationJobResult;

/**
 * @author ctranoris
 *
 */
@RestController
//@RequestMapping("/repo")
public class ArtifactsAPIController {
//	

//	private MANOController aMANOController;

	private static final transient Log logger = LogFactory.getLog(ArtifactsAPIController.class.getName());

	private static final String METADATADIR = System.getProperty("user.home") + File.separator + ".portal"
			+ File.separator + "metadata" + File.separator;

//    @Resource(name="authenticationManager")
//    private AuthenticationManager authManager;

	@Autowired
	PortalPropertiesService propsService;

	@Autowired
	UsersService usersService;

	@Autowired
	ManoProviderService manoProviderService;

	@Autowired
	VxFService vxfService;

	@Autowired
	NSDService nsdService;

	@Autowired
	VxFOBDService vxfOBDService;
	@Autowired
	NSDOBDService nsdOBDService;

	@Autowired
	VFImageService vfImageService;

	@Autowired
	ProductService productService;

	@Autowired
	ManoPlatformService manoPlatformService;

	@Autowired
	MANOController aMANOController;

	@Autowired
	CategoryService categoryService;

	@Autowired
	InfrastructureService infrastructureService;

	@Autowired
	DeploymentDescriptorService deploymentDescriptorService;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	BusController busController;

	@Autowired
	ProducerTemplate template;

	@Value("${spring.application.name}")
	private String compname;

	@Autowired
	private CentralLogger centralLogger;

	/**
	 * update the properties to Bus
	 */
	@PostConstruct
	private void sendPropertiesToBus() {
		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
			String props;
			props = mapper.writeValueAsString(propsService.getPropertiesAsMap());
			busController.propertiesUpdate(props);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

	}

	// VxFS API

	private Product addNewProductData(Product prod, MultipartFile image, MultipartFile submittedFile,
			MultipartFile[] screenshots, HttpServletRequest request) throws IOException {

		String uuid = UUID.randomUUID().toString();

		logger.info("prodname = " + prod.getName());
		logger.info("version = " + prod.getVersion());
		logger.info("shortDescription = " + prod.getShortDescription());
		logger.info("longDescription = " + prod.getLongDescription());

		prod.setUuid(uuid);
		prod.setDateCreated(new Date());
		prod.setDateUpdated(new Date());

		// String[] catIDs = categories.split(",");
		// for (String catid : catIDs) {
		// Category category = portalRepositoryRef.getCategoryByID(
		// Integer.valueOf(catid) );
		// prod.addCategory(category);
		// }

		// for (ProductExtensionItem e : extensions) {
		//
		// }
		//
		// String[] exts = extensions.split(",");
		// for (String extparmval : exts) {
		// String[] i = extparmval.split("=");
		// prod.addExtensionItem(i[0], i[1]);
		// }

		String endpointUrl = request.getContextPath();// request.getRequestURI();

		String tempDir = METADATADIR + uuid + File.separator;

		Files.createDirectories(Paths.get(tempDir));

		// If an icon is submitted
		if (image != null) {
			// Get the icon filename
			String imageFileNamePosted = image.getOriginalFilename();// AttachmentUtil.getFileName(image.getHeaders());
			logger.info("image = " + imageFileNamePosted);
			// If there is an icon name
			if (!imageFileNamePosted.equals("")) {
				// Save the icon File
				String imgfile = AttachmentUtil.saveFile(image, tempDir + imageFileNamePosted);
				logger.info("imgfile saved to = " + imgfile);
				// Save the icon file destination
				prod.setIconsrc(
						endpointUrl.toString().replace("http:", "") + "/images/" + uuid + "/" + imageFileNamePosted);
			}
		}

		if (submittedFile != null) {
			// Get the filename
			String aFileNamePosted = submittedFile.getOriginalFilename();// AttachmentUtil.getFileName(submittedFile.getHeaders());
			logger.info("vxfFile = " + aFileNamePosted);
			// Is the filename is not an empty string
			if (!aFileNamePosted.equals("")) {
				// Save the file to the specified path and return the new path.
				String descriptorFilePath = AttachmentUtil.saveFile(submittedFile, tempDir);
				// Set the package location in Product instance
				logger.info("vxffilepath saved to = " + descriptorFilePath);
				prod.setPackageLocation(
						endpointUrl.toString().replace("http:", "") + "/packages/" + uuid + "/" + aFileNamePosted);

				// If it is a VxF Object
				if (prod instanceof VxFMetadata) {
					try {
						AttachmentUtil attachmentInfo = new AttachmentUtil();
						attachmentInfo.extractYAMLfileAndIcon(descriptorFilePath);
						this.loadVxfMetadataFromOSMVxFDescriptorFile((VxFMetadata) prod, attachmentInfo, endpointUrl);
					} catch (NullPointerException e) {
						e.printStackTrace();
						return null;
					}
					logger.info("After " + prod.getPackageLocation());
				} else if (prod instanceof ExperimentMetadata) {
					// If prod is an NS Descriptor
					try {
						AttachmentUtil attachmentInfo = new AttachmentUtil();
						attachmentInfo.extractYAMLfileAndIcon(descriptorFilePath);
						this.loadNSMetadataFromOSMNSDescriptorFile((ExperimentMetadata) prod, attachmentInfo,
								endpointUrl);
					} catch (NullPointerException e) {
						e.printStackTrace();
						return null;
					}
				}
			}
		}
		// screenshots are provided during the call of the function

		if (screenshots != null) {
			String screenshotsFilenames = "";
			int i = 1;
			for (MultipartFile shot : screenshots) {
				String shotFileNamePosted = shot.getOriginalFilename();// AttachmentUtil.getFileName(shot.getHeaders());
				logger.info("Found screenshot image shotFileNamePosted = " + shotFileNamePosted);
				logger.info("shotFileNamePosted = " + shotFileNamePosted);
				if (!shotFileNamePosted.equals("")) {
					shotFileNamePosted = "shot" + i + "_" + shotFileNamePosted;
					String shotfilepath = AttachmentUtil.saveFile(shot, tempDir + shotFileNamePosted);
					logger.info("shotfilepath saved to = " + shotfilepath);
					shotfilepath = endpointUrl.toString().replace("http:", "") + "/images/" + uuid + "/"
							+ shotFileNamePosted;
					screenshotsFilenames += shotfilepath + ",";
					i++;
				}
			}
			if (screenshotsFilenames.length() > 0)
				screenshotsFilenames = screenshotsFilenames.substring(0, screenshotsFilenames.length() - 1);

			prod.setScreenshots(screenshotsFilenames);

		}

//		// we must replace given product categories with the ones from our DB
//		for (Category c : prod.getCategories()) {
//			Category catToUpdate = categoryService.findById( c.getId() );
//			// logger.info("BEFORE PROD SAVE, category "+catToUpdate.getName()+"
//			// contains Products: "+ catToUpdate.getProducts().size() );
//			//prod.getCategories().set(prod.getCategories().indexOf(c), catToUpdate);
//			prod.getCategories().add(catToUpdate);
//
//		}

		// if it's a VxF we need also to update the images that this VxF will use
		if (prod instanceof VxFMetadata) {
			VxFMetadata vxfm = (VxFMetadata) prod;
			for (VFImage vfimg : vxfm.getVfimagesVDU()) {
				vfimg.getUsedByVxFs().add(vxfm);
			}
		}

		// Save now vxf for User
		PortalUser vxfOwner = usersService.findById(prod.getOwner().getId());
		vxfOwner.addProduct(prod);
		prod.setOwner(vxfOwner); // replace given owner with the one from our DB

		PortalUser owner = usersService.updateUserInfo(vxfOwner, false);

		Product registeredProd = null;
		if (prod instanceof VxFMetadata) {
			registeredProd = vxfService.getVxFByUUID(uuid);
		} else {
			registeredProd = nsdService.getdNSDByUUID(uuid);
		}

		// now fix category references
		for (Category c : prod.getCategories()) {
			Category catToUpdate = categoryService.findById(c.getId());
			catToUpdate.addProduct(registeredProd);
			categoryService.updateCategoryInfo(catToUpdate);
		}

		return registeredProd;
	}

	private void loadVxfMetadataFromOSMVxFDescriptorFile(VxFMetadata prod, AttachmentUtil attachmentInfo,
			String endpointUrl) throws IOException, NullPointerException {
		VxFMetadata tmp_prod = busController.getVNFDMetadataFromMANO(prod.getPackagingFormat().name(),
				attachmentInfo.getDescriptorYAMLfile());
		if (tmp_prod != null) {
			// *************LOAD THE Product Object from the VNFD Descriptor
			// START************************************
			// Check if a vnfd with this id already exists in the DB
			List<VxFMetadata> existingvxf = vxfService.getAllVxFByName(tmp_prod.getName());
			if (existingvxf != null && existingvxf.size()>0) {
				throw new IOException(
						"Descriptor with same name already exists. No updates were performed. Please change the name of the descriptor");
			}
			// Get the name for the db
			prod.setName(tmp_prod.getName());
			prod.setVersion(tmp_prod.getVersion());
			prod.setVendor(tmp_prod.getVendor());
			prod.setShortDescription(tmp_prod.getShortDescription());
			prod.setLongDescription(tmp_prod.getLongDescription());

			prod.setValidationStatus(ValidationStatus.UNDER_REVIEW);
			prod.getVfimagesVDU().clear();// clear previous referenced images
			for (VFImage vfImage : ((VxFMetadata) tmp_prod).getVfimagesVDU()) {
				String imageName = vfImage.getName();
				if ((imageName != null) && (!imageName.equals(""))) {
					VFImage sm = vfImageService.getVFImageByName(imageName);
					if (sm == null) {
						sm = new VFImage();
						sm.setName(imageName);
						PortalUser vfImagewner = usersService.findById(prod.getOwner().getId());
						sm.setOwner(vfImagewner);
						sm.setShortDescription("Automatically created during vxf " + prod.getName()
								+ " submission. Owner must update.");
						String uuidVFImage = UUID.randomUUID().toString();
						sm.setUuid(uuidVFImage);
						sm.setDateCreated(new Date());
						sm = vfImageService.saveVFImage(sm);
					}
					prod.getVfimagesVDU().add(sm);
				}
			}

			// Store the requirements in HTML
			prod.setDescriptorHTML(tmp_prod.getDescriptorHTML());
			// Store the YAML file
			prod.setDescriptor(tmp_prod.getDescriptor());

			// If we got an IconfilePath file from/through the vnfExtractor
			if (attachmentInfo.getIconfilePath() != null && tmp_prod.getIconsrc() != null) {
				Path path = Paths.get(tmp_prod.getIconsrc());
				String imageFileNamePosted = path.getFileName().toString();
				logger.info("image = " + imageFileNamePosted);
				// If the name is not empty
				if (!imageFileNamePosted.equals("")) {
					String imgfile = AttachmentUtil.saveFile(attachmentInfo.getIconfilePath(),
							METADATADIR + prod.getUuid() + File.separator + imageFileNamePosted);
					logger.info("imgfile saved to = " + imgfile);
					prod.setIconsrc(
							endpointUrl.replace("http:", "") + "/images/" + prod.getUuid() + "/" + imageFileNamePosted);
				}
			}
			// *************LOAD THE Product Object from the VNFD Descriptor
			// END************************************
		} else {
			throw new NullPointerException();
		}
	}

	private void loadNSMetadataFromOSMNSDescriptorFile(ExperimentMetadata prod, AttachmentUtil attachmentInfo,
			String endpointUrl) throws IOException, NullPointerException {
		ExperimentMetadata tmp_prod = busController.getNSDMetadataFromMANO(prod.getPackagingFormat().name(),
				attachmentInfo.getDescriptorYAMLfile());
		if (tmp_prod != null) {
			// *************LOAD THE Product Object from the NSD Descriptor
			// START************************************
			// Check if a vnfd with this id already exists in the DB

			ExperimentMetadata existingns = nsdService.getNSDByName(tmp_prod.getName());
			if ((existingns != null)) {
				throw new IOException("Descriptor with same name already exists. No updates were performed.");
			}
			prod.setName(tmp_prod.getName());
			prod.setVersion(tmp_prod.getVersion());
			prod.setVendor(tmp_prod.getVendor());
			prod.setShortDescription(tmp_prod.getShortDescription());
			prod.setLongDescription(tmp_prod.getLongDescription());

			for (ConstituentVxF cvxf : tmp_prod.getConstituentVxF()) {
				cvxf.setVxfref(vxfService.getVxFByName(cvxf.getVnfdidRef()));
				((ExperimentMetadata) prod).getConstituentVxF().add(cvxf);
			}
			// Store the requirements in HTML
			prod.setDescriptorHTML(tmp_prod.getDescriptorHTML());
			// Store the YAML file
			prod.setDescriptor(tmp_prod.getDescriptor());

			// If we got an IconfilePath file from/through the vnfExtractor
			if (attachmentInfo.getIconfilePath() != null && tmp_prod.getIconsrc() != null) {
				Path path = Paths.get(tmp_prod.getIconsrc());
				String imageFileNamePosted = path.getFileName().toString();
				logger.info("image = " + imageFileNamePosted);
				// If the name is not empty
				if (!imageFileNamePosted.equals("")) {
					String imgfile = AttachmentUtil.saveFile(attachmentInfo.getIconfilePath(),
							METADATADIR + prod.getUuid() + File.separator + imageFileNamePosted);
					logger.info("imgfile saved to = " + imgfile);
					prod.setIconsrc(endpointUrl + "/images/" + prod.getUuid() + "/" + imageFileNamePosted);
				}
			}
			// *************LOAD THE Product Object from the NSD Descriptor
			// END************************************
		} else {
			throw new NullPointerException();
		}
	}

	private void updateVxfMetadataFromOSMVxFDescriptorFile(VxFMetadata prevProduct, AttachmentUtil attachmentInfo,
			String endpointUrl) throws IOException, NullPointerException {
		VxFMetadata tmp_prod = busController.getVNFDMetadataFromMANO(prevProduct.getPackagingFormat().name(),
				attachmentInfo.getDescriptorYAMLfile());
		if (tmp_prod != null) {
			// on update we need to check if name and version are the same. Only then we
			// will accept it
			if (!prevProduct.getName().equals(tmp_prod.getName())
					|| !prevProduct.getVersion().equals(tmp_prod.getVersion())) {
				throw new IOException(
						"Name and version are not equal to existing descriptor. No updates were performed.");
			}
			if (((VxFMetadata) prevProduct).isCertified()) {
				throw new IOException("Descriptor is already Validated and cannot change! No updates were performed.");
			}

			// Get the name for the db
			prevProduct.setName(tmp_prod.getName());
			prevProduct.setVersion(tmp_prod.getVersion());
			prevProduct.setVendor(tmp_prod.getVendor());
			prevProduct.setShortDescription(tmp_prod.getShortDescription());
			prevProduct.setLongDescription(tmp_prod.getLongDescription());

			((VxFMetadata) prevProduct).setValidationStatus(ValidationStatus.UNDER_REVIEW);

			for (VFImage img : ((VxFMetadata) prevProduct).getVfimagesVDU()) {
				logger.info("img.getUsedByVxFs().remove(prevProduct) = " + img.getUsedByVxFs().remove(prevProduct));
				vfImageService.updateVFImageInfo(img);
			}
			((VxFMetadata) prevProduct).getVfimagesVDU().clear();// clear previous referenced images
			for (VFImage vfImage : ((VxFMetadata) tmp_prod).getVfimagesVDU()) {
				String imageName = vfImage.getName();
				if ((imageName != null) && (!imageName.equals(""))) {
					VFImage sm = vfImageService.getVFImageByName(imageName);
					if (sm == null) {
						sm = new VFImage();
						sm.setName(imageName);
						PortalUser vfImagewner = usersService.findById(prevProduct.getOwner().getId());
						sm.setOwner(vfImagewner);
						sm.setShortDescription("Automatically created during vxf " + prevProduct.getName()
								+ " submission. Owner must update.");
						String uuidVFImage = UUID.randomUUID().toString();
						sm.setUuid(uuidVFImage);
						sm.setDateCreated(new Date());
						sm = vfImageService.saveVFImage(sm);
					}
					if (!((VxFMetadata) prevProduct).getVfimagesVDU().contains(sm)) {
						((VxFMetadata) prevProduct).getVfimagesVDU().add(sm);
						sm.getUsedByVxFs().add(((VxFMetadata) prevProduct));
						vfImageService.updateVFImageInfo(sm);
					}
				}
			}
			// Store the requirements in HTML
			prevProduct.setDescriptorHTML(tmp_prod.getDescriptorHTML());
			// Store the YAML file
			prevProduct.setDescriptor(tmp_prod.getDescriptor());
			// If we got an IconfilePath file from/through the vnfExtractor
			if (attachmentInfo.getIconfilePath() != null && tmp_prod.getIconsrc() != null) {
				Path path = Paths.get(tmp_prod.getIconsrc());
				String imageFileNamePosted = path.getFileName().toString();
				logger.info("image = " + imageFileNamePosted);
				// If the name is not empty
				if (!imageFileNamePosted.equals("")) {
					String imgfile = AttachmentUtil.saveFile(attachmentInfo.getIconfilePath(),
							METADATADIR + prevProduct.getUuid() + File.separator + imageFileNamePosted);
					logger.info("imgfile saved to = " + imgfile);
					prevProduct.setIconsrc(endpointUrl.replace("http:", "") + "/images/" + prevProduct.getUuid() + "/"
							+ imageFileNamePosted);
				}
			}
			// *************LOAD THE Product Object from the VNFD Descriptor
			// END************************************
		} else {
			throw new NullPointerException();
		}
	}

	private void updateNSMetadataFromOSMNSDescriptorFile(ExperimentMetadata prevProduct, AttachmentUtil attachmentInfo,
			String endpointUrl) throws IOException, NullPointerException {
		ExperimentMetadata tmp_prod = busController.getNSDMetadataFromMANO(prevProduct.getPackagingFormat().name(),
				attachmentInfo.getDescriptorYAMLfile());
		if (tmp_prod != null) {

			// *************LOAD THE Product Object from the NSD Descriptor
			// START************************************
			// on update we need to check if name and version are the same. Only then we
			// will accept it
			if (!prevProduct.getName().equals(tmp_prod.getName())
					|| !prevProduct.getVersion().equals(tmp_prod.getVersion())) {
				throw new IOException(
						"Name and version are not equal to existing descriptor. No updates were performed.");
			}
			if (((ExperimentMetadata) prevProduct).isValid()) {
				throw new IOException("Descriptor is already Validated and cannot change! No updates were performed.");
			}
			prevProduct.setName(tmp_prod.getName());
			prevProduct.setVersion(tmp_prod.getVersion());
			prevProduct.setVendor(tmp_prod.getVendor());
			prevProduct.setShortDescription(tmp_prod.getShortDescription());
			prevProduct.setLongDescription(tmp_prod.getLongDescription());

			for (ConstituentVxF cvxf : tmp_prod.getConstituentVxF()) {
				((ExperimentMetadata) prevProduct).getConstituentVxF().add(cvxf);
			}

			// Store the requirements in HTML
			prevProduct.setDescriptorHTML(tmp_prod.getDescriptorHTML());
			// Store the YAML file
			prevProduct.setDescriptor(tmp_prod.getDescriptor());

			// If we got an IconfilePath file from/through the vnfExtractor
			if (attachmentInfo.getIconfilePath() != null && tmp_prod.getIconsrc() != null) {
				Path path = Paths.get(tmp_prod.getIconsrc());
				String imageFileNamePosted = path.getFileName().toString();
				logger.info("image = " + imageFileNamePosted);
				// If the name is not empty
				if (!imageFileNamePosted.equals("")) {
					String imgfile = AttachmentUtil.saveFile(attachmentInfo.getIconfilePath(),
							METADATADIR + prevProduct.getUuid() + File.separator + imageFileNamePosted);
					logger.info("imgfile saved to = " + imgfile);
					prevProduct.setIconsrc(endpointUrl.replace("http:", "") + "/images/" + prevProduct.getUuid() + "/"
							+ imageFileNamePosted);
				}
			}
			// *************LOAD THE Product Object from the NSD Descriptor
			// END************************************
		} else {
			throw new NullPointerException();
		}

	}

	/******************* VxFs API ***********************/

	@GetMapping(value = "/vxfs", produces = "application/json")
	public ResponseEntity<?> getAllVxFs(@RequestParam(name = "categoryid", required = false) Long categoryid) {

		logger.info("getVxFs categoryid=" + categoryid);
		List<VxFMetadata> vxfs = vxfService.getPublishedVxFsByCategory(categoryid); // portalRepositoryRef.getVxFs(categoryid,
																					// true);
		return ResponseEntity.ok(vxfs);
	}

	// @PreAuthorize("#oauth2.hasScope('read') and #oauth2.hasScope('admin')")
	@GetMapping(value = "/admin/vxfs", produces = "application/json")
	@ResponseBody
	public ResponseEntity<?> getVxFs(@RequestParam(name = "categoryid", required = false) Long categoryid,
			HttpServletRequest request) {
		logger.info("getVxFs categoryid=" + categoryid);

		// Object attr = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
		// SecurityContextHolder.setContext((SecurityContext) attr);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		PortalUser u = usersService.findByUsername(authentication.getName());

		if (u != null) {
			List<VxFMetadata> vxfs;

			if (u.getRoles().contains(UserRoleType.ROLE_ADMIN)) {
				vxfs = vxfService.getVxFsByCategory(categoryid);
			} else {
				vxfs = vxfService.getVxFsByUserID((long) u.getId());
			}

			return ResponseEntity.ok(vxfs);

		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body("User not found in registry");
		}

	}

	@PostMapping(value = "/admin/vxfs")
	public ResponseEntity<?> addVxFMetadata(@ModelAttribute("vxf") String avxf,
			@RequestParam(name = "prodIcon", required = false) MultipartFile prodIcon,
			@RequestParam("prodFile") MultipartFile prodFile,
			@RequestParam(name = "screenshots", required = false) MultipartFile[] screenshots,
			HttpServletRequest request) {

		VxFMetadata vxf = null;
		try {
			vxf = objectMapper.readValue(avxf, VxFMetadata.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Object attr = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
		// SecurityContextHolder.setContext((SecurityContext) attr);

		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if (u == null) {
			return (ResponseEntity<String>) ResponseEntity.badRequest().body("User "
					+ SecurityContextHolder.getContext().getAuthentication().getName() + " not found in registry");
		}

		String emsg = "";

		VxFMetadata vxfsaved = null;
		try {

			logger.info("Received @POST for vxf : " + vxf.getName());
			logger.info("Received @POST for vxf.extensions : " + vxf.getExtensions());
			vxfsaved = (VxFMetadata) addNewProductData(vxf, prodIcon, prodFile, screenshots, request);
		} catch (Exception e) {
			vxfsaved = null;
			e.printStackTrace();
			logger.error("Exception during adding New Product Data with message" + e.getMessage());
			emsg = e.getMessage();
		}

		if (vxfsaved != null) {
			// ======================================================
			// AUTOMATIC ONBOARDING PROCESS -START
			// Need to select MANO Provider, convert vxfMetadata to VxFOnBoardedDescriptor
			// and pass it as an input.

			// Get the MANO providers which are set for automatic onboarding

			busController.newVxFUploadedToPortalRepo(vxf.getId());

			List<MANOprovider> MANOprovidersEnabledForOnboarding = manoProviderService
					.getMANOprovidersEnabledForOnboarding();

			for (MANOprovider mp : MANOprovidersEnabledForOnboarding) {
				logger.error(" ========> vxfsaved.getPackagingFormat() = " + vxfsaved.getPackagingFormat());
				if (vxfsaved.getPackagingFormat().toString().equals(mp.getSupportedMANOplatform().getVersion())) {
					// Create VxfOnboardedDescriptor
					VxFOnBoardedDescriptor obd = new VxFOnBoardedDescriptor();
					// Get the first one for now
					obd.setObMANOprovider(mp);
					obd.setUuid(UUID.randomUUID().toString());
					VxFMetadata refVxF = (VxFMetadata) vxfService.getVxFById(vxfsaved.getId());
					// Fill the VxFMetadata of VxFOnBoardedDescriptor
					obd.setVxf(refVxF);
					// save obd
					obd = vxfOBDService.updateVxFOnBoardedDescriptor(obd);

					// Update the VxFMetadata Object with the obd Object
					refVxF.getVxfOnBoardedDescriptors().add(obd);

					// save product
					refVxF = (VxFMetadata) vxfService.updateProductInfo(refVxF);

					// save VxFonBoardedDescriptor or not ???

					// set proper scheme (http or https)
					// MANOController.setHTTPSCHEME( request.getRequestURL().toString() );

					if (obd.getVxf().getOwner() == null) {
						logger.error(" ========> obd.getVxf().getOwner() == null ");
					}

					// ***************************************************************************************************************************\
					// Because in portal.api.mano we need the url for the package location in order
					// not to ask back, if the package locations
					// does not contain http add the default maindomain value.
					// We can either add it here or change that where the pLocation is set initially
					// for the object.
					// Get the location of the package
					String pLocation = obd.getVxf().getPackageLocation();
					logger.info("VxF Package Location: " + pLocation);
					if (!pLocation.contains("http")) {
						pLocation = propsService.getPropertyByName("maindomain").getValue() + pLocation;
						obd.getVxf().setPackageLocation(pLocation);
						vxfService.updateProductInfo(obd.getVxf());
					}
					logger.info("PROPER VxF Package Location: " + pLocation);
					// ***************************************************************************************************************************

					// Send the message for automatic onboarding
					// busController.onBoardVxFAdded( obd );

					try {
						String[] fpath = obd.getVxf().getPackageLocation().split("/");
						logger.info("uuid: " + fpath[fpath.length - 2]);
						logger.info("Package: " + fpath[fpath.length - 1]);
						String vxfAbsfile = METADATADIR + fpath[fpath.length - 2] + File.separator
								+ fpath[fpath.length - 1];
						File afile = new File(vxfAbsfile);
						Path path = Paths.get(afile.getAbsolutePath());
						// ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
						// busController.onBoardVxFAdded( obd, afile, resource );

						CompositeVxFOnBoardDescriptor compositeobdobj = new CompositeVxFOnBoardDescriptor();
						compositeobdobj.setFilename(afile.getName());
						compositeobdobj.setAllBytes(Files.readAllBytes(path));
						compositeobdobj.setObd(obd);
						busController.onBoardVxFAddedByCompositeObj(compositeobdobj);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			// AUTOMATIC ONBOARDING PROCESS -END
			// ======================================================
			VxFMetadata vxfr = (VxFMetadata) vxfService.getVxFById(vxfsaved.getId());// rereading this, seems to
																							// keep the DB connection
			busController.validateVxF(vxfr);
			return ResponseEntity.ok(vxfr);
		} else {
			return (ResponseEntity<String>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{ \"message\" : \"Requested entity cannot be installed. " + emsg + "\"}");
			// return (ResponseEntity<?>) ResponseEntity.badRequest().body( "Requested
			// entity cannot be installed. " + emsg );
		}

	}

	/**
	 * @param userID
	 * @return true if user logged is equal to the requested id of owner, or is
	 *         ROLE_ADMIN
	 */
	private boolean checkUserIDorIsAdmin(long userID) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		logger.info("principal 1=  " + authentication.getAuthorities()
				.contains(new SimpleGrantedAuthority(UserRoleType.ROLE_ADMIN.getValue())));
		logger.info("principal 2=  " + authentication.getAuthorities()
				.contains(new SimpleGrantedAuthority(UserRoleType.ROLE_TESTBED_PROVIDER.getValue())));
		logger.info("principal 2=  " + authentication.getAuthorities()
				.contains(new SimpleGrantedAuthority(UserRoleType.ROLE_MENTOR.getValue())));

		if (authentication.getAuthorities().contains(new SimpleGrantedAuthority(UserRoleType.ROLE_ADMIN.getValue()))) {
			logger.info("checkUserIDorIsAdmin, authentication role =  " + authentication.getAuthorities()
					.contains(new SimpleGrantedAuthority(UserRoleType.ROLE_ADMIN.getValue())));
			return true;
		}

		PortalUser uToFind = usersService.findById(userID);
		if ((uToFind != null) && (uToFind.getUsername() == authentication.getName())) {
			logger.info("checkUserIDorIsAdmin, user is equal with request");
			return true;
		}

		return false;
	}

	@PutMapping(value = "/admin/vxfs/{bid}", produces = "application/json", consumes = "multipart/form-data")
	public ResponseEntity<?> updateVxFMetadata(@PathVariable("bid") int bid, @ModelAttribute("vxf") String avxf,
			@RequestParam(name = "prodIcon", required = false) MultipartFile prodIcon,
			@RequestParam(name = "prodFile", required = false) MultipartFile prodFile,
			@RequestParam(name = "screenshots", required = false) MultipartFile[] screenshots,
			HttpServletRequest request) throws ForbiddenException {

		VxFMetadata vxf = null;
		try {
			vxf = objectMapper.readValue(avxf, VxFMetadata.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if (u == null) {
			return (ResponseEntity<?>) ResponseEntity.badRequest().body("User not found in registry");
		}

		String emsg = "";
		VxFMetadata vxfsaved = null;

		try {

			if (!checkUserIDorIsAdmin(vxf.getOwner().getId())) {
				throw new ForbiddenException("The requested page is forbidden"); // return (ResponseEntity<?>)
																					// ResponseEntity.status(HttpStatus.FORBIDDEN);

			}

			logger.info("Received @PUT for vxf : " + vxf.getName());
			logger.info("Received @PUT for vxf.extensions : " + vxf.getExtensions());

			vxfsaved = (VxFMetadata) updateProductMetadata(vxf, prodIcon, prodFile, screenshots, request);
		} catch (IOException e) {
			vxfsaved = null;
			e.printStackTrace();
			logger.error(e.getMessage());
			emsg = e.getMessage();
		}

		if (vxfsaved != null) {

			busController.updatedVxF(vxfsaved);
			// notify only if validation changed

			if (prodFile != null) { // if the descriptor changed then we must re-trigger validation

				String vxfFileNamePosted = prodFile.getName();
				if (!vxfFileNamePosted.equals("unknown")) {

					busController.validateVxF(vxf);
				}
			}

			return ResponseEntity.ok(vxf);
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest()
					.body("Requested entity cannot be installed. " + emsg);
		}

	}

	// VxFs related API

	private Product updateProductMetadata(Product prod, MultipartFile image, MultipartFile prodFile,
			MultipartFile[] screenshots, HttpServletRequest request) throws IOException {

		logger.info("userid = " + prod.getOwner().getId());
		logger.info("prodname = " + prod.getName());
		logger.info("prodid = " + prod.getId());

		logger.info("produuid = " + prod.getUuid());
		logger.info("version = " + prod.getVersion());
		logger.info("shortDescription = " + prod.getShortDescription());
		logger.info("longDescription = " + prod.getLongDescription());

		// first remove all references of the product from the previous
		// categories

		Product prevProduct = (Product) productService.getProductByID(prod.getId());

		prevProduct.setDateUpdated(new Date());

		String endpointUrl = request.getContextPath();// request.getRequestURI();

		String tempDir = METADATADIR + prevProduct.getUuid() + File.separator;

		Files.createDirectories(Paths.get(tempDir));

		// If an icon is submitted
		if (image != null) {
			// Get the icon filename
			String imageFileNamePosted = image.getOriginalFilename();// AttachmentUtil.getFileName(image.getHeaders());
			logger.info("image = " + imageFileNamePosted);
			// If there is an icon name
			if (!imageFileNamePosted.equals("unknown")) {
				// Save the icon File
				String imgfile = AttachmentUtil.saveFile(image, tempDir + imageFileNamePosted);
				logger.info("imgfile saved to = " + imgfile);
				// Save the icon file destination
				prevProduct.setIconsrc(endpointUrl.toString().replace("http:", "") + "/images/" + prevProduct.getUuid()
						+ "/" + imageFileNamePosted);
			}
		}

		if (prevProduct instanceof VxFMetadata) {
			((VxFMetadata) prevProduct).setPackagingFormat(((VxFMetadata) prod).getPackagingFormat());
			prevProduct.setTermsOfUse(prod.getTermsOfUse());
			prevProduct.setPublished(prod.isPublished());
			((VxFMetadata) prevProduct).setCertifiedBy(((VxFMetadata) prod).getCertifiedBy());
			((VxFMetadata) prevProduct).getSupportedMANOPlatforms().clear();
			for (MANOplatform mp : ((VxFMetadata) prod).getSupportedMANOPlatforms()) {
				MANOplatform mpdb = manoPlatformService.getMANOplatformByID(mp.getId());
				((VxFMetadata) prevProduct).getSupportedMANOPlatforms().add(mpdb);
			}
			// if ( !((VxFMetadata) prevProduct).isCertified() ){ //allow for now to change
			// state
			((VxFMetadata) prevProduct).setCertified(((VxFMetadata) prod).isCertified());
			// }
			((VxFMetadata) prevProduct).setCertifiedBy(((VxFMetadata) prod).getCertifiedBy());

		} else if (prevProduct instanceof ExperimentMetadata) {

			prevProduct.setTermsOfUse(prod.getTermsOfUse());
			prevProduct.setPublished(prod.isPublished());
			// if ( !((ExperimentMetadata) prevProduct).isValid() ){ //allow for now to
			// change state
			((ExperimentMetadata) prevProduct).setValid(((ExperimentMetadata) prod).isValid());
			// }
			((ExperimentMetadata) prevProduct).setPackagingFormat(((ExperimentMetadata) prod).getPackagingFormat());
		}

		if (prodFile != null) {
			// Get the filename
			String aFileNamePosted = prodFile.getOriginalFilename();// AttachmentUtil.getFileName(prodFile.getHeaders());
			logger.info("vxfFile = " + aFileNamePosted);
			// Is the filename is not an empty string
			if (!aFileNamePosted.equals("unknown")) {
				String descriptorFilePath = AttachmentUtil.saveFile(prodFile, tempDir + aFileNamePosted);
				// Set the package location in Product instance
				logger.info("vxffilepath saved to = " + descriptorFilePath);
				prevProduct.setPackageLocation(endpointUrl.toString().replace("http:", "") + "/packages/"
						+ prevProduct.getUuid() + "/" + aFileNamePosted);
				// If it is a VxF Object
				if (prevProduct instanceof VxFMetadata) {
					try {
						AttachmentUtil attachmentInfo = new AttachmentUtil();
						attachmentInfo.extractYAMLfileAndIcon(descriptorFilePath);
						this.updateVxfMetadataFromOSMVxFDescriptorFile((VxFMetadata) prevProduct, attachmentInfo,
								endpointUrl);
					} catch (NullPointerException e) {
						logger.error("Null Pointer Exception during update VxF Metadata From OSM VxF Descriptor File");
						return null;
					}
					logger.info("After " + prod.getPackageLocation());
					// If prod is an NS Descriptor
				} else if (prevProduct instanceof ExperimentMetadata) {
					try {
						AttachmentUtil attachmentInfo = new AttachmentUtil();
						attachmentInfo.extractYAMLfileAndIcon(descriptorFilePath);
						this.loadNSMetadataFromOSMNSDescriptorFile((ExperimentMetadata) prod, attachmentInfo,
								endpointUrl);
					} catch (NullPointerException e) {
						logger.error("Null Pointer Exception during loading NS Metadata From OSM NS Descriptor File");
						return null;
					}

				}

			}
		}

		String screenshotsFilenames = "";
		int i = 1;
		if (screenshots != null){
			for (MultipartFile shot : screenshots) {
				String shotFileNamePosted = shot.getOriginalFilename(); // AttachmentUtil.getFileName(shot.getHeaders());
				logger.info("Found screenshot image shotFileNamePosted = " + shotFileNamePosted);
				logger.info("shotFileNamePosted = " + shotFileNamePosted);
				if (!shotFileNamePosted.equals("")) {
					shotFileNamePosted = "shot" + i + "_" + shotFileNamePosted;
					String shotfilepath = AttachmentUtil.saveFile(shot, tempDir + shotFileNamePosted);
					logger.info("shotfilepath saved to = " + shotfilepath);
					shotfilepath = endpointUrl.toString().replace("http:", "") + "/images/" + prevProduct.getUuid() + "/"
							+ shotFileNamePosted;
					screenshotsFilenames += shotfilepath + ",";
					i++;
				}
			}
		}
		if (screenshotsFilenames.length() > 0)
			screenshotsFilenames = screenshotsFilenames.substring(0, screenshotsFilenames.length() - 1);

		prevProduct.setScreenshots(screenshotsFilenames);

		// save product
		prevProduct = productService.updateProductInfo(prevProduct);

		// now fix category product references
		// first remove all
		for (Category c : prevProduct.getCategories()) {
			logger.info("Will remove product " + prevProduct.getName() + ", from Previous Category " + c.getName());
			c.removeProduct(prevProduct);
			categoryService.updateCategoryInfo(c);
		}
		// add only the defined
		for (Category catToUpdate : prod.getCategories()) {
			// Product p = portalRepositoryRef.getProductByID(prod.getId());
			Category c = categoryService.findById(catToUpdate.getId());
			c.addProduct(prevProduct);
			categoryService.updateCategoryInfo(c);
		}

//		if (vxfOwner.getProductById(prod.getId()) == null)
//			vxfOwner.addProduct(prod);
//		portalRepositoryRef.updateUserInfo( vxfOwner);
		return prevProduct;
	}

	@GetMapping(value = "/images/{uuid}/{imgfile:.+}", produces = { "image/jpeg", "image/png" })
	public @ResponseBody ResponseEntity<byte[]> getEntityImage(@PathVariable("uuid") String uuid,
			@PathVariable("imgfile") String imgfile) {
		logger.info("getEntityImage of uuid: " + uuid);
		String imgAbsfile = METADATADIR + uuid + File.separator + imgfile;
		logger.info("Image RESOURCE FILE: " + imgAbsfile);
		File file = new File(imgAbsfile);
		try {
			Path path = Paths.get(file.getAbsolutePath());
			// ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
			HttpHeaders headers = new HttpHeaders();
			InputStream in = new FileInputStream(file);

			byte[] media = IOUtils.toByteArray(in);
			headers.setCacheControl(CacheControl.noCache().getHeaderValue());
			headers.setContentType(MediaType.parseMediaType("image/jpeg;image/png"));

			ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(media, headers, HttpStatus.OK);
			return responseEntity;

		} catch (Exception e) {
			logger.error("Couldn't serialize response ByteArrayResource", e);
			return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
		}

	}

	@GetMapping(value = "/packages/{uuid}/{vxffile:.+}")
	public ResponseEntity<ByteArrayResource> downloadVxFPackage(@PathVariable("uuid") String uuid,
			@PathVariable("vxffile") String vxffile) throws IOException {

		logger.info("vxffile: " + vxffile);
		logger.info("uuid: " + uuid);

		String vxfAbsfile = METADATADIR + uuid + File.separator + vxffile;
		logger.info("VxF RESOURCE FILE: " + vxfAbsfile);
		File file = new File(vxfAbsfile);

		if ((uuid.equals("77777777-668b-4c75-99a9-39b24ed3d8be"))
				|| (uuid.equals("22cab8b8-668b-4c75-99a9-39b24ed3d8be"))) {
			URL res = getClass().getResource("/files/" + vxffile);
			logger.info("TEST LOCAL RESOURCE FILE: " + res);
			file = new File(res.getFile());
		}

		Product avxf = productService.getProducttByUUID(uuid);
//		PortalUser u =  usersService.findByUsername( SecurityContextHolder.getContext().getAuthentication().getName() );
//		if ((u == null) && (!avxf.isPublished() )) {
//			return (ResponseEntity<ByteArrayResource>) ResponseEntity.badRequest().build();
//		}

		Path path = Paths.get(file.getAbsolutePath());
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=" + file.getName())
				.contentLength(file.length()).contentType(MediaType.parseMediaType("application/gzip")).body(resource);

//		ResponseBuilder response = (ResponseEntity<?>).ok((Object) file);
//		response.header("Content-Disposition", "attachment; filename=" + file.getName());
//		return response.build();
	}

	@DeleteMapping(value = "/admin/vxfs/{vxfid}", produces = "application/json")
	public ResponseEntity<?> deleteVxF(@PathVariable("vxfid") int vxfid, HttpServletRequest request)
			throws ForbiddenException {

		// Object attr = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
		// SecurityContextHolder.setContext((SecurityContext) attr);

		VxFMetadata vxf = (VxFMetadata) vxfService.getVxFById(vxfid);

		if (!checkUserIDorIsAdmin(vxf.getOwner().getId())) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN);
		}
		// Get the OnBoarded Descriptors to OffBoard them
		List<VxFOnBoardedDescriptor> vxfobds = vxf.getVxfOnBoardedDescriptors();
		if (vxf.isCertified()) {
			return (ResponseEntity<?>) ResponseEntity.badRequest()
					.body("vxf with id=" + vxfid + " is Certified and will not be deleted");

		}
		if (vxfobds.size() > 0) {
			for (VxFOnBoardedDescriptor vxfobd_tmp : vxfobds) {
				if (vxfobd_tmp.getOnBoardingStatus() != OnBoardingStatus.ONBOARDED) {
					// vxf.getVxfOnBoardedDescriptors().remove(vxfobd_tmp);
					// vxfService.updateProductInfo(vxf);
					// vxfobd_tmp.setObMANOprovider(null);
					// vxfobd_tmp.setVxf(null);
					// vxfOBDService.deleteVxFOnBoardedDescriptor(vxfobd_tmp);
					continue;
				}
				OnBoardingStatus previous_status = vxfobd_tmp.getOnBoardingStatus();
				vxfobd_tmp.setOnBoardingStatus(OnBoardingStatus.OFFBOARDING);
				centralLogger.log(CLevel.INFO, "Onboarding Status change of VxF " + vxfobd_tmp.getVxf().getName()
						+ " to " + vxfobd_tmp.getOnBoardingStatus(), compname);

				VxFOnBoardedDescriptor u = vxfOBDService.updateVxFOnBoardedDescriptor(vxfobd_tmp);

				ResponseEntity<String> response = null;
				try {
					// response = aMANOController.offBoardVxFFromMANOProvider( vxfobd_tmp );
					response = busController.offBoardVxF(vxfobd_tmp);
					logger.info("offBoard VxF response:" + response.toString());
				} catch (HttpClientErrorException e) {
					logger.info("offBoard VxF exception:" + e.toString());
					logger.info("offBoard VxF exception response:" + response.toString());
//					vxfobd_tmp.setOnBoardingStatus(previous_status);
//					centralLogger.log(CLevel.INFO, "Onboarding Status change of VxF " + vxfobd_tmp.getVxf().getName()
//							+ " to " + vxfobd_tmp.getOnBoardingStatus(), compname);
//					vxfobd_tmp.setFeedbackMessage(e.getResponseBodyAsString());
//					u = vxfOBDService.updateVxFOnBoardedDescriptor(vxfobd_tmp);
//					return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
//							.body(e.getResponseBodyAsString());
				}

				if ((response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError())
						&& (!response.getStatusCode().equals(HttpStatus.NOT_FOUND))) // If response is 400like or
																						// 500like quit the deletion.
				{
					vxfobd_tmp.setOnBoardingStatus(previous_status);
					vxfobd_tmp.setFeedbackMessage(response.getBody());
					u = vxfOBDService.updateVxFOnBoardedDescriptor(vxfobd_tmp);
					return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders())
							.body(response.getBody());
				} else {
					if (response.getBody() != null) {
						vxfobd_tmp.setFeedbackMessage(response.getBody().toString());
					} else {
						vxfobd_tmp.setFeedbackMessage(response.toString());
					}
				}
				// UnCertify Upon OffBoarding
				// vxfobd_tmp.getVxf().setCertified(false);
				vxfobd_tmp.setOnBoardingStatus(OnBoardingStatus.OFFBOARDED);
				try {
					centralLogger.log(CLevel.INFO, "Onboarding Status change of VxF " + vxfobd_tmp.getVxf().getName()
							+ " to " + vxfobd_tmp.getOnBoardingStatus(), compname);
				} catch (Exception e) {
					centralLogger.log(CLevel.INFO, "No related VxF found for " + vxfobd_tmp.getId() + " in status  "
							+ vxfobd_tmp.getOnBoardingStatus(), compname);
				}
				u = vxfOBDService.updateVxFOnBoardedDescriptor(vxfobd_tmp);
				// busController.offBoardVxFSucceded( u );
			}
		}
		busController.deletedVxF(vxf);

		// remove from categories
		for (Category c : vxf.getCategories()) {
			if (c.getProducts().contains(vxf)) {
				c.getProducts().remove(vxf);
				categoryService.updateCategoryInfo(c);
			}
		}

		vxf.getCategories().clear();

		// remove from onboard descriptos
		for (VxFOnBoardedDescriptor vxfobd_tmp : vxfobds) {
			VxFOnBoardedDescriptor sm = vxfOBDService.getVxFOnBoardedDescriptorByID(vxfobd_tmp.getId());
			vxfOBDService.deleteVxFOnBoardedDescriptor(sm);

		}
		vxf.getVxfOnBoardedDescriptors().clear();

		// remove from constituent vxfs
		// Get deployment descriptorvxfplacements. We need to get the deployment
		// descriptors involved.

		PortalUser owner = usersService.findById(vxf.getOwner().getId());
		owner.getProducts().remove(vxf);
		usersService.updateUserInfo(owner, false);
		vxf.setOwner(null);

		// check also if deleted from consistuent VNFs
		vxfService.deleteProduct(vxf);
		return ResponseEntity.ok().body("{}");
	}

	@DeleteMapping(value = "/admin/vxfs/{vxfid}/softdelete", produces = "application/json")
	public ResponseEntity<?> softDeleteVxF(@PathVariable("vxfid") int vxfid, HttpServletRequest request)
			throws ForbiddenException {


		VxFMetadata vxf = (VxFMetadata) vxfService.getVxFById(vxfid);

		if (!checkUserIDorIsAdmin(vxf.getOwner().getId())) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN);
		}
		// Get the OnBoarded Descriptors to OffBoard them
		List<VxFOnBoardedDescriptor> vxfobds = vxf.getVxfOnBoardedDescriptors();
		if (vxf.isCertified()) {
			return (ResponseEntity<?>) ResponseEntity.badRequest()
					.body("vxf with id=" + vxfid + " is Certified and will not be deleted");
		}
		try
		{
			busController.deletedVxF(vxf);
			// remove from categories
			for (Category c : vxf.getCategories()) {
				if (c.getProducts().contains(vxf)) {
					c.getProducts().remove(vxf);
					categoryService.updateCategoryInfo(c);
				}
			}
	
			vxf.getCategories().clear();
	
			// remove from onboard descriptos
			for (VxFOnBoardedDescriptor vxfobd_tmp : vxfobds) {
				VxFOnBoardedDescriptor sm = vxfOBDService.getVxFOnBoardedDescriptorByID(vxfobd_tmp.getId());
				vxfOBDService.deleteVxFOnBoardedDescriptor(sm);
	
			}
			vxf.getVxfOnBoardedDescriptors().clear();
	
			PortalUser owner = usersService.findById(vxf.getOwner().getId());
			owner.getProducts().remove(vxf);
			usersService.updateUserInfo(owner, false);
			vxf.setOwner(null);
	
			// check also if deleted from consistuent VNFs
			vxfService.deleteProduct(vxf);
		}
		catch(Exception e)
		{
			return (ResponseEntity<?>) ResponseEntity.badRequest()
					.body("vxf with id=" + vxfid + " could not be deleted. Please check for possible dependencies.");			
		}
		return ResponseEntity.ok().body("{}");
	}
	
	
	@GetMapping(value = "/vxfs/{vxfid}", produces = "application/json")
	public ResponseEntity<?> getVxFMetadataByID(@PathVariable("vxfid") int vxfid) throws ForbiddenException {
		logger.info("getVxFMetadataByID  vxfid=" + vxfid);
		VxFMetadata vxf = (VxFMetadata) vxfService.getVxFById(vxfid);

		if (vxf != null) {

			if (!vxf.isPublished()) {
				throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																				// ResponseEntity.status(HttpStatus.FORBIDDEN);
			}

			return ResponseEntity.ok(vxf);
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest()
					.body("vxf with id=" + vxfid + " not found in portal registry");
		}
	}

	@GetMapping(value = "/admin/vxfs/{vxfid}", produces = "application/json")
	public ResponseEntity<?> getAdminVxFMetadataByID(@PathVariable("vxfid") int vxfid) throws ForbiddenException {

		logger.info("getAdminVxFMetadataByID  vxfid=" + vxfid);
		VxFMetadata vxf = (VxFMetadata) vxfService.getVxFById(vxfid);

		if (vxf != null) {

			PortalUser u = usersService
					.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

			if (!checkUserIDorIsAdmin(vxf.getOwner().getId())
					&& !u.getRoles().contains(UserRoleType.ROLE_TESTBED_PROVIDER)) {
				throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																				// ResponseEntity.status(HttpStatus.FORBIDDEN);
			}

			return ResponseEntity.ok(vxf);
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest()
					.body("vxf with id=" + vxfid + " not found in portal registry");

		}
	}

	@GetMapping(value = "/vxfs/uuid/{uuid}", produces = "application/json")
	public ResponseEntity<?> getVxFMetadataByUUID(@PathVariable("uuid") String uuid, HttpServletRequest request)
			throws ForbiddenException {

		logger.info("Received GET for vxf uuid: " + uuid);
		VxFMetadata vxf = null;

		String endpointUrl = request.getRequestURI();
		if (uuid.equals("77777777-668b-4c75-99a9-39b24ed3d8be")) {
			vxf = new VxFMetadata();
			vxf.setUuid(uuid);
			vxf.setName("IntegrTestLocal example service");
			vxf.setShortDescription("An example local service");
			vxf.setVersion("1.0.0");
			vxf.setIconsrc("");
			vxf.setLongDescription("");

			vxf.setPackageLocation(endpointUrl.toString().replace("http:", "")
					+ "/packages/77777777-668b-4c75-99a9-39b24ed3d8be/examplevxf.tar.gz");
			// }else if (uuid.equals("12cab8b8-668b-4c75-99a9-39b24ed3d8be")) {
			// vxf = new VxFMetadata(uuid, "AN example service");
			// vxf.setShortDescription("An example local service");
			// vxf.setVersion("1.0.0rc1");
			// vxf.setIconsrc("");
			// vxf.setLongDescription("");
			// //URI endpointUrl = uri.getBaseUri();
			//
			// vxf.setPackageLocation( endpointUrl
			// +"repo/packages/12cab8b8-668b-4c75-99a9-39b24ed3d8be/examplevxf.tar.gz");
		} else if (uuid.equals("22cab8b8-668b-4c75-99a9-39b24ed3d8be")) {
			vxf = new VxFMetadata();
			vxf.setUuid(uuid);
			vxf.setName("IntegrTestLocal example ErrInstall service");
			vxf.setShortDescription("An example ErrInstall local service");
			vxf.setVersion("1.0.0");
			vxf.setIconsrc("");
			vxf.setLongDescription("");
			// URI endpointUrl = uri.getBaseUri();

			vxf.setPackageLocation(endpointUrl.toString().replace("http:", "")
					+ "/packages/22cab8b8-668b-4c75-99a9-39b24ed3d8be/examplevxfErrInstall.tar.gz");
		} else {
			vxf = (VxFMetadata) vxfService.getVxFByUUID(uuid);
		}

		if (vxf != null) {

			if (!vxf.isPublished()) {
				throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																				// ResponseEntity.status(HttpStatus.FORBIDDEN);
			}

			return ResponseEntity.ok(vxf);
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest()
					.body("Installed vxf with uuid=" + uuid + " not found in local registry");

		}

	}

	// Experiments related API

	@GetMapping(value = "/admin/experiments", produces = "application/json")
	public ResponseEntity<?> getApps(@RequestParam(name = "categoryid", required = false) Long categoryid,
			HttpServletRequest request) {

		// Object attr = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
		// SecurityContextHolder.setContext((SecurityContext) attr);
		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if (u != null) {
			List<ExperimentMetadata> apps;

			if (u.getRoles().contains(UserRoleType.ROLE_ADMIN)) {
				apps = nsdService.getdNSDsByCategory(categoryid);
			} else {
				apps = nsdService.gedNSDsByUserID((long) u.getId());
			}

			return ResponseEntity.ok(apps);

		} else {

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}

	}

	@GetMapping(value = "/experiments", produces = "application/json")
	public ResponseEntity<?> getAllApps(@RequestParam(name = "categoryid", required = false) Long categoryid) {

		//
		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if (u != null) {
			return getAllDeployableExperiments();
		} else {

			logger.info("getexperiments categoryid=" + categoryid);
			List<ExperimentMetadata> nsds = nsdService.getPublishedNSDsByCategory(categoryid);
			return ResponseEntity.ok(nsds);
		}
	}

	/**
	 * @return all User's Valid experiments as well as all Public and Valid
	 *         experiments
	 */
	@GetMapping(value = "/admin/experiments/deployable", produces = "application/json")
	public ResponseEntity<?> getAllDeployableExperiments() {

		//
		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if (u != null) {
			List<ExperimentMetadata> userexpr;

			if (u.getRoles().contains(UserRoleType.ROLE_ADMIN)) {
				userexpr = nsdService.getdNSDsByCategory((long) -1);
			} else {
				userexpr = nsdService.gedNSDsByUserID((long) u.getId());
			}

			List<ExperimentMetadata> deplExps = new ArrayList<ExperimentMetadata>(userexpr);
			List<ExperimentMetadata> pubExps = new ArrayList<ExperimentMetadata>(
					nsdService.getPublishedNSDsByCategory((long) -1));
			List<ExperimentMetadata> returnedExps = new ArrayList<ExperimentMetadata>();
			for (ExperimentMetadata e : pubExps) {

				boolean found = false;
				boolean onboarded = false;
				for (ExperimentMetadata depl : deplExps) {
					if (depl.getId() == e.getId()) {
						found = true;
					}
				}
				if (!found) {
					deplExps.add(e);// add no duplicate public experiments
				}
			}
			for (ExperimentMetadata depl : deplExps) {
				// If it is not already included and it has been onboarded
				if (depl.getExperimentOnBoardDescriptors().size() > 0) {
					for (ExperimentOnBoardDescriptor eobd : depl.getExperimentOnBoardDescriptors()) {
						if (eobd.getOnBoardingStatus() == OnBoardingStatus.ONBOARDED) {
							returnedExps.add(depl);
						}
					}
				}
			}

//			for (Iterator<ExperimentMetadata> iter = deplExps.listIterator(); iter.hasNext(); ) { //filter only valid
//				ExperimentMetadata a = iter.next();
//			    if ( !a.isValid() ) {
//			        iter.remove();
//			    }
//			}			

			return ResponseEntity.ok(returnedExps);

		} else {

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}

	@PostMapping(value = "/admin/experimentobds/action", produces = "application/json")
	public ResponseEntity<?> postExperimentsOBDAction(@Valid @RequestBody String payload) {

		String ret = template.requestBody("jms:queue:ns.action.run", payload, String.class);
		logger.info("Message Received from AMQ:" + ret);
		// Get the response and Map object to ExperimentMetadata
		ResponseEntity<String> response = null;
		logger.info("From ActiveMQ:" + ret.toString());
		// Map object to VxFOnBoardedDescriptor
		JSONObject obj = new JSONObject(ret);
		logger.info("Status Code of response:" + obj.get("statusCode"));
		response = new ResponseEntity<String>(obj.get("body").toString(),
				HttpStatus.valueOf(obj.get("statusCode").toString()));
		logger.info("Response from action " + response);
		return response;
	}

	/**
	 * @return all User's Valid experiments as well as all Public and Valid
	 *         experiments
	 */
	@GetMapping(value = "/admin/experimentobds/deployable", produces = "application/json")
	public ResponseEntity<?> getAllDeployableExperimentsOBDs() {

		//
		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if (u != null) {
			List<ExperimentMetadata> userexpr;

			if (u.getRoles().contains(UserRoleType.ROLE_ADMIN)) {
				userexpr = nsdService.getdNSDsByCategory((long) -1);
			} else {
				userexpr = nsdService.gedNSDsByUserID((long) u.getId());
			}

			List<ExperimentMetadata> deplExps = new ArrayList<ExperimentMetadata>(userexpr);
			List<ExperimentMetadata> pubExps = new ArrayList<ExperimentMetadata>(
					nsdService.getPublishedNSDsByCategory((long) -1));
			List<ExperimentOnBoardDescriptor> returnedExpOBDs = new ArrayList<ExperimentOnBoardDescriptor>();
			for (ExperimentMetadata e : pubExps) {

				boolean found = false;
				boolean onboarded = false;
				for (ExperimentMetadata depl : deplExps) {
					if (depl.getId() == e.getId()) {
						found = true;
					}
				}
				if (!found) {
					deplExps.add(e);// add no duplicate public experiments
				}
			}
			for (ExperimentMetadata depl : deplExps) {
				// If it is not already included and it has been onboarded
				if (depl.getExperimentOnBoardDescriptors().size() > 0) {
					for (ExperimentOnBoardDescriptor eobd : depl.getExperimentOnBoardDescriptors()) {
						if (eobd.getOnBoardingStatus() == OnBoardingStatus.ONBOARDED) {
							returnedExpOBDs.add(eobd);
						}
					}
				}
			}

//			for (Iterator<ExperimentMetadata> iter = deplExps.listIterator(); iter.hasNext(); ) { //filter only valid
//				ExperimentMetadata a = iter.next();
//			    if ( !a.isValid() ) {
//			        iter.remove();
//			    }
//			}			

			return ResponseEntity.ok(returnedExpOBDs);

		} else {

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}

	@GetMapping(value = "/experiments/{appid}", produces = "application/json")
	public ResponseEntity<?> getExperimentMetadataByID(@PathVariable("appid") int appid) throws ForbiddenException {
		logger.info("getAppMetadataByID  appid=" + appid);
		ExperimentMetadata app = nsdService.getProductByID(appid);

		if (app != null) {
			if (!app.isPublished()) {
				throw new ForbiddenException("The requested page is forbidden");
			}

			return ResponseEntity.ok(app);
		} else {

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}

	@GetMapping(value = "/admin/experiments/{appid}", produces = "application/json")
	public ResponseEntity<?> getAdminExperimentMetadataByID(@PathVariable("appid") int appid)
			throws ForbiddenException {

		logger.info("getAppMetadataByID  appid=" + appid);
		ExperimentMetadata app = nsdService.getProductByID(appid);

		if (app != null) {

			if (!checkUserIDorIsAdmin(app.getOwner().getId())) {
				throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																				// ResponseEntity.status(HttpStatus.FORBIDDEN)
																				// ;
			}

			return ResponseEntity.ok(app);
		} else {

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}

	@GetMapping(value = "/experiments/uuid/{uuid}", produces = "application/json")
	public ResponseEntity<?> getAppMetadataByUUID(@PathVariable("uuid") String uuid) throws ForbiddenException {
		logger.info("Received GET for app uuid: " + uuid);

		ExperimentMetadata app = nsdService.getdNSDByUUID(uuid);

		if (app != null) {
			if (!app.isPublished()) {
				throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																				// ResponseEntity.status(HttpStatus.FORBIDDEN)
																				// ;
			}
			return ResponseEntity.ok(app);
		} else {

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}

	}

	@PostMapping(value = "/admin/experiments", produces = "application/json", consumes = "multipart/form-data")
	public ResponseEntity<?> addExperimentMetadata(final @ModelAttribute("exprm") String exp,
			@RequestParam(name = "prodIcon", required = false) MultipartFile prodIcon,
			@RequestParam(name = "prodFile", required = false) MultipartFile prodFile,
			@RequestParam(name = "screenshots", required = false) MultipartFile[] screenshots,
			HttpServletRequest request) {

		ExperimentMetadata experiment = null;

		try {
			experiment = objectMapper.readValue(exp, ExperimentMetadata.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Object attr = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
		// SecurityContextHolder.setContext((SecurityContext) attr);
		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if (u == null) {

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}

		String emsg = "";

		ExperimentMetadata experimentSaved = null;

		try {

			logger.info("Received @POST for experiment : " + experiment.getName());

			experimentSaved = (ExperimentMetadata) addNewProductData(experiment, prodIcon, prodFile, screenshots,
					request);

		} catch (IOException e) {
			experimentSaved = null;
			e.printStackTrace();
			logger.error(e.getMessage());
			emsg = e.getMessage();
		}

		if (experimentSaved != null) {

			busController.newNSDAdded(experimentSaved);
			busController.validateNSD(experimentSaved);

			// ======================================================
			// AUTOMATIC ONBOARDING PROCESS -START
			// Need to select MANO Provider, convert vxfMetadata to VxFOnBoardedDescriptor
			// and pass it as an input.

			// Get the MANO providers which are set for automatic onboarding
			List<MANOprovider> MANOprovidersEnabledForOnboarding = manoProviderService
					.getMANOprovidersEnabledForOnboarding();

			for (MANOprovider mp : MANOprovidersEnabledForOnboarding) {
				logger.info("check for ONBOARDING on " + mp.getName());
				logger.info("experimentSaved.getPackagingFormat().toString() = "
						+ experimentSaved.getPackagingFormat().toString());
				logger.info(
						"mp.getSupportedMANOplatform().getVersion()) " + mp.getSupportedMANOplatform().getVersion());
				if (experimentSaved.getPackagingFormat().toString()
						.equals(mp.getSupportedMANOplatform().getVersion())) {
					// Create NSDOnboardDescriptor
					ExperimentOnBoardDescriptor obd = new ExperimentOnBoardDescriptor();
					// Get the first one for now
					obd.setObMANOprovider(mp);
					obd.setUuid(UUID.randomUUID().toString());
					ExperimentMetadata refNSD = (ExperimentMetadata) nsdService.getProductByID(experimentSaved.getId());
					// Fill the NSDMetadata of NSDOnBoardedDescriptor
					obd.setExperiment(refNSD);
					// save VxFonBoardedDescriptor or not ???
					obd = nsdOBDService.updateExperimentOnBoardDescriptor(obd);

					// Update the NSDMetadata Object with the obd Object
					refNSD.getExperimentOnBoardDescriptors().add(obd);

					// save product
					refNSD = (ExperimentMetadata) nsdService.updateProductInfo(refNSD);

					// ***************************************************************************************************************************\
					// Because in portal.api.mano we need the url for the package location in order
					// not to ask back, if the package locations
					// does not contain http add the default maindomain value.
					// We can either add it here or change that where the pLocation is set initially
					// for the object.
					// Get the location of the package
					String pLocation = obd.getExperiment().getPackageLocation();
					logger.info("VxF Package Location: " + pLocation);
					if (!pLocation.contains("http")) {
						pLocation = propsService.getPropertyByName("maindomain").getValue() + pLocation;
						obd.getExperiment().setPackageLocation(pLocation);
						productService.updateProductInfo(obd.getExperiment());
					}
					logger.info("PROPER VxF Package Location: " + pLocation);
					// ***************************************************************************************************************************

					// Send the message for automatic onboarding
					// busController.newNSDAdded( vxf );

					// set proper scheme (http or https)
					// MANOController.setHTTPSCHEME( request.getRequestURL().toString() );
					// busController.onBoardNSD( obd );
					try {
						String[] fpath = obd.getExperiment().getPackageLocation().split("/");
						logger.info("uuid: " + fpath[fpath.length - 2]);
						logger.info("Package: " + fpath[fpath.length - 1]);
						String vxfAbsfile = METADATADIR + fpath[fpath.length - 2] + File.separator
								+ fpath[fpath.length - 1];
						File afile = new File(vxfAbsfile);
						Path path = Paths.get(afile.getAbsolutePath());
//						ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
//						busController.onBoardVxFAdded( vobd, afile, resource );

						CompositeExperimentOnBoardDescriptor compositeobdobj = new CompositeExperimentOnBoardDescriptor();
						compositeobdobj.setFilename(afile.getName());
						compositeobdobj.setAllBytes(Files.readAllBytes(path));
						compositeobdobj.setObd(obd);
						busController.onBoardNSDAddedByCompositeObj(compositeobdobj);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			// AUTOMATIC ONBOARDING PROCESS -END
			// ======================================================

			ExperimentMetadata experimentr = (ExperimentMetadata) nsdService.getProductByID(experimentSaved.getId()); // rereading
																														// this,
																														// seems
																														// to
																														// keep
																														// the
																														// DB
																														// connection

			return ResponseEntity.ok(experimentr);
		} else {

			return (ResponseEntity<?>) ResponseEntity.badRequest().body("{ \"message\" : \"" + emsg + "\"}");
		}

	}

	@PutMapping(value = "/admin/experiments/{aid}", produces = "application/json", consumes = "multipart/form-data")
	public ResponseEntity<?> updateExperimentMetadata(@PathVariable("aid") int aid,
			final @ModelAttribute("exprm") String ex,
			@RequestParam(name = "prodIcon", required = false) MultipartFile prodIcon,
			@RequestParam(name = "prodFile", required = false) MultipartFile prodFile,
			@RequestParam(name = "screenshots", required = false) MultipartFile[] screenshots,
			HttpServletRequest request) throws ForbiddenException {

		ExperimentMetadata expmeta = null;
		try {
			expmeta = objectMapper.readValue(ex, ExperimentMetadata.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String emsg = "";
		ExperimentMetadata expmetasaved = null;
		try {

			if (!checkUserIDorIsAdmin(expmeta.getOwner().getId())) {
				throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																				// ResponseEntity.status(HttpStatus.FORBIDDEN)
																				// ;
			}

			logger.info("Received @POST for experiment : " + expmeta.getName());
			// logger.info("Received @POST for app.containers : " +
			// appmeta.getContainers().size());

			expmetasaved = (ExperimentMetadata) updateProductMetadata(expmeta, prodIcon, prodFile, screenshots,
					request);

		} catch (IOException e) {
			expmetasaved = null;
			e.printStackTrace();
			logger.error(e.getMessage());
			emsg = e.getMessage();
		}

		if (expmetasaved != null) {

			busController.updateNSD(expmetasaved);

			if (prodFile != null) { // if the descriptor changed then we must re-trigger validation
				String a = prodFile.getName();
				if (!a.equals("unknown")) {
					busController.validationUpdateNSD(expmetasaved);
				}
			}

			return ResponseEntity.ok(expmetasaved);
		} else {

			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}

	}

	@DeleteMapping(value = "/admin/experiments/{appid}", produces = "application/json")
	public ResponseEntity<?> deleteExperiment(@PathVariable("appid") int appid, HttpServletRequest request)
			throws ForbiddenException {

		// Object attr = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
		// SecurityContextHolder.setContext((SecurityContext) attr);

		// Get the OnBoarded Descriptors to OffBoard them
		Set<ExperimentOnBoardDescriptor> expobds = null;

		ExperimentMetadata nsd = (ExperimentMetadata) nsdService.getProductByID(appid);

		if (!checkUserIDorIsAdmin(nsd.getOwner().getId())) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN);
		}
		
		// If the NSD is Valid exit
		if (nsd.isValid()) {
			return (ResponseEntity<?>) ResponseEntity.badRequest()
					.body("ExperimentMetadata with id=" + appid + " is Validated and will not be deleted");
		}
		
		try
		{
			expobds = nsd.getExperimentOnBoardDescriptors();
		}
		catch(Exception e)
		{
			centralLogger.log(CLevel.INFO, "Unable to retrieve experiment onboard descriptors for NSD "+nsd.getUuid(), compname);			
		}
		// If there are Experiment OnBoard Descriptors		
		if (expobds!=null && expobds.size() > 0) {
			// Foreach Experiment OnBoard Descriptors
			for (ExperimentOnBoardDescriptor expobd_tmp : expobds) {
				if (expobd_tmp.getOnBoardingStatus() != OnBoardingStatus.ONBOARDED) {
					continue;
				}
				OnBoardingStatus previous_status = expobd_tmp.getOnBoardingStatus();
				expobd_tmp.setOnBoardingStatus(OnBoardingStatus.OFFBOARDING);
				centralLogger.log(CLevel.INFO, "Onboarding Status change of VxF " + expobd_tmp.getExperiment().getName()
						+ " to " + expobd_tmp.getOnBoardingStatus(), compname);
				ExperimentOnBoardDescriptor u = nsdOBDService.updateExperimentOnBoardDescriptor(expobd_tmp);

				ResponseEntity<String> response = null;
				try {
					// response = aMANOController.offBoardNSDFromMANOProvider( expobd_tmp );
					// Check response. If it not possible, quit the deletion.
					response = busController.offBoardNSD(expobd_tmp);
					logger.info("offBoard NSD response:" + response.toString());
				} catch (HttpStatusCodeException e) {
					logger.info("offBoard NSD exception:" + e.toString());
					logger.info("offBoard NSD exception response:" + response.toString());
					//expobd_tmp.setOnBoardingStatus(previous_status);
					//centralLogger.log(CLevel.INFO, "Boarding Status change of VxF "
					//		+ expobd_tmp.getExperiment().getName() + " to " + expobd_tmp.getOnBoardingStatus(),
					//		compname);
					//expobd_tmp.setFeedbackMessage(e.getResponseBodyAsString());
					//u = nsdOBDService.updateExperimentOnBoardDescriptor(expobd_tmp);
					//return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
					//		.body(e.getResponseBodyAsString());
				}
				// if the deletion fails with a 400like or 500like update the object.
				if ((response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError())
						&& (!response.getStatusCode().equals(HttpStatus.NOT_FOUND))) // If response is 400like or
																						// 500like quit the deletion.
				{
					expobd_tmp.setOnBoardingStatus(previous_status);
					expobd_tmp.setFeedbackMessage(response.getBody());
					u = nsdOBDService.updateExperimentOnBoardDescriptor(expobd_tmp);
					return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders())
							.body(response.getBody());
												
				} else {
					if (response.getBody() != null) {
						expobd_tmp.setFeedbackMessage(response.getBody().toString());
					} else {
						expobd_tmp.setFeedbackMessage(response.toString());
					}
				}
				// UnCertify Upon OffBoarding
				// vxfobd_tmp.getVxf().setCertified(false);
				expobd_tmp.setOnBoardingStatus(OnBoardingStatus.OFFBOARDED);
				try {
					centralLogger.log(CLevel.INFO, "Onboarding Status change of NSD "
							+ expobd_tmp.getExperiment().getName() + " to " + expobd_tmp.getOnBoardingStatus(),
							compname);
				} catch (Exception e) {
					centralLogger.log(CLevel.INFO, "No related NSD found for " + expobd_tmp.getId() + " in status  "
							+ expobd_tmp.getOnBoardingStatus(), compname);
				}

				u = nsdOBDService.updateExperimentOnBoardDescriptor(expobd_tmp);
				// busController.offBoardNSDSucceded( u );
			}
		}
		else
		{
			centralLogger.log(CLevel.INFO, "No expobds found for "+nsd.getUuid(), compname);			
		}
		busController.deletedExperiment(nsd);

		// Remove NS dependencies
		for (DeploymentDescriptor d : deploymentDescriptorService.getDeploymentsByExperimentId(nsd.getId())) {
			d.setExperiment(null);
			// Clear VxFPlacement to be able to delete the related VxFs also after NSD
			// deletion.
			d.getVxfPlacements().clear();
			deploymentDescriptorService.updateDeploymentDescriptor(d);
		}
		// nsd.getDeploymentDescriptors().clear();

		// Remove NSD from the categories
		for (Category c : nsd.getCategories()) {
			if (c.getProducts().contains(nsd)) {
				c.getProducts().remove(nsd);
				categoryService.updateCategoryInfo(c);
			}
		}
		nsd.getCategories().clear();

		// Remove NSD from OnBoarded descriptors
		for (ExperimentOnBoardDescriptor expobd_tmp : expobds) {
			ExperimentOnBoardDescriptor sm = nsdOBDService.getExperimentOnBoardDescriptorByID(expobd_tmp.getId());
			nsdOBDService.deleteExperimentOnBoardDescriptor(sm);
		}
		nsd.getExperimentOnBoardDescriptors().clear();

		// Remove NSD from Portal Users
		PortalUser owner = usersService.findById(nsd.getOwner().getId());
		owner.getProducts().remove(nsd);
		usersService.updateUserInfo(owner, false);
		nsd.setOwner(null);

		// Delete NSD from Products
		nsdService.deleteProduct(nsd);
		return ResponseEntity.ok().body("{}");
	}

	@DeleteMapping(value = "/admin/experiments/{appid}/softdelete", produces = "application/json")
	public ResponseEntity<?> softdeleteExperiment(@PathVariable("appid") int appid, HttpServletRequest request)
			throws ForbiddenException {


		// Get the OnBoarded Descriptors to OffBoard them
		Set<ExperimentOnBoardDescriptor> expobds = null;

		ExperimentMetadata nsd = (ExperimentMetadata) nsdService.getProductByID(appid);

		if (!checkUserIDorIsAdmin(nsd.getOwner().getId())) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN);
		}
		
		// If the NSD is Valid exit
		if (nsd.isValid()) {
			return (ResponseEntity<?>) ResponseEntity.badRequest()
					.body("ExperimentMetadata with id=" + appid + " is Validated and will not be deleted");
		}
		
		try
		{
			expobds = nsd.getExperimentOnBoardDescriptors();
		}
		catch(Exception e)
		{
			centralLogger.log(CLevel.INFO, "Unable to retrieve experiment onboard descriptors for NSD "+nsd.getUuid(), compname);			
		}
		
		try
		{
			busController.deletedExperiment(nsd);
	
			// Remove NS dependencies
			for (DeploymentDescriptor d : deploymentDescriptorService.getDeploymentsByExperimentId(nsd.getId())) {
				d.setExperiment(null);
				// Clear VxFPlacement to be able to delete the related VxFs also after NSD
				// deletion.
				d.getVxfPlacements().clear();
				deploymentDescriptorService.updateDeploymentDescriptor(d);
			}
			// nsd.getDeploymentDescriptors().clear();
	
			// Remove NSD from the categories
			for (Category c : nsd.getCategories()) {
				if (c.getProducts().contains(nsd)) {
					c.getProducts().remove(nsd);
					categoryService.updateCategoryInfo(c);
				}
			}
			nsd.getCategories().clear();
	
			// Remove NSD from OnBoarded descriptors
			for (ExperimentOnBoardDescriptor expobd_tmp : expobds) {
				ExperimentOnBoardDescriptor sm = nsdOBDService.getExperimentOnBoardDescriptorByID(expobd_tmp.getId());
				nsdOBDService.deleteExperimentOnBoardDescriptor(sm);
			}
			nsd.getExperimentOnBoardDescriptors().clear();
	
			// Remove NSD from Portal Users
			PortalUser owner = usersService.findById(nsd.getOwner().getId());
			owner.getProducts().remove(nsd);
			usersService.updateUserInfo(owner, false);
			nsd.setOwner(null);
	
			// Delete NSD from Products
			nsdService.deleteProduct(nsd);
		}
		catch(Exception e)
		{
			return (ResponseEntity<?>) ResponseEntity.badRequest()
					.body("ExperimentMetadata with id=" + appid + " could not be deleted. Please check for possible dependencies.");			
		}
		return ResponseEntity.ok().body("{}");
	}
	
	@GetMapping(value = "/admin/properties", produces = "application/json")
	public ResponseEntity<?> getProperties() throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		List<PortalProperty> props = propsService.getProperties();
		for (PortalProperty portalProperty : props) {
			if (portalProperty.getName().equals("mailpassword")) {
				portalProperty.setValue("***");
			}
		}
		return ResponseEntity.ok(props);
	}

	@PutMapping(value = "/admin/properties/{propid}", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> updateProperty(@PathVariable("propid") long propid, @Valid @RequestBody PortalProperty p)
			throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		PortalProperty previousProperty = propsService.getPropertyByID(propid);

		PortalProperty u = propsService.updateProperty(p);
		if (u != null) {

			sendPropertiesToBus();

			return ResponseEntity.ok(u);
		} else {

			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}

	}

	@GetMapping(value = "/admin/properties/{propid}", produces = "application/json")
	public ResponseEntity<?> getPropertyById(@PathVariable("propid") long propid) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		PortalProperty sm = propsService.getPropertyByID(propid);

		if (sm.getName().equals("mailpassword")) {
			sm.setValue("");
		}
		if (sm != null) {
			return ResponseEntity.ok(sm);
		} else {

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}

	@GetMapping(value = "/admin/deployments", produces = "application/json")
	public ResponseEntity<?> getAllDeployments(@RequestParam(name = "status", required = false) String status) {

		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		if (u != null) {
			logger.info("getAllDeployments for userid: " + u.getId());
			List<DeploymentDescriptor> deployments;
			if ((u.getRoles().contains(UserRoleType.ROLE_ADMIN))) {
				if ((status != null) && status.equals("COMPLETED")) {
					deployments = deploymentDescriptorService.getAllCompletedDeploymentDescriptors();
				} else if ((status != null) && status.equals("REJECTED")) {
					deployments = deploymentDescriptorService.getAllRejectedDeploymentDescriptors();
				} else if ((status != null) && status.equals("FAILED")) {
					deployments = deploymentDescriptorService.getAllFailedDeploymentDescriptors();
				} else if ((status != null) && status.equals("FAILED_OSM_REMOVED")) {
					deployments = deploymentDescriptorService.getAllRemovedDeploymentDescriptors();
				} else {
					deployments = deploymentDescriptorService.getAllDeploymentDescriptors();
				}
			} else if ((u.getRoles().contains(UserRoleType.ROLE_MENTOR))) {
				if ((status != null) && status.equals("COMPLETED")) {
					deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByMentor((long) u.getId(),
							"COMPLETED");
				} else if ((status != null) && status.equals("REJECTED")) {
					deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByMentor((long) u.getId(),
							"REJECTED");
				} else if ((status != null) && status.equals("FAILED")) {
					deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByMentor((long) u.getId(),
							"FAILED");
				} else if ((status != null) && status.equals("FAILED_OSM_REMOVED")) {
					deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByMentor((long) u.getId(),
							"FAILED_OSM_REMOVED");
				} else {
					deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByMentor((long) u.getId(),
							null);
				}
			} else {

				if ((status != null) && status.equals("COMPLETED")) {
					deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByUser((long) u.getId(),
							"COMPLETED");
				} else if ((status != null) && status.equals("REJECTED")) {
					deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByUser((long) u.getId(),
							"REJECTED");
				} else if ((status != null) && status.equals("FAILED")) {
					deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByUser((long) u.getId(),
							"FAILED");
				} else if ((status != null) && status.equals("FAILED_OSM_REMOVED")) {
					deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByUser((long) u.getId(),
							"FAILED_OSM_REMOVED");
				} else {
					deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByUser((long) u.getId(), null);
				}
			}

			return ResponseEntity.ok(deployments);
		} else {

			return ResponseEntity.notFound().build();
		}

	}

//	@GET
//	@Path("/admin/deployments/user")
//	@Produces("application/json")
//	public Response getAllDeploymentsofUser() {
//
//		PortalUser u = portalRepositoryRef.getUserBySessionID(ws.getHttpServletRequest().getSession().getId());
//
//		if (u != null) {
//			logger.info("getAllDeploymentsofUser for userid: " + u.getId());
//			List<DeploymentDescriptor> deployments;
//			deployments = portalRepositoryRef.getAllDeploymentDescriptorsByUser( (long) u.getId() ); 
//
//			return Response.ok().entity(deployments).build();
//		} else {
//			ResponseBuilder builder = Response.status(Status.NOT_FOUND);
//			builder.entity("User not found in portal registry or not logged in");
//			throw new WebApplicationException(builder.build());
//		}
//
//	}

	@GetMapping(value = "/admin/deployments/user", produces = "application/json")
	public ResponseEntity<?> getAllDeploymentsofUser(@RequestParam(name = "status", required = false) String status) {

		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if (u != null) {
			logger.info("getAllDeploymentsofUser for userid: " + u.getId());
			List<DeploymentDescriptor> deployments = new ArrayList<DeploymentDescriptor>();

			if ((status != null) && status.equals("COMPLETED")) {
				deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByUser((long) u.getId(),
						"COMPLETED");
			} else if ((status != null) && status.equals("REJECTED")) {
				deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByUser((long) u.getId(),
						"REJECTED");
			} else if ((status != null) && status.equals("FAILED")) {
				deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByUser((long) u.getId(), "FAILED");
			} else if ((status != null) && status.equals("FAILED_OSM_REMOVED")) {
				deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByUser((long) u.getId(),
						"FAILED_OSM_REMOVED");
			} else {
				deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByUser((long) u.getId(), null);
			}

			return ResponseEntity.ok(deployments);
		} else {

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}

	}

	@GetMapping(value = "/admin/deployments/scheduled", produces = "application/json")
	public ResponseEntity<?> getAllScheduledDeploymentsofUser() {

		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if (u != null) {
			logger.info("getAllDeploymentsofUser for userid: " + u.getId());
			List<DeploymentDescriptor> deployments;

			if ((u.getRoles().contains(UserRoleType.ROLE_ADMIN))
					|| (u.getRoles().contains(UserRoleType.ROLE_TESTBED_PROVIDER))) {
				deployments = deploymentDescriptorService.getAllDeploymentDescriptorsScheduled();
			} else {
				deployments = deploymentDescriptorService.getAllDeploymentDescriptorsByUser((long) u.getId(), null);
			}

			return ResponseEntity.ok(deployments);
		} else {
			return (ResponseEntity<?>) ResponseEntity.notFound();

		}

	}

	@PostMapping(value = "/admin/deployments", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> addDeployment(@Valid @RequestBody DeploymentDescriptor deployment,
			HttpServletRequest request) {

		logger.info("AddDeployment request received:" + deployment.toJSON());

		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		
		if (u != null) {
			logger.info("addDeployment for userid: " + u.getId());

//			for (DeploymentDescriptor d : u.getDeployments()) {
//				logger.info("deployment already for userid: " + d.getId());
//			}
			String uuid = deployment.getExperimentFullDetails().getUuid();
			deployment.setUuid(uuid);			
			deployment.setDateCreated(new Date());
			deployment.setStatus(DeploymentDescriptorStatus.UNDER_REVIEW);
			centralLogger.log(CLevel.INFO,
					"Status change of deployment " + deployment.getName() + " to " + deployment.getStatus(), compname);
			logger.info("Status change of deployment " + deployment.getName() + " to " + deployment.getStatus());

			logger.info("reattach user from the DB model");
			u = usersService.findById(u.getId());
			deployment.setOwner(u); // reattach from the DB model

			logger.info("reattach ExperimentMetadata from the DB model");
			// Get the Experiment Metadata from the id of the experiment from the deployment
			// request
			ExperimentMetadata baseApplication = (ExperimentMetadata) nsdService
					.getProductByID(deployment.getExperiment().getId());
			deployment.setExperiment(baseApplication); // reattach from the DB model

			logger.info("reattach InfrastructureForAll from the DB model");
			deployment.setInfrastructureForAll(infrastructureService.getInfrastructureByID(deployment.getInfrastructureForAll().getId()));
			
			logger.info("deployment.getExperimentFullDetails().getUuid():"+deployment.getExperimentFullDetails().getExperimentOnBoardDescriptors().toArray(new ExperimentOnBoardDescriptor[deployment.getExperimentFullDetails().getExperimentOnBoardDescriptors().size()])[0].getDeployId());
			ExperimentOnBoardDescriptor tmp = nsdOBDService.getExperimentOnBoardedDescriptorByUUid(deployment.getExperimentFullDetails().getExperimentOnBoardDescriptors().toArray(new ExperimentOnBoardDescriptor[deployment.getExperimentFullDetails().getExperimentOnBoardDescriptors().size()])[0].getDeployId());
			logger.info("Fetched ExperimentOnBoardDescriptor with Deployid:"+tmp.getDeployId()+" and "+tmp.getId());			
			deployment.setObddescriptor_uuid(tmp);
			
			logger.info("reattach Mentor from the DB model");
			deployment.setMentor(usersService.findById(deployment.getMentor().getId()));

			logger.info("reattach DeploymentDescriptorVxFPlacement from the DB model");
			for (DeploymentDescriptorVxFPlacement pl : deployment.getVxfPlacements()) {
				Infrastructure infr = infrastructureService.getInfrastructureByID(pl.getInfrastructure().getId());
				pl.setInfrastructure(infr);				
				VxFMetadata vv = vxfOBDService.getVxFIdByVxFMANOProviderIDAndMP(pl.getConstituentVxF().getVnfdidRef(),infr.getMp().getId());
				pl.getConstituentVxF().setVxfref(vv);

			}

			logger.info("update deployment to the DB model");
			DeploymentDescriptor deploymentSaved = deploymentDescriptorService.updateDeploymentDescriptor(deployment);

			logger.info("update user owner to the DB model");
			u.getDeployments().add(deploymentSaved);
			usersService.updateUserInfo(u, false);

			logger.info("NS status change is now " + deploymentSaved.getStatus());

			//u = portalRepositoryRef.updateUserInfo(u);
			//deployment = portalRepositoryRef.getDeploymentByUUID( deployment.getUuid() );//reattach from model

			//busController.newDeploymentRequest(deploymentSaved);

			//String adminemail = PortalRepository.getPropertyByName("adminEmail").getValue();
			//if ((adminemail != null) && (!adminemail.equals(""))) {
			//	String subj = "[5GinFIREPortal] New Deployment Request";
			//	EmailUtil.SendRegistrationActivationEmail(adminemail,
			//			"5GinFIREPortal New Deployment Request by user : " + u.getUsername() + ", " + u.getEmail()+ "\n<br/> Status: " + deployment.getStatus().name()+ "\n<br/> Description: " + deployment.getDescription()   ,
			//			subj);
			//}

			return ResponseEntity.ok(deploymentSaved);
		} else {
			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}

	@DeleteMapping(value = "/admin/deployments/{id}", produces = "application/json")
	public ResponseEntity<?> deleteDeployment(@PathVariable("id") int id) {
		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		DeploymentDescriptor dep = deploymentDescriptorService.getDeploymentByID(id);
		if (u != null) {
			if (u.getRoles().contains(UserRoleType.ROLE_ADMIN) || u.getId() == dep.getOwner().getId()) {
				logger.info("deleteDeployment: Owner matches user for Deployment" + dep.getId());
				PortalUser owner = usersService.findById(dep.getOwner().getId());
				owner.getDeployments().remove(dep);
				usersService.updateUserInfo(owner, false);
				deploymentDescriptorService.deleteDeployment(dep);
				return ResponseEntity.ok("{}");
			}
		}

		return (ResponseEntity<?>) ResponseEntity.notFound();
	}

	public void stripDeployment(int id) {
		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		DeploymentDescriptor dep = deploymentDescriptorService.getDeploymentByID(id);
		if (u != null) {
			if (u.getRoles().contains(UserRoleType.ROLE_ADMIN) || u.getId() == dep.getOwner().getId()) {
				logger.info("deleteDeployment: Owner matches user for Deployment" + dep.getId());
				PortalUser owner = usersService.findById(dep.getOwner().getId());
				owner.getDeployments().remove(dep);
				usersService.updateUserInfo(owner, false);
				// Get dependent records and delete them but keep the final record

			}
		}
	}

	@GetMapping(value = "/admin/deployments/{id}", produces = "application/json")
	public ResponseEntity<?> getDeploymentById(@PathVariable("id") long deploymentId) {

		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if (u != null) {
			logger.info("getDeploymentById for id: " + deploymentId);
			DeploymentDescriptor deployment = null;
			try {
				deployment = deploymentDescriptorService.getDeploymentByID(deploymentId);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			logger.info("got Deployment for id: " + deployment.getId() + " with mentor id: "
					+ deployment.getMentor().getId() + " with owner id: " + deployment.getOwner().getId());
			logger.info("u.getId() is:" + u.getId());
			if ((u.getRoles().contains(UserRoleType.ROLE_ADMIN)) || (deployment.getMentor().getId() == u.getId())
					|| (deployment.getOwner().getId() == u.getId())) {

				return ResponseEntity.ok(deployment);
			}
		}

		return (ResponseEntity<?>) ResponseEntity.notFound();

	}
	
	@Autowired
	private SessionFactory  sessionFactory;

	@PutMapping(value = "/admin/deployments/{id}", produces = "application/json", consumes = "application/json")
	@Transactional
	public ResponseEntity<?> updateDeployment(@PathVariable("id") int id,
			@Valid @RequestBody DeploymentDescriptor receivedDeployment) {
	    // Open a new session and begin a transaction
	    Session session = sessionFactory.openSession();
	    Transaction transaction = session.beginTransaction();

	    try {
			PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
	
			if ((u != null)) {
	
				if ((u.getRoles().contains(UserRoleType.ROLE_ADMIN))
						|| u.getApikey().equals(receivedDeployment.getMentor().getApikey())) // only admin or Deployment
																								// Mentor can alter a
																								// deployment
				{
					DeploymentDescriptor aDeployment = deploymentDescriptorService
							.getDeploymentByID(receivedDeployment.getId());
		
					aDeployment.setName(receivedDeployment.getName());
					aDeployment.setFeedback(receivedDeployment.getFeedback());
					aDeployment.setStartDate(receivedDeployment.getStartDate());
					aDeployment.setEndDate(receivedDeployment.getEndDate());
	
					logger.info("Previous Status is :" + aDeployment.getStatus() + ",New Status is:"
							+ receivedDeployment.getStatus() + " and Instance Id is " + aDeployment.getInstanceId());
	
					if (receivedDeployment.getStatus() != aDeployment.getStatus()) {
						aDeployment.setStatus(receivedDeployment.getStatus());
						centralLogger.log(CLevel.INFO,
								"Status change of deployment " + aDeployment.getName() + " to " + aDeployment.getStatus(),
								compname);
						logger.info(
								"Status change of deployment " + aDeployment.getName() + " to " + aDeployment.getStatus());
						aDeployment.getExperimentFullDetails();
						aDeployment.getInfrastructureForAll();
	
						logger.info("updateDeployment for id: " + aDeployment.getId());
	
						if (receivedDeployment.getStatus() == DeploymentDescriptorStatus.SCHEDULED
								&& aDeployment.getInstanceId() == null) {
							for (ExperimentOnBoardDescriptor tmpExperimentOnBoardDescriptor : aDeployment.getExperimentFullDetails()
									.getExperimentOnBoardDescriptors()) {
								aDeployment.setStatus(receivedDeployment.getStatus());
								centralLogger.log(CLevel.INFO, "Status change of deployment " + aDeployment.getName()
										+ " to " + aDeployment.getStatus(), compname);
								logger.info("Status change of deployment " + aDeployment.getName() + " to "
										+ aDeployment.getStatus());
								aDeployment = deploymentDescriptorService.updateDeploymentDescriptor(aDeployment);								
								logger.info("NS status change is now " + aDeployment.getStatus());
								aDeployment = deploymentDescriptorService.getDeploymentByIdEager( aDeployment.getId() );
								busController.scheduleExperiment(aDeployment);
							}
						} else if (receivedDeployment.getStatus() == DeploymentDescriptorStatus.RUNNING
								&& aDeployment.getInstanceId() == null) {
							for (ExperimentOnBoardDescriptor tmpExperimentOnBoardDescriptor : aDeployment.getExperimentFullDetails()
									.getExperimentOnBoardDescriptors()) {
								aDeployment.setStatus(receivedDeployment.getStatus());
								centralLogger.log(CLevel.INFO, "Status change of deployment " + aDeployment.getName()
										+ " to " + aDeployment.getStatus(), compname);
								logger.info("Status change of deployment " + aDeployment.getName() + " to "
										+ aDeployment.getStatus());
								aDeployment = deploymentDescriptorService.updateDeploymentDescriptor(aDeployment);
								logger.info("NS status change is now " + aDeployment.getStatus());
	
								busController.deployExperiment(aDeployment);
							}
						} else if (receivedDeployment.getStatus() == DeploymentDescriptorStatus.COMPLETED
								&& aDeployment.getInstanceId() != null) {
							aDeployment.setStatus(receivedDeployment.getStatus());
							centralLogger.log(CLevel.INFO, "Status change of deployment " + aDeployment.getName() + " to "
									+ aDeployment.getStatus(), compname);
							logger.info("Status change of deployment " + aDeployment.getName() + " to "
									+ aDeployment.getStatus());
							aDeployment = deploymentDescriptorService.updateDeploymentDescriptor(aDeployment);
							logger.info("NS status change is now " + aDeployment.getStatus());
							busController.completeExperiment(aDeployment);
						} else if (receivedDeployment.getStatus() == DeploymentDescriptorStatus.REJECTED
								&& aDeployment.getInstanceId() == null) {
							aDeployment.setStatus(receivedDeployment.getStatus());
							centralLogger.log(CLevel.INFO, "Status change of deployment " + aDeployment.getName() + " to "
									+ aDeployment.getStatus(), compname);
							logger.info("Status change of deployment " + aDeployment.getName() + " to "
									+ aDeployment.getStatus());
							aDeployment = deploymentDescriptorService.updateDeploymentDescriptor(aDeployment);
							logger.info("NS status change is now " + aDeployment.getStatus());
							busController.rejectExperiment(aDeployment);
							logger.info("Deployment Rejected");
						} else {
							return (ResponseEntity<?>) ResponseEntity.badRequest().body("Inconsistent status change");
						}
					} else {
	
						logger.info("Previous status is the same so just update deployment info");
						aDeployment = deploymentDescriptorService.updateDeploymentDescriptor(aDeployment);
						aDeployment = deploymentDescriptorService.getDeploymentByIdEager( aDeployment.getId() );
						busController.updateDeploymentRequest(aDeployment);
					}
			        transaction.commit();

			        // Return the response
			        return ResponseEntity.ok(aDeployment);
				}
	
			}
	    } catch (Exception e) {
	        // If an exception is thrown, rollback the transaction
	        if (transaction != null && transaction.isActive()) {
	            transaction.rollback();
	        }
	        // Log the exception (optional)
	        logger.error("Error updating deployment", e);
	        // Return an error response
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
	    } finally {
	        // Close the session
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Request failed");
	}

	/********************************************************************************
	 * 
	 * admin MANO platforms
	 * 
	 ********************************************************************************/

	@GetMapping(value = "/manoplatforms", produces = "application/json")
	public ResponseEntity<?> getMANOplatforms() {
		return ResponseEntity.ok(manoPlatformService.getMANOplatforms());
	}

	@GetMapping(value = "/admin/manoplatforms", produces = "application/json")
	public ResponseEntity<?> getAdminMANOplatforms() throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		return ResponseEntity.ok(manoPlatformService.getMANOplatforms());
	}

	@PostMapping(value = "/admin/manoplatforms", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> addMANOplatform(@Valid @RequestBody MANOplatform c) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		MANOplatform u = manoPlatformService.addMANOplatform(c);

		if (u != null) {
			return ResponseEntity.ok(u);
		} else {

			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}
	}

	@PutMapping(value = "/admin/manoplatforms/{mpid}", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> updateMANOplatform(@PathVariable("mpid") int mpid, @Valid @RequestBody MANOplatform c)
			throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		MANOplatform previousMP = manoPlatformService.getMANOplatformByID(mpid);

		previousMP.setDescription(c.getDescription());
		previousMP.setName(c.getName());
		previousMP.setVersion(c.getVersion());

		MANOplatform u = manoPlatformService.updateMANOplatformInfo(previousMP);

		if (u != null) {
			return ResponseEntity.ok(u);
		} else {

			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}

	}

	@DeleteMapping(value = "/admin/manoplatforms/{mpid}", produces = "application/json")
	public ResponseEntity<?> deleteMANOplatform(@PathVariable("mpid") int mpid) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		MANOplatform m = manoPlatformService.getMANOplatformByID(mpid);

		manoPlatformService.deleteMANOplatform(m);
		return ResponseEntity.ok("{}");

	}

	@GetMapping(value = "/manoplatforms/{mpid}", produces = "application/json")
	public ResponseEntity<?> getMANOplatformById(@PathVariable("mpid") int mpid) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		MANOplatform sm = manoPlatformService.getMANOplatformByID(mpid);

		if (sm != null) {
			return ResponseEntity.ok(sm);
		} else {

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}

	@GetMapping(value = "/admin/manoplatforms/{mpid}", produces = "application/json")
	public ResponseEntity<?> getAdminMANOplatformById(@PathVariable("mpid") int mpid) throws ForbiddenException {
		return getMANOplatformById(mpid);
	}

	/********************************************************************************
	 * 
	 * admin MANO providers
	 * 
	 ********************************************************************************/

	/**
	 * @return
	 * @throws ForbiddenException
	 */
	@GetMapping(value = "/admin/manoproviders", produces = "application/json")
	public ResponseEntity<?> getAdminMANOproviders() throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN);
		}

		return ResponseEntity.ok(manoProviderService.getMANOproviders());
	}

	@PostMapping(value = "/admin/manoproviders", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> addMANOprovider(@Valid @RequestBody MANOprovider c) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		c.setSupportedMANOplatform(manoPlatformService.getMANOplatformByID(c.getSupportedMANOplatform().getId())); // to
																													// properly
																													// attach
																													// from
																													// the
																													// model
		MANOprovider u = manoProviderService.addMANOprovider(c);

		if (u != null) {
			return ResponseEntity.ok(u);
		} else {

			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}
	}

	@PutMapping(value = "/admin/manoproviders/{mpid}", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> updateMANOprovider(@PathVariable("mpid") int mpid, @Valid @RequestBody MANOprovider c)
			throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		MANOprovider prev = manoProviderService.getMANOproviderByID(c.getId());
		prev.setApiEndpoint(c.getApiEndpoint());
		prev.setAuthorizationBasicHeader(c.getAuthorizationBasicHeader());
		prev.setDescription(c.getDescription());
		prev.setName(c.getName());
		prev.setSupportedMANOplatform(c.getSupportedMANOplatform());
		prev.setVims(c.getVims());

		MANOprovider u = manoProviderService.updateMANOproviderInfo(c);

		if (u != null) {
			return ResponseEntity.ok(u);
		} else {

			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}

	}

	@DeleteMapping(value = "/admin/manoproviders/{mpid}", produces = "application/json")
	public ResponseEntity<?> deleteMANOprovider(@PathVariable("mpid") int mpid) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		MANOprovider prev = manoProviderService.getMANOproviderByID(mpid);

		manoProviderService.deleteMANOprovider(prev);
		return ResponseEntity.ok("{}");

	}

	@GetMapping(value = "/admin/manoproviders/{mpid}", produces = "application/json")
	public ResponseEntity<?> getAdminMANOproviderById(@PathVariable("mpid") int mpid) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		MANOprovider sm = manoProviderService.getMANOproviderByID(mpid);

		if (sm != null) {
			return ResponseEntity.ok(sm);
		} else {

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}

	/********************************************************************************
	 * 
	 * admin VxFOnBoardedDescriptors
	 * 
	 ********************************************************************************/

	@GetMapping(value = "/admin/vxfobds", produces = "application/json")
	public ResponseEntity<?> getVxFOnBoardedDescriptors() {
		return ResponseEntity.ok(vxfOBDService.getVxFOnBoardedDescriptors());
	}

	@PostMapping(value = "/admin/vxfobds/", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> addVxFOnBoardedDescriptor(@Valid @RequestBody VxFMetadata aVxF) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		if (aVxF != null) {

			VxFMetadata refVxF = (VxFMetadata) vxfService.getVxFById(aVxF.getId());
			VxFOnBoardedDescriptor obd = new VxFOnBoardedDescriptor();
			obd.setVxf(refVxF);
			obd.setUuid(UUID.randomUUID().toString());
			// ?????
			refVxF.getVxfOnBoardedDescriptors().add(obd);

			// save product
			refVxF = (VxFMetadata) vxfService.updateProductInfo(refVxF);

			if (refVxF != null) {
				return ResponseEntity.ok(refVxF);
			} else {
				return (ResponseEntity<?>) ResponseEntity.badRequest().build();
			}

		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}
	}

	@PutMapping(value = "/admin/vxfobds/{mpid}", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> updateVxFOnBoardedDescriptor(@PathVariable("mpid") int mpid,
			@Valid @RequestBody VxFOnBoardedDescriptor c) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN);
		}
		VxFOnBoardedDescriptor u = vxfOBDService.updateVxFOnBoardedDescriptor(c);

		if (u != null) {
			return ResponseEntity.ok(u);
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}

	}

	@DeleteMapping(value = "/admin/vxfobds/{mpid}")
	public ResponseEntity<?> deleteVxFOnBoardedDescriptor(@PathVariable("mpid") int mpid) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN);
		}

		VxFOnBoardedDescriptor sm = vxfOBDService.getVxFOnBoardedDescriptorByID(mpid);

		vxfOBDService.deleteVxFOnBoardedDescriptor(sm);
		return ResponseEntity.ok("{}");

	}

	@GetMapping(value = "/admin/vxfobds/{mpid}", produces = "application/json")
	public ResponseEntity<?> getVxFOnBoardedDescriptorById(@PathVariable("mpid") int mpid) throws ForbiddenException {
		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		VxFOnBoardedDescriptor sm = vxfOBDService.getVxFOnBoardedDescriptorByID(mpid);

		if (sm != null) {
			return ResponseEntity.ok(sm);
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
			// throw new WebApplicationException(builder.build());
		}
	}

	@GetMapping(value = "/admin/vxfobds/{mpid}/status", produces = "application/json")
	public ResponseEntity<?> getVxFOnBoardedDescriptorByIdCheckMANOProvider(@PathVariable("mpid") int mpid)
			throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		VxFOnBoardedDescriptor obds = vxfOBDService.getVxFOnBoardedDescriptorByID(mpid);

		if (obds == null) {
			return (ResponseEntity<?>) ResponseEntity.notFound();
		}

		///**
		// * the following polling will be performed automatically by CAMEL with a timer
		// */
		//
		//if (obds.getOnBoardingStatus().equals(OnBoardingStatus.ONBOARDING)) {
		//
		//	Vnfd vnfd = null;
		//	List<Vnfd> vnfds = OSMClient.getInstance(obds.getObMANOprovider()).getVNFDs();
		//	for (Vnfd v : vnfds) {
		//		if (v.getId().equalsIgnoreCase(obds.getVxfMANOProviderID())
		//				|| v.getName().equalsIgnoreCase(obds.getVxfMANOProviderID())) {
		//			vnfd = v;
		//			break;
		//		}
		//	}
		//
		//	if (vnfd == null) {
		//		obds.setOnBoardingStatus(OnBoardingStatus.UNKNOWN);
		//	} else {
		//		obds.setOnBoardingStatus(OnBoardingStatus.ONBOARDED);
		//	}
		//
		//	obds = portalRepositoryRef.updateVxFOnBoardedDescriptor(obds);
		//
		//}

		return ResponseEntity.ok(obds);

	}

	@PutMapping(value = "/admin/vxfobds/{mpid}/onboard", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> onBoardDescriptor(@PathVariable("mpid") int mpid,
			@Valid @RequestBody final VxFOnBoardedDescriptor vxfobd) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		try {
			VxFOnBoardedDescriptor vobd = vxfOBDService.getVxFOnBoardedDescriptorByID(mpid);
			MANOprovider mp = manoProviderService.getMANOproviderByID(vxfobd.getObMANOprovider().getId());

			vobd.setObMANOprovider(mp);
			vobd = vxfOBDService.updateVxFOnBoardedDescriptor(vobd);
			// ***************************************************************************************************************************\
			// Because in portal.api.mano we need the url for the package location in order
			// not to ask back, if the package locations
			// does not contain http add the default maindomain value.
			// We can either add it here or change that where the pLocation is set initially
			// for the object.
			// Get the location of the package
			String pLocation = vobd.getVxf().getPackageLocation();
			logger.info("VxF Package Location: " + pLocation);
			if (!pLocation.contains("http")) {
				pLocation = propsService.getPropertyByName("maindomain").getValue() + pLocation;
				vobd.getVxf().setPackageLocation(pLocation);
				productService.updateProductInfo(vobd.getVxf());
			}
			logger.info("PROPER VxF Package Location: " + pLocation);
			// ***************************************************************************************************************************\
			// busController.onBoardVxFAdded( vobd );
			// aMANOController.onBoardVxFToMANOProvider( vxfobd.getId() );
			try {
				String[] fpath = vobd.getVxf().getPackageLocation().split("/");
				logger.info("uuid: " + fpath[fpath.length - 2]);
				logger.info("Package: " + fpath[fpath.length - 1]);
				String vxfAbsfile = METADATADIR + fpath[fpath.length - 2] + File.separator + fpath[fpath.length - 1];
				File afile = new File(vxfAbsfile);
				Path path = Paths.get(afile.getAbsolutePath());
				CompositeVxFOnBoardDescriptor compositeobdobj = new CompositeVxFOnBoardDescriptor();
				compositeobdobj.setFilename(afile.getName());
				compositeobdobj.setAllBytes(Files.readAllBytes(path));
				compositeobdobj.setObd(vobd);
				busController.onBoardVxFAddedByCompositeObj(compositeobdobj);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (Exception e) {

			return (ResponseEntity<?>) ResponseEntity.badRequest()
					.body("{ \"message\" : \"" + e.getStackTrace() + "\"}");
		}

		return ResponseEntity.ok(vxfobd);

	}

	@PutMapping(value = "/admin/vxfobds/{mpid}/offboard", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> offBoardDescriptor(@PathVariable("mpid") int mpid,
			@Valid @RequestBody final VxFOnBoardedDescriptor clobd) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		OnBoardingStatus previous_status = clobd.getOnBoardingStatus();

		VxFOnBoardedDescriptor obd = vxfOBDService.getVxFOnBoardedDescriptorByID(clobd.getId());
		obd.setOnBoardingStatus(OnBoardingStatus.OFFBOARDING);
		centralLogger.log(CLevel.INFO,
				"Onboarding Status change of VxF " + obd.getVxf().getName() + " to " + obd.getOnBoardingStatus(),
				compname);
		VxFOnBoardedDescriptor updatedObd = vxfOBDService.updateVxFOnBoardedDescriptor(obd);

		ResponseEntity<String> response = null;
		try {
			// response = aMANOController.offBoardVxFFromMANOProvider( updatedObd );
			busController.offBoardVxF(updatedObd);
		} catch (HttpClientErrorException e) {
			updatedObd.setOnBoardingStatus(previous_status);
			centralLogger.log(CLevel.INFO, "Onboarding Status change of VxF " + updatedObd.getVxf().getName() + " to "
					+ updatedObd.getOnBoardingStatus(), compname);
			updatedObd.setFeedbackMessage(e.getResponseBodyAsString());
			updatedObd = vxfOBDService.updateVxFOnBoardedDescriptor(updatedObd);
			JSONObject result = new JSONObject(e.getResponseBodyAsString()); // Convert String to JSON Object

			return (ResponseEntity<?>) ResponseEntity.status(e.getRawStatusCode()).contentType(MediaType.TEXT_PLAIN)
					.body("OffBoarding Failed! " + e.getStatusText() + ", " + result.getString("detail"));
		}

		if (response == null) {
			updatedObd.setOnBoardingStatus(previous_status);
			updatedObd.setFeedbackMessage(
					"Null Response on OffBoarding request.Requested VxFOnBoardedDescriptor with ID=\" + updatedObd.getId() + \" cannot be offboarded.");
			updatedObd = vxfOBDService.updateVxFOnBoardedDescriptor(updatedObd);

			return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.contentType(MediaType.TEXT_PLAIN)
					.body("Requested VxFOnBoardedDescriptor with ID=" + updatedObd.getId() + " cannot be offboarded");
		}

		updatedObd.setOnBoardingStatus(OnBoardingStatus.OFFBOARDED);
		centralLogger.log(CLevel.INFO, "Onboarding Status change of VxF " + updatedObd.getVxf().getName() + " to "
				+ updatedObd.getOnBoardingStatus(), compname);
		updatedObd.setFeedbackMessage(response.getBody().toString());
		updatedObd = vxfOBDService.updateVxFOnBoardedDescriptor(updatedObd);
		busController.offBoardVxF(updatedObd);

		return ResponseEntity.ok(updatedObd);

	}

	/********************************************************************************
	 * 
	 * admin ExperimentOnBoardDescriptors
	 * 
	 * @throws ForbiddenException
	 * 
	 ********************************************************************************/

	@GetMapping(value = "/admin/experimentobds", produces = "application/json")
	public ResponseEntity<?> getExperimentOnBoardDescriptors() throws ForbiddenException {
		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		return ResponseEntity.ok(nsdOBDService.getExperimentOnBoardDescriptors());
	}

	@PostMapping(value = "/admin/experimentobds/", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> addExperimentOnBoardDescriptor(@Valid @RequestBody ExperimentMetadata exp)
			throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		if (exp != null) {

			ExperimentMetadata refExp = (ExperimentMetadata) nsdService.getProductByID(exp.getId());
			ExperimentOnBoardDescriptor obd = new ExperimentOnBoardDescriptor();
			obd.setExperiment(refExp);
			obd.setUuid(UUID.randomUUID().toString());
			refExp.getExperimentOnBoardDescriptors().add(obd);

			// save product
			refExp = (ExperimentMetadata) nsdService.updateProductInfo(refExp);

			if (refExp != null) {
				return ResponseEntity.ok(refExp);
			} else {
				return (ResponseEntity<?>) ResponseEntity.badRequest().build();
			}

		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}

	}

	@PutMapping(value = "/admin/experimentobds/{mpid}", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> updateExperimentOnBoardDescriptor(@PathVariable("mpid") int mpid,
			@Valid @RequestBody ExperimentOnBoardDescriptor c) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		ExperimentOnBoardDescriptor u = nsdOBDService.updateExperimentOnBoardDescriptor(c);

		if (u != null) {
			return ResponseEntity.ok(u);
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}

	}

	@DeleteMapping(value = "/admin/experimentobds/{mpid}")
	public ResponseEntity<?> deleteExperimentOnBoardDescriptor(@PathVariable("mpid") int mpid)
			throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		ExperimentOnBoardDescriptor u = nsdOBDService.getExperimentOnBoardDescriptorByID(mpid);
		nsdOBDService.deleteExperimentOnBoardDescriptor(u);
		return ResponseEntity.ok("{}");

	}

	@GetMapping(value = "/admin/experimentobds/{mpid}", produces = "application/json")
	public ResponseEntity<?> getExperimentOnBoardDescriptorById(@PathVariable("mpid") int mpid)
			throws ForbiddenException {
		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN);
		}
		ExperimentOnBoardDescriptor sm = nsdOBDService.getExperimentOnBoardDescriptorByID(mpid);

		if (sm != null) {
			return ResponseEntity.ok(sm);
		} else {
			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}

	@GetMapping(value = "/admin/experimentobds/{mpid}/status", produces = "application/json")
	public ResponseEntity<?> getExperimentOnBoardDescriptorByIdCheckMANOProvider(@PathVariable("mpid") int mpid)
			throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		ExperimentOnBoardDescriptor sm = nsdOBDService.getExperimentOnBoardDescriptorByID(mpid);

		if (sm == null) {
			return (ResponseEntity<?>) ResponseEntity.notFound();
		}

//		
//		if (sm.getOnBoardingStatus().equals(OnBoardingStatus.ONBOARDING)) {
//
//			Nsd nsd = null;
//			List<Nsd> nsds = OSMClient.getInstance(sm.getObMANOprovider()).getNSDs();
//			if ( nsds != null ) {
//				for (Nsd v : nsds) {
//					if (v.getId().equalsIgnoreCase(sm.getVxfMANOProviderID())
//							|| v.getName().equalsIgnoreCase(sm.getVxfMANOProviderID())) {
//						nsd = v;
//						break;
//					}
//				}
//			}
//
//			if (nsd == null) {
//				sm.setOnBoardingStatus(OnBoardingStatus.UNKNOWN);
//			} else {
//				sm.setOnBoardingStatus(OnBoardingStatus.ONBOARDED);
//			}
//
//			sm = portalRepositoryRef.updateExperimentOnBoardDescriptor(sm);
//
//		}

		return ResponseEntity.ok(sm);

	}

	@PutMapping(value = "/admin/experimentobds/{mpid}/onboard", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> onExperimentBoardDescriptor(@PathVariable("mpid") int mpid,
			@Valid @RequestBody final ExperimentOnBoardDescriptor ed) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		try {
			ExperimentOnBoardDescriptor experimentonboarddescriptor = nsdOBDService
					.getExperimentOnBoardDescriptorByID(ed.getId());
			MANOprovider mp = manoProviderService.getMANOproviderByID(ed.getObMANOprovider().getId());
			experimentonboarddescriptor.setObMANOprovider(mp);
			experimentonboarddescriptor.getObMANOprovider().setId(ed.getObMANOprovider().getId());
			try {

				String[] fpath = experimentonboarddescriptor.getExperiment().getPackageLocation().split("/");
				logger.info("uuid: " + fpath[fpath.length - 2]);
				logger.info("Package: " + fpath[fpath.length - 1]);
				String vxfAbsfile = METADATADIR + fpath[fpath.length - 2] + File.separator + fpath[fpath.length - 1];
				File afile = new File(vxfAbsfile);
				Path path = Paths.get(afile.getAbsolutePath());
				CompositeExperimentOnBoardDescriptor compositeobdobj = new CompositeExperimentOnBoardDescriptor();
				compositeobdobj.setFilename(afile.getName());
				compositeobdobj.setAllBytes(Files.readAllBytes(path));
				compositeobdobj.setObd(experimentonboarddescriptor);
				busController.onBoardNSDAddedByCompositeObj(compositeobdobj);

				return ResponseEntity.ok(experimentonboarddescriptor);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("onExperimentBoardDescriptor, OSM4 fails authentication. Aborting Onboarding action.");
			centralLogger.log(CLevel.ERROR,
					"onExperimentBoardDescriptor, OSM4 fails authentication. Aborting Onboarding action.", compname);

			return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.contentType(MediaType.TEXT_PLAIN)
					.body("Requested Experiment Descriptor with ID=" + ed.getId() + " cannot be onboarded");
		}

		return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.contentType(MediaType.TEXT_PLAIN)
				.body("Requested Experiment Descriptor with ID=" + ed.getId() + " cannot be onboarded");
	}

	@PutMapping(value = "/admin/experimentobds/{mpid}/offboard", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> offBoardExperimentDescriptor(@PathVariable("mpid") int mpid,
			@Valid @RequestBody final ExperimentOnBoardDescriptor c) throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}

		ExperimentOnBoardDescriptor u = nsdOBDService.getExperimentOnBoardDescriptorByID(c.getId());

		OnBoardingStatus previous_status = u.getOnBoardingStatus();
		u.setOnBoardingStatus(OnBoardingStatus.OFFBOARDING);
		centralLogger.log(CLevel.INFO,
				"Onboarding Status change of VxF " + u.getExperiment().getName() + " to " + u.getOnBoardingStatus(),
				compname);
		ExperimentOnBoardDescriptor uExper = nsdOBDService.updateExperimentOnBoardDescriptor(u);

		ResponseEntity<String> response = null;
		try {
			// response = aMANOController.offBoardNSDFromMANOProvider(uExper);
			response = busController.offBoardNSD(uExper);
		} catch (HttpClientErrorException e) {
			uExper.setOnBoardingStatus(previous_status);
			centralLogger.log(CLevel.INFO, "Onboarding Status change of VxF " + uExper.getExperiment().getName()
					+ " to " + uExper.getOnBoardingStatus(), compname);
			uExper.setFeedbackMessage(e.getResponseBodyAsString());
			uExper = nsdOBDService.updateExperimentOnBoardDescriptor(uExper);
			JSONObject result = new JSONObject(e.getResponseBodyAsString()); // Convert String to JSON Object

			return (ResponseEntity<?>) ResponseEntity.status(e.getRawStatusCode()).contentType(MediaType.TEXT_PLAIN)
					.body("OffBoarding Failed! " + e.getStatusText() + ", " + result.getString("detail"));
		}

		if (response == null) {
			uExper.setOnBoardingStatus(previous_status);
			centralLogger.log(CLevel.INFO, "Onboarding Status change of VxF " + uExper.getExperiment().getName()
					+ " to " + uExper.getOnBoardingStatus(), compname);
			uExper.setFeedbackMessage(
					"Null response on OffBoarding request.Requested NSOnBoardedDescriptor with ID=\" + c.getId() + \" cannot be offboarded.");
			uExper = nsdOBDService.updateExperimentOnBoardDescriptor(uExper);

			return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.contentType(MediaType.TEXT_PLAIN)
					.body("Requested NSOnBoardedDescriptor with ID=" + c.getId() + " cannot be offboarded");
		}
		// Set Valid to false if it is OffBoarded
		uExper.getExperiment().setValid(false);
		uExper.setOnBoardingStatus(OnBoardingStatus.OFFBOARDED);
		centralLogger.log(CLevel.INFO, "Onboarding Status change of VxF " + uExper.getExperiment().getName() + " to "
				+ uExper.getOnBoardingStatus(), compname);
		uExper.setFeedbackMessage(response.getBody().toString());
		uExper = nsdOBDService.updateExperimentOnBoardDescriptor(uExper);
		busController.offBoardNSD(uExper);

		return ResponseEntity.ok(uExper);
	}

	/**
	 * 
	 * Infrastructure object API
	 */

	@GetMapping(value = "/admin/infrastructures", produces = "application/json")
	public ResponseEntity<?> getAdminInfrastructures() {
		return ResponseEntity.ok(infrastructureService.getInfrastructures());
	}

	@PostMapping(value = "/admin/infrastructures", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> addInfrastructure(@Valid @RequestBody Infrastructure c, HttpServletRequest request)
			throws ForbiddenException {

		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}		
		Infrastructure infrastructure = new Infrastructure(); 
		int MPfoundByName=0;
		
		if(c.getMp() != null){
			MPfoundByName=1;
			infrastructure.setMp(c.getMp());
		}

		infrastructure.setDatacentername(c.getDatacentername());
		infrastructure.setEmail(c.getEmail());
		infrastructure.setVIMid(c.getVIMid());
		infrastructure.setName(c.getName());
		infrastructure.setOrganization(c.getOrganization());
		Infrastructure u = infrastructureService.addInfrastructure(infrastructure);
		
		if (u != null && MPfoundByName==1) {
			return ResponseEntity.ok(u);
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}
	}

	@PutMapping(value = "/admin/infrastructures/{infraid}", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> updateInfrastructure(@PathVariable("infraid") int infraid,
			@Valid @RequestBody Infrastructure c) throws ForbiddenException {
		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		Infrastructure infrastructure = infrastructureService.getInfrastructureByID(infraid);
		int MPfoundByName=0;
		if(c.getMp() != null){
			MPfoundByName=1;
			infrastructure.setMp(c.getMp());
		}
		infrastructure.setDatacentername(c.getDatacentername());
		infrastructure.setEmail(c.getEmail());
		infrastructure.setVIMid(c.getVIMid());
		infrastructure.setName(c.getName());
		infrastructure.setOrganization(c.getOrganization());
		Infrastructure u = infrastructureService.updateInfrastructureInfo(infrastructure);

		if (u != null && MPfoundByName==1) {
			return ResponseEntity.ok(u);
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}

	}

	@DeleteMapping(value = "/admin/infrastructures/{infraid}", produces = "application/json")
	public ResponseEntity<?> deleteInfrastructure(@PathVariable("infraid") int infraid) throws ForbiddenException {
		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		Infrastructure infrastructure = infrastructureService.getInfrastructureByID(infraid);
		infrastructureService.deleteInfrastructure(infrastructure);
		return ResponseEntity.ok("{}");

	}

	@GetMapping(value = "/admin/infrastructures/{infraid}", produces = "application/json")
	public ResponseEntity<?> getInfrastructureById(@PathVariable("infraid") int infraid) throws ForbiddenException {
		if (!checkUserIDorIsAdmin(-1)) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN)
																			// ;
		}
		Infrastructure sm = infrastructureService.getInfrastructureByID(infraid);

		if (sm != null) {
			return ResponseEntity.ok(sm);
		} else {
			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
	}

	@PostMapping(value = "/admin/infrastructures/{infraid}/images/{vfimageid}", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> addImageToInfrastructure(@PathVariable("infraid") int infraid,
			@PathVariable("vfimageid") int vfimageid) throws ForbiddenException {

		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if ((!u.getRoles().contains(UserRoleType.ROLE_ADMIN))
				&& (!u.getRoles().contains(UserRoleType.ROLE_TESTBED_PROVIDER))) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN);

		}

		Infrastructure infrs = infrastructureService.getInfrastructureByID(infraid);
		VFImage vfimg = vfImageService.getVFImageByID(vfimageid);

		if ((infrs != null) && (vfimg != null)) {

			if (vfimg.getDeployedInfrastructureById(infrs.getId()) == null) {
				vfimg.getDeployedInfrastructures().add(infrs);
			}
			if (infrs.getSupportedImageById(vfimg.getId()) == null) {
				infrs.getSupportedImages().add(vfimg);
			}

			vfImageService.updateVFImageInfo(vfimg);
			infrastructureService.updateInfrastructureInfo(infrs);
			return ResponseEntity.ok(infrs);
		} else {
			return (ResponseEntity<?>) ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Validation Result
	 * 
	 * @throws ForbiddenException
	 */

	@PutMapping(value = "/admin/validationjobs/{vxf_id}", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> updateUvalidationjob(@PathVariable("vxf_id") int vxfid,
			@Valid @RequestBody ValidationJobResult vresult) throws ForbiddenException {
		logger.info("Received PUT ValidationJobResult for vxfid: " + vresult.getVxfid());

		PortalUser u = usersService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

		if ((!u.getRoles().contains(UserRoleType.ROLE_ADMIN))
				&& (!u.getRoles().contains(UserRoleType.ROLE_TESTBED_PROVIDER))) {
			throw new ForbiddenException("The requested page is forbidden");// return (ResponseEntity<?>)
																			// ResponseEntity.status(HttpStatus.FORBIDDEN);

		}

		vxfid = vresult.getVxfid();

		Product prod = productService.getProductByID(vxfid);

		if (prod == null) {
			logger.info("updateUvalidationjob: prod == null for VXF with id=" + vxfid + ". Return Status NOT_FOUND");
			centralLogger.log(CLevel.INFO,
					"updateUvalidationjob: prod == null for VXF with id=" + vxfid + ". Return Status NOT_FOUND",
					compname);

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}
		if (!(prod instanceof VxFMetadata)) {
			logger.info("updateUvalidationjob: prod not instance of VxFMetadata for VXF with id=" + vxfid
					+ ". Return Status NOT_FOUND");
			centralLogger.log(CLevel.INFO,
					"updateUvalidationjob: prod == null for VXF with id=" + vxfid + ". Return Status NOT_FOUND",
					compname);

			return (ResponseEntity<?>) ResponseEntity.notFound();
		}

		VxFMetadata vxf = (VxFMetadata) prod;

//		We select by desing not to certify upon Successful Validation. Thus we comment this.
//		if ( vresult.getValidationStatus() ) {
//			vxf.setCertified( true );
//			vxf.setCertifiedBy( "5GinFIRE " );			
//		}
		vxf.setValidationStatus(ValidationStatus.COMPLETED);

		ValidationJob validationJob = new ValidationJob();
		validationJob.setDateCreated(new Date());
		validationJob.setJobid(vresult.getBuildId() + "");
		validationJob.setOutputLog(vresult.getOutputLog());
		validationJob.setValidationStatus(vresult.getValidationStatus());
		validationJob.setVxfid(vxfid);
		vxf.getValidationJobs().add(validationJob);

		// save product
		vxf = (VxFMetadata) productService.updateProductInfo(vxf);

		busController.updatedVxF(vxf);
		busController.updatedValidationJob(vxf);

		VxFMetadata vxfr = (VxFMetadata) productService.getProductByID(vxfid); // rereading this, seems to keep the DB
																				// connection

		return ResponseEntity.ok(vxfr);
	}
}
