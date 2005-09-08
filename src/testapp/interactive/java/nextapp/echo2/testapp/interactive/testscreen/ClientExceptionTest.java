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

public class ClientExceptionTest extends Column {

    /**
     * Component that can be configured to throw JavaScript
     * exception on client/server sync.
     */
    public static class ExComponent extends Component {

        private boolean broken;
        
        public ExComponent(boolean broken) {
            super();
            this.broken = broken;
        }
         
        public boolean isBroken() {
            return broken;
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

        static {
            WebRenderServlet.getServiceRegistry().add(EXCOMPONENT_SERVICE);
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
         * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(
         *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate,
         *      nextapp.echo2.app.Component)
         */
        public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) { }
        
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
            Document document = rc.getServerMessage().getDocument();
            Element spanElement = document.createElement("span");
            spanElement.setAttribute("id", ContainerInstance.getElementId(component));
            DomUtil.setElementText(spanElement, ((ExComponent) component).isBroken() ? "Broken" : "NotBroken");
            if (((ExComponent) component).isBroken()) {
                renderFailDirective(rc);
            }
            parentNode.appendChild(spanElement);
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

    public ClientExceptionTest() {
        super();
        
        SplitPaneLayoutData splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setInsets(new Insets(10));
        setLayoutData(splitPaneLayoutData);
        
        Button button;
        
        button = new Button("Add Broken Component");
        button.setStyleName("Default");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add(new ExComponent(true));
            }
        });
        add(button);
        
        button = new Button("Add Not-Broken Component");
        button.setStyleName("Default");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add(new ExComponent(false));
            }
        });
        add(button);
    }
}