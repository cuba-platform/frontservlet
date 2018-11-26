/*
 * Copyright (c) 2008-2017 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.frontservlet;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.Supplier;

public class AppFrontServlet extends DispatcherServlet {

    /*
     * The field is used to prevent double initialization of the servlet.
     * Double initialization might occur during single WAR deployment when we call the method from initializer.
     */
    protected volatile boolean initialized = false;

    protected String contextName;
    protected Supplier<ApplicationContext> parentContextProvider;

    public AppFrontServlet() {
        this("", null);
    }

    public AppFrontServlet(String contextName, Supplier<ApplicationContext> parentContextProvider) {
        super();
        setContextClass(AnnotationConfigWebApplicationContext.class);
        setContextConfigLocation(AppFrontConfig.class.getName());
        this.contextName = contextName;
        this.parentContextProvider = parentContextProvider;
    }

    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getRequestURI().endsWith(contextName + "/")) {
            response.sendRedirect("index.html");
        } else {
            super.doService(request, response);
        }
    }

    @Override
    protected WebApplicationContext initWebApplicationContext() {
        WebApplicationContext wac = findWebApplicationContext();
        if (wac == null) {
            ApplicationContext parent = null;
            if (parentContextProvider != null) {
                parent = parentContextProvider.get();
            }
            wac = createWebApplicationContext(parent);
        }

        onRefresh(wac);

        // Publish the context as a servlet context attribute.
        String attrName = getServletContextAttributeName();
        getServletContext().setAttribute(attrName, wac);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Published WebApplicationContext of servlet '" + getServletName() +
                    "' as ServletContext attribute with name [" + attrName + "]");
        }

        return wac;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        if (!initialized) {
            super.init(config);
            initialized = true;
        }
    }
}
