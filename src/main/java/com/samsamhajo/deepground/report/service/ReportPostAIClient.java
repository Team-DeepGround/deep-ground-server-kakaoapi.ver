package com.samsamhajo.deepground.report.service;


import com.samsamhajo.deepground.report.dto.AIReportResponse;
import com.samsamhajo.deepground.report.enums.ReportReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportPostAIClient {

    private final ChatClient reportPostClient;

    public AIReportResponse reviewFeed(ReportReason reportReason, String reportContent, String feedContent) {
        var converter = new BeanOutputConverter<>(AIReportResponse.class);
        String format = converter.getFormat();

        // 시스템 프롬프트 구성
        String system = """
                사용자 신고를 판단하는 AI 필터링 시스템입니다.
                다음 형식에 맞게 결과를 반환해야합니다.:
                %s
                """.formatted(format);

        // 사용자 입력 구성
        Map<String, Object> promptVars = Map.of(
                "reportReason", reportReason.name(),
                "reportContent", reportContent,
                "feedContent", feedContent
        );
        try {
            var result = reportPostClient.prompt()
                    .system(system)
                    .user(p -> p.text("""
                        다음은 사용자가 신고한 게시글 내용과 사유입니다.

                        - 신고 사유: {reportReason}
                        - 신고 상세 내용: {reportContent}
                        - 게시글 본문: {feedContent}

                        이 신고가 실제로 제재가 필요한지 판단하고, 이유와 신뢰도를 함께 알려주세요.
                        """).params(promptVars))
                    .call()
                    .entity(AIReportResponse.class);

            result.setResultByConfidence();
            log.info("AI 판단 결과: {}, 이유: [{}], 신뢰도: {}", result.getResult(), result.getReason(), result.getConfidence());
            return result;

        } catch (Exception e) {
            log.error("AI 리뷰 중 오류 발생: reportReason={}, error={}", reportReason, e.getMessage(), e);
            return AIReportResponse.createPendingResponse();
        }
    }
}
