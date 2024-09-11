package com.grace.granos.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
@PropertySource("classpath:application.properties") // 指定属性文件的位置
@Service
public class FileStorageService {
	private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
	private static final String BUCKET_NAME = "adroit-lock-435109-j7.appspot.com";
	private final Storage storage;
	@Value("${temp.folder.attendance}") // 从属性文件中注入 temp 文件夹路径的值
	private String tempFolderPath;
	@Value("${spring.profiles.active}") // 从属性文件中注入 temp 文件夹路径的值
	private String environment;
	public FileStorageService() {
		// 初始化 Storage 客户端
		this.storage = StorageOptions.getDefaultInstance().getService();
	}

	public boolean isRunningOnGCP() {
		logger.info("we are running on " + environment);
		return "gcp".equals(environment);
	}

	public String doesFolderExist(String folderName) {
		if (isRunningOnGCP()) {
			// 获取指定 bucket
			Bucket bucket = storage.get(BUCKET_NAME);
			if (bucket == null) {
				return BUCKET_NAME + "//" + folderName;
			}

		} else {
			File folder = new File(tempFolderPath + folderName);
			if (!folder.exists()) {
				if (!folder.mkdirs()) {
					return null;
				}
			}
			return folder.getPath();
		}
		return null;
	}

	public void createFile(String filename, byte[] bytes) throws IOException {
		if (isRunningOnGCP()) {
			List<Acl> acls = new ArrayList<>();
			acls.add(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
			// the inputstream is closed by default, so we don't need to close it here
			Blob blob = storage.create(BlobInfo.newBuilder(BUCKET_NAME, filename).setAcl(acls).build(),
					bytes);

		} else {
            // 将字符串转换为 Path 对象
            Path path = Paths.get(filename);
            Files.write(path, bytes);
		}

	}
}
