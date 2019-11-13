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

import java.util.concurrent.Future;

import javax.validation.Valid;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.ExperimentMetadata;
import io.openslice.model.ExperimentOnBoardDescriptor;
import io.openslice.model.PortalUser;
import io.openslice.model.VFImage;
import io.openslice.model.VxFMetadata;
import io.openslice.model.VxFOnBoardedDescriptor;
import portal.api.mano.MANOStatus;


/**
 * Exposes messages to Bus. Usually they should be aynchronous.
 * Consult http://camel.apache.org/uris.html for URIs
 * sendmessage(direct:mplampla) is Synchronous in same Context
 * sendmessage(seda:mplampla) is aSynchronous in same Context
 *  * 
 * @author ctranoris
 * 
 * 
 *
 */

@Configuration
@Component
public class BusController  {

	/** */
	private static BusController instance;
	
	/** the Camel Context configure via Spring. See bean.xml*/	

	/** This is set by method setActx, see later */
	static CamelContext contxt;


	private static final transient Log logger = LogFactory.getLog( BusController.class.getName());


	/**
	 * @return
	 */
	public static synchronized BusController getInstance() {
		if (instance == null) {
			instance = new BusController();
		}
		return instance;
	}

	/**
	 * @param actx
	 */
	@Autowired
	public void setActx(CamelContext actx) {
		BusController.contxt = actx;
		logger.info( "BusController configure() contxt = " + contxt);
	}



	
	/**
	 * Asynchronously sends to the routing bus (seda:users.create?multipleConsumers=true) that a new user is added
	 * @param user a {@link PortalUser}
	 */
	public void newUserAdded( PortalUser user ) {
		logger.info( "contxt = " + contxt);
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:users.create?multipleConsumers=true");
		template.withBody( user ).asyncSend();
		
	}

	/**
	 * Asynchronously sends to the routing bus (seda:deployments.create?multipleConsumers=true) that a new user is added
	 * @param deployment a {@link DeploymentDescriptor}
	 */
	public void newDeploymentRequest( DeploymentDescriptor deployment) {

		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:deployments.create?multipleConsumers=true");
		template.withBody( deployment ).asyncSend();
		
	}

	/**
	 * Asynchronously sends to the routing bus (seda:deployments.create?multipleConsumers=true) that a new user is added
	 * @param deployment a {@link DeploymentDescriptor}
	 */
	public void rejectDeploymentRequest( DeploymentDescriptor deployment ) {

		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:deployments.reject?multipleConsumers=true");
		template.withBody( deployment ).asyncSend();
		
	}
	
	/**
	 * Asynchronously sends to the routing bus (seda:deployments.create?multipleConsumers=true) that a new user is added
	 * @param deployment a {@link DeploymentDescriptor}
	 */
	public void updateDeploymentRequest( DeploymentDescriptor deployment ) {

		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:deployments.update?multipleConsumers=true");
		template.withBody( deployment ).asyncSend();
		
	}

