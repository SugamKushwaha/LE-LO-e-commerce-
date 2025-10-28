package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.ecom.ShoppingCartApplication;
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
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

    AdminController(ShoppingCartApplication shoppingCartApplication) {
    }
	
	@GetMapping("/")
	public String index() {
		return "Admin/index";
	}
	
	
	@GetMapping("/loadProduct")
	public String addProduct(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return "admin/add_product";
	}
	
	@GetMapping("/loadCategory")
	public String addCategory(Model m) {
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/add_category";
	}
	
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category,
	                           @RequestParam("file") MultipartFile file,
	                           HttpSession session)throws IOException{

	    String imgName = file != null ? file.getOriginalFilename() : "default.jpg";
	    category.setImgName(imgName);

	    Boolean existCategory = categoryService.existCategory(category.getName());
	    
	    if (existCategory) {
	       session.setAttribute("errorMsg", "Already Exsit");
	    } else {
	        Category saveCategory = categoryService.saveCategory(category);
	        if (ObjectUtils.isEmpty(saveCategory)) {
	            session.setAttribute("errorMsg", "Not saved");
	        } else {
//	        	File saveFile = new ClassPathResource("static/img").getFile();
//	           Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator+file.getOriginalFilename());
//	          // System.out.println(path);
//	           Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
	        	String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/img/category_img";
	        	File uploadFolder = new File(uploadDir);
	        	if (!uploadFolder.exists()) {
	        	    uploadFolder.mkdirs();
	        	}

	        	Path path = Paths.get(uploadDir + File.separator + file.getOriginalFilename());
	        	Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

	        	session.setAttribute("succMsg", "Saved successfully");
	        }
	    }

	    return "redirect:/admin/loadCategory";
	}

	
	
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		
		Boolean deleteCategory= categoryService.deleteCategory(id);
		if(deleteCategory) {
			session.setAttribute("succMsg", "category delete success");
		}else {
			session.setAttribute("errorMsg", "Somethiing wrong on server");
		}
		
		return"redirect:/admin/loadCategory";
		
	}

	
	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return"/admin/edit_category";
	}
	
	
	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file")MultipartFile file, HttpSession session) throws IOException {
		
		Category Oldcategory  =categoryService.getCategoryById(category.getId());
		String imgName = file.isEmpty() ? Oldcategory.getImgName() : file.getOriginalFilename();
		
		if(!ObjectUtils.isEmpty(category)) {
			Oldcategory.setName(category.getName());
			Oldcategory.setIsActive(category.getIsActive());
			Oldcategory.setImgName(imgName);
		}
		
		Category updateCategory = categoryService.saveCategory(Oldcategory);
		
		if(!ObjectUtils.isEmpty(updateCategory)) {
			
			if(file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();
		           Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator+file.getOriginalFilename());
		        //   System.out.println(path);
		           Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				
				}
			
			session.setAttribute("succMsg", "Category update succes");
		}else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		
		return"redirect:/admin/loadEditCategory/"+category.getId();
	}
	
	
