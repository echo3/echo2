package nextapp.echo2.testapp.interactive.testscreen;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.app.util.DomUtil;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.servermessage.DomUpdate;
import nextapp.echo2.webrender.service.JavaScriptService;

/**
 * A test to examine failure behavior with client script exceptions.
 */
public class ClientExceptionTest extends Column {

    /**
     * Component that can be configured to throw JavaScript
     * exception on client/server sync.
     */
    public static class ExComponent extends Component {

        public static final int MODE_WORKING = 0;
        public static final int MODE_FAIL_ON_RENDER_ONCE = 1;
        public static final int MODE_FAIL_ON_RENDER_EVERY_TIME = 2;
        public static final int MODE_LOAD_BROKEN_JS_ONCE = 3;
        public static final int MODE_LOAD_BROKEN_JS_EVERY_TIME = 4;
        
        private int mode;
        
        public ExComponent(int mode) {
            super();
            this.mode = mode;
        }
         
        public int getMode() {
            return mode;
        }
        
        public void setMode(int mode) {
            this.mode = mode;
        }
    }

    /**
     * Peer class for <code>ExComponent</code> that will throw a JavaScript 
     * error depending on component state.
     */
    public static class ExPeer implements ComponentSynchronizePeer, DomUpdateSupport {

        /**
         * Service to provide supporting JavaScript library.
         */
        private static final Service EXCOMPONENT_SERVICE = JavaScriptService.forResource("EchoTestApp.ExComponent",
                "/nextapp/echo2/testapp/interactive/resource/js/ExComponent.js");
        
        /**
         * Service to provide supporting JavaScript library.
         */
        private static final Service EXCOMPONENT_BROKEN_SERVICE = JavaScriptService.forResource("EchoTestApp.ExComponentBroken",
                "/nextapp/echo2/testapp/interactive/resource/js/ExComponentBroken.js");

        static {
            WebRenderServlet.getServiceRegistry().add(EXCOMPONENT_SERVICE);
            WebRenderServlet.getServiceRegistry().add(EXCOMPONENT_BROKEN_SERVICE);
        }
        
        /**
         * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
         */
        public String getContainerId(Component child) {
            throw new UnsupportedOperationException("Component does not support children.");
        }

        /**
         * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext,
         *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
         */
        public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
            DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
            renderHtml(rc, update, htmlFragment, component);
            DomUpdate.renderElementAdd(rc.getServerMessage(), targetId, htmlFragment);
        }
        
        /**
         * Renders a directive to dynamically load a broken script module.
         * 
         * @param rc the relevant <code>RenderContext</code>
         */
        private void renderBrokenScriptModule(RenderContext rc) {
            rc.getServerMessage().addLibrary(EXCOMPONENT_BROKEN_SERVICE.getId());
        }

        /**
         * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(
         *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate,
         *      nextapp.echo2.app.Component)
         */
        public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) { }
        
        /**
         * Renders a directive that will fail during processing.
         * 
         * @param rc the relevant <code>RenderContext</code>
         */
        private void renderFailDirective(RenderContext rc) {
            rc.getServerMessage().addLibrary(EXCOMPONENT_SERVICE.getId());
            rc.getServerMessage().appendPartDirective(ServerMessage.GROUP_ID_POSTUPDATE, "ExComponent.MessageProcessor", "fail");
        }

        /**
         * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext,
         *      nextapp.echo2.app.update.ServerComponentUpdate,
         *      org.w3c.dom.Node, nextapp.echo2.app.Component)
         */
        public void renderHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component) {
            ExComponent exComponent = (ExComponent) component;
            Document document = rc.getServerMessage().getDocument();
            Element spanElement = document.createElement("span");
            spanElement.setAttribute("id", ContainerInstance.getElementId(component));
            parentNode.appendChild(spanElement);
            
            switch (exComponent.getMode()) {
            case ExComponent.MODE_FAIL_ON_RENDER_ONCE:
                DomUtil.setElementText(spanElement, "[fail on render once]");
                renderFailDirective(rc);
                exComponent.setMode(ExComponent.MODE_WORKING);
                break;
            case ExComponent.MODE_FAIL_ON_RENDER_EVERY_TIME:
                DomUtil.setElementText(spanElement, "[fail on render every time]");
                renderFailDirective(rc);
                break;
            case ExComponent.MODE_LOAD_BROKEN_JS_ONCE:
                DomUtil.setElementText(spanElement, "[load broken script module]");
                renderBrokenScriptModule(rc);
                exComponent.setMode(ExComponent.MODE_WORKING);
                break;
            case ExComponent.MODE_LOAD_BROKEN_JS_EVERY_TIME:
                DomUtil.setElementText(spanElement, "[load broken script module]");
                renderBrokenScriptModule(rc);
                break;
            default:
                DomUtil.setElementText(spanElement, "[non-broken]");
            }
        }

        /**
         * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(
         *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate,
         *      java.lang.String)
         */
        public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
            String parentId = ContainerInstance.getElementId(update.getParent());
            DomUpdate.renderElementRemove(rc.getServerMessage(), parentId);
            renderAdd(rc, update, targetId, update.getParent());
            return true;
        }
    }

    /**
     * Creates a new <code>ClientExceptionTest</code>.
     */
    public ClientExceptionTest() {
        super();
        
        SplitPaneLayoutData splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setInsets(new Insets(10));
        setLayoutData(splitPaneLayoutData);
        
        Button button;
        
        button = new Button("Add working component (Control Case)");
        button.setStyleName("Default");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add(new ExComponent(ExComponent.MODE_WORKING));
            }
        });
        add(button);
        
        button = new Button("Add broken component that fails to render (ONCE).");
        button.setStyleName("Default");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add(new ExComponent(ExComponent.MODE_FAIL_ON_RENDER_ONCE));
            }
        });
        add(button);
        
        button = new Button("Add broken component that will dynamically load broken JavaScript module (ONCE).");
        button.setStyleName("Default");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add(new ExComponent(ExComponent.MODE_LOAD_BROKEN_JS_ONCE));
            }
        });
        add(button);
        
        button = new Button("Add broken component that fails to render (EVERY TIME).");
        button.setStyleName("Default");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add(new ExComponent(ExComponent.MODE_FAIL_ON_RENDER_EVERY_TIME));
            }
        });
        add(button);
        
        button = new Button("Add broken component that will dynamically load broken JavaScript module (EVERY TIME).");
        button.setStyleName("Default");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add(new ExComponent(ExComponent.MODE_LOAD_BROKEN_JS_EVERY_TIME));
            }
        });
        add(button);
    }
}