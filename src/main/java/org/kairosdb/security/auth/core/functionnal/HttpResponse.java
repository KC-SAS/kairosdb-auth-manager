package org.kairosdb.security.auth.core.functionnal;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@FunctionalInterface
public interface HttpResponse
{
    void apply(HttpServletResponse response) throws IOException;
}
