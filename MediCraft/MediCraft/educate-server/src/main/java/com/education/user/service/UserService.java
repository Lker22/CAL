package com.education.user.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.education.entity.SysUser;
import com.education.result.Result;
import com.education.vo.LoginVO;
import com.education.vo.PasswordVO;
import com.education.vo.RegisterVO;
import com.education.vo.UserInfoVO;

public interface UserService extends IService<SysUser> {
    /**
     * 用户注册 附带自动创建空白学生画像
     */
    Result<?> register(RegisterVO vo);

    /**
     * 用户登录 返回Jwt令牌
     */
    Result<?> login(LoginVO vo);

    /**
     * 获取当前用户信息
     */
    Result<UserInfoVO> getCurrentUserInfo();

    /**
     * 更新用户信息
     */
    Result<?> updateUserInfo(UserInfoVO userInfoVO);

    /**
     * 修改密码
     */
    Result<?> updatePassword(PasswordVO passwordVO);

    /**
     * 退出登录
     */
    Result<?> logout();
}