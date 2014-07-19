package de.beyondjava.kendoFaces.puiBody;

import java.io.IOException;
import java.util.logging.Logger;

import javax.faces.application.*;
import javax.faces.component.UIComponent;
import javax.faces.context.*;
import javax.faces.render.FacesRenderer;

import com.sun.faces.renderkit.html_basic.BodyRenderer;

import de.beyondjava.angularFaces.core.RendererUtils;

/**
 * PuiBody is an HtmlBody that activates the AngularDart framework.
 */
@ResourceDependencies({
        @ResourceDependency(library = "kendo2014.2.716", name = "kendo.common.min.css", target = "head"),
        @ResourceDependency(library = "kendo2014.2.716", name = "kendo.default.min.css", target = "head") })
@FacesRenderer(componentFamily = "javax.faces.Output", rendererType = "de.beyondjava.kendoFaces.puiBody.PuiBody")
public class PuiBodyRenderer extends BodyRenderer implements RendererUtils {
    private static final Logger LOGGER = Logger.getLogger("de.beyondjava.kendoFaces.puiButton.PuiBodyRenderer");

    static {
        LOGGER.info("KendoFaces renderer of 'PuiBody' is available for use.");
    }

    public PuiBodyRenderer() {
        LOGGER.info(getClass().getName() + " is being initialized");
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        super.encodeBegin(context, component);
        ResponseWriter writer = context.getResponseWriter();
        String ngApp = (String) component.getAttributes().get("ng-app");
        if (null != ngApp) {
            writer.writeAttribute("ng-app", ngApp, null);
        }
        else {
            writer.append(" ng-app ");
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String main = (String) component.getAttributes().get("mainclassfile");
        if (main == null) {
            main = "main.js";
        }
        if (!main.endsWith(".js")) {
            main = main + ".js";
        }
        writer.append("<script src='http://cdn.kendostatic.com/2014.2.716/js/jquery.min.js'></script>");
        writer.append("<script src='http://cdn.kendostatic.com/2014.2.716/js/angular.min.js'></script>");
        writer.append("<script src='http://cdn.kendostatic.com/2014.2.716/js/kendo.all.min.js'></script>");
        writer.append("<script src='" + main + "'></script>");

        super.encodeEnd(context, component);
    }
};
