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


package portal.api.validation.ci;

import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.openslice.model.ExperimentMetadata;
import io.openslice.model.Product;
import io.openslice.model.ValidationJob;
import io.openslice.model.ValidationStatus;
import io.openslice.model.VxFMetadata;
import portal.api.service.PortalPropertiesService;



/**
 * @author ctranoris
 *
 */
@Component
@Configuration
public class ValidationCIRouteBuilder extends RouteBuilder {

	private static String JENKINSCIKEY = "";
	private static String PIPELINE_TOKEN = "test";	 
	private static String JENKINSCIURL = "";
	

	@Autowired
	PortalPropertiesService propsService;
	
	public void configure() {

		if (propsService.getPropertyByName("jenkinsciurl").getValue() != null) {
			JENKINSCIURL = propsService.getPropertyByName("jenkinsciurl").getValue();
		}
		if (propsService.getPropertyByName("jenkinscikey").getValue() != null) {
			JENKINSCIKEY = propsService.getPropertyByName("jenkinscikey").getValue();
		}
		if (propsService.getPropertyByName("pipelinetoken").getValue() != null) {
			PIPELINE_TOKEN = propsService.getPropertyByName("pipelinetoken").getValue();
		}
		
		if ( ( JENKINSCIURL == null ) || JENKINSCIURL.equals( "" ) ){
			return; //no routing towards JENKINS
		}
		if ( ( JENKINSCIKEY == null ) || JENKINSCIKEY.equals( "" ) ){
			return;//no routing towards JENKINS
		}
		
		
		/**
		 * Create VxF Validate New Route
		 */
		// This needs testing 12052019
		from("seda:vxf.new.validation?multipleConsumers=true")
		.log( "Submit new validation request for VNF_ID=${body}" )	
		.errorHandler(deadLetterChannel("direct:dlq_validations")
				.maximumRedeliveries( 3 ) //let's try 3 times to send it....
				.redeliveryDelay( 30000 ).useOriginalMessage()
				//.deadLetterHandleNewException( false )
				//.logExhaustedMessageHistory(false)
				.logExhausted(true)
				.logHandled(true)
				//.retriesExhaustedLogLevel(LoggingLevel.WARN)
				.retryAttemptedLogLevel( LoggingLevel.WARN) )
		.delay(30000)
		.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http.HttpMethods.POST))
		.process( headerExtractProcessor )
		.toD( "http://" + JENKINSCIURL + "/job/validation_pipeline/buildWithParameters?token=" + PIPELINE_TOKEN + "&VNF_ID=${header.id}")
		.to("stream:out");
				
		
		/**
		 * dead Letter Queue Users if everything fails to connect
		 */
		from("direct:dlq_validations")
		//.setBody()
		//.body(String.class)
		.process( ErroneousValidationProcessor )
		.to( "seda:vxf.validationresult.update?multipleConsumers=true")
		.to("stream:out");
		
	}
	
	Processor ErroneousValidationProcessor = new Processor() {
		
		@Override
		public void process(Exchange exchange) throws Exception {

			Map<String, Object> headers = exchange.getIn().getHeaders(); 
			Product aProd = exchange.getIn().getBody( Product.class ); 
			
		    		
			if (aProd instanceof VxFMetadata) {
				((VxFMetadata) aProd).setValidationStatus( ValidationStatus.COMPLETED );
			} else if (aProd instanceof ExperimentMetadata) {
				((ExperimentMetadata) aProd).setValidationStatus( ValidationStatus.COMPLETED );
			}
			
			
			if ( aProd.getValidationJobs() != null ) {
				ValidationJob j = new ValidationJob();
				j.setDateCreated( new Date() );
				j.setJobid("ERROR");
				j.setValidationStatus(false);
				j.setOutputLog( "There is an error from the Validation Service" );
				aProd.getValidationJobs().add(j);
			}
		    
		    exchange. getOut().setBody( aProd  );
		    // copy attachements from IN to OUT to propagate them
		    //exchange.getOut().setAttachments(exchange.getIn().getAttachments());
			
		}
	};
	
	
	Processor headerExtractProcessor = new Processor() {
		
		@Override
		public void process(Exchange exchange) throws Exception {

			Map<String, Object> headers = exchange.getIn().getHeaders(); 
			//VxFMetadata m = exchange.getIn().getBody( VxFMetadata.class );
		    //headers.put("id", m.getId()  );
			long id = Long.parseLong(exchange.getIn().getBody().toString());
			headers.put("id", id);
		    String encoding = Base64.getEncoder().encodeToString( (JENKINSCIKEY).getBytes() );
		    headers.put("Authorization",  "Basic " + encoding  );
		    
		    exchange.getOut().setHeaders(headers);
		    
//		    //copy Description to Comment
//		    aBug.setComment( BugzillaClient.createComment( aBug.getDescription() ) );
//		    //delete Description
//		    aBug.setDescription( null );
//		    aBug.setAlias( null ); //dont put any Alias		
//		    aBug.setCc( null );
		    
		    exchange.getOut().setBody( "" );
		    // copy attachements from IN to OUT to propagate them
		    //exchange.getOut().setAttachments(exchange.getIn().getAttachments());
			
		}
	};

}