	/**
	 * Asynchronously sends to the routing bus (seda:vxf.create?multipleConsumers=true) that a new vxf is added
	 * @param deployment a {@link VxFMetadata}
	 */
	// NOT USED
	public void newVxFUploadedToPortalRepo(long vxfmetadataid) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:vxf.create?multipleConsumers=true");
		template.withBody( vxfmetadataid ).asyncSend();				
	}

	/**
	 * Asynchronously sends to the routing bus (seda:vxf.onboard?multipleConsumers=true) to upload a new vxf
	 * @param deployment a {@link VxFMetadata}
	 */
	public void onBoardVxFAdded(VxFOnBoardedDescriptor obd) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:vxf.onboard?multipleConsumers=true");
		template.withBody( obd ).asyncSend();				
	}

	public void onBoardVxFFailed(VxFOnBoardedDescriptor vxfobds_final) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:vxf.onboard.fail?multipleConsumers=true");
		template.withBody( vxfobds_final ).asyncSend();			
	}

	public void onBoardVxFSucceded(VxFOnBoardedDescriptor vxfobds_final) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:vxf.onboard.success?multipleConsumers=true");
		template.withBody( vxfobds_final ).asyncSend();				
	}
	
	/**
	 * Asynchronously sends to the routing bus (seda:nsd.create?multipleConsumers=true) that a new NSD experiment is added
	 * @param deployment a {@link ExperimentMetadata}
	 */
	// There is no listener for nsd.create
	public void newNSDAdded(ExperimentMetadata experimentSaved) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.create?multipleConsumers=true");
		template.withBody( experimentSaved ).asyncSend();		
	}

	public void onBoardNSDFailed( ExperimentOnBoardDescriptor uexpobds) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.onboard.fail?multipleConsumers=true");
		template.withBody( uexpobds ).asyncSend();			
	}

	public void onBoardNSDSucceded(ExperimentOnBoardDescriptor uexpobdid) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.onboard.success?multipleConsumers=true");
		template.withBody( uexpobdid ).asyncSend();				
	}
	

	public void scheduleExperiment( DeploymentDescriptor aDeployment) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.schedule?multipleConsumers=true");
		template.withBody( aDeployment ).asyncSend();				
	}
	

	public void deployExperiment( DeploymentDescriptor  deploymentdescriptorid) {		
		logger.info( "deployExperiment: to(\"seda:nsd.deploy?multipleConsumers=true\")");		
		FluentProducerTemplate	template = contxt.createFluentProducerTemplate().to("seda:nsd.deploy?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();	
	}

	// seda:nsd.deployment
	
	public void deploymentInstantiationSucceded(DeploymentDescriptor deploymentdescriptorid)
	{
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.deployment.instantiation.success?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();						
	}
	
	public void deploymentInstantiationFailed(DeploymentDescriptor deploymentdescriptorid)
	{
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.deployment.instantiation.fail?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();						
	}

	public void deploymentTerminationSucceded(DeploymentDescriptor deploymentdescriptorid)
	{
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.deployment.termination.success?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();						
	}
	
	public void deploymentTerminationFailed(DeploymentDescriptor deploymentdescriptorid)
	{
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.deployment.termination.fail?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();						
	}
	
	public void completeExperiment(DeploymentDescriptor deploymentdescriptorid) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.deployment.complete?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();				
	}

	public void deleteExperiment(DeploymentDescriptor deploymentdescriptorid) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.deployment.delete?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();						
	}

	public void rejectExperiment(DeploymentDescriptor deploymentdescriptorid) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.deployment.reject?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();				
	}

