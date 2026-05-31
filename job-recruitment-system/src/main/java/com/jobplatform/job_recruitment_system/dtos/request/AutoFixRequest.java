package com.jobplatform.job_recruitment_system.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobplatform.job_recruitment_system.dtos.dto.FeedbackDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoFixRequest {
    @JsonProperty("cvId")
    private Long cvId;
    @NotBlank(message = "Vui lòng chọn một mẫu CV (Template Key)")
    private String templateKey;
    @JsonProperty("feedback")
    private FeedbackDto feedback;

}
