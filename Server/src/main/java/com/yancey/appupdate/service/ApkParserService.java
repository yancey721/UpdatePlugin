package com.yancey.appupdate.service;

import com.yancey.appupdate.dto.ParsedApkData;
import com.yancey.appupdate.exception.ApkParseException;
import lombok.extern.slf4j.Slf4j;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.Icon;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * APK文件解析服务
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Slf4j
@Service
public class ApkParserService {

    /**
     * 解析APK文件
     * 
     * @param apkFile APK文件
     * @return 解析后的APK数据
     * @throws IOException 解析异常
     */
    public ParsedApkData parseApk(File apkFile) throws IOException {
        if (!apkFile.exists()) {
            throw new ApkParseException("APK文件不存在: " + apkFile.getAbsolutePath());
        }

        if (!apkFile.canRead()) {
            throw new ApkParseException("无法读取APK文件: " + apkFile.getAbsolutePath());
        }

        try (ApkFile apk = new ApkFile(apkFile)) {
            ApkMeta apkMeta = apk.getApkMeta();
            
            if (apkMeta == null) {
                throw new ApkParseException("无法解析APK元数据");
            }

            // 计算文件大小和MD5
            long fileSize = apkFile.length();
            String md5 = calculateMd5(apkFile);

            // 构建解析结果
            ParsedApkData parsedData = new ParsedApkData();
            parsedData.setPackageName(apkMeta.getPackageName());
            parsedData.setVersionCode(String.valueOf(apkMeta.getVersionCode()));
            parsedData.setVersionName(apkMeta.getVersionName());
            parsedData.setAppName(apkMeta.getLabel());
            parsedData.setFileSize(fileSize);
            parsedData.setMd5(md5);

            // 可选：提取应用图标
            // byte[] icon = apk.getIconFile();
            // parsedData.setIcon(icon);

            log.info("APK解析成功: {} - {} ({})", 
                    parsedData.getAppName(), 
                    parsedData.getVersionName(), 
                    parsedData.getPackageName());

            return parsedData;
            
        } catch (Exception e) {
            log.error("APK文件解析失败: {}", apkFile.getName(), e);
            throw new ApkParseException("APK文件解析失败: " + apkFile.getName(), e);
        }
    }

    /**
     * 计算文件MD5值
     * 
     * @param file 文件
     * @return MD5值
     * @throws IOException IO异常
     */
    public String calculateMd5(File file) throws IOException {
        if (!file.exists()) {
            throw new ApkParseException("文件不存在: " + file.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            String md5 = DigestUtils.md5Hex(fis);
            log.debug("文件MD5计算完成: {} -> {}", file.getName(), md5);
            return md5;
        } catch (IOException e) {
            log.error("计算文件MD5失败: {}", file.getName(), e);
            throw new ApkParseException("计算文件MD5失败: " + file.getName(), e);
        }
    }

    /**
     * 验证文件是否为有效的APK文件
     * 
     * @param file 文件
     * @return 是否为有效APK
     */
    public boolean isValidApkFile(File file) {
        if (!file.exists() || !file.canRead()) {
            return false;
        }

        try (ApkFile apk = new ApkFile(file)) {
            ApkMeta apkMeta = apk.getApkMeta();
            return apkMeta != null && 
                   apkMeta.getPackageName() != null && 
                   !apkMeta.getPackageName().trim().isEmpty();
        } catch (Exception e) {
            log.debug("APK文件验证失败: {}", file.getName(), e);
            return false;
        }
    }

    /**
     * 获取APK文件的基本信息（不包含MD5计算，用于快速检查）
     * 
     * @param apkFile APK文件
     * @return APK基本信息
     */
    public ApkMeta getApkMeta(File apkFile) {
        try (ApkFile apk = new ApkFile(apkFile)) {
            return apk.getApkMeta();
        } catch (Exception e) {
            log.error("获取APK元数据失败: {}", apkFile.getName(), e);
            throw new ApkParseException("获取APK元数据失败: " + apkFile.getName(), e);
        }
    }

    /**
     * 提取APK图标
     * 
     * @param apkFile APK文件
     * @return 图标字节数组，如果没有图标则返回null
     */
    public byte[] extractIcon(File apkFile) {
        try (ApkFile apk = new ApkFile(apkFile)) {
            Icon icon = apk.getIconFile();
            if (icon != null) {
                return icon.getData();
            }
            return null;
        } catch (Exception e) {
            log.warn("提取APK图标失败: {}", apkFile.getName(), e);
            return null;
        }
    }
} 