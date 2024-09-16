package com.ispan.eeit188_final.controller;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ispan.eeit188_final.dto.HouseExternalResourceDTO;
import com.ispan.eeit188_final.model.HouseExternalResource;
import com.ispan.eeit188_final.service.HouseExternalResourceService;

@RestController
@RequestMapping("/house-external-resource")
@CrossOrigin
public class HouseExternalResourceController {

    @Autowired
    private HouseExternalResourceService houseExternalResourceService;

    // 上傳多張相片
    @PostMapping("/")
    public ResponseEntity<?> uploadMultipleFile(
            @RequestParam UUID houseId,
            @RequestParam(required = false) MultipartFile[] files) {
        if (houseId != null && files != null) {
            List<HouseExternalResource> createdResources;
            try {
                createdResources = houseExternalResourceService.uploadMultipleFile(houseId, files);
                if (!createdResources.isEmpty()) {
                    // 生成多個資源的 URI 列表
                    List<URI> locations = createdResources.stream()
                            .map(resource -> ServletUriComponentsBuilder.fromCurrentRequest()
                                    .path("/{id}")
                                    .buildAndExpand(resource.getId())
                                    .toUri())
                            .collect(Collectors.toList());

                    // 返回創建的資源列表和 URI 列表
                    return ResponseEntity.created(locations.get(0))
                            .body(Map.of(
                                    "resources", createdResources,
                                    "locations", locations));
                }
                HouseExternalResourceDTO error = HouseExternalResourceDTO.builder()
                        .message("新增失敗, 找不到房源")
                        .build();
                return ResponseEntity.badRequest().body(error); // Return 400 BadRequest
            } catch (IOException e) {
                HouseExternalResourceDTO error = HouseExternalResourceDTO.builder()
                        .message("新增失敗, 讀取資料錯誤: " + e.getMessage())
                        .build();
                return ResponseEntity.badRequest().body(error); // Return 400 BadRequest
            }
        }
        HouseExternalResourceDTO error = HouseExternalResourceDTO.builder().message("新增失敗").build();
        return ResponseEntity.badRequest().body(error); // Return 400 BadRequest
    }

    // 根據 HouseId 查詢所有資源
    @GetMapping("/house/{houseId}")
    public ResponseEntity<?> findByHouseId(@PathVariable UUID houseId, @ModelAttribute HouseExternalResourceDTO dto) {
        if (houseId != null) {
            Page<HouseExternalResource> result = houseExternalResourceService.findByHouseId(houseId, dto);
            return ResponseEntity.ok(result); // Return 400 BadRequest
        }
        HouseExternalResourceDTO error = HouseExternalResourceDTO.builder().message("查詢失敗").build();
        return ResponseEntity.badRequest().body(error); // Return 400 BadRequest
    }

    // 根據 ID 查詢 資源詳細資料
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        HouseExternalResource finded = houseExternalResourceService.findById(id);
        if (finded != null) {
            return ResponseEntity.ok(finded); // Return 200
        }
        return ResponseEntity.notFound().build(); // Return 404 NotFound
    }

    // 根據 ID 查詢 資源詳細資料
    @GetMapping("/image/{id}")
    public ResponseEntity<?> getImageById(@PathVariable UUID id) {
        HouseExternalResource finded = houseExternalResourceService.findById(id);
        if (finded != null) {
            byte[] imageData = finded.getImage();
            if (imageData != null && imageData.length > 0) {
                HttpHeaders headers = new HttpHeaders();
                
                // 設置 MIME 類型為 JPEG，根據需要可以動態調整
                String mimeType = finded.getType().equalsIgnoreCase("png") ? MediaType.IMAGE_PNG_VALUE : MediaType.IMAGE_JPEG_VALUE;
                // headers.setContentType(MediaType.parseMediaType(mimeType));
                headers.setContentType(MediaType.parseMediaType(finded.getType()));
                
                // 設置 Cache-Control，允許快取 30 天
                CacheControl cacheControl = CacheControl.maxAge(30, TimeUnit.DAYS).mustRevalidate();
                headers.setCacheControl(cacheControl);
                
                // 可選：設置 ETag 或 Last-Modified 來提高快取效率
                headers.setETag("\"" + finded.getId() + "\"");

                return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
            } else {
                // 若圖片數據不存在，返回 404
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        }
        return ResponseEntity.notFound().build(); // Return 404 NotFound
    }

    // 刪除資源 By Id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable UUID id) {
        if (id!= null) {
            Boolean result = houseExternalResourceService.deleteById(id);
            if ( result ) {
                return ResponseEntity.noContent().build(); // Return 201 No Content
            }
            return ResponseEntity.notFound().build(); // Return 404 NotFound
        }
        HouseExternalResourceDTO error = HouseExternalResourceDTO.builder().message("刪除失敗，缺少Id").build();
        return ResponseEntity.badRequest().body(error); // Return 400 BadRequest
    }

    // @PutMapping("/{id}")
    // public HouseExternalResource updateById(
    //         @RequestBody HouseExternalResourceDTO dto) {
    //     return houseExternalResourceService.modify(her);
    // }

    // 分頁查詢 ChatExternalResource
    @GetMapping("/all")
    public Page<HouseExternalResource> findAll(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return houseExternalResourceService.findAllHer(pageNumber, pageSize);
    }

}
