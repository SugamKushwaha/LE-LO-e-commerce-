package com.ecom.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ecom.service.CommonService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class CommonServiceImpl implements CommonService{

	@Override
	public void removeSessionMessage() {
		
	HttpServletRequest request=	((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest();
	 HttpSession session = request.getSession();
	 session.removeAttribute("succMsg");
	 session.removeAttribute("errorMsg");
	
	}

}
