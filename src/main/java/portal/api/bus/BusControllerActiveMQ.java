package portal.api.bus;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.ExperimentMetadata;
import io.openslice.model.ExperimentOnBoardDescriptor;
import io.openslice.model.PortalUser;
import io.openslice.model.VxFMetadata;
import io.openslice.model.VxFOnBoardedDescriptor;
import portal.api.service.DeploymentDescriptorService;
import portal.api.service.NSDService;
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
		.marshal().json( JsonLibrary.Jackson, VxFMetadata.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:vxf.onboard" );
		
		from("seda:vxf.onboard.fail?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, VxFOnBoardedDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:vxf.onboard.fail" );
		
		
		from("seda:vxf.onboard.success?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, VxFOnBoardedDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:vxf.onboard.success" );
		
				
		from("seda:vxf.offboard?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, VxFMetadata.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:vxf.offboard" );
		
		
		
		from("seda:nsd.onboard?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, ExperimentMetadata.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.onboard" );
		
		from("seda:nsd.onboard.success?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, ExperimentOnBoardDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.onboard.success" );

		
		
		from("seda:nsd.offboard?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, ExperimentMetadata.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.offboard" );
		
		from("seda:nsd.onboard.fail?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, ExperimentOnBoardDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.onboard.fail" );
		
		
		from("seda:deployments.create?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:deployments.create" );
		
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
		.bean( deploymentDescriptorService, "getDeploymentEagerData" )
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deploy" );
		
		from("seda:nsd.deployment.instantiation.success?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.instantiation.success" );
		
		from("seda:nsd.deployment.instantiation.fail?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.instantiation.fail" );
		
		from("seda:nsd.deployment.termination.success?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.termination.success" );
		
		from("seda:nsd.deployment.termination.fail?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.termination.fail" );
		
		from("seda:nsd.deployment.complete?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.complete" );
		
		from("seda:nsd.deployment.delete?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.delete" );

		from("seda:nsd.deployment.reject?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.deployment.reject" );
		

		from("seda:nsd.instance.termination.success?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.instance.termination.success" );

		from("seda:nsd.instance.termination.fail?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.instance.termination.fail" );

		from("seda:nsd.instance.deletion.success?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.instance.deletion.success" );

		from("seda:nsd.instance.deletion.fail?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, DeploymentDescriptor.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:nsd.instance.deletion.fail" );
		
		
		/**
		 * Response message queues
		 */
		
		from("activemq:queue:getVxFByID")
		.log( "activemq:queue:getVxFByID for ${body} !" )		
		.bean( vxfService, "getProductByIDEagerData" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		

		from("activemq:queue:getNSDByID")
		.log( "activemq:queue:getNSDByID for ${body} !" )		
		.bean( nsdService, "getProductByIDEagerData" )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		

	}

}
