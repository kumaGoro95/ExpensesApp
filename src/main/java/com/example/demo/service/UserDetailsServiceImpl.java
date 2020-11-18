package com.example.demo.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.model.SiteUser;
import com.example.demo.repository.SiteUserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private final SiteUserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		SiteUser user =  userRepository.findByUsername(username);
		if(user == null) {
			//ユーザーが見つからなければ、SpringSecurityの以下の例外をthrowする。
			throw new UsernameNotFoundException(username + " not found");
		}
		return createUserDetails(user);
	}
	
	public User createUserDetails(SiteUser user) {
		Set<GrantedAuthority> grantedAuthories = new HashSet<>();
		grantedAuthories.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
		
		return new User(user.getUsername(), user.getPassword(), grantedAuthories);
	}
}
