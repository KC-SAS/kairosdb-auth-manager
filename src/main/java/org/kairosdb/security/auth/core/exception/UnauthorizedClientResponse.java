package org.kairosdb.security.auth.core.exception;

import org.kairosdb.security.auth.core.functionnal.HttpResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UnauthorizedClientResponse extends Exception
{
    private final int weight;
    private final HttpResponse response;

    /**
     * Create new exception containing a weight and a lambda.<br>
     * The weight is used to define priority (more is better).
     *
     * @param weight Weight of the response (priority)
     * @param response Lambda constructing the response
     */
    public UnauthorizedClientResponse(int weight, HttpResponse response)
    {
        this.weight = weight;
        this.response = response;
    }

    public int weight()
    {
        return weight;
    }

    public void sendResponse(HttpServletResponse httpServletResponse) throws IOException
    {
        response.apply(httpServletResponse);
    }
}
