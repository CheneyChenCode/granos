package com.grace.granos.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.grace.granos.model.CustomException;
@PropertySource("classpath:application.properties") // 指定属性文件的位置
@Service
public class FileStorageService {
	private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
	private static final String BUCKET_NAME = "magnificent-ray-450707-v4.appspot.com";
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
			if (bucket != null) {
				return folderName;
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

	public void createFile(String filename, byte[] bytes) throws CustomException{
		if (isRunningOnGCP()) {
			List<Acl> acls = new ArrayList<>();
			acls.add(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
			// the inputstream is closed by default, so we don't need to close it here
			Blob blob = storage.create(BlobInfo.newBuilder(BUCKET_NAME, filename).setAcl(acls).build(),
					bytes);

		} else {
            // 将字符串转换为 Path 对象
            Path path = Paths.get(filename);
            try {
				Files.write(path, bytes);
			} catch (IOException e) {
				throw new CustomException("bucket not existed.",2005);
			}
		}

	}
	public InputStream getAvatar(String fileName) {
		InputStream imageStream = null;
		if (isRunningOnGCP()) {
	        Blob blob = storage.get(BUCKET_NAME, "avatar/"+fileName);
	        if (blob == null || !blob.exists()) {
	        	return imageStream;
	        }
	        // 獲取圖片文件流
	        byte[] content = blob.getContent(Blob.BlobSourceOption.generationMatch());
	        imageStream = new ByteArrayInputStream(content);
		}else {
	            Resource resource = new ClassPathResource("static/images/"+fileName+".png");
	            try {
					imageStream = resource.getInputStream();
				} catch (IOException e) {
					return imageStream;
				}
		}
        return imageStream;
	}
}
