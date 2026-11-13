package org.delcom.ifs25035springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;

public final class AuthSession {

    private static final String USER_KEY = "cashflow_user_id";
    private static final String LOGGED_OUT_KEY = "cashflow_logged_out";
    private static final String COOKIE_NAME = "cashflow_uid";
    private static final int COOKIE_MAX_AGE = 8 * 3600;

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        vaadinSession.setAttribute(USER_KEY, user.getId());
        vaadinSession.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);

        HttpSession http = httpRequest().getSession();
        http.setAttribute(USER_KEY, user.getId());
        http.removeAttribute(LOGGED_OUT_KEY);

        Cookie cookie = new Cookie(COOKIE_NAME, String.valueOf(user.getId()));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        VaadinService.getCurrentResponse().addCookie(cookie);
    }

    public static void logout() {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        vaadinSession.setAttribute(USER_KEY, null);
        vaadinSession.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);

        HttpSession http = httpRequest().getSession();
        http.removeAttribute(USER_KEY);
        http.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);

        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        VaadinService.getCurrentResponse().addCookie(cookie);
    }

    /** Memulihkan sesi setelah refresh. Mengembalikan null bila belum login. */
    public static User currentUser(IUserRepository repository) {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession == null) {
            return null;
        }
        if (Boolean.TRUE.equals(vaadinSession.getAttribute(LOGGED_OUT_KEY))) {
            return null;
        }

        Integer id = (Integer) vaadinSession.getAttribute(USER_KEY);
        if (id == null) {
            HttpServletRequest request = httpRequest();
            HttpSession http = request.getSession(false);
            if (http != null && Boolean.TRUE.equals(http.getAttribute(LOGGED_OUT_KEY))) {
                return null;
            }
            if (http != null) {
                id = (Integer) http.getAttribute(USER_KEY);
            }
            if (id == null && request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (COOKIE_NAME.equals(cookie.getName())) {
                        try {
                            id = Integer.parseInt(cookie.getValue());
                        } catch (NumberFormatException ignored) {
                            id = null;
                        }
                    }
                }
            }
            if (id != null) {
                vaadinSession.setAttribute(USER_KEY, id);
            }
        }
        if (id == null) {
            return null;
        }
        try {
            return repository.findById(id);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    private static HttpServletRequest httpRequest() {
        return ((VaadinServletRequest) VaadinService.getCurrentRequest()).getHttpServletRequest();
    }
}