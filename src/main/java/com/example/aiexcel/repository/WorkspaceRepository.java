package com.example.aiexcel.repository;

import com.example.aiexcel.model.FileWorkspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工作区仓库
 */
@Repository
public interface WorkspaceRepository extends JpaRepository<FileWorkspace, Long> {
    
    /**
     * 根据用户ID获取工作区列表
     */
    List<FileWorkspace> findByUserId(String userId);

    /**
     * 根据用户ID和是否公开获取工作区
     */
    List<FileWorkspace> findByUserIdAndIsPublic(String userId, Boolean isPublic);

    /**
     * 获取公开的工作区
     */
    List<FileWorkspace> findByIsPublicTrue();

    /**
     * 根据父工作区ID获取子工作区
     */
    List<FileWorkspace> findByParentWorkspaceId(Long parentWorkspaceId);

    /**
     * 根据用户ID和父工作区ID获取工作区
     */
    List<FileWorkspace> findByUserIdAndParentWorkspaceId(String userId, Long parentWorkspaceId);

    /**
     * 根据名称搜索用户工作区
     */
    List<FileWorkspace> findByUserIdAndNameContainingIgnoreCase(String userId, String name);

    /**
     * 检查用户是否已存在同名工作区
     */
    boolean existsByUserIdAndName(String userId, String name);
}