#KairosDB - Authentication manager

This KairosDB module can help you to create authentication for the WebUI and the API.


##Usage
To create an authentication, you need to implement two interface.

* **AuthenticationFilter**, used to authenticate clients
````java
public class SimpleAuthFilter implements AuthenticationFilter
{
    @Override
    public boolean tryAuthentication(HttpServletRequest httpServletRequest) throws UnauthorizedClientResponse
    {
        //Authenticate client here using the request

        //For example, to authorize client with and 'AllowedClient' header
        String allowed = httpServletRequest.getHeader("AllowedClient");
        if (allowed != null && !allowed.equals("denied"))
            return true;

        //If you want to send a response, you can throw a special exception
        if (allowed == null)
            throw new UnauthorizedClientResponse(64, httpResponse -> httpResponse.sendError(401, "Nope"));

        //Or just return false to deny your client without response
        return false;
    }
}
````

* **AuthenticationModule**, used to configure filter rules
````java
class SimpleAuthModule implements AuthenticationModule
{
    @Override
    public void configure(Properties properties, FilterManager filterManager)
    {
        //Configure the filter rules here.

        //For example, to load some filter
        // filter GET on '/api/*'
        filterManager.filter("/api/*").through(SimpleAuthFilter.class);
        // filter GET, POST and PATCH on '/api/v1/*' (method name ignore case)
        filterManager.filter("/api/v1/*")
                .from("GET").from("Post").from("patch")
                .through(SimpleAuthFilter.class)
                .through(AnotherAuthFilter.class);

        //You can also use tools into Utils class to help you
        Utils.filtersFrom(properties, "kairosdb.security.auth.filter_path.", SimpleAuthFilter.class)
                .forEach(f -> f.accept(filterManager));

        //Or this tool if you want to create filters easily
        // filter GET on '/api/*'
        Utils.pathToFilter("/api/*", SimpleAuthFilter.class).accept(filterManager);
        // filter GET, POST and PATCH on '/api/v1/*' (method name ignore case)
        Utils.pathToFilter("/api/v1/*|GET|Post|patch", SimpleAuthFilter.class, AnotherAuthFilter.class).accept(filterManager);
    }
}
````

> CAUTION: the wildcard path ``*`` in filter rule must only be used at the end of path and just after ``/``.  
> ``/api/*/entry`` or ``/api*`` paths are not valid (and not checked)


##License
This module is licensed under the MIT license. See [License file](LICENSE.md) for more information.