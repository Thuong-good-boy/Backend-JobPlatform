package com.jobplatform.job_recruitment_system.dtos.request;

import com.jobplatform.job_recruitment_system.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadRequest {
    private String fileUrl;
    private String fileName;

}
