package ea.sof.ms_questions.filter;

import com.google.gson.Gson;
import ea.sof.ms_questions.service.AuthService;
import ea.sof.shared.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

//@Component
public class AuthFiler implements Filter {
    @Autowired
    AuthService authService;

    private Gson gson = new Gson();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("Bearer Token");

        Response result = authService.isValidToken(token);
        if(!result.getSuccess()){
            HttpServletResponse customResp = ((HttpServletResponse) response);
            customResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Response resp = new Response(false, "Invalid token");

            String respJson = this.gson.toJson(resp);

            PrintWriter out = customResp.getWriter();
            customResp.setContentType("application/json");
            customResp.setCharacterEncoding("UTF-8");
            out.print(respJson);
            out.flush();
        }else {
            request.setAttribute("decoded_token", result.getData().get("decoded_token"));
            chain.doFilter(request, response);
        }

    }
}
