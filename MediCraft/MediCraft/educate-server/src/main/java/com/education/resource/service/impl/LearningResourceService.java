package com.education.resource.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.resource.mapper.LearningResourceMapper;
import com.education.dto.ResourceQueryDTO;
import com.education.entity.LearningResource;
import org.springframework.stereotype.Service;


/**
 * 资源列表
 */
@Service
public class LearningResourceService extends ServiceImpl<LearningResourceMapper, LearningResource> {

    /**
     * 分页查询我的学习资源（分类+关键词）
     * @param dto 查询条件
     * @return 分页结果
     */
    public IPage<LearningResource> queryMyResource(ResourceQueryDTO dto) {
        Page<LearningResource> page = new Page<>(dto.getPage(), dto.getPageSize());
        return this.baseMapper.queryMyResource(page, dto);
    }
}