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
package portal.api.bus;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.openslice.model.CompositeExperimentOnBoardDescriptor;
import io.openslice.model.CompositeVxFOnBoardDescriptor;
import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.ExperimentOnBoardDescriptor;
import io.openslice.model.PortalUser;
import io.openslice.model.VxFOnBoardedDescriptor;
import portal.api.service.DeploymentDescriptorService;
import portal.api.service.InfrastructureService;
import portal.api.service.ManoProviderService;
import portal.api.service.NSDOBDService;
import portal.api.service.NSDService;
import portal.api.service.PortalPropertiesService;
import portal.api.service.UsersService;
import portal.api.service.VxFOBDService;
import portal.api.service.VxFService;

@Configuration
@Component
public class BusControllerActiveMQ  extends RouteBuilder {

	@Autowired
	CamelContext actx;


	@Autowired
	VxFService vxfService;
	

	@Autowired
	NSDService nsdService;

	@Autowired
	DeploymentDescriptorService deploymentDescriptorService;
	
	@Autowired
	VxFOBDService vxfObdService;

	@Autowired
	NSDOBDService nsdObdService;

	@Autowired
	ManoProviderService manoProviderService;

	@Autowired
	InfrastructureService infraStructureService;

	@Autowired
	NSDService experimentService;
	
	@Autowired
	PortalPropertiesService portalPropertyService;
	
	@Autowired
	UsersService usersService;

	@Value("${NFV_CATALOG_GET_NSD_BY_ID}")
	private String NFV_CATALOG_GET_NSD_BY_ID = "";
	
	@Value("${NFV_CATALOG_DEPLOY_NSD_REQ}")
	private String NFV_CATALOG_DEPLOY_NSD_REQ = "";

	@Value("${NFV_CATALOG_GET_DEPLOYMENT_BY_ID}")
	private String NFV_CATALOG_GET_DEPLOYMENT_BY_ID = "";
	
	
	@Value("${GET_USER_BY_USERNAME}")
	private String GET_USER_BY_USERNAME = "";
	

	@Value("${NFV_CATALOG_UPD_DEPLOYMENT_BY_ID}")
	private String NFV_CATALOG_UPD_DEPLOYMENT_BY_ID = "";
	
	private static final transient Log logger = LogFactory.getLog(BusControllerActiveMQ.class.getName());

	// template.withBody( objectMapper.writeValueAsString(user) ).asyncSend();

