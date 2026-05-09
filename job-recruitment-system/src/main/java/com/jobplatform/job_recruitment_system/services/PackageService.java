package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.CandidateProResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.PackageCompany;
import com.jobplatform.job_recruitment_system.dtos.request.PackageRequest;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.PackageMapper;
import com.jobplatform.job_recruitment_system.models.Package;
import com.jobplatform.job_recruitment_system.repositories.PackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageService {
    private final PackageRepository packageRepository;
    private  final PackageMapper packageMapper;
    public List<CandidateProResponse> getCadidateProResponse(){
        return  packageRepository.getCadidateProResponse();
    }
    public  List<PackageCompany> getpackageCompany(){
        return packageRepository.getpackageCompany();
    }
    public  List<Package> getAllPackage(){
        return  packageRepository.findAll();
    }
    public  void createPackage(PackageRequest packageRequest){
        Package aPackage = packageMapper.fromRequest(packageRequest);
        packageRepository.save(aPackage);
    }
    public  void updatePackage(Long id,PackageRequest packageRequest){
        Package aPackage = packageRepository.findById(id).orElseThrow( ()-> new AppException(ErrorCode.PACKAGE_01));
        packageMapper.updatePackage(aPackage,packageRequest);
        packageRepository.save(aPackage);
    }
    public  void deletePackage(Long id){
        Package aPackage = packageRepository.findById(id).orElseThrow( ()-> new AppException(ErrorCode.PACKAGE_01));
        packageRepository.delete(aPackage);
    }
}
