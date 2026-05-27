package com.education.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.entity.StudentProfile;

public interface StudentProfileService extends IService<StudentProfile> {

    StudentProfile getByUserId();

    void saveOrUpdateProfile(StudentProfile profile);

    boolean removeById(Long id);

}