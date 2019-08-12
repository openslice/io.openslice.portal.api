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

@Configuration
@Component
public class BusControllerActiveMQ  extends RouteBuilder {

	@Autowired
	CamelContext actx;


	private static final transient Log logger = LogFactory.getLog(BusControllerActiveMQ.class.getName());

	// template.withBody( objectMapper.writeValueAsString(user) ).asyncSend();

	@Override
	public void configure() throws Exception {
		
		from("seda:users.create?multipleConsumers=true")
		.marshal().json( JsonLibrary.Jackson, PortalUser.class, true)
		.convertBodyTo( String.class )
		.to( "activemq:topic:users.create" );

	}

}
