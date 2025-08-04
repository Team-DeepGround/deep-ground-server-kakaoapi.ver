package com.samsamhajo.deepground.feed.feed.service;

import com.samsamhajo.deepground.feed.feed.entity.Feed;
import com.samsamhajo.deepground.feed.feed.entity.FeedLike;
import com.samsamhajo.deepground.feed.feed.exception.FeedErrorCode;
import com.samsamhajo.deepground.feed.feed.exception.FeedException;
import com.samsamhajo.deepground.feed.feed.repository.FeedLikeRepository;
import com.samsamhajo.deepground.feed.feed.repository.FeedRepository;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.exception.MemberErrorCode;
import com.samsamhajo.deepground.member.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FeedLikeServiceTest {

    private FeedRepository feedRepository;
    private FeedLikeRepository feedLikeRepository;
    private FeedLikeService feedLikeService;

    private static final String TEST_CONTENT = "테스트 피드 내용입니다.";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "테스트유저";

    @BeforeEach
    void setUp() {
        feedRepository = mock(FeedRepository.class);
        feedLikeRepository = mock(FeedLikeRepository.class);
        
        feedLikeService = new FeedLikeService(
            feedRepository,
            feedLikeRepository
        );
    }

    @Test
    @DisplayName("피드 좋아요 증가 성공")
    void feedLikeIncreaseSuccess() {
        // given
        Member testMember = Member.createLocalMember(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
        Feed testFeed = Feed.of(TEST_CONTENT, testMember);
        FeedLike feedLike = FeedLike.of(testFeed, testMember);

        ReflectionTestUtils.setField(testMember, "id", 1L);
        ReflectionTestUtils.setField(testFeed, "id", 1L);
        ReflectionTestUtils.setField(feedLike, "id", 1L);

        when(feedRepository.getById(1L)).thenReturn(testFeed);
        when(feedLikeRepository.existsByFeedIdAndMemberId(1L, 1L)).thenReturn(false);
        when(feedLikeRepository.save(any(FeedLike.class))).thenReturn(feedLike);

        // when
        feedLikeService.feedLikeIncrease(1L, testMember);

        // then
        verify(feedLikeRepository).save(any(FeedLike.class));
    }

    @Test
    @DisplayName("피드 좋아요 증가 실패 - 이미 좋아요를 누른 경우")
    void feedLikeIncreaseFailWithAlreadyLiked() throws NoSuchFieldException, IllegalAccessException {
        // given
        Member testMember = mock(Member.class);

        // given
        when(feedLikeRepository.existsByFeedIdAndMemberId(1L, 1L)).thenReturn(true);
        when(testMember.getId()).thenReturn(1L);

        // when & then
        assertThatThrownBy(() -> feedLikeService.feedLikeIncrease(1L, testMember))
                .isInstanceOf(FeedException.class)
                .hasFieldOrPropertyWithValue("errorCode", FeedErrorCode.FEED_LIKE_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("피드 좋아요 감소 성공")
    void feedLikeDecreaseSuccess() {
        // given
        Member testMember = Member.createLocalMember(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
        Feed testFeed = Feed.of(TEST_CONTENT, testMember);
        FeedLike feedLike = FeedLike.of(testFeed, testMember);

        ReflectionTestUtils.setField(testMember, "id", 1L);
        ReflectionTestUtils.setField(testFeed, "id", 1L);
        ReflectionTestUtils.setField(feedLike, "id", 1L);

        when(feedLikeRepository.countByFeedId(1L)).thenReturn(1);
        when(feedLikeRepository.getByFeedIdAndMemberId(1L, 1L)).thenReturn(feedLike);

        // when
        feedLikeService.feedLikeDecrease(1L, 1L);

        // then
        verify(feedLikeRepository).delete(feedLike);
    }

    @Test
    @DisplayName("피드 좋아요 감소 실패 - 좋아요가 없는 경우")
    void feedLikeDecreaseFailWithNoLike() {
        // given

        when(feedLikeRepository.countByFeedId(1L)).thenReturn(0);

        // when & then
        assertThatThrownBy(() -> feedLikeService.feedLikeDecrease(1L, 1L))
                .isInstanceOf(FeedException.class)
                .hasFieldOrPropertyWithValue("errorCode", FeedErrorCode.FEED_LIKE_MINUS_NOT_ALLOWED);
    }

    @Test
    @DisplayName("피드 좋아요 감소 실패 - 존재하지 않는 좋아요")
    void feedLikeDecreaseFailWithInvalidLike() {
        // given
        when(feedLikeRepository.countByFeedId(1L)).thenReturn(1);
        when(feedLikeRepository.getByFeedIdAndMemberId(1L, 1L))
                .thenThrow(new FeedException(FeedErrorCode.FEED_LIKE_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> feedLikeService.feedLikeDecrease(1L, 1L))
                .isInstanceOf(FeedException.class)
                .hasFieldOrPropertyWithValue("errorCode", FeedErrorCode.FEED_LIKE_NOT_FOUND);
    }

    @Test
    @DisplayName("피드 좋아요 수 조회 성공")
    void countFeedLikeByFeedIdSuccess() {
        // given
        when(feedLikeRepository.countByFeedId(1L)).thenReturn(5);

        // when
        int count = feedLikeService.countFeedLikeByFeedId(1L);

        // then
        assertThat(count).isEqualTo(5);
    }

    @Test
    @DisplayName("피드 좋아요 여부 확인 성공")
    void isLikedSuccess() {
        // given
        when(feedLikeRepository.existsByFeedIdAndMemberId(1L, 1L)).thenReturn(true);

        // when
        boolean isLiked = feedLikeService.isLiked(1L, 1L);

        // then
        assertThat(isLiked).isTrue();
    }

    @Test
    @DisplayName("피드의 모든 좋아요 삭제 성공")
    void deleteAllByFeedIdSuccess() {
        // given
        Long feedId = 1L;

        // when
        feedLikeService.deleteAllByFeedId(feedId);

        // then
        verify(feedLikeRepository).deleteAllByFeedId(feedId);
    }
} 