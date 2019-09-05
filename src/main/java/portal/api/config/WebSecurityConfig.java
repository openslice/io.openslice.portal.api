package portal.api.config;


import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import io.openslice.model.UserRoleType;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    

    @Autowired
    private DataSource dataSource;


    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;


    @Autowired
    public void globalUserDetails(final AuthenticationManagerBuilder auth) throws Exception {
        // @formatter:off
    	auth.jdbcAuthentication()
    	.dataSource(dataSource)
    	.usersByUsernameQuery("select username, password, active from portal_user where username = ?")
    	.authoritiesByUsernameQuery( "select username, role_type from portal_user INNER JOIN portal_user_roles ON portal_user.id=portal_user_roles.portal_user_id where username = ?" )
    	//.rolePrefix("ROLE_")
    	//.authoritiesByUsernameQuery("select username, authority " + "from authorities where username=?")
        .passwordEncoder(new BCryptPasswordEncoder());
    	/**
    	 * the given roles here are ROLE_0 for UserRoleType.MENTOR.ordinal( 
    	 * or ROLE_4 for  UserRoleType.MENTOR.ordinal()
    	 * etc.
    	 * These are used in Controllers at @Secured({ "ROLE_0" }) annotations
    	 */
    	
    	 
    	
    	
//	auth.inMemoryAuthentication()
//	  .withUser("john").password(passwordEncoder.encode("123")).roles("USER").and()
//	  .withUser("tom").password(passwordEncoder.encode("111")).roles("ADMIN").and()
//	  .withUser("user1").password(passwordEncoder.encode("pass")).roles("USER").and()
//	  .withUser("admin").password(passwordEncoder.encode("changeme")).roles("ADMIN");
    }// @formatter:on

    @Override
    @Bean("authenticationManager")
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    //see also https://www.baeldung.com/securing-a-restful-web-service-with-spring-security
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // @formatter:off
		http.authorizeRequests()
		.antMatchers("/sessions/**").permitAll()
		.antMatchers("/register/**").permitAll()
		//.antMatchers("/sessions/logout").permitAll()
		.antMatchers("/categories/**").permitAll()
		.antMatchers("/experiments/**").permitAll()
		.antMatchers("/vxfs/**").permitAll()
	    //.antMatchers("/repo/admin/**").hasRole("ADMIN")
		.antMatchers("/login").permitAll()
		.antMatchers("/images/**").permitAll()
		.antMatchers("/packages/**").permitAll()
		.antMatchers("/testweb/**").permitAll()		
		.antMatchers("/oauth/token/revokeById/**").permitAll()
		.antMatchers("/tokens/**").permitAll()
		.antMatchers("/actuator/**").permitAll()
		.anyRequest().authenticated()
		//.and().formLogin().permitAll()
		.and().csrf().disable()
		.cors().and().csrf().disable()
		.exceptionHandling()
	    .authenticationEntryPoint(restAuthenticationEntryPoint)
	    .and()
		.logout();
		// @formatter:on
    }
    

	 @Bean
	    CorsConfigurationSource corsConfigurationSource() {
	        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	        CorsConfiguration corsConfiguration = new CorsConfiguration();
	        corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
	        corsConfiguration.setAllowedMethods(Arrays.asList("*"));
	        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
	        corsConfiguration.setAllowCredentials(true);
	        corsConfiguration.setMaxAge(1800L);
	        source.registerCorsConfiguration("/**", corsConfiguration); // restrict path here
	        return source;
	 }

}