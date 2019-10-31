package portal.api.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.consul.ConsulConstants;
import org.apache.camel.component.consul.endpoint.ConsulKeyValueActions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Configuration
@Component
@Profile("!testing")
public class PropertiesRouteBuilder  extends RouteBuilder {


	@Value("${spring.cloud.consul.host}")
	private String consulHost;
	
	
	
	private static final transient Log logger = LogFactory.getLog( PropertiesRouteBuilder.class.getName() );
	
	public void configure() {

		
		logger.info("consulHost for publishing properties is: " + consulHost );
		
		
		 from("seda:properties.update?multipleConsumers=true")
		    .setHeader(ConsulConstants.CONSUL_ACTION, constant( ConsulKeyValueActions.PUT) )
		    .setHeader(ConsulConstants.CONSUL_KEY, constant("config/openslice/osdata") )
         	.toD("consul:kv?url=" + consulHost) 
            .to("log:camel-consul?level=INFO");
		
		 
	
	}
}