//	public void osm4CommunicationFailed(Class<MANOStatus> manostatus)
//	{
//		FluentProducerTemplate template = actx.createFluentProducerTemplate().to("seda:communication.osm4.fail?multipleConsumers=true");
//		template.withBody(manostatus).asyncSend();						
//	}
//
//	public void osm4CommunicationRestored(Class<MANOStatus> manostatus)
//	{
//		FluentProducerTemplate template = actx.createFluentProducerTemplate().to("seda:communication.osm4.success?multipleConsumers=true");
//		template.withBody(manostatus).asyncSend();						
//	}
//	
	public void osm5CommunicationFailed(Class<MANOStatus> manostatus)
	{
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:communication.osm5.fail?multipleConsumers=true");
		template.withBody(manostatus).asyncSend();						
	}

	public void osm5CommunicationRestored(Class<MANOStatus> manostatus)
	{
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:communication.osm5.success?multipleConsumers=true");
		template.withBody(manostatus).asyncSend();						
	}
	
	public void terminateInstanceSucceded(DeploymentDescriptor deploymentdescriptorid)
	{
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.instance.termination.success?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();						
	}

	public void terminateInstanceFailed(DeploymentDescriptor deploymentdescriptorid)
	{
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.instance.termination.fail?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();						
	}

	public void deleteInstanceSucceded(DeploymentDescriptor deploymentdescriptorid) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.instance.deletion.success?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();								
	}
		
	public void deleteInstanceFailed(DeploymentDescriptor deploymentdescriptorid) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.instance.deletion.fail?multipleConsumers=true");
		template.withBody( deploymentdescriptorid ).asyncSend();								
	}
		
	/**
	 * Asynchronously sends to the routing bus (seda:vxf.update?multipleConsumers=true) that a vxf is updated
	 * @param deployment a {@link VxFMetadata}
	 */
	// There is no listener for this
	public void updatedVxF(VxFMetadata vxfmetadataid) {

		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:vxf.update?multipleConsumers=true");
		template.withBody( vxfmetadataid ).asyncSend();
		
	}

	/**
	 * Asynchronously sends to the routing bus (seda:nsd.update?multipleConsumers=true) that a  NSD experiment is updated
	 * @param experiment a {@link ExperimentMetadata}
	 */
	// There is no listener for this
	public void updateNSD(ExperimentMetadata expmetasaved) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.update?multipleConsumers=true");
		template.withBody( expmetasaved ).asyncSend();		
	}

	
	/**
	 * Asynchronously sends to the routing bus (seda:vxf.new.validation?multipleConsumers=true)to trigger VxF validation
	 * @param vxf a {@link VxFMetadata}
	 */
	public void validateVxF(VxFMetadata vxfmetadataid) {

		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:vxf.new.validation?multipleConsumers=true");
		template.withBody( vxfmetadataid ).asyncSend();
		
	}
		
	/**
	 * Asynchronously sends to the routing bus (seda:vxf.validationresult.update?multipleConsumers=true)to trigger update VxF validation
	 * @param vresult  a {@link ValidationJobResult}
	 */
	public void updatedValidationJob(VxFMetadata vxf) {

		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:vxf.validationresult.update?multipleConsumers=true");
		template.withBody( vxf ).asyncSend();
		
	}

	/**
	 * Asynchronously sends to the routing bus (seda:nsd.validate.new?multipleConsumers=true) to trigger NSD validation
	 * @param deployment a {@link ExperimentMetadata}
	 */
	// There is no listener for this
	public void validateNSD(ExperimentMetadata experimentSaved) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.validate.new?multipleConsumers=true");
		template.withBody( experimentSaved ).asyncSend();		
	}
	
	
	/**
	 * Asynchronously sends to the routing bus (seda:nsd.validate.update?multipleConsumers=true) to trigger NSD validation
	 * @param deployment a {@link ExperimentMetadata}
	 */
	// There is no listener for this
	public void validationUpdateNSD(ExperimentMetadata expmetasaved) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.validate.update?multipleConsumers=true");
		template.withBody( expmetasaved ).asyncSend();		
	}

	/**
	 * Asynchronously sends to the routing bus (seda:vxf.deleted?multipleConsumers=true) that a vxf is deleted
	 * @param deployment a {@link VxFMetadata}
	 */
	// There is no listener for this
	public void deletedVxF(VxFMetadata vxf) {

		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:vxf.deleted?multipleConsumers=true");
		template.withBody( vxf ).asyncSend();
		
	}

	/**
	 * Asynchronously sends to the routing bus (seda:nsd.deleted?multipleConsumers=true) that a vxf is deleted
	 * @param deployment a {@link ExperimentMetadata}
	 */
	// There is no listener for this
	public void deletedExperiment(ExperimentMetadata nsd) {

		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.deleted?multipleConsumers=true");
		template.withBody( nsd ).asyncSend();
		
	}



	/**
	 * Asynchronously sends to the routing bus (seda:vxf.offboard?multipleConsumers=true) to trigger new VXF offboarding 
	 * @param deployment a {@link VxFOnBoardedDescriptor}
	 */
	public void offBoardVxF(VxFOnBoardedDescriptor u) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:vxf.offboard?multipleConsumers=true");
		template.withBody( u ).asyncSend();		
	}

	/**
	 * Asynchronously sends to the routing bus (seda:nsd.onboard?multipleConsumers=true) to trigger new NSD onboarding 
	 * @param deployment a {@link ExperimentOnBoardDescriptor}
	 */
	public void onBoardNSD(ExperimentOnBoardDescriptor obd) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.onboard?multipleConsumers=true");
		template.withBody( obd ).asyncSend();		
	}

	/**
	 * Asynchronously sends to the routing bus (seda:nsd.offboard?multipleConsumers=true) to trigger new NSD offboarding 
	 * @param deployment a {@link ExperimentOnBoardDescriptor}
	 */
	public void offBoardNSD(ExperimentOnBoardDescriptor u) {
		FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:nsd.offboard?multipleConsumers=true");
		template.withBody( u ).asyncSend();		
	}

	/**
	 * @param vfimg
	 */
	public void newVFImageAdded(VFImage vfimg) {
		// TODO Auto-generated method stub
		
	}

	public void aVFImageUpdated(VFImage vfimg) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Asynchronously sends to the routing bus (seda:nsd.offboard?multipleConsumers=true) to trigger new NSD offboarding 
	 * @param deployment a {@link ExperimentOnBoardDescriptor}
	 */
	public void propertiesUpdate(String props) {
		
		if ( contxt != null ) {
			FluentProducerTemplate template = contxt.createFluentProducerTemplate().to("seda:properties.update?multipleConsumers=true");
			template.withBody( props ).asyncSend();
			
		}		
	}


}
