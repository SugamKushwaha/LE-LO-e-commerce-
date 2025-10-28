package com.ecom.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailuerHandlerImpl extends SimpleUrlAuthenticationFailureHandler{
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userService;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		String email=request.getParameter("username");
		
		UserDtls userDtls = userRepository.findByEmail(email);
		
		if(userDtls !=null) {
		
		if(userDtls.getIsEnable()) {
			if(userDtls.getAccountNonLocked()) {
				if(userDtls.getFailedAttempt()< AppConstant.ATTEMPT_TIME) {
					
					userService.increaseFailedAttempt(userDtls);
					
				}else {
					userService.userAccountLock(userDtls);
					exception = new LockedException("Your Account is Locked ! Faild Attemt");
				}
				
			}else {
				if(userService.unlockAccountTimeExpired(userDtls)) {
					exception = new LockedException("Your Account is unLocked !! Please Try Login.."); 
				}else {
				exception = new LockedException("Your account is Locked !! Please Try After sometimes");
				}
			}
			
		}else {
			exception = new LockedException("Your account is inActive");
		}
	  } else {
			exception = new LockedException("Email & Password is invalid");
		}
		
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}

	
}
