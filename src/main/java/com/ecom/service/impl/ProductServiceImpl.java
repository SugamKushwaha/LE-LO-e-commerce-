package com.ecom.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Product;
import com.ecom.repository.ProductRepository;
import com.ecom.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService{
	
	@Autowired
	private ProductRepository productRepository;

	@Override
	public Product saveProduct(Product product) {
		
		return productRepository.save(product);
	}

	@Override
	public List<Product> getAllProducts() {
		
		return productRepository.findAll();
	}

	@Override
	public Boolean deleteProduct(Integer id) {
	Product product = productRepository.findById(id).orElse(null);
	
	if(!ObjectUtils.isEmpty(product)) {
		productRepository.delete(product);
		return true;
	}
		return false;
	}

	@Override
	public Product getProductById(Integer id) {
	   Product product=	productRepository.findById(id).orElse(null);
		return product;
	}

	@Override
	public Product updateProduct(Product product, MultipartFile image){
		Product dbproduct = getProductById(product.getId());
		String imageName=  image.isEmpty() ? dbproduct.getImage() : image.getOriginalFilename();
		dbproduct.setTitle(product.getTitle());
		dbproduct.setDescription(product.getDescription());
		dbproduct.setCategory(product.getCategory());
		dbproduct.setPrice(product.getPrice());
		dbproduct.setStock(product.getStock());
		dbproduct.setImage(imageName);
		dbproduct.setIsActive(product.getIsActive());
		dbproduct.setDiscount(product.getDiscount());
		
	   Double discount=	product.getPrice()*(product.getDiscount()/100.0);
	   Double discountPrice= product.getPrice()-discount;
	   
	   dbproduct.setDiscountPrice(discountPrice);
		
		Product updateProduct = productRepository.save(dbproduct);
		
		if(!ObjectUtils.isEmpty(updateProduct)) {
			if(!image.isEmpty()) {
				try {
					File saveFile = new ClassPathResource("static/img").getFile();
			           Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"product_img"+File.separator+image.getOriginalFilename());
			          // System.out.println(path);
			           Files.copy(image.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
			        	
					  }catch(Exception e) {
			    	 e.printStackTrace();
			     }
			}
			return product;
		}
		
		return null;
	}

	@Override
	public List<Product> getAllActiveProducts(String category) {
		List<Product> products= null;
		if(ObjectUtils.isEmpty(category)) {
		   products = productRepository.findByIsActiveTrue();
		}else {
			products = productRepository.findByCategory(category);
		}
		
		return products;
	}

	@Override
	public List<Product> searchProduct(String ch) {
		
	  return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
		
	}


	@Override
	public List<Product> getProductsByCategory(String category) {
		
		
		return productRepository.findByCategory(category);
	}
	    
}
