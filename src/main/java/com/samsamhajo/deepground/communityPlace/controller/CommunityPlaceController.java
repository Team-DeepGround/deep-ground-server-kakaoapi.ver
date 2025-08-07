package com.samsamhajo.deepground.communityPlace.controller;


import com.samsamhajo.deepground.auth.security.CustomUserDetails;
import com.samsamhajo.deepground.communityPlace.dto.SelectCommunityPlace;
import com.samsamhajo.deepground.communityPlace.dto.request.CreateReviewDto;
import com.samsamhajo.deepground.communityPlace.dto.request.ModifyReviewDto;
import com.samsamhajo.deepground.communityPlace.dto.request.ReviewDetailDto;
import com.samsamhajo.deepground.communityPlace.dto.request.SummaryDto;
import com.samsamhajo.deepground.communityPlace.dto.response.ModifyReviewResponseDto;
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

import static org.springframework.data.redis.connection.ReactiveStreamCommands.AddStreamRecord.body;

@RestController
@RequestMapping("/communityPlace")
@RequiredArgsConstructor
public class CommunityPlaceController {

    private final CommunityPlaceService communityPlaceService;

    @PostMapping(value = "/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> createReview(
            @Valid @ModelAttribute CreateReviewDto createReviewDto,
            @RequestParam Long specificAddressId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        ReviewResponseDto reviewResponseDto = communityPlaceService.createReview(createReviewDto, specificAddressId,customUserDetails.getMember().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of(CommunityPlaceSuccessCode.REVIEW_CREATED, reviewResponseDto));
    }

    @GetMapping("/reviews/{specificAddressId}")
    public ResponseEntity<SuccessResponse> getCommunityPlaceReviews(
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable Long specificAddressId) {
        ReviewListResponseDto reviewListResponseDto = communityPlaceService.SearchReviews(specificAddressId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CommunityPlaceSuccessCode.COMMUNITY_PLACE_SUCCESS_SEARCH, reviewListResponseDto));
    }
  
    @GetMapping("/byReviewCount")
    public ResponseEntity<SuccessResponse<List<SelectCommunityPlace>>> selectCommunityPlaceByReviewCount() {

        List<SelectCommunityPlace> communityPlaceReview = communityPlaceService.selectCommunityPlaceByReviewCount();

        return ResponseEntity
                .ok(SuccessResponse.of(CommunityPlaceSuccessCode.COMMUNITYPLACE_SUCCESS_SELECT_BY_REVIEW_COUNT,communityPlaceReview));
    }

    @GetMapping("/byReviewScope")
    public ResponseEntity<SuccessResponse<List<SelectCommunityPlace>>> selectCommunityPlaceByReviewScope() {

        List<SelectCommunityPlace> communityPlaceReview = communityPlaceService.selectCommunityPlaceByReviewScope();

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

    @PutMapping(value = "/modify/{specificAddressId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> modifyReview(
            ModifyReviewDto modifyReviewDto,
            @PathVariable Long specificAddressId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        ModifyReviewResponseDto modifyReviewResponseDto = communityPlaceService.modifyCommunityPlaceReview(modifyReviewDto, specificAddressId, customUserDetails.getMember().getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(CommunityPlaceSuccessCode.COMMUNITY_PLACE_SUCCESS_REVIEW_MODIFIED, modifyReviewResponseDto));
    }

    @GetMapping("/review/my")
    public ResponseEntity<SummaryDto> getMyReview(@RequestParam Long scheduleId,
                                                        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        SummaryDto dto = communityPlaceService.getMyReviewSummary(scheduleId, customUserDetails.getMember().getId());
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

}


