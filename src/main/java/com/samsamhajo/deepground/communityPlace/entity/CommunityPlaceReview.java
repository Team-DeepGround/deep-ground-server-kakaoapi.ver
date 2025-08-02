package com.samsamhajo.deepground.communityPlace.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "community_place_reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPlaceReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_place_reviews_id")
    private Long id;

    @Column(name = "community_place_scope")
    private double scope;

    @Column(name = "community_place_content")
    private String content;

    @Column(name = "place_id")
    private Long placeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specific_address_id")
    @JsonBackReference //순환참조 방지 : (Depth 깊이 에러 발생)
    private SpecificAddress specificAddress;

    private CommunityPlaceReview(double scope,String content, Long placeId, Member member){
        this.scope = scope;
        this.content = content;
        this.placeId = placeId;
        this.member = member;
    }

    public static CommunityPlaceReview of(double scope,String content, Long placeId, Member member, SpecificAddress specificAddress) {
        CommunityPlaceReview review = new CommunityPlaceReview(scope, content, placeId, member);
        review.specificAddress = specificAddress;
        specificAddress.getCommunityPlaceReviews().add(review);
        return review;
    }
}
