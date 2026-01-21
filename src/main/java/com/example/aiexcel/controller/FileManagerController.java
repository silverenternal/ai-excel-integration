package com.example.aiexcel.controller;

import com.example.aiexcel.model.FileWorkspace;
import com.example.aiexcel.model.WorkspaceFile;
import com.example.aiexcel.service.FileManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文件管理控制器
 * 提供工作区和文件管理功能的API
 */
@RestController
@RequestMapping("/api/files")
public class FileManagerController {

    @Autowired
    private FileManagerService fileManagerService;

    private static final Logger logger = LoggerFactory.getLogger(FileManagerController.class);

    /**
     * 创建新工作区
     */
    @PostMapping("/workspace/create")
    public ResponseEntity<Map<String, Object>> createWorkspace(@RequestBody Map<String, Object> requestBody) {
        logger.info("Received request to create workspace");

        try {
            // 验证必需参数
            String name = (String) requestBody.get("name");
            String userId = (String) requestBody.get("userId");

            if (name == null || name.trim().isEmpty()) {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "Workspace name is required"
                );
                return ResponseEntity.badRequest().body(response);
            }

            if (userId == null || userId.trim().isEmpty()) {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "User ID is required"
                );
                return ResponseEntity.badRequest().body(response);
            }

            // 获取可选参数
            String description = (String) requestBody.get("description");
            Long parentWorkspaceId = requestBody.get("parentWorkspaceId") != null ? 
                                    ((Number) requestBody.get("parentWorkspaceId")).longValue() : null;

            // 创建工作区
            FileWorkspace workspace = fileManagerService.createWorkspace(name, description, userId, parentWorkspaceId);

            Map<String, Object> response = Map.of(
                "success", true,
                "data", workspace,
                "message", "Workspace created successfully"
            );

