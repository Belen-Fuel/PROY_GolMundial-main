package ec.edu.utn.golmundial.config;

import java.io.IOException;
import java.net.URL;

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
 * Intercepta rutas inexistentes del frontend y muestra la página 404
 * personalizada sin afectar la API REST, los recursos de Jakarta Faces
 * ni los archivos estáticos existentes.
 */
@WebFilter(
        filterName = "RutaNoEncontradaFilter",
        urlPatterns = "/*",
        dispatcherTypes = DispatcherType.REQUEST
)
public class RutaNoEncontradaFilter implements Filter {

    private static final String PAGINA_404 = "/errores/404.html";

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String metodo = request.getMethod();

        /*
         * No se intervienen solicitudes que puedan modificar información.
         * El filtro se usa únicamente para navegación GET y HEAD.
         */
        if (!"GET".equalsIgnoreCase(metodo)
                && !"HEAD".equalsIgnoreCase(metodo)) {
            filterChain.doFilter(request, response);
            return;
        }

        String contexto = request.getContextPath();
        String uri = request.getRequestURI();
        String ruta = uri.substring(contexto.length());

        /*
         * La raíz debe continuar para que WildFly aplique index.html
         * como archivo de bienvenida.
         */
        if (ruta.isBlank() || "/".equals(ruta)) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Estas rutas son procesadas dinámicamente por Jakarta REST,
         * Jakarta Faces o por los recursos estáticos de la aplicación.
         */
        if (esRutaDinamica(ruta)) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Si el archivo o directorio existe físicamente dentro del WAR,
         * se permite que continúe su procesamiento normal.
         */
        URL recurso = request.getServletContext().getResource(ruta);

        if (recurso != null) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * La ruta no existe. Se conserva el código HTTP 404 y se muestra
         * la página personalizada sin cambiar la URL escrita en el navegador.
         */
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        request.getRequestDispatcher(PAGINA_404).forward(request, response);
    }

    private boolean esRutaDinamica(String ruta) {
        return ruta.equals("/api")
                || ruta.startsWith("/api/")
                || ruta.startsWith("/jakarta.faces.resource/")
                || ruta.startsWith("/resources/")
                || ruta.startsWith("/errores/");
    }
}