//	Category controller part
	
	
	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product,@RequestParam("file") MultipartFile image,HttpSession session) throws IOException{
		
	   String imageName =	image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
	   product.setImage(imageName);
	   product.setDiscount(0);
	   product.setDiscountPrice(product.getPrice());
	  
	   Product saveProduct = productService.saveProduct(product);
		
		if(!ObjectUtils.isEmpty(saveProduct)) {
			
			File saveFile = new ClassPathResource("static/img").getFile();
	           Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"product_img"+File.separator+image.getOriginalFilename());
	          // System.out.println(path);
	           Files.copy(image.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
	        	
	        session.setAttribute("succMsg", "Product Saved");
		}else {
			 session.setAttribute("errorMsg", "Something Went Wrong");
		}
		
		return"redirect:/admin/loadProduct";
	}

	
	@GetMapping("/products")
	public String loadViewProduct(Model m,@RequestParam(defaultValue = "") String ch) {
		List<Product> products = null;
		if(ch!=null && ch.length()>0) {
			products = productService.searchProduct(ch);
		}else {
			products =	productService.getAllProducts();
		}
		m.addAttribute("products",products);
		return"admin/products";
	}
	
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id,HttpSession session) {
	   Boolean deleteProduct =	productService.deleteProduct(id);
	   if(deleteProduct) {
		   session.setAttribute("succMsg","Product delete succes");
	   }else {
		   session.setAttribute("errorMsg", "something wrong on server");
	   }
		return"redirect:/admin/products";
		
	}
	
	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id,Model m) {
		m.addAttribute("product",productService.getProductById(id));
		m.addAttribute("categories",categoryService.getAllCategory());
		return"admin/edit_product";
	}
	
	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product,@RequestParam("file") MultipartFile image, HttpSession session, Model m) {
	
		if(product.getDiscount() <0 || product.getDiscount()>100){
			session.setAttribute("errorMsg", "Invalid Discount");
		}else {
			
	   Product updateProduct = productService.updateProduct(product, image);
		if(!ObjectUtils.isEmpty(updateProduct)) {
			session.setAttribute("succMsg", "Product Updated");
		}else {
			session.setAttribute("errorMsg", "Something went wrong");
		 }
		}
		return"redirect:/admin/editProduct/" + product.getId();
	}
	
	@GetMapping("/users")
	public String getAllUsers(Model m,@RequestParam Integer type) {
		List<UserDtls> users = null;
		if(type==1) {
			users = userService.getUsers("ROLE_USER");
		}else {
			users = userService.getUsers("ROLE_ADMIN");
		}
	   
		m.addAttribute("userType",type);
		m.addAttribute("users",users);
		return "/admin/users";
	}
	
	
	@GetMapping("/update-status")
	public String updateUserAccountStatus(@RequestParam Boolean status,@RequestParam Integer id,@RequestParam Integer type, HttpSession session) {
		
	   Boolean f =	userService.updateAccountStatus(id,status);
		if(f) {
			session.setAttribute("succMsg", "Account Status Updated");
		}else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
	   
		return "redirect:/admin/users?type="+type;
	}
	
	@GetMapping("/orders")
	public String getAllOrders(Model m) {
		
	   List<ProductOrder> allOrder = orderService.getAllOrders();
	   m.addAttribute("orders",allOrder);
	   m.addAttribute("srch",false);
		
		return "/admin/order";
	}
	
	
	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {
		
		OrderStatus[] values =  OrderStatus.values();
		String status = null;
		
		for(OrderStatus orderSt:values) {
			if(orderSt.getId().equals(st)) {
				status= orderSt.getName();
			}
		}
		
		ProductOrder  updateOrder = orderService.updateOrderStatus(id, status);
		
		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status updated");
		}else {
			session.setAttribute("errorMsg", "Status not updated");
		}
		
		return"redirect:/admin/orders";
	}
	
	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId,Model m,HttpSession session) {
		
	   ProductOrder order =	orderService.getOrdersByOrderId(orderId.trim());
	   
	   if(ObjectUtils.isEmpty(order)) {
		   session.setAttribute("errorMsg", "Incorrect orderId");
		   m.addAttribute("orderDtls",null);
	   }else {
		   m.addAttribute("orderDtls",order);
	   }
	   m.addAttribute("srch",true);
		
		return"/admin/order";
	}
	
	
	@GetMapping("/add-admin")
	public String addAdmin() {
		return"/admin/add_admin";
	}
	
	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
	   String imageName = file.isEmpty() ? "default.jpg": file.getOriginalFilename();
	   user.setProfileImage(imageName);
		 UserDtls saveUser = userService.saveAdmin(user);
		
		 if(!ObjectUtils.isEmpty(saveUser)) {
			 if(!file.isEmpty()) {
				 File saveFile = new ClassPathResource("static/img").getFile();
		           Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"profile_img"+File.separator+file.getOriginalFilename());
		          // System.out.println(path);
		           Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
		        	
			 }
			session.setAttribute("succMsg", "Register Successfully");
		 }else {
			 session.setAttribute("errorMsg", "Not saved");
		 }
		 
		return "redirect:/admin/add-admin";
	}
	
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user,@RequestParam MultipartFile img, HttpSession session ) {
		
	   UserDtls updateUserProfile =	userService.updateUserProfile(user, img);
		if(!ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("succMsg", "Profile Updated");
		}
		
		return"redirect:/admin/profile";
	}
	
	@GetMapping("/profile")
	public String profile() {
		return"/admin/profile";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword,HttpSession session, Principal p) {
		
	   UserDtls loggedInUserDtls = commonUtil.getLoggedInUserDetails(p);
			   
	  Boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDtls.getPassword());
	   if(matches) {
		   String enCodePassword = passwordEncoder.encode(newPassword);
		   loggedInUserDtls.setPassword(enCodePassword);
		   UserDtls updateUser =   userService.updateUser(loggedInUserDtls);
		   
		   if(ObjectUtils.isEmpty(updateUser)) {
			   session.setAttribute("errorMsg", "Password not updated !! Error in server");
		   }else {
			   session.setAttribute("succMsg", "Password updated");
		   }
	   }else {
		   session.setAttribute("errorMsg", "Current Password is incorrect");
	   }
		
		return"redirect:/admin/profile";
	}
}
