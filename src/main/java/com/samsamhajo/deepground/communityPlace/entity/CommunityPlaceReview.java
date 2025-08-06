package com.samsamhajo.deepground.communityPlace.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("is_deleted = false")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "specific_address_id")
    private Long specificAddressId;

    private CommunityPlaceReview(double scope,String content, Long specificAddressId ,Member member){
        this.scope = scope;
        this.content = content;
        this.specificAddressId = specificAddressId;
        this.member = member;
    }

    public static CommunityPlaceReview of(double scope,String content, Member member, Long specificAddressId) {
        CommunityPlaceReview review = new CommunityPlaceReview(scope, content, specificAddressId ,member);
        return review;
    }

    public void updateReview(double scope, String content, Long specificAddressId) {
        this.scope = scope;
        this.content = content;
        this.specificAddressId = specificAddressId;
    }
}
