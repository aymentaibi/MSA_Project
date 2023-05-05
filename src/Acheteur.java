import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

import static java.lang.Thread.sleep;

public class Acheteur extends Agent {
    private int offreMax;
    private int offreActuelle = 0;
    private AID agentVendeur;
    private String nomAchteur;
    private boolean enchereEnCours = true;
    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            nomAchteur =  getLocalName();
            offreMax =(int) Float.parseFloat((args[0].toString()));

            //offreMax = Integer.parseInt(args[0].toString());
        } else {
            nomAchteur = getLocalName();
            offreMax = 0;
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Achteur");
        sd.setName(nomAchteur);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);}
        catch (FIPAException e) {e.printStackTrace();}
        ACLMessage proposeMsg = new ACLMessage(ACLMessage.INFORM);
        proposeMsg.addReceiver(new AID("interface", AID.ISLOCALNAME));
        proposeMsg.setContent("Agent "+nomAchteur +" de type Acheteur CREATED");
        this.send(proposeMsg);
        addBehaviour(new echerer());
    }
    class echerer extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    int prixPropose = Integer.parseInt(msg.getContent());
                    // System.out.println(getLocalName() + " reçoit une proposition de " + msg.getSender().getLocalName() + " : " + prixPropose);
                    // Si le prix proposé est inférieur ou égal au prix maximum de l'acheteur et supérieur à la dernière offre faite
                    if (prixPropose <= offreMax && prixPropose > offreActuelle) {
                        Random random = new Random();
                        offreActuelle = random.nextInt(offreMax - prixPropose + 1) + prixPropose;
                        enchereEnCours = true;
                        ACLMessage proposeMsg = new ACLMessage(ACLMessage.INFORM);
                        proposeMsg.addReceiver(new AID("interface", AID.ISLOCALNAME));
                        proposeMsg.setContent(getLocalName() + " fait une offre de " + offreActuelle);
                        myAgent.send(proposeMsg);
                        //System.out.println(getLocalName() + " fait une offre de " + offreActuelle);
                        //Envoi d'un message de confirmation d'offre
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.PROPOSE );
                        reply.setContent(String.valueOf(offreActuelle));
                        send(reply);
                    } else if (prixPropose > offreMax) {
                        // Envoi d'un message de refus d'offre
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent(String.valueOf(0));
                        send(reply);
                        enchereEnCours = false;
                        //myAgent.doDelete();
                    } else {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent(String.valueOf(offreActuelle));
                        send(reply);
                    }
                }
            }
        }
    }
}
