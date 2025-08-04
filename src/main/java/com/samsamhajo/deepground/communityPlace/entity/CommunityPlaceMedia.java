package com.samsamhajo.deepground.communityPlace.entity;

import com.samsamhajo.deepground.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "community_place_media")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPlaceMedia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_media_id")
    private Long id;

    @Column(name = "review_media_url", nullable = false)
    private String mediaUrl;

    @Column(name = "extension", nullable = false)
    private String extension;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_place_reviews_id")
    private CommunityPlaceReview communityPlaceReview;

    public String getMediaUrl() {
        return mediaUrl;
    }

    private CommunityPlaceMedia(String mediaUrl, String extension, CommunityPlaceReview communityPlaceReview) {
        this.mediaUrl = mediaUrl;
        this.extension = extension;
        this.communityPlaceReview = communityPlaceReview;
    }

    public static CommunityPlaceMedia of(String mediaUrl, String extension, CommunityPlaceReview communityPlaceReview) {
        return new CommunityPlaceMedia(mediaUrl, extension, communityPlaceReview);
    }
}
