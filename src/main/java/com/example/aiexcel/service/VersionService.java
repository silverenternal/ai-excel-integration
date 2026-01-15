package com.example.aiexcel.service;

import com.example.aiexcel.model.FileVersion;
import com.example.aiexcel.repository.VersionRepository;
import com.example.aiexcel.service.excel.ExcelService;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * 文件版本控制服务
 * 提供文件版本管理功能
 */
@Service
public class VersionService {

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private ExcelService excelService;

    private static final Logger logger = LoggerFactory.getLogger(VersionService.class);

    /**
     * 创建新版本
     */
    public FileVersion createVersion(String fileId, MultipartFile file, String changeDescription, String userId) {
        try {
            // 获取当前最大版本号
            Integer maxVersion = versionRepository.findMaxVersionNumberByFileId(fileId);
            Integer newVersionNumber = (maxVersion != null) ? maxVersion + 1 : 1;

            // 编码文件内容
            String encodedContent = encodeFileContent(file);

            // 创建版本记录
            FileVersion version = new FileVersion(
                fileId, 
                newVersionNumber, 
                file.getOriginalFilename(), 
                encodedContent, 
                file.getSize(), 
                changeDescription, 
                userId
            );

            // 如果这是第一个版本，标记为当前版本
            if (newVersionNumber == 1) {
                version.setIsCurrent(true);
            }

            version = versionRepository.save(version);

            logger.info("Created version {} for file: {}", newVersionNumber, fileId);
            return version;
        } catch (Exception e) {
            logger.error("Error creating version for file: {}", fileId, e);
            return null;
        }
    }

    /**
     * 创建新版本（通过Workbook对象）
     */
    public FileVersion createVersionFromWorkbook(String fileId, Workbook workbook, String changeDescription, String userId, String fileName) {
        try {
            // 获取当前最大版本号
            Integer maxVersion = versionRepository.findMaxVersionNumberByFileId(fileId);
            Integer newVersionNumber = (maxVersion != null) ? maxVersion + 1 : 1;

            // 编码工作簿内容
            String encodedContent = encodeWorkbookContent(workbook);

            // 获取文件大小（字节数组长度）
            byte[] contentBytes = Base64.getDecoder().decode(encodedContent);
            Long fileSize = (long) contentBytes.length;

            // 创建版本记录
            FileVersion version = new FileVersion(
                fileId, 
                newVersionNumber, 
                fileName, 
                encodedContent, 
                fileSize, 
                changeDescription, 
                userId
            );

            // 如果这是第一个版本，标记为当前版本
            if (newVersionNumber == 1) {
                version.setIsCurrent(true);
            }

            version = versionRepository.save(version);

            logger.info("Created version {} for file: {} from workbook", newVersionNumber, fileId);
            return version;
        } catch (Exception e) {
            logger.error("Error creating version from workbook for file: {}", fileId, e);
            return null;
        }
    }

    /**
     * 获取文件的所有版本列表
     */
    public List<FileVersion> getFileVersions(String fileId) {
        try {
            List<FileVersion> versions = versionRepository.findByFileIdOrderByVersionNumberDesc(fileId);
            logger.debug("Retrieved {} versions for file: {}", versions.size(), fileId);
            return versions;
        } catch (Exception e) {
            logger.error("Error retrieving versions for file: {}", fileId, e);
            return List.of();
        }
    }

    /**
     * 获取文件的最新版本
     */
    public FileVersion getLatestVersion(String fileId) {
        try {
            FileVersion version = versionRepository.findTopByFileIdOrderByVersionNumberDesc(fileId);
            logger.debug("Retrieved latest version for file: {}", fileId);
            return version;
        } catch (Exception e) {
            logger.error("Error retrieving latest version for file: {}", fileId, e);
            return null;
        }
    }

    /**
     * 获取特定版本
     */
    public FileVersion getVersion(String fileId, Integer versionNumber) {
        try {
            FileVersion version = versionRepository.findByFileIdAndVersionNumber(fileId, versionNumber);
            logger.debug("Retrieved version {} for file: {}", versionNumber, fileId);
            return version;
        } catch (Exception e) {
            logger.error("Error retrieving version {} for file: {}", versionNumber, fileId, e);
            return null;
        }
    }