	@Override
	public void configure() throws Exception {
		
		/**
		 * from internal messaging to ActiveMQ
		 */
		from("seda:users.create?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, PortalUser.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:users.create" );
		
		from("seda:vxf.onboard?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, VxFOnBoardedDescriptor.class, true)
		.convertBodyTo( String.class )
		.log( "Send to activemq:topic:vxf.onboard the payload ${body} !" )
		.to( "activemq:topic:vxf.onboard" );

		from("seda:vxf.onBoardByCompositeObj?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, CompositeVxFOnBoardDescriptor.class, true)
		.convertBodyTo( String.class )
		.log( "Send to activemq:topic:vxf.onBoardByCompositeObj the payload ${body} !" )
		.to( "activemq:topic:vxf.onBoardByCompositeObj" );

		from("seda:vxf.offboard?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, VxFOnBoardedDescriptor.class, true)
		.convertBodyTo( String.class )
		.log( "Send to activemq:topic:vxf.offboard the payload ${body} !" )
		.to( "activemq:topic:vxf.offboard" )
		.log("Got back from activemq:topic:vxf.offboard ${body}");
		
		from("seda:vxf.onboard.fail?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, VxFOnBoardedDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:vxf.onboard.fail" );
		
		
		from("seda:vxf.onboard.success?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, VxFOnBoardedDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:vxf.onboard.success" );
						
		from("seda:nsd.onboard?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, ExperimentOnBoardDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.onboard" );
		
		from("seda:nsd.onBoardByCompositeObj?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, CompositeExperimentOnBoardDescriptor.class, true)
		.convertBodyTo( String.class )
		.log( "Send to activemq:topic:nsd.onBoardByCompositeObj the payload ${body} !" )
		.to( "activemq:topic:nsd.onBoardByCompositeObj" );		
		
		from("seda:nsd.onboard.success?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, ExperimentOnBoardDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.onboard.success" );

		from("seda:nsd.offboard?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, ExperimentOnBoardDescriptor.class, true)
		.convertBodyTo( String.class )
		.log( "Send to activemq:topic:nsd.offboard the payload ${body} !" )
		.to( "activemq:topic:nsd.offboard" )
		.log("Got back from activemq:topic:nsd.offboard ${body}");
	
		from("seda:nsd.onboard.fail?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, ExperimentOnBoardDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.onboard.fail" );
		
		//Dead end
		//from("seda:deployments.create?multipleConsumers=true")
		//.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		//.convertBodyTo( String.class )
		//.to( "activemq:topic:deployments.create" );
		
		from("seda:deployments.reject?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:deployments.reject" );
		
		from("seda:deployments.update?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:deployments.update" );
		
		from("seda:nsd.schedule?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.schedule" );
		
		from("seda:nsd.deploy?multipleConsumers=true")		
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deploy" );
		
		from("seda:nsd.deployment.instantiation.success?multipleConsumers=true")
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.instantiation.success" );
		
		from("seda:nsd.deployment.instantiation.fail?multipleConsumers=true")
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.instantiation.fail" );
		
		from("seda:nsd.deployment.termination.success?multipleConsumers=true")
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.termination.success" );
		
		from("seda:nsd.deployment.termination.fail?multipleConsumers=true")
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.termination.fail" );
		
		from("seda:nsd.deployment.complete?multipleConsumers=true")
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.complete" );
		
		from("seda:nsd.deployment.delete?multipleConsumers=true")
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.delete" );

		from("seda:nsd.deployment.reject?multipleConsumers=true")
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.reject" );
		

		from("seda:nsd.instance.termination.success?multipleConsumers=true")
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.instance.termination.success" );

		from("seda:nsd.instance.termination.fail?multipleConsumers=true")
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.instance.termination.fail" );

		from("seda:nsd.instance.deletion.success?multipleConsumers=true")
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.instance.deletion.success" );

		from("seda:nsd.instance.deletion.fail?multipleConsumers=true")
		.bean( deploymentDescriptorService, "getDeploymentEagerDataJson" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.instance.deletion.fail" );
		
		from("seda:nsd.scalealert?multipleConsumers=true")
		.to("activemq:topic:nsd.scalealert");
		
		/**
		 * Response message queues
		 */
		
		from("activemq:queue:getVxFByID")
		.log( "activemq:queue:getVxFByID for ${body} !" )		
		.bean( vxfService, "getProductByIDDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");

		from("activemq:queue:getVxFByUUIDDataJson")
		.log( "activemq:queue:getVxFByUUDataJson for ${body} !" )		
		.bean( vxfService, "getVxFByUUIDDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getVxFByUUID")
		.log("activemq:queue:getVxFByUUID for ${body} !" )		
		.bean( vxfService , "getVxFByUUID" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getVxFByName")
		.log( "activemq:queue:getVxFByName for ${body} !" )		
		.bean( vxfService, "getProductByNameEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getNSDByID")
		.log( "activemq:queue:getNSDByID for ${body} !" )		
		.bean( nsdService, "getProductByIDDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getRunningInstantiatingAndTerminatingDeployments")
		.log( "activemq:queue:getRunningInstantiatingAndTerminatingDeployments !" )		
		.bean( deploymentDescriptorService, "getRunningInstantiatingAndTerminatingDeploymentsEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getAllDeployments")
		.log( "activemq:queue:getAllDeployments !" )		
		.bean( deploymentDescriptorService, "getAllDeploymentsEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
				
		from("activemq:queue:getDeploymentsToInstantiate")
		.log( "activemq:queue:getDeploymentsToInstantiate !" )		
		.bean( deploymentDescriptorService, "getDeploymentsToInstantiateEagerDataJson")
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getDeploymentsToBeCompleted")
		.log( "activemq:queue:getDeploymentsToBeCompleted !" )		
		.bean( deploymentDescriptorService, "getDeploymentsToBeCompletedEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getDeploymentsToBeDeleted")
		.log( "activemq:queue:getDeploymentsToBeDeleted !" )		
		.bean( deploymentDescriptorService, "getDeploymentsToBeDeletedEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getDeploymentByIdEager")
		//.log( "activemq:queue:getDeploymentByIdEager !" )
		.log(LoggingLevel.INFO, log, "activemq:queue:getDeploymentByIdEager ${body} message received!")		
		.bean( deploymentDescriptorService, "getDeploymentByIdEagerDataJson" )
		.log(LoggingLevel.INFO, log, "activemq:queue:getDeploymentByIdEager replied with ${body} !")
		.to("log:DEBUG?showBody=true&showHeaders=true");

		from("activemq:queue:getDeploymentByInstanceIdEager")
		.log( "activemq:queue:getDeploymentByInstanceIdEager !" )		
		.bean( deploymentDescriptorService, "getDeploymentByInstanceIdEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:updateDeploymentDescriptor")
		.log( "activemq:queue:updateDeploymentDescriptor !" )
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)		
		.bean( deploymentDescriptorService, "updateDeploymentEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getVxFOnBoardedDescriptorByVxFAndMP")
		.log("activemq:queue:getVxFOnBoardedDescriptorByVxFAndMP for ${body} !" )		
		.bean( vxfObdService , "getVxFOnBoardedDescriptorByVxFAndMP" )
		.to("log:DEBUG?showBody=true&showHeaders=true");		

		from("activemq:queue:getVxFOnBoardedDescriptorListDataJson")
		.log("activemq:queue:getVxFOnBoardedDescriptorListDataJson!" )		
		.bean( vxfObdService , "getVxFOnBoardedDescriptorListDataJson" )
		.log(LoggingLevel.INFO, log, "\"activemq:queue:getVxFOnBoardedDescriptorListDataJson replied with ${body} !")
		.to("log:DEBUG?showBody=true&showHeaders=true");		
		
		from("activemq:queue:getExperimentOnBoardDescriptorsDataJson")
		.log("activemq:queue:getExperimentOnBoardDescriptorsDataJson!" )		
		.bean( nsdObdService , "getExperimentOnBoardDescriptorsDataJson" )
		.log(LoggingLevel.INFO, log, "activemq:queue:getExperimentOnBoardDescriptorsDataJson replied with ${body} !")
		.to("log:DEBUG?showBody=true&showHeaders=true");		
		
		from("activemq:queue:updateVxFOnBoardedDescriptor")
		.log( "activemq:queue:updateVxFOnBoardedDescriptor for ${body} !" )		
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.VxFOnBoardedDescriptor.class, true)		
		.bean( vxfObdService , "updateVxFOBDEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");

		from("activemq:queue:addInfrastructure")
		.log( "activemq:queue:addInfrastructure for ${body} !" )		
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.Infrastructure.class, true)		
		.bean( infraStructureService , "addInfrastructureEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");

		from("activemq:queue:updateInfrastructure")
		.log( "activemq:queue:updateInfrastructure for ${body} !" )		
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.Infrastructure.class, true)		
		.bean( infraStructureService , "updateInfrastructureEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:addVxFMetadata")
		.log( "activemq:queue:addVxFMetadata for ${body} !" )		
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.VxFMetadata.class, true)		
		.bean( vxfService , "addVxFMetadataEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:addExperimentMetadata")
		.log( "activemq:queue:addExperimentMetadata for ${body} !" )		
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.ExperimentMetadata.class, true)		
		.bean( nsdService , "addNSDMetadataEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:addVxFOnBoardedDescriptor")
		.log( "activemq:queue:addVxFOnBoardedDescriptor for ${body} !" )		
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.VxFOnBoardedDescriptor.class, true)		
		.bean( vxfObdService , "addVxFOnBoardedDescriptorEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:addExperimentOnBoardedDescriptor")
		.log( "activemq:queue:addExperimentOnBoardedDescriptor for ${body} !" )		
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.ExperimentOnBoardDescriptor.class, true)		
		.bean( nsdObdService , "addExperimentOnBoardedDescriptorEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:updateExperimentOnBoardDescriptor")
		.log( "activemq:queue:updateExperimentOnBoardDescriptor for ${body} !" )		
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.ExperimentOnBoardDescriptor.class, true)		
		.bean( nsdObdService , "updateNSDOBDEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");		
				
