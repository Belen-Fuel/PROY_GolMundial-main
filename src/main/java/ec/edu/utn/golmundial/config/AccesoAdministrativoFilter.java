package ec.edu.utn.golmundial.config;

import java.io.IOException;

import ec.edu.utn.golmundial.bean.LoginBean;
import jakarta.inject.Inject;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Protege las pantallas administrativas antes de que Jakarta Faces
 * empiece a renderizarlas.
 *
 * Esto evita que los beans intenten redirigir desde @PostConstruct
 * cuando la respuesta ya fue enviada parcialmente al navegador.
 */
@WebFilter(
        filterName = "AccesoAdministrativoFilter",
        urlPatterns = {
                "/dashboard.xhtml",
                "/partidos.xhtml",
                "/selecciones.xhtml",
                "/sedes.xhtml",
                "/usuarios.xhtml",
                "/reportes.xhtml",
                "/auditoria.xhtml"
        },
        dispatcherTypes = DispatcherType.REQUEST
)
public class AccesoAdministrativoFilter implements Filter {

    @Inject
    private LoginBean loginBean;

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {

        HttpServletRequest request =
                (HttpServletRequest) servletRequest;

        HttpServletResponse response =
                (HttpServletResponse) servletResponse;

        if (sesionAdministrativaValida()) {
            filterChain.doFilter(request, response);
            return;
        }

        String rutaLogin =
                request.getContextPath() + "/login.xhtml";

        /*
         * Las solicitudes AJAX de Jakarta Faces esperan una respuesta XML
         * especial para poder ejecutar una redirección correctamente.
         */
        if (esSolicitudAjax(request)) {

            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/xml");

            response.getWriter().printf(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<partial-response>"
                    + "<redirect url=\"%s\"/>"
                    + "</partial-response>",
                    rutaLogin
            );

            return;
        }

        response.sendRedirect(rutaLogin);
    }

    private boolean sesionAdministrativaValida() {

        return loginBean != null
                && loginBean.isAdministrador()
                && loginBean.getAuthorizationHeader() != null;
    }

    private boolean esSolicitudAjax(
            HttpServletRequest request
    ) {

        String encabezado =
                request.getHeader("Faces-Request");

        return "partial/ajax".equalsIgnoreCase(encabezado);
    }
}