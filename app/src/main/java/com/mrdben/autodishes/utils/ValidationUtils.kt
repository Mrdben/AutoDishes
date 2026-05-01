package com.mrdben.autodishes.utils

import java.util.regex.Pattern

object ValidationUtils {

    // 1. 用户名规则：
    // - 长度 4-20 位
    // - 只能包含字母、数字、下划线
    // - 必须以字母开头
    private const val USERNAME_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]{3,19}$"

    // 2. 密码强度规则（高强度）：
    // - 长度至少 8 位
    // - 包含大写字母
    // - 包含小写字母
    // - 包含数字
    // - 不强制要求特殊字符
    private const val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$"

    // 3. 手机号规则（简单示例）
    private const val PHONE_PATTERN = "^1[3-9]\\d{9}$"

    /**
     * 校验用户名
     */
    fun validateUsername(username: String): ValidationResult {
        if (username.isBlank()) return ValidationResult(false, "用户名不能为空")
        if (!Pattern.matches(USERNAME_PATTERN, username)) {
            return ValidationResult(false, "用户名需以字母开头，包含4-20位字母、数字或下划线")
        }
        return ValidationResult(true, null)
    }

    /**
     * 校验密码强度
     */
    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) return ValidationResult(false, "密码不能为空")
        if (password.length < 8) return ValidationResult(false, "密码长度不能少于8位")

        // 使用正则进行复杂校验
        if (!Pattern.matches(PASSWORD_PATTERN, password)) {
            return ValidationResult(false, "密码必须包含大小写字母和数字")
        }
        return ValidationResult(true, null)
    }

    /**
     * 校验手机号
     */
    fun validatePhone(phone: String): ValidationResult {
        if (phone.isBlank()) return ValidationResult(false, "手机号不能为空")
        if (!Pattern.matches(PHONE_PATTERN, phone)) {
            return ValidationResult(false, "请输入有效的中国大陆11位手机号")
        }
        return ValidationResult(true, null)
    }
}

// 数据类：用于承载校验结果
data class ValidationResult(
    val isValid: Boolean = false,
    val errorMessage: String? = null
)