package com.grace.granos.util;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.grace.granos.GranosApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GranosApplication.class)
public class MD5utilTest {
	@Autowired
	private EncryptUtil encryptUtil;

	@Test
	public void md5() throws Exception {
		String password="12345";
		int salt= 1;
		Assert.assertEquals("494cb94ea4832196361fb59f77e4dec9",encryptUtil.md5(password, salt));
	}
}
