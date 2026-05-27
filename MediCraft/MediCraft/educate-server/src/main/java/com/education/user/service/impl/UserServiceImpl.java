package com.education.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.context.BaseContext;
import com.education.entity.StudentProfile;
import com.education.entity.SysUser;
import com.education.properties.JwtProperties;
import com.education.result.Result;
import com.education.user.mapper.StudentProfileMapper;
import com.education.user.mapper.SysUserMapper;
import com.education.user.service.UserService;
import com.education.utils.JwtUtil;
import com.education.vo.LoginVO;
import com.education.vo.PasswordVO;
import com.education.vo.RegisterVO;
import com.education.vo.UserInfoVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    //注入用户mapper
    @Resource
    private SysUserMapper sysUserMapper;

    //注入学生画像mapper
    @Resource
    private StudentProfileMapper profileMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private JwtProperties jwtProperties;   // 注入配置

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

//    private static final String JWT_KEY = "AIStudySystem123456789987654321"; // 自定义密钥
//    private static final long TTL_MILLIS = 2 * 60 * 60 * 1000; // 2小时

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> register(RegisterVO vo) {
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getUsername, vo.getUsername());
        Long count = sysUserMapper.selectCount(userWrapper);
        if (count > 0) {
            return Result.fail(400, "该账号已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(vo.getUsername());
        user.setPassword(passwordEncoder.encode(vo.getPassword()));
        //设置属性并插入数据库
        user.setNickName(vo.getNickName());
        user.setMajor(vo.getMajor());
        user.setGrade(vo.getGrade());
        user.setPhone(vo.getPhone());
        user.setEmail(vo.getEmail());
        user.setStatus(1);
        sysUserMapper.insert(user);

        // 注册成功自动创建画像
        StudentProfile profile = new StudentProfile();
        profile.setUserId(user.getId());
        profile.setUpdateScene("注册初始化");
        profileMapper.insert(profile);
        //创建空白学生画像，关联用户ID，设置更新场景为“注册初始话”,插入

        return Result.success("注册成功");
    }

    @Override
    public Result<?> login(LoginVO vo) {
        //根据用户名查询用户，不存在或禁用则返回失败
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, vo.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null) {
            return Result.fail(400, "账号不存在");
        }
        if (user.getStatus() == 0) {
            return Result.fail(400, "账号已禁用");
        }

        //验证密码
        boolean matches = passwordEncoder.matches(vo.getPassword(), user.getPassword());
        if (!matches) {
            return Result.fail(400, "密码错误");
        }

        //创建jwt
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
//        String token = JwtUtil.createJWT(JWT_KEY, TTL_MILLIS, claims);
        // 使用配置的密钥和过期时间
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );


        // 将token存入Redis
        redisTemplate.opsForValue().set(
            "ai:login:token:" + user.getId(),
                token,
                jwtProperties.getUserTtl(),
                TimeUnit.HOURS
        );

        return Result.success(token);
    }

    @Override
    public Result<UserInfoVO> getCurrentUserInfo() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取用户信息：{}", userId);

        SysUser user = getById(userId);
        if (user == null) {
            return Result.fail(401, "用户不存在");
        }

        // 转换为VO，过滤敏感字段
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setNickName(user.getNickName());
        userInfoVO.setMajor(user.getMajor());
        userInfoVO.setGrade(user.getGrade());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setEmail(user.getEmail());
        userInfoVO.setAvatar(user.getAvatar());

        return Result.success(userInfoVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateUserInfo(UserInfoVO userInfoVO) {
        Long userId = BaseContext.getCurrentId();
        log.info("更新用户信息：{}", userId);

        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, userId)
                .set(SysUser::getNickName, userInfoVO.getNickName())
                .set(SysUser::getEmail, userInfoVO.getEmail())
                .set(SysUser::getAvatar, userInfoVO.getAvatar())
                .set(SysUser::getPhone, userInfoVO.getPhone())
                .set(SysUser::getMajor, userInfoVO.getMajor())
                .set(SysUser::getGrade, userInfoVO.getGrade());

        boolean update = update(wrapper);
        return update ? Result.success("更新成功") : Result.fail(400,"更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updatePassword(PasswordVO passwordVO) {
        Long userId = BaseContext.getCurrentId();
        log.info("修改用户密码：{}", userId);

        SysUser user = getById(userId);
        if (user == null) {
            return Result.fail(401, "用户不存在");
        }

        // 校验旧密码
        boolean matches = passwordEncoder.matches(passwordVO.getOldPassword(), user.getPassword());
        if (!matches) {
            return Result.fail(400, "旧密码错误");
        }

        // 加密新密码并更新
        String newPassword = passwordEncoder.encode(passwordVO.getNewPassword());
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, userId)
                .set(SysUser::getPassword, newPassword);

        boolean update = update(wrapper);

        // 修改密码成功后，删除Redis中的旧token
        if (update) {
            redisTemplate.delete("ai:login:token:" + userId);
        }

        return update ? Result.success("密码修改成功，请重新登录") : Result.fail(400,"密码修改失败");
    }

    @Override
    public Result<?> logout() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户退出登录：{}", userId);

        // 删除Redis中的token
        redisTemplate.delete("ai:login:token:" + userId);
        // 清除线程上下文
        BaseContext.removeCurrentId();

        return Result.success("退出登录成功");
    }
}