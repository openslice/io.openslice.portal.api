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


package portal.api.mano;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import portal.api.bus.BusController;
//import portal.api.centrallog.CLevel;
//import portal.api.centrallog.CentralLogger;
import portal.api.service.DeploymentDescriptorService;
import portal.api.service.ManoProviderService;
import portal.api.service.NSDOBDService;
import portal.api.service.NSDService;
import portal.api.service.PortalPropertiesService;
import portal.api.service.VxFOBDService;
import portal.api.service.VxFService;
import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.DeploymentDescriptorStatus;
import io.openslice.model.ExperimentMetadata;
import io.openslice.model.ExperimentOnBoardDescriptor;
import io.openslice.model.MANOprovider;
import io.openslice.model.OnBoardDescriptor;
import io.openslice.model.OnBoardingStatus;
import io.openslice.model.VxFMetadata;
import io.openslice.model.VxFOnBoardedDescriptor;
import io.openslice.centrallog.client.*;



/**
 * @author ctranoris
 *
 */

@Configuration
public class MANOController {

	/** */
	private static final transient Log logger = LogFactory.getLog(MANOController.class.getName());
	

	@Autowired
	VxFOBDService vxfOBDService;
	
	@Autowired
	VxFService vxfService;

	@Autowired
	DeploymentDescriptorService deploymentDescriptorService;
	
	@Autowired
	ManoProviderService manoProviderService;
	
	@Autowired
	NSDService nsdService;

	@Autowired
	NSDOBDService nsdOBDService;

	@Autowired
	PortalPropertiesService propsService;
	

	@Autowired
	BusController busController;
	
	@Autowired
	MANOStatus aMANOStatus;
	
	public MANOController() {

	}
	

	@Value("${spring.application.name}")
	private String compname;


	@Autowired
	private CentralLogger centralLogger;

	
	@Bean("aMANOController")
	public MANOController aMANOControllerBean() {
		return new MANOController();
	}

//	private static String HTTP_SCHEME = "https:";

//	public static void setHTTPSCHEME(String url) {
//		logger.info("setHTTPSCHEME url = " + url);
////		if (url.contains("localhost")) {
////			HTTP_SCHEME = "http:";
////		}
//		// HTTP_SCHEME = url + ":";
//	}

	
	/**
	 * onBoard a VNF to MANO Provider, as described by this descriptor
	 * 
	 * @param vxfobds
	 * @throws Exception
	 */
	public void onBoardVxFToMANOProvider( long vxfobdid ) throws Exception {

		if (vxfOBDService == null) {
			throw new Exception("vxfOBDService is NULL. Cannot load VxFOnBoardedDescriptor");
		}
		
		VxFOnBoardedDescriptor vxfobd = vxfOBDService.getVxFOnBoardedDescriptorByID(vxfobdid);
		// PortalRepository portalRepositoryRef = new PortalRepository();

		if (vxfobd == null) {
			throw new Exception("vxfobd is NULL. Cannot load VxFOnBoardedDescriptor");
		}
		
		if (vxfobd.getVxf() == null) {
			throw new Exception("vxfobd.getVxf() is NULL. Cannot load VxFOnBoardedDescriptor");
		}
		
		if (vxfobd.getVxf().getName() == null) {
			throw new Exception("vxfobd.getVxf() is NULL. Cannot load VxFOnBoardedDescriptor");
		}
		
		
		vxfobd.setOnBoardingStatus(OnBoardingStatus.ONBOARDING);
		// This is the Deployment ID for the portal
		vxfobd.setDeployId(UUID.randomUUID().toString());
//		VxFMetadata vxf = vxfobd.getVxf();
//		if (vxf == null) {
//			vxf = (VxFMetadata) vxfService.getProductByID(vxfobd.getVxfid());
//		}
		centralLogger.log( CLevel.INFO, "Onboarding status change of VxF "+vxfobd.getVxf().getName()+" to "+vxfobd.getOnBoardingStatus(), compname);						
		// Set MANO Provider VxF ID
		vxfobd.setVxfMANOProviderID( vxfobd.getVxf().getName());
		// Set onBoarding Date
		vxfobd.setLastOnboarding(new Date());

		VxFOnBoardedDescriptor vxfobds = vxfOBDService.updateVxFOnBoardedDescriptor(vxfobd);
		if (vxfobds == null) {
			throw new Exception("Cannot load VxFOnBoardedDescriptor");
		}


		String pLocation = vxfobd.getVxf().getPackageLocation();
		logger.info("VxF Package Location: " + pLocation);

		if (!pLocation.contains("http")) {
			pLocation = propsService.getPropertyByName( "maindomain" ).getValue() + pLocation;
		}
//		if (!pLocation.contains("http")) {
//			pLocation = "http:" + pLocation;
//			pLocation = pLocation.replace("\\", "/");
//		}					
		logger.info("PROPER VxF Package Location: " + pLocation);

		
		
	}

