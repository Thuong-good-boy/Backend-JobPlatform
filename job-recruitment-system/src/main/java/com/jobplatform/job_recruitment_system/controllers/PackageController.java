package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.Response.CandidateProResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.PackageCompany;
import com.jobplatform.job_recruitment_system.dtos.request.PackageRequest;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.models.Package;
import com.jobplatform.job_recruitment_system.services.PackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/package")
public class PackageController {
    private  final PackageService packageService;

    @GetMapping
    public  ResponseEntity<?> getAllPackage(){
        List<Package> list= packageService.getAllPackage();
        return ResponseEntity.ok(Map.of("data",list));
    }
    @GetMapping("/candidate")
    public ResponseEntity<?> getPackage(){
        List<CandidateProResponse> list = packageService.getCadidateProResponse();
        return ResponseEntity.ok(Map.of("data",list));
    }
    @GetMapping("/company")
    public  ResponseEntity<?> getpackageCompany(){
        List<PackageCompany> list = packageService.getpackageCompany();
        return ResponseEntity.ok(Map.of("data",list));
    }
    @PostMapping
    public  void createPackage(@Valid @RequestBody PackageRequest packageRequest){
        packageService.createPackage(packageRequest);
    }
    @PutMapping("/{id}")
    public  void updatePackage(@PathVariable("id") Long id, @Valid @RequestBody PackageRequest packageRequest){
        packageService.updatePackage(id,packageRequest);
    }
    @DeleteMapping("/{id}")
    public  void  delete(@PathVariable("id") Long id){
        packageService.deletePackage(id);
    }

}
