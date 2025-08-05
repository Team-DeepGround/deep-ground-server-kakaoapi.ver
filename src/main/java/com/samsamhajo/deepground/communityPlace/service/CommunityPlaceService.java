package com.samsamhajo.deepground.communityPlace.service;


import com.samsamhajo.deepground.communityPlace.dto.CommunityPlaceReviewDto;
import com.samsamhajo.deepground.communityPlace.dto.ReviewStatistics;
import com.samsamhajo.deepground.communityPlace.dto.request.AddressDto;
import com.samsamhajo.deepground.communityPlace.dto.request.CreateReviewDto;
import com.samsamhajo.deepground.communityPlace.dto.request.ReviewDetailDto;
import com.samsamhajo.deepground.communityPlace.dto.request.SearchReviewSummaryDto;
import com.samsamhajo.deepground.communityPlace.dto.response.ReviewListResponseDto;
import com.samsamhajo.deepground.communityPlace.dto.response.ReviewResponseDto;
import com.samsamhajo.deepground.communityPlace.entity.CommunityPlaceMedia;
import com.samsamhajo.deepground.communityPlace.entity.CommunityPlaceReview;
import com.samsamhajo.deepground.communityPlace.entity.SpecificAddress;
import com.samsamhajo.deepground.communityPlace.repository.CommunityPlaceMediaRepository;
import com.samsamhajo.deepground.communityPlace.repository.CommunityPlaceRepository;
import com.samsamhajo.deepground.communityPlace.repository.SpecificAddressRepository;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.exception.MemberErrorCode;
import com.samsamhajo.deepground.member.exception.MemberException;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import com.samsamhajo.deepground.communityPlace.exception.CommunityPlaceErrorCode;
import com.samsamhajo.deepground.communityPlace.exception.CommunityPlaceException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
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

    private final CommunityPlaceRepository communityPlaceRepository;
    private final ValidService validService;
    private final CommunityPlaceMediaService communityPlaceMediaService;
    private final SpecificAddressRepository specificAddressRepository;
    private final MemberRepository memberRepository;
    private final CommunityPlaceMediaRepository communityPlaceMediaRepository;

    @Transactional
    public ReviewResponseDto createReview(CreateReviewDto createReviewDto, Long memberId) {

        // Member가 존재하는지 여부 검증
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.INVALID_MEMBER_ID));

        //AddressDto를 통해 주소, 좌표값 SpecificAddress로 저장
        AddressDto dto = createReviewDto.getAddress();

        /**
         *  GeometryFactory : Point, Polygon등 공간 객체를 생성하는 클래스
         *  PrecisionModel : 좌표의 정밀도 모델
         *  SRID(4326) : SRID는 어떤 좌표계 체계를 사용하고 있는지 나타냄
         *  4326 : WGS84 좌표계로 GPS에서 사용하는 전세계 표준 좌표계라고 함.
         *  MySQL의 공간 데이터 타입(Point, Geometry)은 좌표계가 명확히 지정되지 않는다면 계산할 수 없다고 함.
         *  ->  공간 객체 생성 도구를 사용하고, GPS 좌표계를 지정해주어 보다 정확한 위도/경도 좌표를 갖는 공간 객체를 생성함.
         */
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // dto 내부 메소드를 통해, geoMetryFactory를 이용한 Point 클래스로 좌표 생성
        Point point = dto.toPoint(geometryFactory);

        // 이후 주소 + 좌표를 통해 SpecificAddress 객체 저장
        SpecificAddress address = specificAddressRepository.save(
                SpecificAddress.of(dto.getAddress(), point)
        );

        //SpecificAddress 저장 후, Review 생성
        CommunityPlaceReview review = CommunityPlaceReview.of(
                createReviewDto.getScope(),
                createReviewDto.getContent(),
                createReviewDto.getPlaceId(),
                member,
                address
        );

        // 별점과 리뷰 CommunityPlaceReview에 저장
        communityPlaceRepository.save(review);

        //media 저장
        List<String> mediaUrl = createCommunityPlaceMedia(createReviewDto, review);

        //ReviewResponseDto로 반환
        return ReviewResponseDto.of(
                review.getId(),
                review.getScope(),
                review.getContent(),
                address.getLocation(),
                point.getY(), // latitude(위도)
                point.getX(),// longitude(경도)
                member.getId(),
                review.getPlaceId(),
                mediaUrl
        );
    }

    private List<String> createCommunityPlaceMedia(CreateReviewDto createReviewDto, CommunityPlaceReview communityPlaceReview) {
        return communityPlaceMediaService.createCommunityPlaceMedia(communityPlaceReview, createReviewDto.getImages());

    }

    public ReviewStatistics selectCommunityPlaceReviewsAndScope(Long specificAddressId) {

        specificAddressRepository.findById(specificAddressId)
                .orElseThrow(() -> new CommunityPlaceException(CommunityPlaceErrorCode.COMMUNITYPLACE_NOT_FOUND));

        Double avgScope = specificAddressRepository.avgScopeBySpecificAddressId(specificAddressId);
        Long reviewCount = specificAddressRepository.countReviewBySpecificAddressId(specificAddressId);

        return ReviewStatistics.of(avgScope,reviewCount);
    }
  
    //TODO: 리뷰 작성 로직 구현 후 테스트 코드 작성 후 테스트 및 SWAGGER 통해 컨트롤러 테스트 진행 예정
    public ReviewListResponseDto SearchReviews(Long placeId, Pageable pageable) {

        Page<CommunityPlaceReview> reviewPage = communityPlaceRepository.findByPlaceId(placeId, pageable);

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

    //TODO : 후에 스터디 일정 생성 시 가게정보 저장 로직 완료되면 테스트 예정 + N개의 리뷰마다 N번의 미디어 조회가 발생하기 때문에, 추후에 리팩토링 예정
    public List<CommunityPlaceReviewDto> selectCommunityPlaceByReviewCount() {

        return specificAddressRepository.findAllCommunityPlaceByReviewCountDesc();
    }

    public List<CommunityPlaceReviewDto> selectCommunityPlaceByReviewScope() {

        return specificAddressRepository.findAllCommunityPlaceByReviewScopeDesc();
    }

    //TODO : 후에 가게정보 저장 로직 완성되면, 테스트 예정
    public ReviewDetailDto SearchReviewDetail(Long placeId, Long reviewId) {
        CommunityPlaceReview communityPlaceReview = communityPlaceRepository.findById(reviewId).orElseThrow(
                () -> new CommunityPlaceException(CommunityPlaceErrorCode.REVIEW_NOT_FOUND));

        List<CommunityPlaceMedia> communityPlaceMedia = communityPlaceMediaRepository.findAllByCommunityPlaceReviewId(communityPlaceReview.getId());

        List<String> mediaUrl = communityPlaceMedia.stream()
                .map(CommunityPlaceMedia :: getMediaUrl)
                .collect(Collectors.toList());

        return ReviewDetailDto.of(
                communityPlaceReview.getPlaceId(),
                communityPlaceReview.getId(),
                communityPlaceReview.getContent(),
                communityPlaceReview.getMember().getNickname(),
                communityPlaceReview.getScope(),
                communityPlaceReview.getMember().getId(),
                mediaUrl
        );
    }
}

