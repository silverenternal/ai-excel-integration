package com.example.aiexcel.service;

import com.example.aiexcel.model.FileWorkspace;
import com.example.aiexcel.model.WorkspaceFile;
import com.example.aiexcel.repository.WorkspaceFileRepository;
import com.example.aiexcel.repository.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文件管理服务
 * 提供工作区和文件的管理功能
 */
@Service
public class FileManagerService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceFileRepository workspaceFileRepository;

    private static final Logger logger = LoggerFactory.getLogger(FileManagerService.class);

    /**
     * 创建新工作区
     */
    public FileWorkspace createWorkspace(String name, String description, String userId, Long parentWorkspaceId) {
        try {
            // 检查是否已存在同名工作区
            if (workspaceRepository.existsByUserIdAndName(userId, name)) {
                logger.warn("Workspace with name '{}' already exists for user '{}'", name, userId);
                throw new IllegalArgumentException("Workspace with name '" + name + "' already exists");
            }

            FileWorkspace workspace = new FileWorkspace(name, description, userId, parentWorkspaceId);
            workspace = workspaceRepository.save(workspace);

            logger.info("Created workspace '{}' for user '{}'", name, userId);
            return workspace;
        } catch (Exception e) {
            logger.error("Error creating workspace for user '{}': {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取用户的所有工作区
     */
    public List<FileWorkspace> getUserWorkspaces(String userId) {
        try {
            List<FileWorkspace> workspaces = workspaceRepository.findByUserId(userId);
            logger.debug("Retrieved {} workspaces for user '{}'", workspaces.size(), userId);
            return workspaces;
        } catch (Exception e) {
            logger.error("Error retrieving workspaces for user '{}': {}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取工作区的子工作区
     */
    public List<FileWorkspace> getChildWorkspaces(Long parentWorkspaceId) {
        try {
            List<FileWorkspace> workspaces = workspaceRepository.findByParentWorkspaceId(parentWorkspaceId);
            logger.debug("Retrieved {} child workspaces for parent workspace ID: {}", workspaces.size(), parentWorkspaceId);
            return workspaces;
        } catch (Exception e) {
            logger.error("Error retrieving child workspaces for parent workspace ID {}: {}", parentWorkspaceId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取特定工作区
     */
    public Optional<FileWorkspace> getWorkspaceById(Long id) {
        try {
            Optional<FileWorkspace> workspace = workspaceRepository.findById(id);
            if (workspace.isPresent()) {
                logger.debug("Retrieved workspace with ID: {}", id);
            } else {
                logger.debug("Workspace with ID {} not found", id);
            }
            return workspace;
        } catch (Exception e) {
            logger.error("Error retrieving workspace with ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 更新工作区
     */
    public FileWorkspace updateWorkspace(Long id, String name, String description, String userId) {
        try {
            Optional<FileWorkspace> existingWorkspaceOpt = workspaceRepository.findById(id);
            if (existingWorkspaceOpt.isEmpty()) {
                logger.error("Workspace with ID {} not found for update", id);
                return null;
            }

            FileWorkspace existingWorkspace = existingWorkspaceOpt.get();
            
            // 检查是否是工作区所有者
            if (!existingWorkspace.getUserId().equals(userId)) {
                logger.error("User '{}' does not have permission to update workspace with ID {}", userId, id);
                return null;
            }

            // 检查是否已存在同名工作区（排除当前工作区）
            if (!existingWorkspace.getName().equals(name) && 
                workspaceRepository.existsByUserIdAndName(userId, name)) {
                logger.warn("Workspace with name '{}' already exists for user '{}'", name, userId);
                return null;
            }

            existingWorkspace.setName(name);
            existingWorkspace.setDescription(description);
            existingWorkspace.setUpdatedAt(LocalDateTime.now());

            FileWorkspace updatedWorkspace = workspaceRepository.save(existingWorkspace);
            logger.info("Updated workspace with ID: {}", id);
            return updatedWorkspace;
        } catch (Exception e) {
            logger.error("Error updating workspace with ID {}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除工作区
     */
    public boolean deleteWorkspace(Long id, String userId) {
        try {
            Optional<FileWorkspace> workspaceOpt = workspaceRepository.findById(id);
            if (workspaceOpt.isEmpty()) {
                logger.error("Workspace with ID {} not found for deletion", id);
                return false;
            }

            FileWorkspace workspace = workspaceOpt.get();
            
            // 检查是否是工作区所有者
            if (!workspace.getUserId().equals(userId)) {
                logger.error("User '{}' does not have permission to delete workspace with ID {}", userId, id);
                return false;
            }

            // 先删除工作区中的所有文件
            List<WorkspaceFile> files = workspaceFileRepository.findByWorkspaceId(id);
            workspaceFileRepository.deleteAll(files);

            // 再删除子工作区
            List<FileWorkspace> childWorkspaces = workspaceRepository.findByParentWorkspaceId(id);
            for (FileWorkspace child : childWorkspaces) {
                deleteWorkspace(child.getId(), userId); // 递归删除
            }

            // 最后删除工作区本身
            workspaceRepository.delete(workspace);
            logger.info("Deleted workspace with ID: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting workspace with ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 在工作区中上传文件
     */
    public WorkspaceFile uploadFileToWorkspace(MultipartFile file, Long workspaceId, String userId, String description) {
        try {
            // 验证工作区是否存在
            Optional<FileWorkspace> workspaceOpt = workspaceRepository.findById(workspaceId);
            if (workspaceOpt.isEmpty()) {
                logger.error("Workspace with ID {} not found", workspaceId);
                return null;
            }

            FileWorkspace workspace = workspaceOpt.get();
            // 验证用户权限（简化：检查是否为工作区所有者或工作区为公开）
            boolean isOwner = workspace.getUserId() != null && workspace.getUserId().equals(userId);
            boolean isPublic = workspace.getIsPublic() != null && workspace.getIsPublic();
            if (!isOwner && !isPublic) {
                logger.error("上传被拒绝 — 用户 '{}' 无权限上传文件 '{}' 到工作区 ID:{}。工作区所有者='{}'，isPublic='{}'",
                    userId, file.getOriginalFilename(), workspaceId, workspace.getUserId(), workspace.getIsPublic());
                return null;
            }

            // 创建工作区文件记录
            WorkspaceFile workspaceFile = new WorkspaceFile(
                file.getOriginalFilename(),
                "", // 可以设置为实际存储路径
                file.getSize(),
                getFileType(file.getOriginalFilename()),
                workspaceId,
                userId,
                description
            );

            workspaceFile = workspaceFileRepository.save(workspaceFile);

            logger.info("Uploaded file '{}' to workspace ID: {}", file.getOriginalFilename(), workspaceId);
            return workspaceFile;
        } catch (Exception e) {
            logger.error("Error uploading file to workspace ID {}: {}", workspaceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取工作区中的所有文件
     */
    public List<WorkspaceFile> getWorkspaceFiles(Long workspaceId) {
        try {
            List<WorkspaceFile> files = workspaceFileRepository.findByWorkspaceId(workspaceId);
            logger.debug("Retrieved {} files from workspace ID: {}", files.size(), workspaceId);
            return files;
        } catch (Exception e) {
            logger.error("Error retrieving files from workspace ID {}: {}", workspaceId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取工作区中的Excel文件
     */
    public List<WorkspaceFile> getExcelFilesInWorkspace(Long workspaceId) {
        try {
            List<WorkspaceFile> files = workspaceFileRepository.findByWorkspaceIdAndFileType(workspaceId, "excel");
            logger.debug("Retrieved {} Excel files from workspace ID: {}", files.size(), workspaceId);
            return files;
        } catch (Exception e) {
            logger.error("Error retrieving Excel files from workspace ID {}: {}", workspaceId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 搜索工作区中的文件
     */
    public List<WorkspaceFile> searchFilesInWorkspace(Long workspaceId, String searchTerm) {
        try {
            List<WorkspaceFile> files = workspaceFileRepository.findByWorkspaceIdAndFileNameContainingIgnoreCase(workspaceId, searchTerm);
            logger.debug("Found {} files matching '{}' in workspace ID: {}", files.size(), searchTerm, workspaceId);
            return files;
        } catch (Exception e) {
            logger.error("Error searching files in workspace ID {}: {}", workspaceId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取工作区中收藏的文件
     */
    public List<WorkspaceFile> getFavoriteFilesInWorkspace(Long workspaceId) {
        try {
            List<WorkspaceFile> files = workspaceFileRepository.findByWorkspaceIdAndIsFavoriteTrue(workspaceId);
            logger.debug("Retrieved {} favorite files from workspace ID: {}", files.size(), workspaceId);
            return files;
        } catch (Exception e) {
            logger.error("Error retrieving favorite files from workspace ID {}: {}", workspaceId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取工作区中归档的文件
     */
    public List<WorkspaceFile> getArchivedFilesInWorkspace(Long workspaceId) {
        try {
            List<WorkspaceFile> files = workspaceFileRepository.findByWorkspaceIdAndIsArchivedTrue(workspaceId);
            logger.debug("Retrieved {} archived files from workspace ID: {}", files.size(), workspaceId);
            return files;
        } catch (Exception e) {
            logger.error("Error retrieving archived files from workspace ID {}: {}", workspaceId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取工作区文件总数
     */
    public int getWorkspaceFileCount(Long workspaceId) {
        try {
            int count = workspaceFileRepository.countByWorkspaceId(workspaceId);
            logger.debug("Workspace ID {} has {} files", workspaceId, count);
            return count;
        } catch (Exception e) {
            logger.error("Error counting files in workspace ID {}: {}", workspaceId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 设置工作区公开状态（仅限所有者）
     */
    public boolean setWorkspacePublic(Long id, boolean isPublic, String userId) {
        try {
            Optional<FileWorkspace> workspaceOpt = workspaceRepository.findById(id);
            if (workspaceOpt.isEmpty()) {
                logger.error("Workspace with ID {} not found when setting public", id);
                return false;
            }

            FileWorkspace workspace = workspaceOpt.get();
            // 只有工作区所有者可以修改公开状态
            if (workspace.getUserId() == null || !workspace.getUserId().equals(userId)) {
                logger.error("用户 '{}' 无权限修改工作区 ID {} 的公开状态", userId, id);
                return false;
            }

            workspace.setIsPublic(isPublic);
            workspace.setUpdatedAt(LocalDateTime.now());
            workspaceRepository.save(workspace);

            logger.info("用户 '{}' 将工作区 ID {} 的 isPublic 设置为 {}", userId, id, isPublic);
            return true;
        } catch (Exception e) {
            logger.error("Error setting workspace public status for ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置文件收藏状态
     */
    public boolean setFavoriteStatus(Long fileId, boolean isFavorite, String userId) {
        try {
            Optional<WorkspaceFile> fileOpt = workspaceFileRepository.findById(fileId);
            if (fileOpt.isEmpty()) {
                logger.error("File with ID {} not found", fileId);
                return false;
            }

            WorkspaceFile file = fileOpt.get();
            
            // 验证用户权限
            if (!file.getUploadedBy().equals(userId)) {
                logger.error("User '{}' does not have permission to update file with ID {}", userId, fileId);
                return false;
            }

            file.setIsFavorite(isFavorite);
            file.setUpdatedAt(LocalDateTime.now());
            workspaceFileRepository.save(file);

            logger.info("Set favorite status to {} for file ID: {}", isFavorite, fileId);
            return true;
        } catch (Exception e) {
            logger.error("Error setting favorite status for file ID {}: {}", fileId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置文件归档状态
     */
    public boolean setArchivedStatus(Long fileId, boolean isArchived, String userId) {
        try {
            Optional<WorkspaceFile> fileOpt = workspaceFileRepository.findById(fileId);
            if (fileOpt.isEmpty()) {
                logger.error("File with ID {} not found", fileId);
                return false;
            }

            WorkspaceFile file = fileOpt.get();
            
            // 验证用户权限
            if (!file.getUploadedBy().equals(userId)) {
                logger.error("User '{}' does not have permission to update file with ID {}", userId, fileId);
                return false;
            }

            file.setIsArchived(isArchived);
            file.setUpdatedAt(LocalDateTime.now());
            workspaceFileRepository.save(file);

            logger.info("Set archived status to {} for file ID: {}", isArchived, fileId);
            return true;
        } catch (Exception e) {
            logger.error("Error setting archived status for file ID {}: {}", fileId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null) return "unknown";
        
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
            return "excel";
        } else if (lowerName.endsWith(".csv")) {
            return "csv";
        } else if (lowerName.endsWith(".pdf")) {
            return "pdf";
        } else if (lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) {
            return "document";
        } else {
            return "other";
        }
    }
}