    /**
     * 恢复到特定版本
     */
    public boolean restoreToVersion(String fileId, Integer versionNumber, String userId) {
        try {
            // 获取目标版本
            FileVersion targetVersion = versionRepository.findByFileIdAndVersionNumber(fileId, versionNumber);
            if (targetVersion == null) {
                logger.error("Version {} not found for file: {}", versionNumber, fileId);
                return false;
            }

            // 将当前版本标记为非当前
            FileVersion currentVersion = versionRepository.findByFileIdAndIsCurrentTrue(fileId);
            if (currentVersion != null) {
                currentVersion.setIsCurrent(false);
                versionRepository.save(currentVersion);
            }

            // 将目标版本标记为当前版本
            targetVersion.setIsCurrent(true);
            versionRepository.save(targetVersion);

            // 创建恢复操作的记录（作为新版本）
            Integer maxVersion = versionRepository.findMaxVersionNumberByFileId(fileId);
            Integer newVersionNumber = (maxVersion != null) ? maxVersion + 1 : 1;

            FileVersion restoreVersion = new FileVersion(
                fileId,
                newVersionNumber,
                targetVersion.getFileName() + "_restored_from_v" + versionNumber,
                targetVersion.getFileContent(),
                targetVersion.getFileSize(),
                "Restored from version " + versionNumber,
                userId
            );
            restoreVersion.setIsCurrent(true);
            versionRepository.save(restoreVersion);

            logger.info("Restored file {} to version {}, new version created as {}", fileId, versionNumber, newVersionNumber);
            return true;
        } catch (Exception e) {
            logger.error("Error restoring to version {} for file: {}", versionNumber, fileId, e);
            return false;
        }
    }

    /**
     * 获取当前活跃版本
     */
    public FileVersion getCurrentVersion(String fileId) {
        try {
            FileVersion version = versionRepository.findByFileIdAndIsCurrentTrue(fileId);
            logger.debug("Retrieved current version for file: {}", fileId);
            return version;
        } catch (Exception e) {
            logger.error("Error retrieving current version for file: {}", fileId, e);
            return null;
        }
    }

    /**
     * 比较两个版本
     */
    public String compareVersions(String fileId, Integer version1, Integer version2) {
        try {
            FileVersion v1 = versionRepository.findByFileIdAndVersionNumber(fileId, version1);
            FileVersion v2 = versionRepository.findByFileIdAndVersionNumber(fileId, version2);

            if (v1 == null || v2 == null) {
                logger.error("One or both versions not found for file: {} (v{} and v{})", fileId, version1, version2);
                return null;
            }

            // 这里可以实现详细的比较逻辑
            // 为了简化，我们返回版本信息
            return String.format("Comparing version %d and version %d of file %s", 
                                version1, version2, fileId);
        } catch (Exception e) {
            logger.error("Error comparing versions for file: {} (v{} and v{})", fileId, version1, version2, e);
            return null;
        }
    }

    /**
     * 删除旧版本（保留最新的N个版本）
     */
    public int cleanupOldVersions(String fileId, int keepLatestCount) {
        try {
            List<FileVersion> allVersions = versionRepository.findByFileIdOrderByVersionNumberDesc(fileId);
            
            if (allVersions.size() <= keepLatestCount) {
                logger.debug("Not enough versions to clean up for file: {}", fileId);
                return 0;
            }

            // 保留最新的几个版本，删除其余的
            int deletedCount = 0;
            for (int i = keepLatestCount; i < allVersions.size(); i++) {
                FileVersion version = allVersions.get(i);
                if (!version.getIsCurrent()) { // 不删除当前版本
                    versionRepository.delete(version);
                    deletedCount++;
                }
            }

            logger.info("Cleaned up {} old versions for file: {}", deletedCount, fileId);
            return deletedCount;
        } catch (Exception e) {
            logger.error("Error cleaning up old versions for file: {}", fileId, e);
            return 0;
        }
    }

    /**
     * 将MultipartFile编码为Base64字符串
     */
    private String encodeFileContent(MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    /**
     * 将Workbook编码为Base64字符串
     */
    private String encodeWorkbookContent(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            byte[] workbookBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(workbookBytes);
        }
    }

    /**
     * 将Base64字符串解码为字节数组
     */
    private byte[] decodeContent(String encodedContent) {
        if (encodedContent == null || encodedContent.isEmpty()) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(encodedContent);
    }

    /**
     * 从编码的内容创建Workbook
     */
    public Workbook createWorkbookFromVersionContent(String encodedContent) throws IOException {
        byte[] content = decodeContent(encodedContent);
        if (content.length == 0) {
            return null;
        }
        return excelService.loadWorkbook(new java.io.ByteArrayInputStream(content));
    }
}