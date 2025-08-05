package com.samsamhajo.deepground.communityPlace.repository;

import com.samsamhajo.deepground.communityPlace.dto.SelectCommunityPlaceDto;
import com.samsamhajo.deepground.communityPlace.entity.SpecificAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecificAddressRepository extends JpaRepository<SpecificAddress,Long> {

    Optional<SpecificAddress> findByNameAndLocation(String name, String location);

    @Query("SELECT sa " +
            "FROM SpecificAddress sa " +
            "LEFT JOIN sa.communityPlaceReviews r " +
            "GROUP BY sa " +
            "ORDER BY COUNT(r) DESC")
    List<SpecificAddress> findAllCommunityPlaceByReviewCountDesc();

    @Query("SELECT sa " +
            "FROM SpecificAddress sa " +
            "LEFT JOIN sa.communityPlaceReviews r " +
            "GROUP BY sa " +
            "ORDER BY AVG(r.scope) DESC")
    List<SpecificAddress> findAllCommunityPlaceByReviewScopeDesc();

    @Query("SELECT AVG(r.scope) FROM SpecificAddress sa Left Join sa.communityPlaceReviews r WHERE sa.id = :specificAddressId")
    Double avgScopeBySpecificAddressId( @Param("specificAddressId") Long specificAddressId);

    @Query("SELECT COUNT(r) FROM SpecificAddress sa Left Join sa.communityPlaceReviews r WHERE sa.id = :specificAddressId")
    Long countReviewBySpecificAddressId( @Param("specificAddressId") Long specificAddressId);
}
