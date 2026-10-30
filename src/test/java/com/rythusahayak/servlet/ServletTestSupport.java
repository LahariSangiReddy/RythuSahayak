package com.rythusahayak.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

final class ServletTestSupport {
    private ServletTestSupport() {
    }

    static RequestFixture request(Map<String, String> parameters, String method, String path,
                                  SessionFixture initialSession) {
        return new RequestFixture(parameters, method, path, initialSession);
    }

    static ResponseFixture response() {
        return new ResponseFixture();
    }

    static final class RequestFixture {
        private final Map<String, String> parameters;
        private final Map<String, Object> attributes = new HashMap<>();
        private final String method;
        private final String path;
        private SessionFixture session;
        private String forwardedPath;
        private final HttpServletRequest request;

        private RequestFixture(Map<String, String> parameters, String method, String path,
                               SessionFixture initialSession) {
            this.parameters = parameters;
            this.method = method;
            this.path = path;
            this.session = initialSession;
            this.request = proxy(HttpServletRequest.class, (proxy, called, arguments) -> switch (called.getName()) {
                case "getParameter" -> parameters.get(arguments[0]);
                case "getAttribute" -> attributes.get(arguments[0]);
                case "setAttribute" -> {
                    attributes.put((String) arguments[0], arguments[1]);
                    yield null;
                }
                case "removeAttribute" -> {
                    attributes.remove(arguments[0]);
                    yield null;
                }
                case "getContextPath" -> "/rythusahayak";
                case "getRequestURI" -> "/rythusahayak" + path;
                case "getMethod" -> method;
                case "isSecure" -> false;
                case "getSession" -> getSession(arguments);
                case "getRequestDispatcher" -> dispatcher((String) arguments[0]);
                default -> defaultValue(called.getReturnType());
            });
        }

        HttpServletRequest request() {
            return request;
        }

        String attribute(String name) {
            return (String) attributes.get(name);
        }

        String forwardedPath() {
            return forwardedPath;
        }

        SessionFixture session() {
            return session;
        }

        private Object getSession(Object[] arguments) {
            boolean create = arguments.length == 0 || !Boolean.FALSE.equals(arguments[0]);
            if (session != null && !session.invalidated()) {
                return session.httpSession();
            }
            if (!create) {
                return null;
            }
            session = new SessionFixture();
            return session.httpSession();
        }

        private RequestDispatcher dispatcher(String path) {
            return proxy(RequestDispatcher.class, (proxy, called, arguments) -> {
                if (called.getName().equals("forward")) {
                    forwardedPath = path;
                }
                return defaultValue(called.getReturnType());
            });
        }
    }

    static final class SessionFixture {
        private final Map<String, Object> attributes = new HashMap<>();
        private boolean invalidated;
        private final HttpSession session = proxy(HttpSession.class, (proxy, called, arguments) -> switch (called.getName()) {
            case "getAttribute" -> attributes.get(arguments[0]);
            case "setAttribute" -> {
                attributes.put((String) arguments[0], arguments[1]);
                yield null;
            }
            case "removeAttribute" -> {
                attributes.remove(arguments[0]);
                yield null;
            }
            case "invalidate" -> {
                invalidated = true;
                attributes.clear();
                yield null;
            }
            case "setMaxInactiveInterval" -> null;
            default -> defaultValue(called.getReturnType());
        });

        HttpSession httpSession() {
            return session;
        }

        Object attribute(String name) {
            return attributes.get(name);
        }

        boolean invalidated() {
            return invalidated;
        }

        void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }
    }

    static final class ResponseFixture {
        private int status = HttpServletResponse.SC_OK;
        private String redirect;
        private int errorStatus;
        private boolean errorSent;
        private final HttpServletResponse response = proxy(HttpServletResponse.class, (proxy, called, arguments) -> {
            switch (called.getName()) {
                case "setStatus" -> status = (Integer) arguments[0];
                case "sendRedirect" -> redirect = (String) arguments[0];
                case "sendError" -> {
                    errorSent = true;
                    errorStatus = (Integer) arguments[0];
                }
                default -> {
                }
            }
            if (called.getName().equals("getStatus")) {
                return status;
            }
            return defaultValue(called.getReturnType());
        });

        HttpServletResponse response() {
            return response;
        }

        int status() {
            return status;
        }

        String redirect() {
            return redirect;
        }

        boolean errorSent() {
            return errorSent;
        }

        int errorStatus() {
            return errorStatus;
        }
    }

    static FilterChain chain(Runnable called) {
        return (request, response) -> called.run();
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) {
            return null;
        }
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        throw new IllegalArgumentException("Unsupported primitive type: " + type);
    }
}
