
package com.ecom.util;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {
	
	@Autowired
	private  JavaMailSender mailSender;
	
	@Autowired
	private UserService userService;

	public  Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException {
		
	   MimeMessage message =	mailSender.createMimeMessage();
	   MimeMessageHelper helper = new MimeMessageHelper(message);
	   
	   helper.setFrom("shoppingcart00@gmail.com","Shopping cart");
	   helper.setTo(reciepentEmail);
	   
	   String content = "<p>Hello,</p>" 
               + "<p>You have requested to reset your password.</p>"
               + "<p>Click the link below to change your password:</p>"
               + "<p><a href=\"" + url + "\">Change my Password</a></p>";

		
	   helper.setSubject("Password Reset");
	   helper.setText(content,true);
	   mailSender.send(message);
	   
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {
		
	 String siteurl =  request.getRequestURL().toString();
	
	 return siteurl.replace(request.getServletPath(),"");
	}
	
	String msg = null;
	
	public Boolean sendMailForProductOrder(ProductOrder order,String status) throws Exception{
		
		 msg ="<p>Hello [[name]],</p><p>Thank You ! order is [[orderStatus]].</P>"
				   + "<p><b>Product Details :</b></P>"
				   + "<p>Product Name :[[productName]] </p>"
				   + "<p>Category : [[category]]</p>"
				   + "<p>Quantity : [[Quantity]]</p>"
				   + "<p>Price : [[price]]</p>"
				   + "<p>Payment Type : [[paymentType]]</p>";
		
		MimeMessage message =	mailSender.createMimeMessage();
		   MimeMessageHelper helper = new MimeMessageHelper(message);
		   
		   helper.setFrom("kushwahasugam66@gmail.com","Shopping cart");
		   helper.setTo(order.getOrderAddress().getEmail());
		   
		   msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
		   msg = msg.replace("[[orderStatus]]", status);
		   msg = msg.replace("[[productName]]", order.getProduct().getTitle());
		   msg = msg.replace("[[category]]", order.getProduct().getCategory());
		   msg = msg.replace("[[Quantity]]", order.getQuantity().toString());
		   msg = msg.replace("[[price]]", order.getPrice().toString());
		   msg = msg.replace("[[paymentType]]", order.getPaymentType());
			
		   helper.setSubject("Product Order Status");
		   helper.setText(msg,true);
		   mailSender.send(message);
		   
			return true;
	}
	
	public UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		   UserDtls userDtls =	userService.getUserByEmail(email);
		   return userDtls;
	}
}

