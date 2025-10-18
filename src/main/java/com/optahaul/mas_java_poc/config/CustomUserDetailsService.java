package com.optahaul.mas_java_poc.config;

import java.util.Collections;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optahaul.mas_java_poc.domain.User;
import com.optahaul.mas_java_poc.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Profile("!openapi")
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		return org.springframework.security.core.userdetails.User.builder().username(user.getUsername())
				.password(user.getPassword())
				.authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
				.disabled(!user.getEnabled()).build();
	}
}
