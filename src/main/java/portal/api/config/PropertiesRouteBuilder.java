package portal.api.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.consul.ConsulConstants;
import org.apache.camel.component.consul.endpoint.ConsulKeyValueActions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Configuration
@Component
public class PropertiesRouteBuilder  extends RouteBuilder {


	private static final transient Log logger = LogFactory.getLog( PropertiesRouteBuilder.class.getName() );
	
	public void configure() {

		 from("seda:properties.update?multipleConsumers=true")
		    .setHeader(ConsulConstants.CONSUL_ACTION, constant( ConsulKeyValueActions.PUT) )
		    .setHeader(ConsulConstants.CONSUL_KEY, constant("config/openslice/osdata") )
         	.to("consul:kv?url=http://localhost:8500")
            .to("log:camel-consul?level=INFO");
		
		 
	
	}
}
