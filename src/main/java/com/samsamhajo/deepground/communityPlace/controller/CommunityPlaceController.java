package com.samsamhajo.deepground.communityPlace.controller;


import com.samsamhajo.deepground.auth.security.CustomUserDetails;
import com.samsamhajo.deepground.communityPlace.dto.CommunityPlaceReviewDto;
import com.samsamhajo.deepground.communityPlace.dto.ReviewStatistics;
import com.samsamhajo.deepground.communityPlace.dto.request.CreateReviewDto;
import com.samsamhajo.deepground.communityPlace.dto.request.ReviewDetailDto;
import com.samsamhajo.deepground.communityPlace.dto.response.ReviewListResponseDto;
import com.samsamhajo.deepground.communityPlace.dto.response.ReviewResponseDto;
import com.samsamhajo.deepground.communityPlace.exception.CommunityPlaceSuccessCode;
import com.samsamhajo.deepground.communityPlace.service.CommunityPlaceService;
import com.samsamhajo.deepground.global.success.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/communityPlace")
@RequiredArgsConstructor
public class CommunityPlaceController {

    private final CommunityPlaceService communityPlaceService;

    @PostMapping(value = "/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> createReview(
            @Valid @ModelAttribute CreateReviewDto createReviewDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        ReviewResponseDto reviewResponseDto = communityPlaceService.createReview(createReviewDto, customUserDetails.getMember().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of(CommunityPlaceSuccessCode.REVIEW_CREATED, reviewResponseDto));
    }

    @GetMapping("/{specificAddressId}")
    public ResponseEntity<SuccessResponse> selectCommunityPlaceReviewsAndScope(@PathVariable Long specificAddressId) {

        ReviewStatistics reviewData = communityPlaceService.selectCommunityPlaceReviewsAndScope(specificAddressId);
        return ResponseEntity
                .ok(SuccessResponse.of(CommunityPlaceSuccessCode.COMMUNITYPLACE_SUCCESS_SELECT,reviewData));

    }

    @GetMapping("/reviews/{placeId}")
    public ResponseEntity<SuccessResponse> getCommunityPlaceReviews(
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable Long placeId) {
        ReviewListResponseDto reviewListResponseDto = communityPlaceService.SearchReviews(placeId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CommunityPlaceSuccessCode.COMMUNITY_PLACE_SUCCESS_SEARCH, reviewListResponseDto));
    }
  
    @GetMapping("/ByReviewCount")
    public ResponseEntity<SuccessResponse<List<CommunityPlaceReviewDto>>> selectCommunityPlaceByReviewCount() {

        List<CommunityPlaceReviewDto> communityPlaceReview = communityPlaceService.selectCommunityPlaceByReviewCount();

        return ResponseEntity
                .ok(SuccessResponse.of(CommunityPlaceSuccessCode.COMMUNITYPLACE_SUCCESS_SELECT_BY_REVIEW_COUNT,communityPlaceReview));
    }

    @GetMapping("/ByReviewScope")
    public ResponseEntity<SuccessResponse<List<CommunityPlaceReviewDto>>> selectCommunityPlaceByReviewScope() {

        List<CommunityPlaceReviewDto> communityPlaceReview = communityPlaceService.selectCommunityPlaceByReviewScope();

        return ResponseEntity
                .ok(SuccessResponse.of(CommunityPlaceSuccessCode.COMMUNITYPLACE_SUCCESS_SELECT_BY_REVIEW_SCOPE,communityPlaceReview));
    }

    @GetMapping("/communityPlaceReviewId")
    public ResponseEntity<SuccessResponse> getReviewDetail(
            @PathVariable Long communityPlaceReviewId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        ReviewDetailDto reviewDetailDto = communityPlaceService.SearchReviewDetail(communityPlaceReviewId, customUserDetails.getMember().getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CommunityPlaceSuccessCode.COMMUNITY_PLACE_SUCCESS_REVIEW_DETAIL, reviewDetailDto));
    }
}


