package com.samsamhajo.deepground.communityPlace.repository;

import com.samsamhajo.deepground.communityPlace.dto.SelectCommunityPlace;
import com.samsamhajo.deepground.communityPlace.entity.SpecificAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecificAddressRepository extends JpaRepository<SpecificAddress,Long> {

    Optional<SpecificAddress> findByNameAndLocation(String name, String location);

    @Query("SELECT sa.id AS id,sa.name AS name, sa.location AS location," +
            "sa.phone AS phone,sa.placeId AS placeId, " +
            "sa.latitude AS latitude,sa.longitude AS longitude," +
            "AVG(r.scope) AS avgScope, COUNT(r) AS countReview " +
            "FROM SpecificAddress sa " +
            "LEFT JOIN sa.communityPlaceReviews r " +
            "GROUP BY sa " +
            "ORDER BY COUNT(r) DESC" )
    List<SelectCommunityPlace> findAllCommunityPlaceByReviewCountDesc();

    @Query( "SELECT sa.id AS id,sa.name AS name, sa.location AS location," +
            "sa.phone AS phone,sa.placeId AS placeId, " +
            "sa.latitude AS latitude,sa.longitude AS longitude," +
            "AVG(r.scope) AS avgScope, COUNT(r) AS countReview " +
            "FROM SpecificAddress sa " +
            "LEFT JOIN sa.communityPlaceReviews r " +
            "GROUP BY sa " +
            "ORDER BY AVG(r.scope) DESC")
    List<SelectCommunityPlace> findAllCommunityPlaceByReviewScopeDesc();
}