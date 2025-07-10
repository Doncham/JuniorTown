package org.juniortown.backend.interceptor;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GuestCookieInterceptor implements HandlerInterceptor {
	private static final String COOKIE_NAME = "guestId";
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		Cookie[] cookies = request.getCookies();
		boolean hasGuestId = false;
		if (cookies != null) {
			for(Cookie c : cookies) {
				if (COOKIE_NAME.equals(c.getName())) {
					hasGuestId = true;
					break;
				}
			}
		}
		if(!hasGuestId) {
			String guestId = UUID.randomUUID().toString();
			Cookie cookie = new Cookie(COOKIE_NAME, guestId);
			cookie.setPath("/");
			cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
			response.addCookie(cookie);
		}
		return true;
	}
}
