package framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer les classes contrôleurs du framework.
 * Les classes annotées seront automatiquement détectées et enregistrées
 * pour le routage automatique.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Annotation {
    
    /**
     * Le chemin URL associé au contrôleur (optionnel).
     * Si non spécifié, le nom de la classe en minuscules sera utilisé.
     */
    String path() default "";
}