		from("activemq:queue:getMANOProviderByID")
		.log( "activemq:queue:getMANOproviderByID !" )		
		.bean( manoProviderService, "getMANOproviderByIDEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");


		from("activemq:queue:getMANOProviders")
		.log( "activemq:queue:getMANOproviders !" )		
		.bean( manoProviderService, "getMANOprovidersEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getMANOProvidersForSync")
		.log( "activemq:queue:getMANOprovidersForSync !" )		
		.bean( manoProviderService, "getMANOprovidersForSyncEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getInfrastructures")
		.log( "activemq:queue:getInfrastructures !" )		
		.bean( infraStructureService, "getInfrastructuresEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getExperiments")
		.log( "activemq:queue:getExperiments !" )		
		.bean( experimentService, "getExperimentsEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from("activemq:queue:getVnfds")
		.log( "activemq:queue:getVnfds !" )		
		.bean( vxfService, "getVnfdsEagerDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		from( "activemq:queue:putActionOnNS" )
		.log(LoggingLevel.INFO, log, "activemq:queue:putActionOnNS message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.bean( nsdService, "getProductByIDEagerDataJson")
		.convertBodyTo( String.class );
		
		from( NFV_CATALOG_GET_NSD_BY_ID )
		.log(LoggingLevel.INFO, log, NFV_CATALOG_GET_NSD_BY_ID + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.bean( nsdService, "getProductByIDEagerDataJson")
		.convertBodyTo( String.class );
						
				
		from( NFV_CATALOG_DEPLOY_NSD_REQ )
		.log(LoggingLevel.INFO, log, NFV_CATALOG_DEPLOY_NSD_REQ + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, false)
		.bean( deploymentDescriptorService, "createDeploymentRequestJson")
		.convertBodyTo( String.class );
		
		//
		from( NFV_CATALOG_GET_DEPLOYMENT_BY_ID )
		.log(LoggingLevel.INFO, log, NFV_CATALOG_GET_DEPLOYMENT_BY_ID + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")	
		.bean( deploymentDescriptorService, "getDeploymentByIdEagerDataJson" )
		.convertBodyTo( String.class );
		
		
		from( NFV_CATALOG_UPD_DEPLOYMENT_BY_ID )
		.log(LoggingLevel.INFO, log, NFV_CATALOG_UPD_DEPLOYMENT_BY_ID + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")	
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, false)
		.bean( deploymentDescriptorService, "updateDeploymentEagerDataJson" )
		.convertBodyTo( String.class );
				
		from( GET_USER_BY_USERNAME )
		.log(LoggingLevel.INFO, log, GET_USER_BY_USERNAME + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")	
		.bean( usersService, "getPortalUserByUserNameDataJson" )
		.convertBodyTo( String.class );
		
		from("activemq:queue:getPortalUserByUsername")
		.log( "activemq:queue:getPortalUserByUsername for ${body} !" )					
		.bean( usersService , "getPortalUserByUserNameDataJson" )
		.to("log:DEBUG?showBody=true&showHeaders=true");		
		
	}

}
