import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Thread.sleep;

public class Vendeur extends Agent {
    private int prixDepart;
    private int prixReserve;
    private int meilleurOffre = prixDepart;
    private String gagnant;
    private boolean vendu = false;
    private AID[] AchAgents;

    @Override
    protected void setup() {
        //wait for other Agents too creacted
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            prixDepart = (int) Float.parseFloat(args[0].toString());
            prixReserve = (int) Float.parseFloat(args[1].toString());
        } else {
            prixDepart = 1000;
            prixReserve = 2000;
        }
        addBehaviour(new informerBuyers());
    }


    class FermerEchere extends OneShotBehaviour{
        @Override
        public void action() {
            String s;
            if(meilleurOffre>= prixReserve) {
                 s = "Vendu pour " + meilleurOffre + " à l'agent " + gagnant;
            }
            else{
                s = "Le meilleur offre n'est pas grand que le prix de vente";
            }
            ACLMessage proposeMsg = new ACLMessage(ACLMessage.INFORM);
            proposeMsg.addReceiver(new AID("interface", AID.ISLOCALNAME));
            proposeMsg.addReceiver(new AID(gagnant, AID.ISLOCALNAME));
            proposeMsg.setContent(s);
            myAgent.send(proposeMsg);
        }
    }
    class declecherLechere extends Behaviour {
        Instant start = Instant.now();
        Instant end = Instant.now();
        Duration timeElapsed;
        boolean exit_c = false;
        @Override
        public void action() {
            end = Instant.now();
            timeElapsed = Duration.between(start, end);
            if(timeElapsed.toMillis()>10000){
                addBehaviour(new FermerEchere());
                exit_c = true;
            }
           // MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                String contenu = msg.getContent();
                int offre = Integer.parseInt(contenu);
                if (offre > meilleurOffre) {
                    start = Instant.now();
                    meilleurOffre = offre;
                    gagnant = msg.getSender().getLocalName();
                }
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(String.valueOf(meilleurOffre));
                send(reply);
            }
        }
        public boolean done() {
            return exit_c;
        }
    }

    class informerBuyers extends OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage proposeMsg = new ACLMessage(ACLMessage.PROPOSE);
            DFAgentDescription t = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Achteur");
            t.addServices(sd);
            try {
                DFAgentDescription [] R = DFService.search(myAgent, t);
                AchAgents = new AID[R.length];
                for(int i=0;i<R.length;i++){
                    AchAgents[i]=R[i].getName();
                    proposeMsg.addReceiver(AchAgents[i]);
                }

            } catch (FIPAException e) {
                e.printStackTrace();
            }
            /*
            proposeMsg.addReceiver(new AID("A1", AID.ISLOCALNAME));
            proposeMsg.addReceiver(new AID("A2", AID.ISLOCALNAME));
            proposeMsg.addReceiver(new AID("A3", AID.ISLOCALNAME));
             */
            proposeMsg.setContent(String.valueOf(prixDepart));
            myAgent.send(proposeMsg);
            myAgent.addBehaviour(new declecherLechere());

        }
        }
    }