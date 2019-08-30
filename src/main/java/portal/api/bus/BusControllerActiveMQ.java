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

import io.openslice.model.PortalUser;
import io.openslice.model.VxFMetadata;
import io.openslice.model.VxFOnBoardedDescriptor;
import portal.api.service.VxFService;

@Configuration
@Component
public class BusControllerActiveMQ  extends RouteBuilder {

	@Autowired
	CamelContext actx;


	@Autowired
	VxFService vxfService;

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
		
		
		
		/**
		 * Response message queues
		 */
		
		from("activemq:queue:getVxFByID")
		.log( "activemq:queue:getVxFByID for ${body} !" )		
		.bean( vxfService, "getProductByIDEagerData" )
		//.marshal().json( JsonLibrary.Jackson, VxFMetadata.class, true)
		//.convertBodyTo( String.class )
		.to("log:DEBUG?showBody=true&showHeaders=true");
		

	}

}
