/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.web.comet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.comet.CometProcessor;

/**
 * {@code CometServletTest}
 * <p/>
 *
 * Created on Oct 12, 2011 at 4:55:20 PM
 *
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
@WebServlet("/CometServletTest")
public class CometServletTest extends HttpServlet implements CometProcessor {

    private static final long serialVersionUID = 1L;
    private int count = 0;

    @Override
    public void event(CometEvent event) throws IOException, ServletException {
        HttpSession session = event.getHttpServletRequest().getSession(true);
        String sessid = session.getId();
        System.out.println("[" + sessid + "] " + event.getEventType());
        session.setMaxInactiveInterval(-1);

        ServletOutputStream sos = event.getHttpServletResponse().getOutputStream();
        ServletInputStream sis = event.getHttpServletRequest().getInputStream();

        try {
            switch (event.getEventType()) {
                case BEGIN:
                    sos.flush();
                    break;
                case END:
                    break;
                case ERROR:
                    event.close();
                    break;
                case READ:
                    // Using while (true): Not checking if input is available will trigger a blocking
                    // read. No other event should be triggered (the current READ event will be in progress
                    // until the read timeouts, which will trigger an ERROR event due to an IOException).

                    int len = -1;
                    while ((len = sis.available()) > 0) {
                        byte[] buf = new byte[len];
                        int c = sis.read(buf);
                        String str = new String(buf, 0, c);
                        System.out.println("[" + sessid + "] READ: " + str);
                    }
                    sos.println("[" + sessid + "] " + (count++) + " ");
                    sos.flush();

                    break;
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
