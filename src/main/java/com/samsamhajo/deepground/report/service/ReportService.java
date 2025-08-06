package com.samsamhajo.deepground.report.service;

import com.samsamhajo.deepground.feed.feed.entity.Feed;
import com.samsamhajo.deepground.feed.feed.exception.FeedErrorCode;
import com.samsamhajo.deepground.feed.feed.exception.FeedException;
import com.samsamhajo.deepground.feed.feed.repository.FeedRepository;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.exception.MemberErrorCode;
import com.samsamhajo.deepground.member.exception.MemberException;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import com.samsamhajo.deepground.report.dto.ReportRequest;
import com.samsamhajo.deepground.report.dto.ReportResponse;
import com.samsamhajo.deepground.report.entity.Report;
import com.samsamhajo.deepground.report.enums.AIReviewResult;
import com.samsamhajo.deepground.report.enums.ReportTargetType;
import com.samsamhajo.deepground.report.exception.ReportErrorCode;
import com.samsamhajo.deepground.report.exception.ReportException;
import com.samsamhajo.deepground.report.repository.ReportRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final StudyGroupMemberRepository studyGroupMemberRepository;
    private final ReportPostAIClient aiClient;

    public ReportResponse createReport(ReportRequest request, Long reporterId) {
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 중복 신고 검증
        boolean alreadyReported = reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, request.targetType(), request.targetId());

        if (alreadyReported) {
            throw new ReportException(ReportErrorCode.DUPLICATE_REPORT);
        }

        Long targetId = request.targetId();
        ReportTargetType targetType = request.targetType();

        Member reportedMember = null;
        boolean isAutoBanned = false;
        AIReviewResult result = AIReviewResult.PENDING;

        if (targetType == ReportTargetType.FEED) {
            Feed feed = feedRepository.findById(targetId)
                    .orElseThrow(() -> new FeedException(FeedErrorCode.FEED_NOT_FOUND));

            reportedMember = feed.getMember();

            var aiResult = aiClient.reviewFeed(request.reason(), request.content(), feed.getContent());
            isAutoBanned = aiResult.getResult() == AIReviewResult.ACCEPTED;
            result = aiResult.getResult();

            if (isAutoBanned) {
                feedRepository.delete(feed); // 자동 제재 적용
            }
        }

        if (targetType == ReportTargetType.MEMBER) {
            // 스터디 그룹 내 멤버인지 검증
            reportedMember = memberRepository.findById(targetId)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

            boolean isStudyGroupMember = studyGroupMemberRepository.existsById(targetId);
            if (!isStudyGroupMember) {
                throw new MemberException(MemberErrorCode.MEMBER_NOT_IN_STUDY_GROUP);
            }
        }

        Report report = Report.of(
                targetType,
                targetId,
                request.reason(),
                request.content(),
                isAutoBanned,
                reporter,
                reportedMember,
                result
        );

        reportRepository.save(report);

        return ReportResponse.from(report);
    }
}