	public void checkAndDeleteTerminatedOrFailedDeployments() {
		logger.info("Check and Delete Terminated and Failed Deployments");
		List<DeploymentDescriptor> DeploymentDescriptorsToDelete = deploymentDescriptorService.getDeploymentsToBeDeleted();
		for (DeploymentDescriptor d : DeploymentDescriptorsToDelete) {
			// Launch the deployment
			logger.info("Send to bus control to delete: " + d.getId());
			busController.deleteExperiment(d);
		}
	}
	
	public void checkAndDeployExperimentToMANOProvider() {
		logger.info("This will trigger the check and Deploy Experiments");
		// Check the database for a new deployment in the next minutes
		// If there is a deployment to be made and the status is Scheduled
		List<DeploymentDescriptor> DeploymentDescriptorsToRun = deploymentDescriptorService.getDeploymentsToInstantiate();
		// Foreach deployment
		for (DeploymentDescriptor d : DeploymentDescriptorsToRun) {
			// Launch the deployment
			busController.deployExperiment(d );
		}
	}

	public void checkAndTerminateExperimentToMANOProvider() {
		logger.info("This will trigger the check and Terminate Deployments");
		// Check the database for a deployment to be completed in the next minutes
		// If there is a deployment to be made and the status is Scheduled
		List<DeploymentDescriptor> DeploymentDescriptorsToComplete = deploymentDescriptorService.getDeploymentsToBeCompleted();
		// Foreach deployment
		for (DeploymentDescriptor deployment_descriptor_tmp : DeploymentDescriptorsToComplete) {
			logger.debug("Deployment with id" + deployment_descriptor_tmp.getName() + " with status " + deployment_descriptor_tmp.getStatus() +" is going to be terminated");
			// Terminate the deployment
			busController.completeExperiment(deployment_descriptor_tmp );
		}
	}

