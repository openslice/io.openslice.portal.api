///*-
// * ========================LICENSE_START=================================
// * io.openslice.portal.api
// * %%
// * Copyright (C) 2019 openslice.io
// * %%
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *      http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * =========================LICENSE_END==================================
// */
//
//
//package portal.api.centrallog;
//
//import java.io.IOException;
//import java.time.Instant;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.Future;
//
//import org.apache.camel.CamelContext;
//import org.apache.camel.Exchange;
//import org.apache.camel.FluentProducerTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
///**
// * @author ctranoris
// *
// */
//@Configuration
//public class CentralLogger {
//	
//	/** the Camel Context configure via Spring. See bean.xml*/	
//	private static CamelContext actx;
//	
//	@Autowired
//	public void setActx(CamelContext actx) {
//		CentralLogger.actx = actx;
//	}
//	
//	/**
//	 * @param cl
//	 * @param amessage
//	 * @param componentName
//	 */
//	public static void log(CLevel cl, String amessage){
//	
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("clevel", cl.toString() );
//			map.put("message", amessage );
//			
//
//			String json;
//			try {
//				json = new ObjectMapper().writeValueAsString(map);
//				//System.out.println(json);
//				FluentProducerTemplate template = actx.createFluentProducerTemplate().to("seda:centralLog?multipleConsumers=true");
//				Future<Exchange> result = template.withBody( json ).asyncSend();
//				waitAndStopForTemplate( result, template);
//
//			} catch (JsonProcessingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		
//	}
//	
//	/**
//	 * 
//	 * utility function to stop ProducerTemplate
//	 * @param result
//	 * @param template
//	 */
//	private static void waitAndStopForTemplate(Future<Exchange> result, FluentProducerTemplate template) {
//		while (true) {			
//			if (result.isDone()) {
//				//logger.info( "waitAndStopForTemplate: " + template.toString() + " [STOPPED]");
//				try {
//					template.stop();
//					template.clearAll();
//					template.cleanUp();
//					break;
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//			try {
//				//logger.info( "waitAndStopForTemplate: " + template.toString() + " [WAITING...]");
//				Thread.sleep( 5000 );
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//		
//	}
//	
//	/**
//	 * @param values
//	 * @param componentName
//	 */
//	public static void simpleMon( String values, String componentName){
//		
//	
//
//		String json;
//		try {
//
//			HashMap<String, String> map =
//			        new ObjectMapper().readValue( values, HashMap.class);
//
//			map.put("time", Instant.now().toString());
//			map.put("component", componentName );
//			
//			json = new ObjectMapper().writeValueAsString(map);
//			//System.out.println(json);
//			FluentProducerTemplate template = actx.createFluentProducerTemplate().to("seda:simplemon?multipleConsumers=true");
//			template.withBody( json ).asyncSend();
//
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	
//}
//}
