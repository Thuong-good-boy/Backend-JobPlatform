package com.jobplatform.job_recruitment_system.dtos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicationDto {
    @JsonProperty("type")
    private String type;      // Loại xuất bản (VD: "Book", "Journal Article")

    @JsonProperty("icon")
    private String icon;      // Icon (VD: "\\faBook", "\\faFile*[regular]")

    @JsonProperty("title")
    private String title;     // Tên bài báo/sách

    @JsonProperty("authors")
    private String authors;   // Nhóm tác giả

    @JsonProperty("year")
    private String year;      // Năm xuất bản

    @JsonProperty("publisher")
    private String publisher; // Nơi xuất bản / Tạp chí
}