package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; 


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	//UserDetailsServiceのDIコンテナ
	private final UserDetailsService userDetailsService;
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		//bcrypt・・・パスワード暗号化用
		return new BCryptPasswordEncoder();
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception{
		//セキュリティ設定を無視するパスを指定
		//通常、cssやjs、imgなどの静的リソースを指定する
		web.ignoring().antMatchers("/css/**", "/webjars/**", "/js/**");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http //httpリクエストの設定
		  //認証リクエストの設定
		  .authorizeRequests()
		    //「/login」「/register」をアクセス可能にする
		    .antMatchers("/login", "/register").permitAll()
		    //認証の必要があるように設定
		    .anyRequest().authenticated()
		    .and()
		  //フォームベース認証の設定
		  .formLogin()
		    //ログイン時のURLを指定
		    .loginPage("/login")
		    //認証後にリダイレクトする場所を指定
		    .defaultSuccessUrl("/")
		    .and()
		  //ログアウトの設定
		  .logout()
		    //ログアウト時のURLを指定
		    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		    .and()
		  //Remember-Meの認証を許可
		  .rememberMe();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		auth  //ユーザの設定
		  //userDetailsServiceで、DBからユーザーを参照できるようにする
		  .userDetailsService(userDetailsService)    
		  .passwordEncoder(passwordEncoder());
	}
}
