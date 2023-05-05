import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import javax.swing.*;

public class interfaceAgent extends Agent {
    deroulementEnchere data;
    @Override
    protected void setup() {
        data = new deroulementEnchere();
        data.setVisible(true);
        addBehaviour(new printData());
    }
    class printData extends CyclicBehaviour{
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                data.addnewLine(msg.getContent());
            } else {
                // If no message is received, block the behavior
                block();
            }

        }
    }
}
