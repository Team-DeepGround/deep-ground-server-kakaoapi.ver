package com.samsamhajo.deepground.feed.feedcomment.service;

import com.samsamhajo.deepground.feed.feed.entity.Feed;
import com.samsamhajo.deepground.feed.feedcomment.entity.FeedComment;
import com.samsamhajo.deepground.feed.feedcomment.entity.FeedCommentLike;
import com.samsamhajo.deepground.feed.feedcomment.exception.FeedCommentErrorCode;
import com.samsamhajo.deepground.feed.feedcomment.exception.FeedCommentException;
import com.samsamhajo.deepground.feed.feedcomment.repository.FeedCommentLikeRepository;
import com.samsamhajo.deepground.feed.feedcomment.repository.FeedCommentRepository;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.exception.MemberErrorCode;
import com.samsamhajo.deepground.member.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedCommentLikeServiceTest {

    @Mock
    private FeedCommentRepository feedCommentRepository;
    @Mock
    private FeedCommentLikeRepository feedCommentLikeRepository;

    @InjectMocks
    private FeedCommentLikeService feedCommentLikeService;

    private static final String TEST_CONTENT = "테스트 댓글 내용입니다.";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "테스트유저";

    private Member testMember;
    private Feed testFeed;
    private FeedComment testFeedComment;
    private FeedCommentLike testFeedCommentLike;

    @BeforeEach
    void setUp() {
        testMember = Member.createLocalMember(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
        testFeed = Feed.of(TEST_CONTENT, testMember);
        testFeedComment = FeedComment.of(TEST_CONTENT, testFeed, testMember);
        testFeedCommentLike = FeedCommentLike.of(testFeedComment, testMember);

        ReflectionTestUtils.setField(testMember, "id", 1L);
        ReflectionTestUtils.setField(testFeed, "id", 1L);
        ReflectionTestUtils.setField(testFeedComment, "id", 1L);
        ReflectionTestUtils.setField(testFeedCommentLike, "id", 1L);
    }

    @Test
    @DisplayName("피드 댓글 좋아요 증가 성공")
    void feedLikeIncreaseSuccess() {
        // given
        when(feedCommentRepository.getById(1L)).thenReturn(testFeedComment);
        when(feedCommentLikeRepository.existsByFeedCommentIdAndMemberId(1L, 1L)).thenReturn(false);
        when(feedCommentLikeRepository.save(any(FeedCommentLike.class))).thenReturn(testFeedCommentLike);

        // when
        feedCommentLikeService.feedLikeIncrease(1L, testMember);

        // then
        verify(feedCommentLikeRepository).save(any(FeedCommentLike.class));
    }

    @Test
    @DisplayName("피드 댓글 좋아요 증가 실패 - 이미 좋아요한 경우")
    void feedLikeIncreaseFailWithAlreadyLiked() {
        // given
        when(feedCommentLikeRepository.existsByFeedCommentIdAndMemberId(1L, 1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> feedCommentLikeService.feedLikeIncrease(1L, testMember))
                .isInstanceOf(FeedCommentException.class)
                .hasFieldOrPropertyWithValue("errorCode", FeedCommentErrorCode.FEED_COMMENT_LIKE_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("피드 댓글 좋아요 감소 성공")
    void feedLikeDecreaseSuccess() {
        // given
        when(feedCommentLikeRepository.countByFeedCommentId(1L)).thenReturn(1);
        when(feedCommentLikeRepository.getByFeedCommentIdAndMemberId(1L, 1L)).thenReturn(testFeedCommentLike);

        // when
        feedCommentLikeService.feedLikeDecrease(1L, 1L);

        // then
        verify(feedCommentLikeRepository).delete(testFeedCommentLike);
    }

    @Test
    @DisplayName("피드 댓글 좋아요 감소 실패 - 좋아요가 없는 경우")
    void feedLikeDecreaseFailWithNoLikes() {
        // given
        when(feedCommentLikeRepository.countByFeedCommentId(1L)).thenReturn(0);

        // when & then
        assertThatThrownBy(() -> feedCommentLikeService.feedLikeDecrease(1L, 1L))
                .isInstanceOf(FeedCommentException.class)
                .hasFieldOrPropertyWithValue("errorCode", FeedCommentErrorCode.FEED_LIKE_MINUS_NOT_ALLOWED);
    }

    @Test
    @DisplayName("피드 댓글 좋아요 수 조회 성공")
    void countFeedCommentLikeByFeedIdSuccess() {
        // given
        when(feedCommentLikeRepository.countByFeedCommentId(1L)).thenReturn(5);

        // when
        int count = feedCommentLikeService.countFeedCommentLikeByFeedId(1L);

        // then
        assertThat(count).isEqualTo(5);
    }

    @Test
    @DisplayName("피드 댓글 좋아요 삭제 성공")
    void deleteAllByFeedCommentIdSuccess() {
        // when
        feedCommentLikeService.deleteAllByFeedCommentId(1L);

        // then
        verify(feedCommentLikeRepository).deleteAllByFeedCommentId(1L);
    }

    @Test
    @DisplayName("피드 댓글 좋아요 여부 확인 성공")
    void isLikedSuccess() {
        // given
        when(feedCommentLikeRepository.existsByFeedCommentIdAndMemberId(1L, 1L)).thenReturn(true);

        // when
        boolean isLiked = feedCommentLikeService.isLiked(1L, 1L);

        // then
        assertThat(isLiked).isTrue();
    }
} 