package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.repositories.ReportEvidenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportEvidenceService {
    private  final ReportEvidenceRepository repository;

}
