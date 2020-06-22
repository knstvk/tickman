package com.haulmont.tickman;

import io.jmix.core.security.UserRepository;
import io.jmix.core.security.impl.CoreUser;
import io.jmix.core.security.impl.InMemoryUserRepository;
import io.jmix.security.role.assignment.RoleAssignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TickmanApplication implements ApplicationRunner {

	@Autowired
	private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(TickmanApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		initUsers();
	}

	@Bean
	@Primary
	@ConfigurationProperties(prefix="main.datasource")
	DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	private void initUsers() {
		CoreUser admin = new CoreUser("admin", "{noop}admin", "Admin");
		if (userRepository instanceof InMemoryUserRepository) {
			((InMemoryUserRepository) userRepository).createUser(admin);
		}
	}
}
