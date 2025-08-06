package com.samsamhajo.deepground.communityPlace.service;


import com.samsamhajo.deepground.communityPlace.dto.SelectCommunityPlaceDto;
import com.samsamhajo.deepground.communityPlace.dto.ReviewStatistics;
import com.samsamhajo.deepground.communityPlace.dto.request.CreateReviewDto;
import com.samsamhajo.deepground.communityPlace.dto.request.ModifyReviewDto;
import com.samsamhajo.deepground.communityPlace.dto.request.ReviewDetailDto;
import com.samsamhajo.deepground.communityPlace.dto.request.SearchReviewSummaryDto;
import com.samsamhajo.deepground.communityPlace.dto.response.ModifyReviewResponseDto;
import com.samsamhajo.deepground.communityPlace.dto.response.ReviewListResponseDto;
import com.samsamhajo.deepground.communityPlace.dto.response.ReviewResponseDto;
import com.samsamhajo.deepground.communityPlace.entity.CommunityPlaceMedia;
import com.samsamhajo.deepground.communityPlace.entity.CommunityPlaceReview;
import com.samsamhajo.deepground.communityPlace.entity.SpecificAddress;
import com.samsamhajo.deepground.communityPlace.repository.CommunityPlaceMediaRepository;
import com.samsamhajo.deepground.communityPlace.repository.CommunityPlaceReviewRepository;
import com.samsamhajo.deepground.communityPlace.repository.SpecificAddressRepository;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.exception.MemberErrorCode;
import com.samsamhajo.deepground.member.exception.MemberException;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import com.samsamhajo.deepground.communityPlace.exception.CommunityPlaceErrorCode;
import com.samsamhajo.deepground.communityPlace.exception.CommunityPlaceException;
import com.samsamhajo.deepground.qna.question.Dto.QuestionUpdateRequestDto;
import com.samsamhajo.deepground.qna.question.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPlaceService {

    private final CommunityPlaceReviewRepository communityPlaceReviewRepository;
    private final ValidService validService;
    private final CommunityPlaceMediaService communityPlaceMediaService;
    private final SpecificAddressRepository specificAddressRepository;
    private final MemberRepository memberRepository;
    private final CommunityPlaceMediaRepository communityPlaceMediaRepository;

    @Transactional
    public ReviewResponseDto createReview(CreateReviewDto createReviewDto, Long specificAddressId, Long memberId) {

        // Member가 존재하는지 여부 검증
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.INVALID_MEMBER_ID));

        SpecificAddress specificAddress = specificAddressRepository.findById(specificAddressId).orElseThrow(
                ()-> new CommunityPlaceException(CommunityPlaceErrorCode.COMMUNITY_PLACE_NOT_FOUND));


        /**
         *  GeometryFactory : Point, Polygon등 공간 객체를 생성하는 클래스
         *  PrecisionModel : 좌표의 정밀도 모델
         *  SRID(4326) : SRID는 어떤 좌표계 체계를 사용하고 있는지 나타냄
         *  4326 : WGS84 좌표계로 GPS에서 사용하는 전세계 표준 좌표계라고 함.
         *  MySQL의 공간 데이터 타입(Point, Geometry)은 좌표계가 명확히 지정되지 않는다면 계산할 수 없다고 함.
         *  ->  공간 객체 생성 도구를 사용하고, GPS 좌표계를 지정해주어 보다 정확한 위도/경도 좌표를 갖는 공간 객체를 생성함.
         */
