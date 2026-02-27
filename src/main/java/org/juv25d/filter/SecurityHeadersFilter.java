
package org.juv25d.filter;

import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import java.io.IOException;

/**
 * Filter that adds security headers to every HTTP response.
 * This helps protect against attacks such as Clickjacking and MIME sniffing.
 */
@Global(order = 0)
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        try {
            chain.doFilter(req, res);
        } finally {

                applyDefaultHeader(res, "X-Content-Type-Options", "nosniff" );
                applyDefaultHeader(res, "X-Frame-Options", "DENY" );
                applyDefaultHeader(res, "X-XSS-Protection", "0" );
                applyDefaultHeader(res, "Referrer-Policy", "no-referrer" );

            }
        }

    private void applyDefaultHeader(HttpResponse res, String headerName, String defaultValue) {
        // Kontrollera om headern saknas eller är tom
        if (res.getHeader(headerName) == null) {
            res.setHeader(headerName, defaultValue);
        }
    }

    }


