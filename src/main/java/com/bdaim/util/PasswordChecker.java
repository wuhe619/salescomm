package com.bdaim.util;


import java.util.LinkedHashSet;
import java.util.Set;


/**
 * 密码必须包括（数字、字母（大写或小写）、特殊字符）
 */
public class PasswordChecker {

    private boolean upperCase = false; // 包含大写字母
    private boolean lowerCase = false; // 包含小写字母
    private boolean letter = true; // 包含字母
    private boolean digit = true; // 包含数字
    private boolean special = true; // 包含特殊字符
    private Set<Character> specialCharSet = null; // 特殊字符集合
    private int minLength = 6; // 最小长度
    private int maxLength = 20; // 最大长度

    /**
     * 密码包含字母数字特殊字符中至少2种规则正则表达式
     */
    public static final String LETTER_OR_SPECIAL_MATCHES = "(?!^(\\d+|[a-zA-Z]+|[[!@#$%^&*()\\-+=—_]]+)$)^[\\w[!@#$%^&*()\\-+=—_]]{8,20}$";

    public PasswordChecker() {
        this.specialCharSet = defaultSpecialCharSet();
    }

    /**
     * CRM用户密码规则校验
     * <p>描述:密码包含字母数字特殊字符中至少2种规则正则表达式</p>
     * @param pwd
     * @return
     */
    public static boolean crmPwdCheck(String pwd) {
        if (StringUtil.isEmpty(pwd)) {
            return false;
        }
        return pwd.matches(LETTER_OR_SPECIAL_MATCHES);
    }

    /**
     * 密码符合规则，返回true
     */
    public boolean check(String password) {
        if (password == null || password.length() < this.minLength || password.length() > this.maxLength) {
            // 长度不符合
            return false;
        }

        boolean containUpperCase = false;
        boolean containLowerCase = false;
        boolean containLetter = false;
        boolean containDigit = false;
        boolean containSpecial = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                containUpperCase = true;
                containLetter = true;
            } else if (Character.isLowerCase(ch)) {
                containLowerCase = true;
                containLetter = true;
            } else if (Character.isDigit(ch)) {
                containDigit = true;
            } else if (this.specialCharSet.contains(ch)) {
                containSpecial = true;
            } else {
                // 非法字符
                return false;
            }
        }

        if (this.upperCase && !containUpperCase) {
            return false;
        }
        if (this.lowerCase && !containLowerCase) {
            return false;
        }
        if (this.letter && !containLetter) {
            return false;
        }
        if (this.digit && !containDigit) {
            return false;
        }
        if (this.special && !containSpecial) {
            return false;
        }
        return true;
    }

    public static Set<Character> defaultSpecialCharSet() {
        Set<Character> specialChars = new LinkedHashSet<>();
        // 键盘上能找到的符号
        specialChars.add(Character.valueOf('~'));
        specialChars.add(Character.valueOf('`'));
        specialChars.add(Character.valueOf('!'));
        specialChars.add(Character.valueOf('@'));
        specialChars.add(Character.valueOf('#'));
        specialChars.add(Character.valueOf('$'));
        specialChars.add(Character.valueOf('%'));
        specialChars.add(Character.valueOf('^'));
        specialChars.add(Character.valueOf('&'));
        specialChars.add(Character.valueOf('*'));
        specialChars.add(Character.valueOf('('));
        specialChars.add(Character.valueOf(')'));
        specialChars.add(Character.valueOf('-'));
        specialChars.add(Character.valueOf('_'));
        specialChars.add(Character.valueOf('+'));
        specialChars.add(Character.valueOf('='));
        specialChars.add(Character.valueOf('{'));
        specialChars.add(Character.valueOf('['));
        specialChars.add(Character.valueOf('}'));
        specialChars.add(Character.valueOf(']'));
        specialChars.add(Character.valueOf('|'));
        specialChars.add(Character.valueOf('\\'));
        specialChars.add(Character.valueOf(':'));
        specialChars.add(Character.valueOf(';'));
        specialChars.add(Character.valueOf('"'));
        specialChars.add(Character.valueOf('\''));
        specialChars.add(Character.valueOf('<'));
        specialChars.add(Character.valueOf(','));
        specialChars.add(Character.valueOf('>'));
        specialChars.add(Character.valueOf('.'));
        specialChars.add(Character.valueOf('?'));
        specialChars.add(Character.valueOf('/'));
        return specialChars;
    }

    public static void main(String[] args) {
        PasswordChecker chencker = new PasswordChecker();
        System.out.println(chencker.check("123abc&"));
    }
}