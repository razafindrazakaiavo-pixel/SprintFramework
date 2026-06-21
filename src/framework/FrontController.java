package framework;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    private HashMap<String, String> routes = new HashMap<>();
    private List<Class<?>> controllers = new ArrayList<>();

    @Override
    public void init() {
        // Scan des classes annotées @Annotation
        scanControllers("controller");
        
        // Affichage des classes trouvées
        System.out.println("=== Classes @Annotation trouvées ===");
        for (Class<?> controller : controllers) {
            System.out.println("Classe trouvée : " + controller.getName());
        }
    }

    /**
     * Scanne les classes d'un package à la recherche de celles annotées avec @Annotation
     * @param packageName le nom du package à scanner
     */
    private void scanControllers(String packageName) {
        try {
            // Récupérer le ClassLoader
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            
            // Convertir le nom du package en chemin de ressources
            String path = packageName.replace('.', '/');
            
            // Lister les ressources du package
            InputStream stream = classLoader.getResourceAsStream(path);
            
            if (stream != null) {
                // Pour une approche simple, on va scanner les classes connues
                // Dans un vrai projet, on utiliserait une bibliothèque comme Reflections
                scanKnownControllers(packageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Méthode simplifiée pour scanner les classes connues du package
     * Dans une version future, on pourrait lire depuis un fichier de configuration
     * ou utiliser la réflexion pour lister toutes les classes du package
     */
    private void scanKnownControllers(String packageName) {
        try {
            // On scanne les classes du package controller
            // Pour ce sprint, on charge UserController explicitement
            // Dans une version avancée, on listerait dynamiquement les classes
            
            String[] classNames = {
                "controller.UserController"
            };
            
            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className);
                    
                    // Vérifier si la classe a l'annotation @Annotation
                    if (clazz.isAnnotationPresent(Annotation.class)) {
                        controllers.add(clazz);
                        
                        // Récupérer l'annotation pour obtenir le path
                        Annotation controllerAnnotation = clazz.getAnnotation(Annotation.class);
                        String controllerPath = controllerAnnotation.path();
                        
                        // Si pas de path spécifié, utiliser le nom de la classe en minuscules
                        if (controllerPath.isEmpty()) {
                            String simpleName = clazz.getSimpleName();
                            controllerPath = "/" + simpleName.replace("Controller", "").toLowerCase();
                        }
                        
                        // Scanner les méthodes du contrôleur pour créer les routes
                        scanControllerMethods(clazz, controllerPath);
                    }
                } catch (ClassNotFoundException e) {
                    // Classe non trouvée, on continue
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Scanne les méthodes d'un contrôleur pour enregistrer les routes
     */
    private void scanControllerMethods(Class<?> clazz, String basePath) {
        Method[] methods = clazz.getDeclaredMethods();
        
        for (Method method : methods) {
            // Pour ce sprint, on enregistre toutes les méthodes publiques
            // Dans une version avancée, on pourrait avoir une annotation @Route
            String routePath = basePath + "/" + method.getName();
            String mapping = clazz.getName() + ":" + method.getName();
            
            routes.put(routePath, mapping);
            System.out.println("Route enregistrée : " + routePath + " -> " + mapping);
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Récupérer l'URL demandée
            String uri = request.getRequestURI();

            // Récupérer le contexte de l'application
            String context = request.getContextPath();

            // Extraire la partie de l'URL après le contexte
            String url = uri.substring(context.length());

            response.getWriter().println(
                    "FrontController appelé");

            // Afficher l'URL demandée
            response.getWriter().println(
                    "URL : " + uri);

            response.getWriter().println(
                    "Contexte : " + context);

            response.getWriter().println(
                    "URL demandée : " + url);

            // Trouver la route correspondante
            String mapping = routes.get(url);

            if (mapping == null) {
                response.getWriter().println("404 - Route introuvable");
                return;
            }

            String[] infos = mapping.split(":");

            String className = infos[0];

            String methodName = infos[1];

            Class<?> clazz = Class.forName(className);

            Object controller = clazz.getDeclaredConstructor()
                    .newInstance();

            Method method = clazz.getMethod(methodName);

            Object result = method.invoke(controller);

            response.getWriter()
                    .println(result);

        } catch (Exception e) {
            e.printStackTrace();

            response.getWriter()
                    .println("Erreur : "
                            + e.getMessage());
        }
    }
}