	public void checkAndUpdateRunningDeploymentDescriptors() {
		logger.info("Update Deployment Descriptors");
		List<DeploymentDescriptor> runningDeploymentDescriptors = deploymentDescriptorService.getRunningInstantiatingAndTerminatingDeployments();
		// For each deployment get the status info and the IPs
		for (int i = 0; i < runningDeploymentDescriptors.size(); i++) {
			DeploymentDescriptor deployment_tmp = deploymentDescriptorService.getDeploymentByIdEager(runningDeploymentDescriptors.get(i).getId());
			try {
				// Get the MANO Provider for each deployment				
				MANOprovider sm = manoProviderService.getMANOproviderByID( getExperimOBD( deployment_tmp ).getObMANOprovider().getId() );
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		checkAndDeployExperimentToMANOProvider();
		checkAndTerminateExperimentToMANOProvider();
		checkAndDeleteTerminatedOrFailedDeployments();
	}

	private ExperimentOnBoardDescriptor getExperimOBD(DeploymentDescriptor deployment_tmp) {
		for (ExperimentOnBoardDescriptor e : deployment_tmp.getExperimentFullDetails().getExperimentOnBoardDescriptors()) {
			return e; //return the first one found
		}
		return null;
	}

	public void onBoardNSDToMANOProvider( long uexpobdid ) throws Exception {
		ExperimentOnBoardDescriptor uexpobd = nsdOBDService.getExperimentOnBoardDescriptorByID(uexpobdid);
		
		if (uexpobd == null) {
			throw new Exception("uexpobd is NULL. Cannot load VxFOnBoardedDescriptor");
		}
				
		uexpobd.setOnBoardingStatus(OnBoardingStatus.ONBOARDING);
		centralLogger.log( CLevel.INFO, "Onboarding status change of Experiment "+uexpobd.getExperiment().getName()+" to "+uexpobd.getOnBoardingStatus(), compname);													
		// This is the Deployment ID for the portal
		uexpobd.setDeployId(UUID.randomUUID().toString());
		ExperimentMetadata em = uexpobd.getExperiment();
		if (em == null) {
			em = (ExperimentMetadata) nsdService.getProductByID(uexpobd.getExperimentid());
		}

		/**
		 * The following is not OK. When we submit to OSMClient the createOnBoardPackage
		 * we just get a response something like response = {"output":
		 * {"transaction-id": "b2718ef9-4391-4a9e-97ad-826593d5d332"}} which does not
		 * provide any information. The OSM RIFTIO API says that we could get
		 * information about onboarding (create or update) jobs see
		 * https://open.riftio.com/documentation/riftware/4.4/a/api/orchestration/pkt-mgmt/rw-pkg-mgmt-download-jobs.htm
		 * with /api/operational/download-jobs, but this does not return pending jobs.
		 * So the only solution is to ask again OSM if something is installed or not, so
		 * for now the client (the portal ) must check via the
		 * getVxFOnBoardedDescriptorByIdCheckMANOProvider giving the VNF ID in OSM. OSM
		 * uses the ID of the yaml description Thus we asume that the vxf name can be
		 * equal to the VNF ID in the portal, and we use it for now as the OSM ID. Later
		 * in future, either OSM API provide more usefull response or we extract info
		 * from the VNFD package
		 * 
		 */

		//uexpobd.setVxfMANOProviderID(em.getName()); // Possible Error. This probably needs to be
		uexpobd.setExperimentMANOProviderID(em.getName());

		uexpobd.setLastOnboarding(new Date());

		ExperimentOnBoardDescriptor uexpobds = nsdOBDService.updateExperimentOnBoardDescriptor(uexpobd);
		if (uexpobds == null) {
			throw new Exception("Cannot load NSDOnBoardedDescriptor");
		}

		String pLocation = em.getPackageLocation();
		logger.info("NSD Package Location: " + pLocation);
		if (!pLocation.contains("http")) {
			pLocation = propsService.getPropertyByName( "maindomain" ).getValue() + pLocation;
		}
//		if (!pLocation.contains("http")) {
//			pLocation = "http:" + pLocation;
//			pLocation = pLocation.replace("\\", "/");
//		}				

	
	}

	/**
	 * offBoard a VNF to MANO Provider, as described by this descriptor
	 * 
	 * @param c
	 */
	public ResponseEntity<String> offBoardVxFFromMANOProvider(VxFOnBoardedDescriptor obd)
			throws HttpClientErrorException {
		// TODO Auto-generated method stub
		ResponseEntity<String> response = null;
	
	
		return response;
	}

	private void checkVxFStatus(VxFOnBoardedDescriptor obd) throws Exception {

		CamelContext tempcontext = new DefaultCamelContext();
		MANOController mcontroller = this;
		try {
			RouteBuilder rb = new RouteBuilder() {
				@Override
				public void configure() throws Exception {
					from("timer://getVNFRepoTimer?delay=2000&period=3000&repeatCount=6&daemon=true")
							.log("Will check VNF repo").setBody().constant(obd)
							.bean(mcontroller, "getVxFStatusFromOSM2Client");
				}
			};
			tempcontext.addRoutes(rb);
			tempcontext.start();
			Thread.sleep(30000);
		} finally {
			tempcontext.stop();
		}

	}

	
	public ResponseEntity<String> offBoardNSDFromMANOProvider(ExperimentOnBoardDescriptor uexpobd) {
		// TODO Auto-generated method stub
		ResponseEntity<String> response = null;
		
		
		// return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
		return response;
	}

	public void deployNSDToMANOProvider(int deploymentdescriptorid) {
		DeploymentDescriptor deploymentdescriptor = deploymentDescriptorService.getDeploymentByIdEager(deploymentdescriptorid);
		
		logger.info("deploymentdescriptor.getExperimentFullDetails() = " + getExperimOBD(deploymentdescriptor) ); 
		logger.info("deploymentdescriptor.getExperimentFullDetails() = " + getExperimOBD(deploymentdescriptor).getObMANOprovider());
		
		return;
	}

	public void terminateNSFromMANOProvider(int deploymentdescriptorid) {
		DeploymentDescriptor deploymentdescriptor = deploymentDescriptorService.getDeploymentByIdEager(deploymentdescriptorid);
		
		
	}

	public void deleteNSFromMANOProvider(int deploymentdescriptorid) {
		DeploymentDescriptor deploymentdescriptor = deploymentDescriptorService.getDeploymentByIdEager(deploymentdescriptorid);

		logger.info("Will delete with deploymentdescriptorid : " + deploymentdescriptorid);		
		String aMANOplatform = "";
		try {	
			aMANOplatform = getExperimOBD(deploymentdescriptor).getObMANOprovider().getSupportedMANOplatform().getName();
			logger.info("MANOplatform: " + aMANOplatform);			
		}catch (Exception e) {
			aMANOplatform = "UNKNOWN";
		}		
		
		
		
		
			//if this is not a suported OSM then just complete
			logger.info("Descriptor targets an older not supported OSM deploymentdescriptorid: " + deploymentdescriptorid);		
			deploymentdescriptor.setStatus(DeploymentDescriptorStatus.FAILED_OSM_REMOVED);	
			logger.info( "Status change of deployment " + deploymentdescriptor.getId()+", "+deploymentdescriptor.getName()+" to "+deploymentdescriptor.getStatus());					
			DeploymentDescriptor deploymentdescriptor_final = deploymentDescriptorService.updateDeploymentDescriptor(deploymentdescriptor);
			logger.info("NS status changed is now :" + deploymentdescriptor_final.getStatus());															
		
	}
	// OSM5 END
	
}
