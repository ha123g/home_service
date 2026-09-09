package com.example.home_service_backend.controller;

import com.example.home_service_backend.common.constants.ApiConstants;
import com.example.home_service_backend.common.enums.AuthRoleEnum;
import com.example.home_service_backend.common.enums.FileUploadPurpose;
import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.common.result.ResultData;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.common.utils.SecurityUtils;
import com.example.home_service_backend.security.LoginUser;
import com.example.home_service_backend.service.CosObjectKeyPolicy;
import com.example.home_service_backend.service.CosObjectStorageService;
import com.example.home_service_backend.vo.file.FileUploadView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequestMapping(ApiConstants.FILES_PREFIX)
@Validated
public class FileController {
    private final CosObjectStorageService storageService;

    public FileController(CosObjectStorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * 受控上传图片到腾讯云 COS，返回 Object Key 与后端预览地址。
     */
    @PostMapping
    public ResponseEntity<ResultData<FileUploadView>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("purpose") String purpose) {
        FileUploadPurpose parsed;
        try {
            parsed = FileUploadPurpose.fromParam(purpose);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("400", "上传用途不合法");
        }
        LoginUser login = SecurityUtils.requireLoginUser();
        FileUploadView view = storageService.upload(file, parsed, login.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ResultFactory.buildSuccessData(view));
    }

    /**
     * 将 Object Key 重定向到短期签名 URL，供 &lt;img&gt; 同源加载。
     */
    @GetMapping("/preview")
    public ResponseEntity<Void> preview(@RequestParam @NotBlank @Size(max = 512) String objectKey) {
        LoginUser login = SecurityUtils.requireLoginUser();
        CosObjectKeyPolicy.ParsedKey parsed = storageService.requireReadable(
                objectKey, login.getId(), isFileAdministrator(login));
        String signed = storageService.signedGetUrl(parsed.objectKey());
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(signed)).build();
    }

    private boolean isFileAdministrator(LoginUser login) {
        return login.getAuthorities().stream().anyMatch(item -> {
            String authority = item.getAuthority();
            return ("ROLE_" + AuthRoleEnum.USERNAME_AUD_ADMIN.getValue()).equals(authority)
                    || ("ROLE_" + AuthRoleEnum.USERNAME_SYS_ADMIN.getValue()).equals(authority)
                    || ("ROLE_" + AuthRoleEnum.USERNAME_SUPER_ADMIN.getValue()).equals(authority)
                    || ("ROLE_" + AuthRoleEnum.USERNAME_SEC_ADMIN.getValue()).equals(authority);
        });
    }
}
