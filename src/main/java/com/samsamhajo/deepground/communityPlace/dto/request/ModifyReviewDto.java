package com.samsamhajo.deepground.communityPlace.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ModifyReviewDto {

    private Long communityPlaceReviewId;
    private Long memberId;
    private String content;
    private double scope;
    private Long specificAddressId;
    private List<MultipartFile> images = new ArrayList<>();



}