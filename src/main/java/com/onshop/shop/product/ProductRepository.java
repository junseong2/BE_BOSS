package com.onshop.shop.product;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.seller.Seller;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	
	/** 구매자 */
    // 특정 카테고리의 상품 조회
    List<Product> findByCategoryId(Long categoryId);
    Product findBySeller_SellerIdAndProductId(Long sellerId, Long productId);
    
    List<Product> findByCategoryIdIn(List<Long> categoryIds, Pageable pageable);

    //sellerId를 엔티티 기준으로 수정 (seller.sellerId → seller.id)
    @Query("SELECT p FROM Product p WHERE p.seller.sellerId = :sellerId")
    List<Product> findBySellerSellerId(@Param("sellerId") Long sellerId);

    // 페이지네이션 적용
    //Page<Product> findBySeller_SellerId(Long sellerId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.seller.sellerId = :sellerId")
    Page<Product> findBySellerSellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    // 상품 이름 또는 카테고리명으로 검색
    @Query("SELECT p FROM Product p JOIN p.category c WHERE p.name LIKE %:query% OR c.name LIKE %:query%")
    List<Product> searchByNameOrCategory(@Param("query") String query);
    
	List<Product> findAllByOrderByDailySalesDesc(); // dailySales 기준으로 모든 상품 정렬

	List<Product> findAllByOrderByWeeklySalesDesc(); // weeklySales 기준으로 모든 상품 정렬

	List<Product> findAllByOrderByMonthlySalesDesc(); // monthlySales 기준으로 모든 상품 정렬

	List<Product> findAllByOrderByOverallSalesDesc(); // overallSales 기준으로 모든 상품 정렬
	// List<Product> findBySeller_SellerIdOrderByWeeklySalesDesc(Long sellerId);
	// List<Product> findBySeller_SellerIdOrderByMonthlySalesDesc(Long sellerId);
	// List<Product> findBySeller_SellerIdOrderByRealtimeSalesDesc(Long sellerId);

	List<Product> findBySeller_SellerIdOrderByDailySalesDesc(Long sellerId);

    
    
    /** 판매자 */
    // 판매자(점주)의 상품 조회 (Native Query)
    @Query(value = """
        SELECT p.product_id AS productId, p.name AS name, p.price AS price, 
               c.category_name AS categoryName, p.description AS description, i.stock AS stock,
               i.min_stock AS minStock, p.expiry_date AS expiryDate, p.discount_rate AS discountRate, p.origin_price AS originPrice,
               p.g_image AS gImage
        FROM product p
        LEFT JOIN category c ON c.category_id = p.category_id
        LEFT JOIN inventory i ON i.product_id = p.product_id
        WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
        ORDER BY p.product_id DESC
    """, nativeQuery = true)
    Page<SellerProductsDTO> findBySellerSellerIdAndSearch(@Param("sellerId") Long sellerId, @Param("search") String search, Pageable pageable);

    // 점주 전용 상품 조회 (단일)
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.id = :productId")
    Product findBySellerIdAndProductId(@Param("sellerId") Long sellerId, @Param("productId") Long productId);


    // 상품 삭제(다중) - DELETE 쿼리로 변경
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Product p WHERE p.id IN (:productIds) AND p.seller = :seller")
    int deleteAllByIdInBatchAndSeller(@Param("productIds") List<Long> productIds, @Param("seller") Seller seller);


	List<Product> findBySeller_SellerIdOrderByWeeklySalesDesc(Long sellerId);


	List<Product> findBySeller_SellerIdOrderByMonthlySalesDesc(Long sellerId);
    
    // 상품 삭제(다중) - DELETE 쿼리로 변경
    @Modifying
    @Transactional
    @Query("DELETE FROM Product p WHERE p.id IN (:ids)")
    int deleteProductsByIds(@Param("ids") List<Long> productIds);

    // 판매자가 등록한 상품 존재 유무 확인
    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId")
    int existsBySellerId(@Param("sellerId") Long sellerId);
    
	// 판매자가 등록한 상품 개수
	@Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId AND p.name LIKE %:name%")
	Long countBySellerSellerIdAndName(@Param("sellerId") Long sellerId, @Param("name") String name);


	// 가격 낮은 순
	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.price ASC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerAndSearchOrderByPriceAsc(@Param("sellerId") Long sellerId,
			@Param("search") String search, Pageable pageable);

	// 가격 높은 순
	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.price DESC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerAndSearchOrderByPriceDesc(@Param("sellerId") Long sellerId,
			@Param("search") String search, Pageable pageable);

	// 등록일 최신순 (created_register는 테이블 컬럼명)
	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.created_register DESC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerAndSearchOrderByCreatedDateDesc(@Param("sellerId") Long sellerId,
			@Param("search") String search, Pageable pageable);

	
	// 판매량 높은 순
	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.overall_sales DESC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerAndSearchOrderBySalesDesc(@Param("sellerId") Long sellerId,
			@Param("search") String search, Pageable pageable);

	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.created_register DESC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerSellerIdAndSearchOrderByCreatedRegisterDesc(@Param("sellerId") Long sellerId,
			@Param("search") String search, Pageable pageable);

	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.overall_sales DESC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerSellerIdAndSearchOrderByOverallSalesDesc(@Param("sellerId") Long sellerId,
			@Param("search") String search, Pageable pageable);

	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.price DESC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerSellerIdAndSearchOrderByPriceDesc(@Param("sellerId") Long sellerId,
			@Param("search") String search, Pageable pageable);

	// 가격 낮은 순 정렬
	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.price ASC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerSellerIdAndSearchOrderByPriceAsc(@Param("sellerId") Long sellerId,
			@Param("search") String search, Pageable pageable);

	@Query("SELECT DISTINCT p.category.id FROM Product p WHERE p.seller.id = :sellerId")
	List<Long> findDistinctCategoryIdsBySellerId(@Param("sellerId") Long sellerId);

	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId
			      AND p.category_id = :categoryId
			      AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.price ASC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerAndCategoryOrderByPriceAsc(@Param("sellerId") Long sellerId,
			@Param("categoryId") Long categoryId, @Param("search") String search, Pageable pageable);

	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId
			      AND p.category_id = :categoryId
			      AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.price DESC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerAndCategoryOrderByPriceDesc(@Param("sellerId") Long sellerId,
			@Param("categoryId") Long categoryId, @Param("search") String search, Pageable pageable);

	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId
			      AND p.category_id = :categoryId
			      AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.created_register DESC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerAndCategoryOrderByCreatedRegisterDesc(@Param("sellerId") Long sellerId,
			@Param("categoryId") Long categoryId, @Param("search") String search, Pageable pageable);

	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId
			      AND p.category_id = :categoryId
			      AND p.name LIKE CONCAT('%', :search, '%')
			    ORDER BY p.overall_sales DESC
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerAndCategoryOrderByOverallSalesDesc(@Param("sellerId") Long sellerId,
			@Param("categoryId") Long categoryId, @Param("search") String search, Pageable pageable);

	@Query(value = """
			    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
			           c.category_name AS categoryName, p.description AS description, i.stock AS stock
			    FROM product p
			    LEFT JOIN category c ON c.category_id = p.category_id
			    LEFT JOIN inventory i ON i.product_id = p.product_id
			    WHERE p.seller_id = :sellerId
			      AND p.category_id = :categoryId
			      AND p.name LIKE CONCAT('%', :search, '%')
			""", nativeQuery = true)
	Page<SellerProductsDTO> findBySellerAndCategory(@Param("sellerId") Long sellerId,
			@Param("categoryId") Long categoryId, @Param("search") String search, Pageable pageable);

    @Query("SELECT p.productId FROM Product p")
    Page<Long> findRandomProductIds(Pageable pageable);

    
    
    @Query(value = """
    	    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
    	           c.category_name AS categoryName, p.description AS description, i.stock AS stock, p.g_image AS gImage
    	    FROM product p
    	    LEFT JOIN category c ON c.category_id = p.category_id
    	    LEFT JOIN inventory i ON i.product_id = p.product_id
    	    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
    	    ORDER BY p.price ASC
    	""", nativeQuery = true)
    	Page<SellerProductsListDTO> findSellerProductsByPriceAsc(@Param("sellerId") Long sellerId,
    	        @Param("search") String search, Pageable pageable);

    @Query(value = """
    	    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
    	           c.category_name AS categoryName, p.description AS description, i.stock AS stock, p.g_image AS gImage
    	    FROM product p
    	    LEFT JOIN category c ON c.category_id = p.category_id
    	    LEFT JOIN inventory i ON i.product_id = p.product_id
    	    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
    	    ORDER BY p.price DESC
    	""", nativeQuery = true)
    	Page<SellerProductsListDTO> findSellerProductsByPriceDesc(@Param("sellerId") Long sellerId,
    	        @Param("search") String search, Pageable pageable);
    @Query(value = """
    	    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
    	           c.category_name AS categoryName, p.description AS description, i.stock AS stock, p.g_image AS gImage
    	    FROM product p
    	    LEFT JOIN category c ON c.category_id = p.category_id
    	    LEFT JOIN inventory i ON i.product_id = p.product_id
    	    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
    	    ORDER BY p.created_register DESC
    	""", nativeQuery = true)
    	Page<SellerProductsListDTO> findSellerProductsByCreatedDateDesc(@Param("sellerId") Long sellerId,
    	        @Param("search") String search, Pageable pageable);
    @Query(value = """
    	    SELECT p.product_id AS productId, p.name AS name, p.price AS price,
    	           c.category_name AS categoryName, p.description AS description, i.stock AS stock, p.g_image AS gImage
    	    FROM product p
    	    LEFT JOIN category c ON c.category_id = p.category_id
    	    LEFT JOIN inventory i ON i.product_id = p.product_id
    	    WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
    	    ORDER BY p.overall_sales DESC
    	""", nativeQuery = true)
    	Page<SellerProductsListDTO> findSellerProductsBySalesDesc(@Param("sellerId") Long sellerId,
    	        @Param("search") String search, Pageable pageable);

    // 가격 내림차순 정렬 (Category 포함)
    @Query(value = """
        SELECT p.product_id AS productId, p.name AS name, p.price AS price,
               c.category_name AS categoryName, p.description AS description, i.stock AS stock, p.g_image AS gImage
        FROM product p
        LEFT JOIN category c ON c.category_id = p.category_id
        LEFT JOIN inventory i ON i.product_id = p.product_id
        WHERE p.seller_id = :sellerId AND p.category_id = :categoryId AND p.name LIKE CONCAT('%', :search, '%')
        ORDER BY p.price DESC
    """, nativeQuery = true)
    Page<SellerProductsListDTO> findSellerProductsByCategoryAndPriceDesc(@Param("sellerId") Long sellerId,
                                                                          @Param("categoryId") Long categoryId,
                                                                          @Param("search") String search,
                                                                          Pageable pageable);

    // 가격 오름차순 정렬 (Category 포함)
    @Query(value = """
        SELECT p.product_id AS productId, p.name AS name, p.price AS price,
               c.category_name AS categoryName, p.description AS description, i.stock AS stock, p.g_image AS gImage
        FROM product p
        LEFT JOIN category c ON c.category_id = p.category_id
        LEFT JOIN inventory i ON i.product_id = p.product_id
        WHERE p.seller_id = :sellerId AND p.category_id = :categoryId AND p.name LIKE CONCAT('%', :search, '%')
        ORDER BY p.price ASC
    """, nativeQuery = true)
    Page<SellerProductsListDTO> findSellerProductsByCategoryAndPriceAsc(@Param("sellerId") Long sellerId,
                                                                          @Param("categoryId") Long categoryId,
                                                                          @Param("search") String search,
                                                                          Pageable pageable);

    // 최신 등록일 순 정렬 (Category 포함)
    @Query(value = """
        SELECT p.product_id AS productId, p.name AS name, p.price AS price,
               c.category_name AS categoryName, p.description AS description, i.stock AS stock, p.g_image AS gImage
        FROM product p
        LEFT JOIN category c ON c.category_id = p.category_id
        LEFT JOIN inventory i ON i.product_id = p.product_id
        WHERE p.seller_id = :sellerId AND p.category_id = :categoryId AND p.name LIKE CONCAT('%', :search, '%')
        ORDER BY p.created_register DESC
    """, nativeQuery = true)
    Page<SellerProductsListDTO> findSellerProductsByCategoryAndCreatedDateDesc(@Param("sellerId") Long sellerId,
                                                                                @Param("categoryId") Long categoryId,
                                                                                @Param("search") String search,
                                                                                Pageable pageable);

    // 판매량 높은 순 정렬 (Category 포함)
    @Query(value = """
        SELECT p.product_id AS productId, p.name AS name, p.price AS price,
               c.category_name AS categoryName, p.description AS description, i.stock AS stock, p.g_image AS gImage
        FROM product p
        LEFT JOIN category c ON c.category_id = p.category_id
        LEFT JOIN inventory i ON i.product_id = p.product_id
        WHERE p.seller_id = :sellerId AND p.category_id = :categoryId AND p.name LIKE CONCAT('%', :search, '%')
        ORDER BY p.overall_sales DESC
    """, nativeQuery = true)
    Page<SellerProductsListDTO> findSellerProductsByCategoryAndSalesDesc(@Param("sellerId") Long sellerId,
                                                                          @Param("categoryId") Long categoryId,
                                                                          @Param("search") String search,
                                                                          Pageable pageable);

    
    // 카테고리와 검색어를 기준으로 상품 조회
    @Query(value = """
        SELECT p.product_id AS productId, p.name AS name, p.price AS price,
               c.category_name AS categoryName, p.description AS description, i.stock AS stock, p.g_image AS gImage
        FROM product p
        LEFT JOIN category c ON c.category_id = p.category_id
        LEFT JOIN inventory i ON i.product_id = p.product_id
        WHERE p.seller_id = :sellerId AND p.category_id = :categoryId AND p.name LIKE CONCAT('%', :search, '%')
    """, nativeQuery = true)
    Page<SellerProductsListDTO> findSellerProductsByCategory(@Param("sellerId") Long sellerId,
                                                              @Param("categoryId") Long categoryId,
                                                              @Param("search") String search,
                                                              Pageable pageable);    
    @Query(value = """
            SELECT p.product_id AS productId, p.name AS name, p.price AS price,
                   c.category_name AS categoryName, p.description AS description, i.stock AS stock, p.g_image AS gImage
            FROM product p
            LEFT JOIN category c ON c.category_id = p.category_id
            LEFT JOIN inventory i ON i.product_id = p.product_id
            WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :search, '%')
        """, nativeQuery = true)
        Page<SellerProductsListDTO> findSellerProductsBySearch(@Param("sellerId") Long sellerId,
                                                                @Param("search") String search,
                                                                Pageable pageable);
    
    @EntityGraph(attributePaths = {"seller"})
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findDetailWithSellerById(@Param("productId") Long productId);

}



