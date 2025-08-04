package com.samsamhajo.deepground.feed.feedreply.service;

import com.samsamhajo.deepground.feed.feedcomment.entity.FeedComment;
import com.samsamhajo.deepground.feed.feedcomment.exception.FeedCommentErrorCode;
import com.samsamhajo.deepground.feed.feedcomment.exception.FeedCommentException;
import com.samsamhajo.deepground.feed.feedcomment.repository.FeedCommentRepository;
import com.samsamhajo.deepground.feed.feedreply.entity.FeedReply;
import com.samsamhajo.deepground.feed.feedreply.exception.FeedReplyErrorCode;
import com.samsamhajo.deepground.feed.feedreply.exception.FeedReplyException;
import com.samsamhajo.deepground.feed.feedreply.model.FeedReplyCreateRequest;
import com.samsamhajo.deepground.feed.feedreply.model.FeedReplyUpdateRequest;
import com.samsamhajo.deepground.feed.feedreply.repository.FeedReplyRepository;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.exception.MemberErrorCode;
import com.samsamhajo.deepground.member.exception.MemberException;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedReplyServiceTest {

    @Mock
    private FeedCommentRepository feedCommentRepository;
    @Mock
    private FeedReplyRepository feedReplyRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private FeedReplyMediaService feedReplyMediaService;
    @Mock
    private FeedReplyLikeService feedReplyLikeService;

    @InjectMocks
    private FeedReplyService feedReplyService;

    private static final String TEST_CONTENT = "테스트 답글 내용입니다.";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "테스트유저";

    private Member testMember;
    private FeedComment testFeedComment;
    private FeedReply testFeedReply;
    private MockMultipartFile testImage;

    @BeforeEach
    void setUp() {
        testMember = Member.createLocalMember(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
        testFeedComment = FeedComment.of(TEST_CONTENT, null, testMember);
        testFeedReply = FeedReply.of(TEST_CONTENT, testFeedComment, testMember);
        testImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        ReflectionTestUtils.setField(testMember, "id", 1L);
        ReflectionTestUtils.setField(testFeedComment, "id", 1L);
        ReflectionTestUtils.setField(testFeedReply, "id", 1L);
    }

    @Test
    @DisplayName("피드 답글 생성 성공")
    void createFeedReplySuccess() {
        // given
        FeedReplyCreateRequest request = new FeedReplyCreateRequest(1L, TEST_CONTENT, List.of(testImage));

        when(feedCommentRepository.getById(1L)).thenReturn(testFeedComment);
        when(feedReplyRepository.save(any(FeedReply.class))).thenReturn(testFeedReply);

        // when
        FeedComment result = feedReplyService.createFeedReply(request, testMember);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(feedReplyMediaService).createFeedReplyMedia(any(FeedReply.class), anyList());
    }

    @Test
    @DisplayName("피드 답글 생성 실패 - 내용이 비어있는 경우")
    void createFeedReplyFailWithEmptyContent() {
        // given
        FeedReplyCreateRequest request = new FeedReplyCreateRequest(1L, "", List.of());

        // when & then
        assertThatThrownBy(() -> feedReplyService.createFeedReply(request, testMember))
                .isInstanceOf(FeedCommentException.class)
                .hasFieldOrPropertyWithValue("errorCode", FeedCommentErrorCode.INVALID_FEED_COMMENT_CONTENT);
    }

    @Test
    @DisplayName("피드 답글 수정 성공")
    void updateFeedReplySuccess() {
        // given
        String updatedContent = "수정된 답글 내용입니다.";
        FeedReplyUpdateRequest request = new FeedReplyUpdateRequest(updatedContent, List.of(testImage));

        when(feedReplyRepository.getById(1L)).thenReturn(testFeedReply);

        // when
        FeedReply updatedReply = feedReplyService.updateFeedReply(1L, request);

        // then
        assertThat(updatedReply.getContent()).isEqualTo(updatedContent);
        verify(feedReplyMediaService).updateFeedReplyMedia(any(FeedReply.class), anyList());
    }

    @Test
    @DisplayName("피드 답글 수정 실패 - 내용이 비어있는 경우")
    void updateFeedReplyFailWithEmptyContent() {
        // given
        FeedReplyUpdateRequest request = new FeedReplyUpdateRequest("", List.of());

        // when & then
        assertThatThrownBy(() -> feedReplyService.updateFeedReply(1L, request))
                .isInstanceOf(FeedReplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", FeedReplyErrorCode.INVALID_FEED_REPLY_CONTENT);
    }

    @Test
    @DisplayName("피드 답글 삭제 성공")
    void deleteFeedReplySuccess() {
        // when
        feedReplyService.deleteFeedReplyId(1L);

        // then
        verify(feedReplyMediaService).deleteAllByFeedReplyId(1L);
        verify(feedReplyLikeService).deleteAllByFeedReplyId(1L);
        verify(feedReplyRepository).deleteById(1L);
    }

    @Test
    @DisplayName("피드 댓글의 모든 답글 삭제 성공")
    void deleteAllByFeedCommentIdSuccess() {
        // given
        when(feedReplyRepository.findAllByFeedCommentId(1L)).thenReturn(List.of(testFeedReply));

        // when
        feedReplyService.deleteAllByFeedCommentId(1L);

        // then
        verify(feedReplyMediaService).deleteAllByFeedReplyId(1L);
        verify(feedReplyLikeService).deleteAllByFeedReplyId(1L);
        verify(feedReplyRepository).deleteAll(List.of(testFeedReply));
    }

    @Test
    @DisplayName("피드 답글 수 조회 성공")
    void countFeedRepliesByFeedCommentIdSuccess() {
        // given
        when(feedReplyRepository.countByFeedCommentId(1L)).thenReturn(5);

        // when
        int count = feedReplyService.countFeedRepliesByFeedCommentId(1L);

        // then
        assertThat(count).isEqualTo(5);
    }

    @Test
    @DisplayName("피드 답글 목록 조회 성공")
    void getFeedRepliesSuccess() {
        // given
        ReflectionTestUtils.setField(testFeedReply, "createdAt", LocalDateTime.now());
        when(feedReplyRepository.findAllByFeedCommentId(1L)).thenReturn(List.of(testFeedReply));
        when(feedReplyMediaService.getFeedReplyMediaIds(1L)).thenReturn(List.of());
        when(feedReplyLikeService.countFeedReplyLikeByFeedReplyId(1L)).thenReturn(3);
        when(feedReplyLikeService.isLiked(1L, 1L)).thenReturn(true);
        ReflectionTestUtils.setField(testFeedReply, "createdAt", LocalDateTime.now());

        // when
        var result = feedReplyService.getFeedReplies(1L, 1L);

        // then
        assertThat(result.getFeedReplies()).hasSize(1);
        var reply = result.getFeedReplies().get(0);
        assertThat(reply.getFeedReplyId()).isEqualTo(1L);
        assertThat(reply.getContent()).isEqualTo(TEST_CONTENT);
        assertThat(reply.getMemberId()).isEqualTo(1L);
        assertThat(reply.getMemberName()).isEqualTo(TEST_NICKNAME);
        assertThat(reply.getMediaIds()).isEmpty();
        assertThat(reply.getLikeCount()).isEqualTo(3);
    }
} 