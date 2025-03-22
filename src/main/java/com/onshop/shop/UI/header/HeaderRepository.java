package com.onshop.shop.UI.header;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.onshop.shop.store.Seller;

@Repository
public interface HeaderRepository extends JpaRepository<Header, Long> {
	 List<Header> findBySeller(Seller seller);
	 
	 Optional<Header> findByHeaderIdAndSeller_SellerId(Long headerId, Long sellerId);

	 
}
