package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.ObjectUtils;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
public class HomeController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private CartService cartService;
	
	@ModelAttribute
	public void getUserDetails(Principal p,Model m) {
		if(p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user",userDtls);
		   Integer countCart =	cartService.getCountCart(userDtls.getId());
		   m.addAttribute("countCart",countCart);
		}
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys",allActiveCategory);
	}
	
	@GetMapping("/")
	public String index(Model m) {
		
	  List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
			  .filter(c -> !"TVs".equalsIgnoreCase(c.getName())) 
			  .filter(c -> !"AC".equalsIgnoreCase(c.getName())) 
			  .filter(c -> !"FZ".equalsIgnoreCase(c.getName())) 
			  .sorted((c1,c2)->c2.getId().compareTo(c1.getId()))
			  .limit(6).toList();
	  List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
			  .filter(p -> !"TVs".equalsIgnoreCase(p.getCategory()))
			  .filter(p -> !"AC".equalsIgnoreCase(p.getCategory()))
			  .filter(p -> !"FZ".equalsIgnoreCase(p.getCategory()))
			   .sorted((p1,p2)->p2.getId().compareTo(p1.getId()))
			    .limit(18)
			    .toList();
	  
	  List<Product> tvProducts = productService.getProductsByCategory("TVs");
	    m.addAttribute("tvProducts", tvProducts);
	    
	    List<Product> ACProducts = productService.getProductsByCategory("AC");
	    m.addAttribute("ACProducts", ACProducts);
	    
	    List<Product> FZProducts = productService.getProductsByCategory("FZ");
	    m.addAttribute("FZProducts", FZProducts);

	  m.addAttribute("category",allActiveCategory);
	  m.addAttribute("products",allActiveProducts);
	  
	 
	  
		return "index";
	}
	
	

	     
	@GetMapping("/signin")
	public String login() {
		return"login";
	}
	
	@GetMapping("/Register")
	public String register() {
		return"register";
	}
	
	@GetMapping("/products")
	public String products(Model m, @RequestParam(value = "category",defaultValue = "" ) String category) {
	  // System.out.println("category= "+category);
	   List<Category>categories =	categoryService.getAllActiveCategory();
	   List<Product> products = productService.getAllActiveProducts(category);
	   m.addAttribute("categories", categories);
	   m.addAttribute("products", products);
	   m.addAttribute("paramValue",category);
	  
		return"product";
	}
	
	@GetMapping("/product/{id}")
	public String vproducts(@PathVariable int id, Model m) {
	   Product productById=	productService.getProductById(id);
		m.addAttribute("product",productById);

		 if (productById != null && productById.getCategory() != null) {
       List<Product> sameCategoryProducts = productService.getAllActiveProducts(productById.getCategory());
       // Remove current product from the list
       sameCategoryProducts.removeIf(p -> p.getId() == productById.getId());
       m.addAttribute("sameCategoryProducts", sameCategoryProducts);
   }
		
		return"view_product";
	}

	
	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
	   String imageName = file.isEmpty() ? "default.jpg": file.getOriginalFilename();
	   user.setProfileImage(imageName);
		 UserDtls saveUser = userService.saveUser(user);
		
		 if(!ObjectUtils.isEmpty(saveUser)) {
			 if(!file.isEmpty()) {
				 File saveFile = new ClassPathResource("static/img").getFile();
		           Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"profile_img"+File.separator+file.getOriginalFilename());
		          // System.out.println(path);
		           Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
		        	
			 }
			session.setAttribute("succMsg", "Saved Successfully");
		 }else {
			 session.setAttribute("errorMsg", "Not saved");
		 }
		 
		return "redirect:/signin";
	}
	
	
	// Forget password logic
	
	@GetMapping("/forget-password")
	public String showForgetPassword() {
		
		return "forget_password.html";
	}
	
	
	
	@PostMapping("/forget-password")
	public String proccessForgetPassword(@RequestParam String email, HttpSession session, HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
		
		UserDtls userByEmail = userService.getUserByEmail(email);
		
		if(ObjectUtils.isEmpty(userByEmail)) {
			session.setAttribute("errorMsg","Invalid Email");
		}else {
			
			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email,resetToken);
			
		//	Genrate URL : http://
			
			 String url= CommonUtil.generateUrl(request)+"/reset-password?token=" + resetToken;
			
			Boolean sendMail = commonUtil.sendMail(url,email);
			
			if(sendMail) {
				session.setAttribute("succMsg", "Please Check Your email, Password reset link sent");
			}else {
				session.setAttribute("errorMsg", "Something wrong on server ! Email not sent");
			}
		}
		
		return "redirect:/forget-password";
	}
	
	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, HttpSession session, Model m ) {
		
		UserDtls userByToken = userService.getUserByToken(token);
		
		if(userByToken == null) {
			m.addAttribute("msg","Your link is expired");
			return "error";
		}
		m.addAttribute("token",token);
		
		return "reset_password";
	}
	
	
	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token,@RequestParam String password, HttpSession session, Model m) {
		
		UserDtls userByToken = userService.getUserByToken(token);
		
		if(userByToken == null) {
			m.addAttribute("errorMsg","Your link is expired");
			return "error";
		}else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			session.setAttribute("succMsg", "Password change successfully");
			m.addAttribute("msg","password change successfully");
			return "error";
		}
	}
	
	@GetMapping("/search-product")
	public String searchProduct(@RequestParam String ch,Model m) {
		
		List<Product> searchProduct = productService.searchProduct(ch);
		m.addAttribute("products",searchProduct);
		
		 List<Category>categories =	categoryService.getAllActiveCategory();
		
		   m.addAttribute("categories", categories);
		   
		
		return"product";
	}
}

