package com.onshop.shop.domain.address.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.onshop.shop.domain.address.entity.Address;
import com.onshop.shop.domain.user.entity.User;

/**
 * Address 엔티티를 위한 JPA Repository 인터페이스입니다.
 * 사용자 주소 관련 CRUD 및 커스텀 쿼리 메서드를 제공합니다.
 *
 * <p><b>⚠ 주의:</b> 서비스 계층에서 트랜잭션을 관리해야 하며,
 * 이 레포지토리에 @Transactional을 직접 붙이는 것은 권장되지 않습니다.
 * 기존에 붙여 있던 것을 제거하긴 했는데, 혹여나 트랜잭션 처리 문제가 생기면, 해당 이노테이션을 문제가 발생하는 서비스 메서드 위에 붙이세요.
 * </p>
 * @author Youngwan Kim
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * 특정 사용자(User)의 모든 주소를 삭제합니다.
     *
     * @param user 주소를 삭제할 사용자
     */
    void deleteByUser(User user);

    /**
     * 특정 사용자 ID를 가진 사용자의 주소를 모두 삭제합니다.
     *
     * @param userId 사용자 ID
     */
    void deleteByUserUserId(Long userId);

    /**
     * 해당 사용자가 보유한 주소의 개수를 반환합니다.
     *
     * @param user 사용자
     * @return 주소 수
     */
    long countByUser(User user);

    /**
     * 사용자 ID에 해당하는 주소를 JPQL로 일괄 삭제합니다.
     * <p>⚠ 반드시 @Modifying과 함께 사용되어야 합니다.</p>
     *
     * @param userId 사용자 ID
     */
    @Modifying
    @Query("DELETE FROM Address a WHERE a.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자 ID를 가진 사용자 중 기본 주소로 설정된 주소를 반환합니다.
     *
     * @param userId 사용자 ID
     * @return Optional 기본 주소
     */
    Optional<Address> findByUser_UserIdAndIsDefaultTrue(Long userId);

    /**
     * 사용자 ID로 해당 사용자의 모든 주소를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 주소 리스트
     */
    List<Address> findByUserUserId(Long userId);

    /**
     * 특정 사용자와 주소1(address1), 우편번호(post) 조합이 존재하는지 여부를 확인합니다.
     * 중복 주소 체크 시 사용됩니다.
     *
     * @param user 사용자
     * @param address1 주소1
     * @param post 우편번호
     * @return true if 존재함
     */
    boolean existsByUserAndAddress1AndPost(User user, String address1, String post);

    /**
     * 특정 사용자의 모든 주소를 조회합니다.
     *
     * @param user 사용자
     * @return 주소 리스트
     */
    List<Address> findByUser(User user);

    /**
     * 사용자와 주소1, 우편번호 기준으로 특정 주소를 조회합니다.
     * 기본 주소 설정 시 중복 확인 및 기존 주소 갱신에 사용됩니다.
     *
     * @param user 사용자
     * @param address1 주소1
     * @param post 우편번호
     * @return Address 객체
     */
    Address findByUserAndAddress1AndPost(User user, String address1, String post);
}
