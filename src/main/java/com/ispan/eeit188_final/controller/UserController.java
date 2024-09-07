package com.ispan.eeit188_final.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.ispan.eeit188_final.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 用ID查尋特定用戶
    @GetMapping("/{id}")
    public ResponseEntity<String> getUserById(@PathVariable UUID id) {

        return userService.findById(id);
    }

    // 批量查尋用戶
    @GetMapping("/")
    public ResponseEntity<String> getUsers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {

        return userService.getUsers(pageNo, pageSize);
    }

    // 創建新用戶
    @PostMapping("/")
    public ResponseEntity<String> createUser(
            @RequestBody String jsonRequest) {

        return userService.createUser(jsonRequest);
    }

    // 登入功能
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String jsonRequest) throws JSONException {

        return userService.login(jsonRequest);
    }

    // 刪除用戶
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {

        return userService.deleteById(id);
    }

    // 更新用戶資訊
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable UUID id,
            @RequestBody String jsonRequest) {

        return userService.update(id, jsonRequest);
    }

    // 密碼驗證（更新密碼前的驗證機制）
    @PostMapping("/{id}/check-password")
    public ResponseEntity<String> checkPassword(
            @PathVariable UUID id,
            @RequestBody String jsonRequest) {

        return userService.checkPassword(id, jsonRequest);
    }

    // 忘記密碼（發送密碼更新連結到指定email）(未完成)
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody String jsonRequest) {

        return userService.forgotPassword(jsonRequest);
    }

    // 設定新密碼
    @PutMapping("/{id}/set-new-password")
    public ResponseEntity<String> setNewPassword(
            @PathVariable UUID id,
            @RequestBody String jsonRequest) {

        return userService.setNewPassword(id, jsonRequest);
    }

    // 上傳大頭貼圖片（base64格式）
    @PutMapping("/{id}/upload-avatar")
    public ResponseEntity<String> uploadAvater(@PathVariable UUID id,
            @RequestBody String jsonRequest) {

        return userService.uploadAvater(id, jsonRequest);
    }

    // 上傳個人主頁背景圖片（byte[]）
    @PutMapping("/{id}/upload-background-image")
    public ResponseEntity<String> uploadBackgroundImageBlob(@PathVariable UUID id,
            @RequestParam("backgroundImage") MultipartFile backgroundImage) throws IOException {

        return userService.uploadBackgroundImage(id, backgroundImage);
    }

    // 下載個人主頁背景圖片（byte[]）
    @GetMapping("/{id}/download-background-image")
    public ResponseEntity<Resource> downloadBackgroundImage(@PathVariable UUID id) {

        return userService.getBackgroundImage(id);
    }

    // 移除大頭貼圖片
    @PutMapping("/{id}/remove-avatar")
    public ResponseEntity<String> removeAvatar(@PathVariable UUID id) {

        return userService.deleteAvatar(id);
    }

    // 移除個人主頁背景圖片
    @PutMapping("/{id}/remove-background-image")
    public ResponseEntity<String> removeBackgroundImage(@PathVariable UUID id) {

        return userService.deleteBackgroundImage(id);
    }
}