//        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
//
//        // dto 내부 메소드를 통해, geoMetryFactory를 이용한 Point 클래스로 좌표 생성
//        Point point = dto.toPoint(geometryFactory);
//
//        // 이후 주소 + 좌표를 통해 SpecificAddress 객체 저장
//        SpecificAddress address = specificAddressRepository.save(
//                SpecificAddress.of(dto.getAddress(), point)
//        );

        //SpecificAddress 저장 후, Review 생성함
        CommunityPlaceReview review = CommunityPlaceReview.of(
                createReviewDto.getScope(),
                createReviewDto.getContent(),
                member,
                specificAddress.getId()
        );

        // 별점과 리뷰 CommunityPlaceReview에 저장
        communityPlaceReviewRepository.save(review);

        //media 저장
        List<String> mediaUrl = createCommunityPlaceMedia(createReviewDto, review);

        //ReviewResponseDto로 반환
        return ReviewResponseDto.of(
                review.getId(),
                review.getScope(),
                review.getContent(),
                member.getId(),
                specificAddressId,
                mediaUrl
        );
    }

    private List<String> createCommunityPlaceMedia(CreateReviewDto createReviewDto, CommunityPlaceReview communityPlaceReview) {
        return communityPlaceMediaService.createCommunityPlaceMedia(communityPlaceReview, createReviewDto.getImages());
    }

    private List<String> updateCommunityPlaceMedia(ModifyReviewDto modifyReviewDto, CommunityPlaceReview communityPlaceReview) {
        return communityPlaceMediaService.createCommunityPlaceMedia(communityPlaceReview, modifyReviewDto.getImages());
    }

    public ReviewStatistics selectCommunityPlaceReviewsAndScope(Long specificAddressId) {

        specificAddressRepository.findById(specificAddressId)
                .orElseThrow(() -> new CommunityPlaceException(CommunityPlaceErrorCode.COMMUNITY_PLACE_NOT_FOUND));

        Double avgScope = specificAddressRepository.avgScopeBySpecificAddressId(specificAddressId);
        if (avgScope == null) {
            throw new CommunityPlaceException(CommunityPlaceErrorCode.REVIEW_COUNT_NOT_FOUND);
        }
        Long reviewCount = specificAddressRepository.countReviewBySpecificAddressId(specificAddressId);
        if (reviewCount == null) {
            throw new CommunityPlaceException(CommunityPlaceErrorCode.REVIEW_AVG_SCOPE_NOT_FOUND);
        }

        return ReviewStatistics.of(avgScope,reviewCount);
    }
  
    //TODO: 리뷰 작성 로직 구현 후 테스트 코드 작성 후 테스트 및 SWAGGER 통해 컨트롤러 테스트 진행 예정
    public ReviewListResponseDto SearchReviews(Long specificAddressId, Pageable pageable) {

        Page<CommunityPlaceReview> reviewPage = communityPlaceReviewRepository.findBySpecificAddressId(specificAddressId, pageable);

        List<SearchReviewSummaryDto> reviews = reviewPage.stream()
                .map(communityPlaceReview -> {
                    List<String> mediaUrl = communityPlaceMediaRepository.findAllByCommunityPlaceReviewId(communityPlaceReview.getId())
                            .stream()
                            .map((CommunityPlaceMedia:: getMediaUrl))
                            .toList();

                    return SearchReviewSummaryDto.of(communityPlaceReview, mediaUrl);
                }).toList();

        return ReviewListResponseDto.of(reviews, reviewPage.getTotalPages());
    }

    public List<SelectCommunityPlaceDto> selectCommunityPlaceByReviewCount() {

        List<SpecificAddress> selectCommunityPlaceByReviewCountDesc = specificAddressRepository.findAllCommunityPlaceByReviewCountDesc();
        if (selectCommunityPlaceByReviewCountDesc.isEmpty()) {
            throw new CommunityPlaceException(CommunityPlaceErrorCode.COMMUNITY_PLACE_NOT_FOUND);
        }

        return selectCommunityPlaceByReviewCountDesc.stream()
                .map(SelectCommunityPlaceDto::of)
                .toList();
    }

    public List<SelectCommunityPlaceDto> selectCommunityPlaceByReviewScope() {

        List<SpecificAddress> selectCommunityPlaceByAvgScope = specificAddressRepository.findAllCommunityPlaceByReviewScopeDesc();
        if (selectCommunityPlaceByAvgScope.isEmpty()) {
            throw new CommunityPlaceException(CommunityPlaceErrorCode.COMMUNITY_PLACE_NOT_FOUND);
        }
        return selectCommunityPlaceByAvgScope.stream()
                .map(SelectCommunityPlaceDto::of)
                .toList();
    }

    //TODO : 후에 가게정보 저장 로직 완성되면, 테스트 예정
    public ReviewDetailDto SearchReviewDetail(Long communityPlaceReviewId, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.INVALID_MEMBER_ID));

        CommunityPlaceReview communityPlaceReview = communityPlaceReviewRepository.findById(communityPlaceReviewId).orElseThrow(
                () -> new CommunityPlaceException(CommunityPlaceErrorCode.REVIEW_NOT_FOUND));

        List<CommunityPlaceMedia> communityPlaceMedia = communityPlaceMediaRepository.findAllByCommunityPlaceReviewId(communityPlaceReview.getId());

        List<String> mediaUrl = communityPlaceMedia.stream()
                .map(CommunityPlaceMedia :: getMediaUrl)
                .collect(Collectors.toList());

        return ReviewDetailDto.of(
                communityPlaceReview.getId(),
                communityPlaceReview.getContent(),
                communityPlaceReview.getMember().getNickname(),
                communityPlaceReview.getScope(),
                communityPlaceReview.getMember().getId(),
                mediaUrl
        );
    }

    //TODO : 후에 프론트 연동 후 시간 남으면 TEST코드 제대로 작성 예정 그전에는 swagger로 테스트 예정
    public ModifyReviewResponseDto modifyCommunityPlaceReview(ModifyReviewDto modifyReviewDto, Long specificAddressId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.INVALID_MEMBER_ID));

        CommunityPlaceReview communityPlaceReview = communityPlaceReviewRepository.findById(modifyReviewDto.getCommunityPlaceReviewId()).orElseThrow(
                () -> new CommunityPlaceException(CommunityPlaceErrorCode.REVIEW_NOT_FOUND));

        communityPlaceReviewRepository.deleteAllByCommunityPlaceReviewId(communityPlaceReview.getId());
        communityPlaceReview.updateReview(modifyReviewDto.getScope(), modifyReviewDto.getContent(), modifyReviewDto.getSpecificAddressId());
        List<String> mediaUrl = updateCommunityPlaceMedia(modifyReviewDto, communityPlaceReview);

        return ModifyReviewResponseDto.of(
                communityPlaceReview.getId(),
                memberId,
                modifyReviewDto.getScope(),
                modifyReviewDto.getContent(),
                modifyReviewDto.getSpecificAddressId(),
                mediaUrl
        );
    }
}