            logger.info("Successfully created workspace '{}'", name);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid workspace creation request: {}", e.getMessage());
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error creating workspace: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error creating workspace: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户的所有工作区
     */
    @GetMapping("/workspaces/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserWorkspaces(@PathVariable String userId) {
        logger.info("Received request to get workspaces for user: {}", userId);

        try {
            List<FileWorkspace> workspaces = fileManagerService.getUserWorkspaces(userId);

            Map<String, Object> response = Map.of(
                "success", true,
                "data", workspaces,
                "count", workspaces.size()
            );

            logger.info("Successfully returned {} workspaces for user: {}", workspaces.size(), userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving workspaces for user '{}': {}", userId, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error retrieving workspaces: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取工作区的详细信息
     */
    @GetMapping("/workspace/{id}")
    public ResponseEntity<Map<String, Object>> getWorkspace(@PathVariable Long id) {
        logger.info("Received request to get workspace with ID: {}", id);

        try {
            Optional<FileWorkspace> workspaceOpt = fileManagerService.getWorkspaceById(id);

            if (workspaceOpt.isPresent()) {
                Map<String, Object> response = Map.of(
                    "success", true,
                    "data", workspaceOpt.get()
                );
                logger.info("Successfully returned workspace with ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "Workspace not found"
                );
                logger.warn("Workspace with ID {} not found", id);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Error retrieving workspace with ID {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error retrieving workspace: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新工作区
     */
    @PutMapping("/workspace/{id}")
    public ResponseEntity<Map<String, Object>> updateWorkspace(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        logger.info("Received request to update workspace with ID: {}", id);

        try {
            // 验证必需参数
            String name = (String) requestBody.get("name");
            String userId = (String) requestBody.get("userId");

            if (name == null || name.trim().isEmpty()) {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "Workspace name is required"
                );
                return ResponseEntity.badRequest().body(response);
            }

            if (userId == null || userId.trim().isEmpty()) {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "User ID is required"
                );
                return ResponseEntity.badRequest().body(response);
            }

            // 获取可选参数
            String description = (String) requestBody.get("description");

            // 更新工作区
            FileWorkspace workspace = fileManagerService.updateWorkspace(id, name, description, userId);

            if (workspace != null) {
                Map<String, Object> response = Map.of(
                    "success", true,
                    "data", workspace,
                    "message", "Workspace updated successfully"
                );
                logger.info("Successfully updated workspace with ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "Workspace not found or unauthorized"
                );
                logger.warn("Failed to update workspace with ID: {} (not found or unauthorized)", id);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Error updating workspace with ID {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error updating workspace: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 设置工作区公开状态（仅工作区所有者可用）
     */
    @PostMapping("/workspace/{id}/set-public")
    public ResponseEntity<Map<String, Object>> setWorkspacePublic(
            @PathVariable Long id,
            @RequestParam String userId,
            @RequestParam(name = "isPublic", required = false, defaultValue = "true") boolean isPublic) {

        logger.info("Received request to set workspace ID:{} public={} by user: {}", id, isPublic, userId);

        try {
            boolean success = fileManagerService.setWorkspacePublic(id, isPublic, userId);
            if (success) {
                Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "Workspace public status updated"
                );
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "Workspace not found or unauthorized"
                );
                return ResponseEntity.status(403).body(response);
            }
        } catch (Exception e) {
            logger.error("Error setting workspace public status: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error updating workspace public status: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除工作区
     */
    @DeleteMapping("/workspace/{id}")
    public ResponseEntity<Map<String, Object>> deleteWorkspace(@PathVariable Long id, @RequestParam String userId) {
        logger.info("Received request to delete workspace with ID: {} for user: {}", id, userId);

        try {
            boolean success = fileManagerService.deleteWorkspace(id, userId);

            if (success) {
                Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "Workspace deleted successfully"
                );
                logger.info("Successfully deleted workspace with ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "Workspace not found or unauthorized"
                );
                logger.warn("Failed to delete workspace with ID: {} (not found or unauthorized)", id);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Error deleting workspace with ID {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error deleting workspace: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 在工作区中上传文件
     */
    @PostMapping("/workspace/{workspaceId}/upload")
    public ResponseEntity<Map<String, Object>> uploadFileToWorkspace(
            @PathVariable Long workspaceId,
            @RequestParam("file") MultipartFile file,
            @RequestParam String userId,
            @RequestParam(value = "description", required = false) String description) {
        logger.info("Received request to upload file '{}' to workspace ID: {}", file.getOriginalFilename(), workspaceId);

        try {
            // 验证文件
            if (file.isEmpty()) {
                logger.error("File is empty");
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "File is empty"
                );
                return ResponseEntity.badRequest().body(response);
            }

            // 先检查工作区是否存在以及权限，返回更明确的状态码
            java.util.Optional<com.example.aiexcel.model.FileWorkspace> wsOpt = fileManagerService.getWorkspaceById(workspaceId);
            if (wsOpt.isEmpty()) {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "Workspace not found"
                );
                logger.warn("上传失败（404） — 工作区 ID:{} 未找到，用户 '{}' 尝试上传文件 '{}'",
                    workspaceId, userId, file.getOriginalFilename());
                return ResponseEntity.status(404).body(response);
            }

            com.example.aiexcel.model.FileWorkspace ws = wsOpt.get();
            boolean isOwner = ws.getUserId() != null && ws.getUserId().equals(userId);
            boolean isPublic = ws.getIsPublic() != null && ws.getIsPublic();
            if (!isOwner && !isPublic) {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "User does not have permission to upload to this workspace"
                );
                logger.error("上传失败（403） — 用户 '{}' 尝试上传文件 '{}' 到工作区 ID:{}，但无权限。请确认 userId 或 将工作区设为公开。",
                    userId, file.getOriginalFilename(), workspaceId);
                return ResponseEntity.status(403).body(response);
            }

            // 上传文件到工作区
            WorkspaceFile workspaceFile = fileManagerService.uploadFileToWorkspace(file, workspaceId, userId, description);

            if (workspaceFile != null) {
                Map<String, Object> response = Map.of(
                    "success", true,
                    "data", workspaceFile,
                    "message", "File uploaded successfully"
                );
                logger.info("Successfully uploaded file '{}' to workspace ID: {}", file.getOriginalFilename(), workspaceId);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "Failed to upload file"
                );
                logger.error("Failed to upload file '{}' to workspace ID: {}", file.getOriginalFilename(), workspaceId);
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            logger.error("Error uploading file '{}' to workspace ID {}: {}", file.getOriginalFilename(), workspaceId, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error uploading file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取工作区中的所有文件
     */
    @GetMapping("/workspace/{workspaceId}/files")
    public ResponseEntity<Map<String, Object>> getWorkspaceFiles(@PathVariable Long workspaceId) {
        logger.info("Received request to get files from workspace ID: {}", workspaceId);

        try {
            List<WorkspaceFile> files = fileManagerService.getWorkspaceFiles(workspaceId);

            Map<String, Object> response = Map.of(
                "success", true,
                "data", files,
                "count", files.size()
            );

            logger.info("Successfully returned {} files from workspace ID: {}", files.size(), workspaceId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving files from workspace ID {}: {}", workspaceId, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error retrieving files: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取工作区中的Excel文件
     */
    @GetMapping("/workspace/{workspaceId}/excel-files")
    public ResponseEntity<Map<String, Object>> getExcelFilesInWorkspace(@PathVariable Long workspaceId) {
        logger.info("Received request to get Excel files from workspace ID: {}", workspaceId);

        try {
            List<WorkspaceFile> files = fileManagerService.getExcelFilesInWorkspace(workspaceId);

            Map<String, Object> response = Map.of(
                "success", true,
                "data", files,
                "count", files.size()
            );

            logger.info("Successfully returned {} Excel files from workspace ID: {}", files.size(), workspaceId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving Excel files from workspace ID {}: {}", workspaceId, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error retrieving Excel files: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 搜索工作区中的文件
     */
    @GetMapping("/workspace/{workspaceId}/search")
    public ResponseEntity<Map<String, Object>> searchFilesInWorkspace(
            @PathVariable Long workspaceId,
            @RequestParam String query) {
        logger.info("Received request to search files in workspace ID: {} with query: {}", workspaceId, query);

        try {
            List<WorkspaceFile> files = fileManagerService.searchFilesInWorkspace(workspaceId, query);

            Map<String, Object> response = Map.of(
                "success", true,
                "data", files,
                "count", files.size()
            );

            logger.info("Successfully found {} files matching '{}' in workspace ID: {}", files.size(), query, workspaceId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error searching files in workspace ID {} with query '{}': {}", workspaceId, query, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error searching files: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取工作区中收藏的文件
     */
    @GetMapping("/workspace/{workspaceId}/favorites")
    public ResponseEntity<Map<String, Object>> getFavoriteFilesInWorkspace(@PathVariable Long workspaceId) {
        logger.info("Received request to get favorite files from workspace ID: {}", workspaceId);

        try {
            List<WorkspaceFile> files = fileManagerService.getFavoriteFilesInWorkspace(workspaceId);

            Map<String, Object> response = Map.of(
                "success", true,
                "data", files,
                "count", files.size()
            );

            logger.info("Successfully returned {} favorite files from workspace ID: {}", files.size(), workspaceId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving favorite files from workspace ID {}: {}", workspaceId, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error retrieving favorite files: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取工作区中归档的文件
     */
    @GetMapping("/workspace/{workspaceId}/archived")
    public ResponseEntity<Map<String, Object>> getArchivedFilesInWorkspace(@PathVariable Long workspaceId) {
        logger.info("Received request to get archived files from workspace ID: {}", workspaceId);

        try {
            List<WorkspaceFile> files = fileManagerService.getArchivedFilesInWorkspace(workspaceId);

            Map<String, Object> response = Map.of(
                "success", true,
                "data", files,
                "count", files.size()
            );

            logger.info("Successfully returned {} archived files from workspace ID: {}", files.size(), workspaceId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving archived files from workspace ID {}: {}", workspaceId, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error retrieving archived files: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取工作区文件总数
     */
    @GetMapping("/workspace/{workspaceId}/count")
    public ResponseEntity<Map<String, Object>> getWorkspaceFileCount(@PathVariable Long workspaceId) {
        logger.info("Received request to get file count for workspace ID: {}", workspaceId);

        try {
            int count = fileManagerService.getWorkspaceFileCount(workspaceId);

            Map<String, Object> response = Map.of(
                "success", true,
                "count", count
            );

            logger.info("Workspace ID {} has {} files", workspaceId, count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error counting files in workspace ID {}: {}", workspaceId, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error counting files: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 设置文件收藏状态
     */
    @PostMapping("/file/{fileId}/favorite")
    public ResponseEntity<Map<String, Object>> setFavoriteStatus(
            @PathVariable Long fileId,
            @RequestParam boolean isFavorite,
            @RequestParam String userId) {
        logger.info("Received request to set favorite status to {} for file ID: {} for user: {}", isFavorite, fileId, userId);

        try {
            boolean success = fileManagerService.setFavoriteStatus(fileId, isFavorite, userId);

            if (success) {
                Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "Favorite status updated successfully"
                );
                logger.info("Successfully updated favorite status for file ID: {}", fileId);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "File not found or unauthorized"
                );
                logger.warn("Failed to update favorite status for file ID: {} (not found or unauthorized)", fileId);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Error updating favorite status for file ID {}: {}", fileId, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error updating favorite status: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 设置文件归档状态
     */
    @PostMapping("/file/{fileId}/archive")
    public ResponseEntity<Map<String, Object>> setArchivedStatus(
            @PathVariable Long fileId,
            @RequestParam boolean isArchived,
            @RequestParam String userId) {
        logger.info("Received request to set archived status to {} for file ID: {} for user: {}", isArchived, fileId, userId);

        try {
            boolean success = fileManagerService.setArchivedStatus(fileId, isArchived, userId);

            if (success) {
                Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "Archived status updated successfully"
                );
                logger.info("Successfully updated archived status for file ID: {}", fileId);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = Map.of(
                    "success", false,
                    "error", "File not found or unauthorized"
                );
                logger.warn("Failed to update archived status for file ID: {} (not found or unauthorized)", fileId);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Error updating archived status for file ID {}: {}", fileId, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error updating archived status: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}