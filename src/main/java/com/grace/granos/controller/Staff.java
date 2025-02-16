package com.grace.granos.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.grace.granos.model.JsonResponse;
import com.grace.granos.model.StaffModel;
import com.grace.granos.model.User;
import com.grace.granos.service.FileStorageService;
import com.grace.granos.service.StaffService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class Staff {
	private static final Logger logger = LoggerFactory.getLogger(Staff.class);
	@Autowired
	private MessageSource messageSource;
	@Autowired
	StaffService staffService;
	@Autowired
	private FileStorageService fileStorageService;
    @GetMapping("/logout")
    public String deleteCookie(HttpServletResponse response) {
        // 要刪除的 Cookie 名稱
        Cookie cookieToDelete = new Cookie("granosUser", null);
        
        // 設置 Cookie 過期時間為 0
        cookieToDelete.setMaxAge(0);
        
        // 設置 Cookie 的路徑
        cookieToDelete.setPath("/");

        // 添加這個過期的 Cookie 到回應中，這樣它就會被瀏覽器刪除
        response.addCookie(cookieToDelete);

        return "index";
    }

	@PostMapping("/changeCharacter")
	public ResponseEntity<JsonResponse> staff(int empId,HttpServletRequest request,Model model,HttpServletResponse response) {
		JsonResponse rs = new JsonResponse();
		Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
		User user=staffService.getUser(request);
		if(user.getJobId()!=1) {
			rs.setStatus(1004);
			rs.setMessage(messageSource.getMessage("1004", null, locale));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
		}
		User userF=staffService.findUserById(empId);
		if(userF!=null) {
			user.setCharacter(userF);
			String json = staffService.userToJson(user);
			Cookie cookie = new Cookie("granosUser", json);
			model.addAttribute("user", user);
			// 将 Cookie 添加到响应中
			rs.setStatus(1000);
			rs.setMessage(messageSource.getMessage("1000", null, locale));
			rs.setData(user);
			response.addCookie(cookie);
			return ResponseEntity.ok(rs);
		}
		rs.setStatus(1001);
		rs.setMessage(messageSource.getMessage("1001", null, locale));
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
	}
	@GetMapping("/findAllUser")
	public ResponseEntity<JsonResponse> findAllUser(Model model,HttpServletRequest request) {
		JsonResponse rs = new JsonResponse();
		Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
		User user=staffService.getUser(request);
		if(user.getJobId()!=1) {
			rs.setStatus(1004);
			rs.setMessage(messageSource.getMessage("1004", null, locale));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
		}
		List<User> userN=staffService.findAllStaff();
		return ResponseEntity.ok(new JsonResponse(userN));
	}
	@PostMapping("/login")
	public ResponseEntity<JsonResponse> Login(@ModelAttribute StaffModel user, Model model, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		JsonResponse rs = new JsonResponse();
		// 从 CookieLocaleResolver 中获取用户的语言环境
		Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.class.getName() + ".LOCALE");
		String forward = null;
		String params = "";
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement();
			String value = request.getParameter(name);
			if ("go".equals(name)) {
				forward = value;
			} else if (!"username".equals(name) && !"password".equals(name)) {
				if (StringUtils.isEmpty(params)) {
					params = name + "=" + value;
				} else {
					params = params + "&" + name + "=" + value;
				}
			}
		}
		if (StringUtils.isEmpty(forward)) {
			forward = "home";
		} else if (StringUtils.isNotEmpty(params)) {
			forward = forward + "?" + params;
		}

		String paddedUsername = StringUtils.leftPad(user.getUsername(), 4, '0');
		user.setUsername(paddedUsername);
		User staff = staffService.Login(user);
		if (staff == null) {
			rs.setStatus(1001);
			rs.setMessage(messageSource.getMessage("1001", null, locale));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
		} else {
			if (StringUtils.isEmpty(staff.getLoginMessage())) {
				model.addAttribute("user", user);
				logger.info("Controller:Staff[" + user.getUsername() + "] login succeed");
				String json = staffService.userToJson(staff);
				// 创建一个新的 Cookie 对象
				Cookie cookie = new Cookie("granosUser", json);
				model.addAttribute("user", user);
				// 将 Cookie 添加到响应中
				rs.setStatus(1000);
				rs.setMessage(messageSource.getMessage("1000", null, locale));
				response.addCookie(cookie);
				//response.sendRedirect(forward);
				rs.setData(forward);
				return ResponseEntity.ok(rs);
			} else if ("password".equals(staff.getLoginMessage())) {
				// return "redirect:/index?error=password";
				rs.setStatus(1003);
				rs.setMessage(messageSource.getMessage("1003", null, locale));
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
			} else {
				rs.setStatus(1002);
				rs.setMessage(messageSource.getMessage("1002", null, locale));
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rs);
			}
		}
	}
	 @GetMapping("/avatar")
	    public ResponseEntity<InputStreamResource> getAvatar(Model model,HttpServletRequest request) {
		 	User user = staffService.getUser(request);
	        String fileName = user.getEmpId() + ".png";
	        InputStream imageStream = fileStorageService.getAvatar(fileName);
	        if (imageStream == null) {
	        	String initials=StringUtils.left(user.getNameEn(),1)+StringUtils.left(user.getLastNameEn(), 1);
	            int size = 100;
	            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
	            Graphics2D g = image.createGraphics();

	            // 抗锯齿
	            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	            // 🎨 生成随机背景色（避免太亮的颜色）
	            Random rand = new Random();
	            Color bgColor = new Color(rand.nextInt(200), rand.nextInt(200), rand.nextInt(200));

	            // 画背景圆
	            g.setColor(bgColor);
	            g.fillOval(0, 0, size, size);

	            // 写字母
	            g.setFont(new Font("Arial", Font.BOLD, 50));
	            g.setColor(Color.WHITE);
	            FontMetrics fm = g.getFontMetrics();
	            int x = (size - fm.stringWidth(initials)) / 2;
	            int y = ((size - fm.getHeight()) / 2) + fm.getAscent()+ fm.getAscent()/2;
	            g.drawString(initials, x, y);

	            g.dispose();

	            // 输出 PNG 图片
	            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	            try {
					ImageIO.write(image, "png", outputStream);
				} catch (IOException e) {
					return ResponseEntity.notFound().build();
				}
	            // 将 ByteArray 转换为 InputStream
	            imageStream = new ByteArrayInputStream(outputStream.toByteArray());

	            return ResponseEntity.ok()
		                .contentType(MediaType.IMAGE_PNG)
		                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
		                .body(new InputStreamResource(imageStream));
	        }
	        return ResponseEntity.ok()
	                .contentType(MediaType.IMAGE_PNG)
	                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
	                .body(new InputStreamResource(imageStream));
	    }
}