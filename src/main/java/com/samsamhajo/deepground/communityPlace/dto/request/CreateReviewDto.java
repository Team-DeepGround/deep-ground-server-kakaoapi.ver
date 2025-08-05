package com.samsamhajo.deepground.communityPlace.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateReviewDto {
    @DecimalMin(value = "1.0", message = "별점은 1.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "별점은 5.0 이하여야 합니다.")
    private double scope;
    @Size(min = 2, message = "리뷰 내용은 최소 2글자 이상 입력해야합니다.")
    private String content;
    private List<MultipartFile> images = new ArrayList<>();


}
