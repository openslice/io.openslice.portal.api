package portal.api;



import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.openslice.model.ExperimentMetadata;
import io.openslice.model.UserSession;
import io.openslice.model.VxFMetadata;
import portal.api.mano.MANOController;
import portal.api.service.CategoryService;
import portal.api.service.DeploymentDescriptorService;
import portal.api.service.InfrastructureService;
import portal.api.service.ManoPlatformService;
import portal.api.service.ManoProviderService;
import portal.api.service.NSDOBDService;
import portal.api.service.NSDService;
import portal.api.service.PortalPropertiesService;
import portal.api.service.ProductService;
import portal.api.service.UsersService;
import portal.api.service.VFImageService;
import portal.api.service.VxFOBDService;
import portal.api.service.VxFService;


@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK , classes = PortalApplication.class)
//@AutoConfigureTestDatabase
@AutoConfigureMockMvc 
@ActiveProfiles("testing")
@TestPropertySource(properties = {"spring.cloud.consul.config.enabled=false",
		"spring.cloud.bus.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"spring.cloud.consul.enabled=false"})



public class InMemoryDBIntegrationTest {


	private static final transient Log logger = LogFactory.getLog( InMemoryDBIntegrationTest.class.getName());
	
    @Autowired
    private MockMvc mvc;

	@Autowired
	PortalPropertiesService propsService;

	@Autowired
	UsersService usersService;

	@Autowired
	ManoProviderService manoProviderService;

	@Autowired
	VxFService vxfService;

	@Autowired
	NSDService nsdService;

	@Autowired
	VxFOBDService vxfOBDService;
	@Autowired
	NSDOBDService nsdOBDService;

	@Autowired
	VFImageService vfImageService;

	@Autowired
	ProductService productService;

	@Autowired
	ManoPlatformService manoPlatformService;

	@Autowired
	MANOController aMANOController;

	@Autowired
	CategoryService categoryService;

	@Autowired
	InfrastructureService infrastructureService;

	@Autowired
	DeploymentDescriptorService deploymentDescriptorService;
	
	@Test
	public void whenFindByName_thenReturnVxF() {
	    // given

		VxFMetadata vxf = new VxFMetadata();
		vxf.setName("aTestVxF");
		vxfService.updateProductInfo(vxf);
	 
	    // when
	    VxFMetadata found = vxfService.getVxFByName(vxf.getName());
	 
	    // then
	    assertThat(found.getName()).isEqualTo( vxf.getName() );
	}
	
	@Test
	public void countDefaultProperties() {
		assertThat( propsService.getProperties().size() )
		.isEqualTo( 11 );

		assertThat( usersService.findAll().size() )
		.isEqualTo( 1 );
	}
	
	@Test
	public void loginAdmin() throws Exception {

		/**
		 * no auth session
		 */
		mvc.perform(get("/admin/users")
				.contentType(MediaType.APPLICATION_JSON)				
				)
	    	.andExpect(status().is(401) );
		
		UserSession pu = new UserSession();
		pu.setUsername("admin");
		pu.setPassword("changeme");
		
		/**
		 * auth
		 */
		 HttpSession session = mvc.perform(post("/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content( toJson( pu ) ))
			    .andExpect(status().isOk())
			    .andExpect(content()
			    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    .andExpect(jsonPath("username", is("admin")))
			    .andReturn().getRequest().getSession();
		 
		 

		mvc.perform(get("/categories")
				.contentType(MediaType.APPLICATION_JSON))
	    	.andExpect(status().isOk())
	    	.andExpect(content()
	    			.contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
	    	.andExpect( jsonPath("$[0].name", is("None")) );
		

		
		/**
		 * with auth session
		 */
		mvc.perform(get("/admin/categories").session( (MockHttpSession) session )
				.contentType(MediaType.APPLICATION_JSON)				
				)
	    	.andExpect(status().isOk())
	    	.andExpect(content()
	    			.contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
	    	.andExpect( jsonPath("$[0].name", is("None")) );
	}
	
	 static byte[] toJson(Object object) throws IOException {
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	        return mapper.writeValueAsBytes(object);
	    }
	 
	 static <T> T toJsonObj(String content, Class<T> valueType)  throws IOException {
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	        return mapper.readValue( content, valueType);
	    }
	 
	 
	@Test
	public void addVxF() throws Exception {
		
		UserSession pu = new UserSession();
		pu.setUsername("admin");
		pu.setPassword("changeme");
		
		/**
		 * auth
		 */
		 HttpSession session = mvc.perform(post("/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content( toJson( pu ) ))
			    .andExpect(status().isOk())
			    .andExpect(content()
			    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    .andExpect(jsonPath("username", is("admin")))
			    .andReturn().getRequest().getSession();
		 			 
		 


		File vxfFile = new File( "src/test/resources/testvxf.txt" );
		InputStream in = new FileInputStream( vxfFile );
		String resvxf = IOUtils.toString(in, "UTF-8");
		logger.info( "resvxf ========> " + resvxf );

		File gz = new File( "src/test/resources/cirros_vnf.tar.gz" );
		InputStream ing = new FileInputStream( gz );
		MockMultipartFile prodFile = new MockMultipartFile("prodFile", "cirros_vnf.tar.gz", "application/x-gzip", IOUtils.toByteArray(ing));
		     
		 
		 mvc.perform(MockMvcRequestBuilders.multipart("/admin/vxfs")
				 .file(prodFile)
				 .param("vxf", resvxf)
				 .session( (MockHttpSession) session ))
	    	.andExpect(status().isOk());
		 
		 assertThat( vxfService.getVxFsByCategory((long) -1) .size() )
			.isEqualTo( 1 );
		 
		 mvc.perform(get("/categories")
					.contentType(MediaType.APPLICATION_JSON))
		    	.andExpect(status().isOk())
		    	.andExpect(content()
		    			.contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
		    	.andExpect( jsonPath("$[0].name", is("None")) )
		    	.andExpect( jsonPath("$[1].name", is("Networking")))
		    	.andExpect( jsonPath("$[1].vxFscount", is( 1 )));

	}
	
	@Test
	public void deleteVxF() throws Exception {
		
		addVxF(); 
		
		UserSession pu = new UserSession();
		pu.setUsername("admin");
		pu.setPassword("changeme");
		
		/**
		 * auth
		 */
		 HttpSession session = mvc.perform(post("/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content( toJson( pu ) ))
			    .andExpect(status().isOk())
			    .andExpect(content()
			    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    .andExpect(jsonPath("username", is("admin")))
			    .andReturn().getRequest().getSession();
		 
		 assertThat( vxfService.getVxFsByCategory((long) -1) .size() )
			.isEqualTo( 1 );

		 mvc.perform(get("/categories")
					.contentType(MediaType.APPLICATION_JSON))
		    	.andExpect(status().isOk())
		    	.andExpect(content()
		    			.contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
		    	.andExpect( jsonPath("$[0].name", is("None")) )
		    	.andExpect( jsonPath("$[1].name", is("Networking")))
		    	.andExpect( jsonPath("$[1].vxFscount", is( 1 )))
		    	.andExpect( jsonPath("$[2].name", is("Service")))
		    	.andExpect( jsonPath("$[2].vxFscount", is( 1 )));
		 
		 String content =  mvc.perform(get("/admin/vxfs")
					.contentType(MediaType.APPLICATION_JSON).session( (MockHttpSession) session ))
		    	.andExpect(status().isOk())
		    	.andExpect(content()
		    			.contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    .andExpect(jsonPath("$[0].name", is( "cirros_vnfd" )))
			    .andReturn().getResponse().getContentAsString();
		 
		 VxFMetadata[] v =  toJsonObj( content, VxFMetadata[].class);
		 
		 mvc.perform(delete("/admin/vxfs/" + v[0].getId())
					 .session( (MockHttpSession) session ))
		    	.andExpect(status().isOk())
		    	.andExpect(content()
		    			.contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
		 
		 assertThat( vxfService.getVxFsByCategory((long) -1) .size() )
			.isEqualTo( 0 );

	}
	
	
	 
		@Test
		public void addNSD() throws Exception {
			
			UserSession pu = new UserSession();
			pu.setUsername("admin");
			pu.setPassword("changeme");
			
			/**
			 * auth
			 */
			 HttpSession session = mvc.perform(post("/sessions")
					.contentType(MediaType.APPLICATION_JSON)
					.content( toJson( pu ) ))
				    .andExpect(status().isOk())
				    .andExpect(content()
				    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				    .andExpect(jsonPath("username", is("admin")))
				    .andReturn().getRequest().getSession();
			 			 
			 


			File nsdFile = new File( "src/test/resources/testnsd.txt" );
			InputStream in = new FileInputStream( nsdFile );
			String resnsd = IOUtils.toString(in, "UTF-8");
			logger.info( "resnsd ========> " + resnsd );

			File gz = new File( "src/test/resources/cirros_2vnf_ns.tar.gz" );
			InputStream ing = new FileInputStream( gz );
			MockMultipartFile prodFile = new MockMultipartFile("prodFile", "cirros_2vnf_ns.tar.gz", "application/x-gzip", IOUtils.toByteArray(ing));
			     
			 
			 mvc.perform(MockMvcRequestBuilders.multipart("/admin/experiments")
					 .file(prodFile)
					 .param("exprm", resnsd)
					 .session( (MockHttpSession) session ))
		    	.andExpect(status().isOk());
			 
			 assertThat( nsdService.getdNSDsByCategory((long) -1) .size() )
				.isEqualTo( 1 );
			 
			 mvc.perform(get("/categories")
						.contentType(MediaType.APPLICATION_JSON))
			    	.andExpect(status().isOk())
			    	.andExpect(content()
			    			.contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    	.andExpect( jsonPath("$[0].name", is("None")) )
			    	.andExpect( jsonPath("$[1].name", is("Networking")))
			    	.andExpect( jsonPath("$[1].appscount", is( 1 )));

		}

		@Test
		public void deleteNSD() throws Exception {
			
			addNSD(); 
			
			UserSession pu = new UserSession();
			pu.setUsername("admin");
			pu.setPassword("changeme");
			
			/**
			 * auth
			 */
			 HttpSession session = mvc.perform(post("/sessions")
					.contentType(MediaType.APPLICATION_JSON)
					.content( toJson( pu ) ))
				    .andExpect(status().isOk())
				    .andExpect(content()
				    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				    .andExpect(jsonPath("username", is("admin")))
				    .andReturn().getRequest().getSession();
			 
			 assertThat( nsdService.getdNSDsByCategory((long) -1) .size() )
				.isEqualTo( 1 );

			 mvc.perform(get("/categories")
						.contentType(MediaType.APPLICATION_JSON))
			    	.andExpect(status().isOk())
			    	.andExpect(content()
			    			.contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    	.andExpect( jsonPath("$[0].name", is("None")) )
			    	.andExpect( jsonPath("$[1].name", is("Networking")))
			    	.andExpect( jsonPath("$[1].appscount", is( 1 )))
			    	.andExpect( jsonPath("$[2].name", is("Service")))
			    	.andExpect( jsonPath("$[2].appscount", is( 1 )));
			 
			 String content =  mvc.perform(get("/admin/experiments")
						.contentType(MediaType.APPLICATION_JSON).session( (MockHttpSession) session ))
			    	.andExpect(status().isOk())
			    	.andExpect(content()
			    			.contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    	 .andExpect(jsonPath("$[0].name", is( "cirros_2vnf_nsd" )))
				    .andReturn().getResponse().getContentAsString();
			 

			 ExperimentMetadata[] n =  toJsonObj( content, ExperimentMetadata[].class );
			 
			 mvc.perform(delete("/admin/experiments/" + n[0].getId() )
						 .session( (MockHttpSession) session ))
			    	.andExpect(status().isOk())
			    	.andExpect(content()
			    			.contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				   ;
			 
			 assertThat( nsdService.getdNSDsByCategory((long) -1) .size() )
				.isEqualTo( 0 );

		}
}