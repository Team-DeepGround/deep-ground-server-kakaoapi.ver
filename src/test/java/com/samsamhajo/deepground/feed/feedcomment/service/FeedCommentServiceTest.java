package com.samsamhajo.deepground.feed.feedcomment.service;

import com.samsamhajo.deepground.feed.feed.entity.Feed;
import com.samsamhajo.deepground.feed.feed.repository.FeedRepository;
import com.samsamhajo.deepground.feed.feedcomment.entity.FeedComment;
import com.samsamhajo.deepground.feed.feedcomment.exception.FeedCommentErrorCode;
import com.samsamhajo.deepground.feed.feedcomment.exception.FeedCommentException;
import com.samsamhajo.deepground.feed.feedcomment.model.FeedCommentCreateRequest;
import com.samsamhajo.deepground.feed.feedcomment.model.FeedCommentUpdateRequest;
import com.samsamhajo.deepground.feed.feedcomment.repository.FeedCommentRepository;
import com.samsamhajo.deepground.feed.feedreply.service.FeedReplyService;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.exception.MemberErrorCode;
import com.samsamhajo.deepground.member.exception.MemberException;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import com.samsamhajo.deepground.notification.event.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedCommentServiceTest {

    @Mock
    private FeedCommentRepository feedCommentRepository;
    @Mock
    private FeedCommentMediaService feedCommentMediaService;
    @Mock
    private FeedRepository feedRepository;
    @Mock
    private FeedReplyService feedReplyService;
    @Mock
    private FeedCommentLikeService feedCommentLikeService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FeedCommentService feedCommentService;

    private static final String TEST_CONTENT = "테스트 댓글 내용입니다.";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "테스트유저";

    private Member testMember;
    private Feed testFeed;
    private FeedComment testFeedComment;
    private MockMultipartFile testImage;

    @BeforeEach
    void setUp() {
        testMember = Member.createLocalMember(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
        testFeed = Feed.of(TEST_CONTENT, testMember);
        testFeedComment = FeedComment.of(TEST_CONTENT, testFeed, testMember);
        testImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        ReflectionTestUtils.setField(testMember, "id", 1L);
        ReflectionTestUtils.setField(testFeed, "id", 1L);
        ReflectionTestUtils.setField(testFeedComment, "id", 1L);
    }

    @Test
    @DisplayName("피드 댓글 생성 성공")
    void createFeedCommentSuccess() {
        // given
        FeedCommentCreateRequest request = new FeedCommentCreateRequest(1L, TEST_CONTENT, List.of(testImage));
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        when(feedRepository.getById(1L)).thenReturn(testFeed);
        when(feedCommentRepository.save(any(FeedComment.class))).thenReturn(testFeedComment);

        // when
        FeedComment createdComment = feedCommentService.createFeedComment(request, testMember);

        // then
        assertThat(createdComment).isNotNull();
        assertThat(createdComment.getContent()).isEqualTo(TEST_CONTENT);
        verify(feedCommentMediaService).createFeedCommentMedia(any(FeedComment.class), anyList());
    }

    @Test
    @DisplayName("피드 댓글 생성 실패 - 내용이 비어있는 경우")
    void createFeedCommentFailWithEmptyContent() {
        // given
        FeedCommentCreateRequest request = new FeedCommentCreateRequest(1L, "", List.of());

        // when & then
        assertThatThrownBy(() -> feedCommentService.createFeedComment(request, testMember))
                .isInstanceOf(FeedCommentException.class)
                .hasFieldOrPropertyWithValue("errorCode", FeedCommentErrorCode.INVALID_FEED_COMMENT_CONTENT);
    }

    @Test
    @DisplayName("피드 댓글 수정 성공")
    void updateFeedCommentSuccess() {
        // given
        String updatedContent = "수정된 댓글 내용입니다.";
        FeedCommentUpdateRequest request = new FeedCommentUpdateRequest(updatedContent, List.of(testImage));

        when(feedCommentRepository.getById(1L)).thenReturn(testFeedComment);

        // when
        FeedComment updatedComment = feedCommentService.updateFeedComment(1L, request);

        // then
        assertThat(updatedComment.getContent()).isEqualTo(updatedContent);
        verify(feedCommentMediaService).updateFeedCommentMedia(any(FeedComment.class), anyList());
    }

    @Test
    @DisplayName("피드 댓글 수정 실패 - 내용이 비어있는 경우")
    void updateFeedCommentFailWithEmptyContent() {
        // given
        FeedCommentUpdateRequest request = new FeedCommentUpdateRequest("", List.of());

        // when & then
        assertThatThrownBy(() -> feedCommentService.updateFeedComment(1L, request))
                .isInstanceOf(FeedCommentException.class)
                .hasFieldOrPropertyWithValue("errorCode", FeedCommentErrorCode.INVALID_FEED_COMMENT_CONTENT);
    }

    @Test
    @DisplayName("피드 댓글 삭제 성공")
    void deleteFeedCommentSuccess() {
        // given & when
        feedCommentService.deleteFeedCommentId(1L);

        // then
        verify(feedReplyService).deleteAllByFeedCommentId(1L);
        verify(feedCommentMediaService).deleteAllByFeedCommentId(1L);
        verify(feedCommentLikeService).deleteAllByFeedCommentId(1L);
        verify(feedCommentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("피드의 모든 댓글 삭제 성공")
    void deleteFeedCommentByFeedSuccess() {
        // given
        when(feedCommentRepository.findAllByFeedId(1L)).thenReturn(List.of(testFeedComment));

        // when
        feedCommentService.deleteFeedCommentByFeed(1L);

        // then
        verify(feedReplyService).deleteAllByFeedCommentId(1L);
        verify(feedCommentMediaService).deleteAllByFeedCommentId(1L);
        verify(feedCommentLikeService).deleteAllByFeedCommentId(1L);
        verify(feedCommentRepository).deleteAll(List.of(testFeedComment));
    }

    @Test
    @DisplayName("피드 댓글 목록 조회 성공")
    void getFeedCommentsSuccess() {
        // given
        when(feedCommentRepository.findAllByFeedId(1L)).thenReturn(List.of(testFeedComment));
        when(feedCommentMediaService.getFeedCommentMediaIds(1L)).thenReturn(List.of());
        when(feedReplyService.countFeedRepliesByFeedCommentId(1L)).thenReturn(2);
        when(feedCommentLikeService.countFeedCommentLikeByFeedId(1L)).thenReturn(5);
        when(feedCommentLikeService.isLiked(1L, 1L)).thenReturn(true);
        // when
        ReflectionTestUtils.setField(testFeedComment, "createdAt", LocalDateTime.now());
        var result = feedCommentService.getFeedComments(1L, 1L);

        // then
        assertThat(result.getFeedComments()).hasSize(1);
        var comment = result.getFeedComments().get(0);
        assertThat(comment.getFeedCommentId()).isEqualTo(1L);
        assertThat(comment.getContent()).isEqualTo(TEST_CONTENT);
        assertThat(comment.getMemberId()).isEqualTo(1L);
        assertThat(comment.getMemberName()).isEqualTo(TEST_NICKNAME);
        assertThat(comment.getMediaIds()).isEmpty();
        assertThat(comment.getReplyCount()).isEqualTo(2);
        assertThat(comment.getLikeCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("피드 댓글 수 조회 성공")
    void countFeedCommentsByFeedIdSuccess() {
        // given
        when(feedCommentRepository.countByFeedId(1L)).thenReturn(5);

        // when
        int count = feedCommentService.countFeedCommentsByFeedId(1L);

        // then
        assertThat(count).isEqualTo(5);
    }
}