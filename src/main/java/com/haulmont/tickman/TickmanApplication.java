package com.haulmont.tickman;

import io.jmix.core.security.CoreSecurityConfiguration;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;

import javax.sql.DataSource;
import java.util.Collections;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TickmanApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(TickmanApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
	}

	@Bean
	@Primary
	@ConfigurationProperties(prefix="main.datasource")
	DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@EnableWebSecurity
	static class SecurityConfiguration extends CoreSecurityConfiguration {

		@Override
		public UserRepository userRepository() {
			InMemoryUserRepository repository = new InMemoryUserRepository();
			repository.addUser(User.builder()
					.username("admin")
					.password("{noop}admin")
					.authorities(Collections.emptyList())
					.build());
			return repository;
		}
	}
}
